package com.taller5.dal;

import com.taller5.inventory.model.Product;
import com.taller5.inventory.repository.ProductRepository;
import com.taller5.payments.model.Payment;
import com.taller5.payments.repository.PaymentRepository;
import com.taller5.billing.model.Invoice;
import com.taller5.billing.repository.InvoiceRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransactionalDal {

  private final ProductRepository productRepo;
  private final PaymentRepository paymentRepo;
  private final InvoiceRepository invoiceRepo;

  public TransactionalDal(ProductRepository productRepo,
                          PaymentRepository paymentRepo,
                          InvoiceRepository invoiceRepo) {
    this.productRepo = productRepo;
    this.paymentRepo = paymentRepo;
    this.invoiceRepo = invoiceRepo;
  }

  // single-attempt method — runs in its own JTA transaction
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  protected CheckoutResult attemptCheckout(Long customerId, Long productId, int qty, BigDecimal amount) {
    // 1) obtener producto (optimistic: no FOR UPDATE)
    Product prod = productRepo.findById(productId)
        .orElseThrow(() -> new IllegalArgumentException("Producto no existe"));

    if (prod.getStock() == null || prod.getStock() < qty) {
      throw new IllegalStateException("Stock insuficiente");
    }

    // 2) decrementar stock y flush para forzar el UPDATE en esta tx
    prod.setStock(prod.getStock() - qty);
    productRepo.saveAndFlush(prod);

    // 3) crear payment + invoice (estas operaciones tocan otras DBs y quedan en la misma JTA tx)
    Payment payment = new Payment();
    payment.setCustomerId(customerId);
    payment.setProductId(productId);
    payment.setAmount(amount.doubleValue());
    payment = paymentRepo.save(payment);

    Invoice invoice = new Invoice();
    invoice.setPaymentId(payment.getId());
    invoice = invoiceRepo.save(invoice);

    return new CheckoutResult(payment.getId(), invoice.getId(), "APPROVED");
  }

  // orchestrator with retries; not @Transactional
  public CheckoutResult checkout(Long customerId, Long productId, int qty, BigDecimal amount) {
    final int maxAttempts = 4;
    final long baseBackoffMs = 100L;

    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      try {
        return attemptCheckout(customerId, productId, qty, amount);
      } catch (OptimisticLockingFailureException ole) {
        if (attempt == maxAttempts) throw new IllegalStateException("Conflict en stock: varios intentos fallidos por colisión", ole);
        sleepBackoff(baseBackoffMs, attempt);
      } catch (jakarta.persistence.PessimisticLockException ple) {
        // bloqueo por espera; reintentar
        if (attempt == maxAttempts) throw new IllegalStateException("Lock wait timeout persistente", ple);
        sleepBackoff(baseBackoffMs, attempt);
      } catch (org.springframework.dao.CannotAcquireLockException cal) {
        if (attempt == maxAttempts) throw new IllegalStateException("Cannot acquire lock", cal);
        sleepBackoff(baseBackoffMs, attempt);
      } catch (Exception ex) {
        // Si es un error fatal de XA/DB probablemente no tenga sentido reintentar aquí:
        // revisa logs, pero hacemos que el controller reciba la excepción
        throw ex;
      }
    }
    throw new IllegalStateException("No se pudo completar checkout tras varios intentos");
  }

  private void sleepBackoff(long baseMs, int attempt) {
    try {
      Thread.sleep(baseMs * attempt);
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrumpido durante backoff", ie);
    }
  }

  public record CheckoutResult(Long paymentId, Long invoiceId, String status) {}
}
