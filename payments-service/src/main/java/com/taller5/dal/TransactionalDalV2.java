package com.taller5.dal;

import com.taller5.inventory.model.Product;
import com.taller5.inventory.repository.ProductRepository;
import com.taller5.payments.model.Payment;
import com.taller5.payments.repository.PaymentRepository;
import com.taller5.billing.model.Invoice;
import com.taller5.billing.repository.InvoiceRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * Versión optimizada que evita transacciones XA distribuidas.
 * Usa el patrón Saga con compensación manual en caso de errores.
 */
@Service
public class TransactionalDalV2 {

  private static final Logger logger = LoggerFactory.getLogger(TransactionalDalV2.class);

  private final ProductRepository productRepo;
  private final PaymentRepository paymentRepo;
  private final InvoiceRepository invoiceRepo;

  public TransactionalDalV2(ProductRepository productRepo,
                           PaymentRepository paymentRepo,
                           InvoiceRepository invoiceRepo) {
    this.productRepo = productRepo;
    this.paymentRepo = paymentRepo;
    this.invoiceRepo = invoiceRepo;
  }

  /**
   * Enfoque Saga: cada operación en su propia transacción, con compensación manual
   */
  public CheckoutResult checkout(Long customerId, Long productId, int qty, BigDecimal amount) {
    final int maxAttempts = 3;
    final long baseBackoffMs = 100L;

    logger.info("Iniciando checkout optimizado para customerId={}, productId={}, qty={}", customerId, productId, qty);

    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      try {
        return performSagaCheckout(customerId, productId, qty, amount);
      } catch (OptimisticLockingFailureException ole) {
        logger.warn("Optimistic locking failure en intento {} de {}: {}", attempt, maxAttempts, ole.getMessage());
        if (attempt == maxAttempts) {
          throw new IllegalStateException("Conflict en stock tras " + maxAttempts + " intentos", ole);
        }
        sleepBackoff(baseBackoffMs, attempt);
      } catch (Exception ex) {
        logger.error("Error durante checkout optimizado en intento {}: {}", attempt, ex.getMessage(), ex);
        if (attempt == maxAttempts) {
          throw new IllegalStateException("Error persistente tras " + maxAttempts + " intentos", ex);
        }
        sleepBackoff(baseBackoffMs, attempt);
      }
    }
    throw new IllegalStateException("No se pudo completar checkout tras " + maxAttempts + " intentos");
  }

  private CheckoutResult performSagaCheckout(Long customerId, Long productId, int qty, BigDecimal amount) {
    // PASO 1: Reservar stock (solo en BD de inventory)
    logger.debug("SAGA PASO 1: Reservando stock para producto {}", productId);
    Long reservationId = reserveStock(productId, qty);
    
    Payment payment = null;
    Invoice invoice = null;
    
    try {
      // PASO 2: Crear payment (solo en BD de payments)
      logger.debug("SAGA PASO 2: Creando payment");
      payment = createPayment(customerId, productId, amount);
      
      // PASO 3: Crear invoice (solo en BD de billing)
      logger.debug("SAGA PASO 3: Creando invoice");
      invoice = createInvoice(payment.getId());
      
      // PASO 4: Confirmar la reserva (commit del stock)
      logger.debug("SAGA PASO 4: Confirmando reserva de stock");
      confirmStockReservation(reservationId);
      
      logger.info("Checkout optimizado completado: paymentId={}, invoiceId={}", payment.getId(), invoice.getId());
      return new CheckoutResult(payment.getId(), invoice.getId(), "APPROVED");
      
    } catch (Exception ex) {
      logger.error("Error en saga, iniciando compensación: {}", ex.getMessage());
      
      // COMPENSACIÓN: Deshacer operaciones en orden inverso
      try {
        if (invoice != null) {
          logger.debug("COMPENSACIÓN: Eliminando invoice {}", invoice.getId());
          deleteInvoice(invoice.getId());
        }
        if (payment != null) {
          logger.debug("COMPENSACIÓN: Eliminando payment {}", payment.getId());
          deletePayment(payment.getId());
        }
        logger.debug("COMPENSACIÓN: Liberando reserva de stock {}", reservationId);
        releaseStockReservation(reservationId);
      } catch (Exception compEx) {
        logger.error("Error durante compensación: {}", compEx.getMessage(), compEx);
        // En un sistema real, esto iría a una cola de compensación manual
      }
      
      throw ex;
    }
  }

  @Transactional(transactionManager = "transactionManager", timeout = 10)
  protected Long reserveStock(Long productId, int qty) {
    Product prod = productRepo.findById(productId)
        .orElseThrow(() -> new IllegalArgumentException("Producto no existe: " + productId));

    logger.debug("Stock actual: {}, solicitado: {}, version: {}", prod.getStock(), qty, prod.getVersion());

    if (prod.getStock() == null || prod.getStock() < qty) {
      throw new IllegalStateException("Stock insuficiente. Disponible: " + prod.getStock() + ", solicitado: " + qty);
    }

    // Crear una "reserva" temporal - simplemente decrementamos el stock
    prod.setStock(prod.getStock() - qty);
    productRepo.save(prod);
    
    // En un sistema real, podrías crear un registro de reserva temporal
    return prod.getId(); // Usamos el ID del producto como reservationId por simplicidad
  }

  @Transactional(transactionManager = "transactionManager", timeout = 5)
  protected void confirmStockReservation(Long reservationId) {
    // En este caso simplificado, la reserva ya está confirmada
    // En un sistema real, aquí moverías de "reservado" a "vendido"
    logger.debug("Reserva {} confirmada", reservationId);
  }

  @Transactional(transactionManager = "transactionManager", timeout = 5)
  protected void releaseStockReservation(Long reservationId) {
    // Compensación: devolver el stock
    try {
      Product prod = productRepo.findById(reservationId)
          .orElse(null);
      
      if (prod != null) {
        // Nota: En un sistema real, tendrías que saber cuánto stock liberar
        // Por simplicidad, asumimos que la cantidad está en algún lado o usas una tabla de reservas
        logger.warn("COMPENSACIÓN: Stock liberado para producto {} (implementar lógica específica)", reservationId);
      }
    } catch (Exception ex) {
      logger.error("Error liberando reserva de stock {}: {}", reservationId, ex.getMessage());
    }
  }

  @Transactional(transactionManager = "transactionManager", timeout = 10)
  protected Payment createPayment(Long customerId, Long productId, BigDecimal amount) {
    Payment payment = new Payment();
    payment.setCustomerId(customerId);
    payment.setProductId(productId);
    payment.setAmount(amount.doubleValue());
    return paymentRepo.save(payment);
  }

  @Transactional(transactionManager = "transactionManager", timeout = 5)
  protected void deletePayment(Long paymentId) {
    try {
      paymentRepo.deleteById(paymentId);
      logger.debug("Payment {} eliminado para compensación", paymentId);
    } catch (Exception ex) {
      logger.error("Error eliminando payment {} durante compensación: {}", paymentId, ex.getMessage());
    }
  }

  @Transactional(transactionManager = "transactionManager", timeout = 10)
  protected Invoice createInvoice(Long paymentId) {
    Invoice invoice = new Invoice();
    invoice.setPaymentId(paymentId);
    return invoiceRepo.save(invoice);
  }

  @Transactional(transactionManager = "transactionManager", timeout = 5)
  protected void deleteInvoice(Long invoiceId) {
    try {
      invoiceRepo.deleteById(invoiceId);
      logger.debug("Invoice {} eliminado para compensación", invoiceId);
    } catch (Exception ex) {
      logger.error("Error eliminando invoice {} durante compensación: {}", invoiceId, ex.getMessage());
    }
  }

  private void sleepBackoff(long baseMs, int attempt) {
    try {
      long sleepTime = baseMs * attempt;
      logger.debug("Esperando {}ms antes del siguiente intento", sleepTime);
      Thread.sleep(sleepTime);
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrumpido durante backoff", ie);
    }
  }

  public record CheckoutResult(Long paymentId, Long invoiceId, String status) {}
}