package com.taller5.dal;

import com.taller5.inventory.model.Product;
import com.taller5.inventory.repository.ProductRepository;
import com.taller5.payments.model.Payment;
import com.taller5.payments.repository.PaymentRepository;
import com.taller5.billing.model.Invoice;
import com.taller5.billing.repository.InvoiceRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;

@Service
public class TransactionalDal {

  private static final Logger logger = LoggerFactory.getLogger(TransactionalDal.class);

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
  @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 30)
  protected CheckoutResult attemptCheckout(Long customerId, Long productId, int qty, BigDecimal amount) {
    try {
      logger.debug("Iniciando checkout para customerId={}, productId={}, qty={}", customerId, productId, qty);
      
      // 1) obtener producto sin hacer flush inmediato
      Product prod = productRepo.findById(productId)
          .orElseThrow(() -> new IllegalArgumentException("Producto no existe: " + productId));

      logger.debug("Producto encontrado: id={}, stock={}, version={}", prod.getId(), prod.getStock(), prod.getVersion());

      if (prod.getStock() == null || prod.getStock() < qty) {
        throw new IllegalStateException("Stock insuficiente. Disponible: " + prod.getStock() + ", solicitado: " + qty);
      }

      // 2) decrementar stock - removemos saveAndFlush para evitar locks prematuros
      prod.setStock(prod.getStock() - qty);
      productRepo.save(prod); // Solo save, sin flush

      // 3) crear payment (esta operación puede fallar pero será parte de la misma XA tx)
      Payment payment = new Payment();
      payment.setCustomerId(customerId);
      payment.setProductId(productId);
      payment.setAmount(amount.doubleValue());
      payment = paymentRepo.save(payment);

      // 4) crear invoice
      Invoice invoice = new Invoice();
      invoice.setPaymentId(payment.getId());
      invoice = invoiceRepo.save(invoice);

      logger.debug("Checkout completado exitosamente: paymentId={}, invoiceId={}", payment.getId(), invoice.getId());
      
      return new CheckoutResult(payment.getId(), invoice.getId(), "APPROVED");
      
    } catch (Exception e) {
      logger.error("Error durante attemptCheckout: {}", e.getMessage(), e);
      throw e; // Re-lanzar para que el retry handler pueda manejarlo
    }
  }

  // orchestrator with retries; not @Transactional
  public CheckoutResult checkout(Long customerId, Long productId, int qty, BigDecimal amount) {
    final int maxAttempts = 5; // Aumentamos a 5 intentos
    final long baseBackoffMs = 50L; // Reducimos el backoff base
    final long maxBackoffMs = 2000L; // Máximo backoff de 2 segundos

    logger.info("Iniciando checkout con retry para customerId={}, productId={}, qty={}", customerId, productId, qty);

    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      try {
        logger.debug("Intento {} de {}", attempt, maxAttempts);
        return attemptCheckout(customerId, productId, qty, amount);
        
      } catch (OptimisticLockingFailureException ole) {
        logger.warn("Optimistic locking failure en intento {} de {}: {}", attempt, maxAttempts, ole.getMessage());
        if (attempt == maxAttempts) {
          throw new IllegalStateException("Conflict en stock tras " + maxAttempts + " intentos por optimistic locking", ole);
        }
        sleepBackoff(baseBackoffMs, attempt, maxBackoffMs);
        
      } catch (jakarta.persistence.PessimisticLockException ple) {
        logger.warn("Pessimistic lock exception en intento {} de {}: {}", attempt, maxAttempts, ple.getMessage());
        if (attempt == maxAttempts) {
          throw new IllegalStateException("Lock wait timeout persistente tras " + maxAttempts + " intentos", ple);
        }
        sleepBackoff(baseBackoffMs, attempt, maxBackoffMs);
        
      } catch (org.springframework.dao.CannotAcquireLockException cal) {
        logger.warn("Cannot acquire lock en intento {} de {}: {}", attempt, maxAttempts, cal.getMessage());
        if (attempt == maxAttempts) {
          throw new IllegalStateException("Cannot acquire lock tras " + maxAttempts + " intentos", cal);
        }
        sleepBackoff(baseBackoffMs, attempt, maxBackoffMs);
        
      } catch (PessimisticLockingFailureException plf) {
        logger.warn("Pessimistic locking failure en intento {} de {}: {}", attempt, maxAttempts, plf.getMessage());
        if (attempt == maxAttempts) {
          throw new IllegalStateException("Pessimistic locking failure persistente tras " + maxAttempts + " intentos", plf);
        }
        sleepBackoff(baseBackoffMs, attempt, maxBackoffMs);
        
      } catch (org.springframework.dao.DataAccessException dae) {
        // Revisar si es un error de timeout o deadlock que puede ser recuperable
        if (isRecoverableDataAccessException(dae)) {
          logger.warn("Error recoverable de acceso a datos en intento {} de {}: {}", attempt, maxAttempts, dae.getMessage());
          if (attempt == maxAttempts) {
            throw new IllegalStateException("Error de acceso a datos persistente tras " + maxAttempts + " intentos", dae);
          }
          sleepBackoff(baseBackoffMs, attempt, maxBackoffMs);
        } else {
          // Error no recuperable, re-lanzar inmediatamente
          logger.error("Error no recoverable de acceso a datos: {}", dae.getMessage(), dae);
          throw dae;
        }
        
      } catch (Exception ex) {
        // Para otros errores, revisar si pueden ser relacionados con XA
        if (isRecoverableXAException(ex)) {
          logger.warn("Error XA recoverable en intento {} de {}: {}", attempt, maxAttempts, ex.getMessage());
          if (attempt == maxAttempts) {
            throw new IllegalStateException("Error XA persistente tras " + maxAttempts + " intentos", ex);
          }
          sleepBackoff(baseBackoffMs, attempt, maxBackoffMs);
        } else {
          // Error fatal, re-lanzar inmediatamente
          logger.error("Error fatal durante checkout: {}", ex.getMessage(), ex);
          throw ex;
        }
      }
    }
    throw new IllegalStateException("No se pudo completar checkout tras " + maxAttempts + " intentos");
  }

  private boolean isRecoverableDataAccessException(org.springframework.dao.DataAccessException dae) {
    String message = dae.getMessage();
    if (message == null) return false;
    
    String lowerMessage = message.toLowerCase();
    return lowerMessage.contains("lock wait timeout") ||
           lowerMessage.contains("deadlock") ||
           lowerMessage.contains("connection") ||
           lowerMessage.contains("timeout") ||
           lowerMessage.contains("xa");
  }

  private boolean isRecoverableXAException(Exception ex) {
    String message = ex.getMessage();
    if (message == null) return false;
    
    String lowerMessage = message.toLowerCase();
    return lowerMessage.contains("xaer_") ||
           lowerMessage.contains("xa") ||
           lowerMessage.contains("transaction") ||
           lowerMessage.contains("timeout");
  }

  private void sleepBackoff(long baseMs, int attempt, long maxBackoffMs) {
    try {
      // Exponential backoff con jitter
      long backoff = Math.min(baseMs * (1L << (attempt - 1)), maxBackoffMs);
      // Añadir jitter aleatorio para evitar thundering herd
      long jitter = (long) (backoff * 0.1 * Math.random());
      long sleepTime = backoff + jitter;
      
      logger.debug("Esperando {}ms antes del siguiente intento (intento {}, backoff base {}ms)", 
                   sleepTime, attempt, baseMs);
      Thread.sleep(sleepTime);
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrumpido durante backoff", ie);
    }
  }

  public record CheckoutResult(Long paymentId, Long invoiceId, String status) {}
}
