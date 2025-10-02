package com.taller5.aggregator.service;

import com.taller5.aggregator.dal.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckoutService {
  private final InventoryDAL inventory;
  private final PaymentsDAL payments;
  private final BillingDAL billing;

  public CheckoutService(InventoryDAL inventory, PaymentsDAL payments, BillingDAL billing) {
    this.inventory = inventory; this.payments = payments; this.billing = billing;
  }

  public record CheckoutReq(Long customerId, Long productId, Integer quantity, Double amount) {}
  public record CheckoutRes(Long paymentId, Long invoiceId, String status) {}

  @Transactional // <-- JTA coordina las 3 BDs (2PC)
  public CheckoutRes checkout(CheckoutReq req) {
    if (req.customerId()==null || req.productId()==null || req.quantity()==null || req.quantity()<=0 || req.amount()==null)
      throw new IllegalArgumentException("Datos incompletos");

    // 1) Reserva en INVENTORY (XA)
    inventory.reserve(req.productId(), req.quantity());

    // 2) Crear pago en PAYMENTS (XA)
    long paymentId = payments.createPayment(req.customerId(), req.productId(), req.amount());

    // 3) Crear factura en BILLING (XA)
    long invoiceId = billing.createInvoice(paymentId);

    // Si algo lanza excepción, JTA hace rollback global
    return new CheckoutRes(paymentId, invoiceId, "APPROVED");
  }
}
