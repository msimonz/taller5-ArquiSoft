package com.taller5.payments.controller;

import com.taller5.payments.model.Payment;
import com.taller5.payments.repository.PaymentRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CheckoutController {
  private final PaymentRepository repo;
  private final WebClient invClient = WebClient.create(System.getProperty("inventory.base", "http://localhost:8081"));
  private final WebClient billClient = WebClient.create(System.getProperty("billing.base", "http://localhost:8082"));

  public CheckoutController(PaymentRepository repo) { this.repo = repo; }

  public record CheckoutReq(Long customerId, Long productId, Integer quantity, Double amount) {}
  public record CheckoutRes(Long paymentId, Long invoiceId, String status) {}
  record Reserve(Long productId, Integer quantity) {}
  record CreateInvoice(Long paymentId) {}
  record CreateInvoiceRes(Long invoiceId) {}

  @PostMapping("/checkout")
  public ResponseEntity<?> checkout(@RequestBody CheckoutReq req) {
    if (req.customerId()==null || req.productId()==null || req.quantity()==null || req.quantity()<=0 || req.amount()==null)
      return ResponseEntity.badRequest().body("Datos incompletos");

    // 1) Reserva en inventario
    var reserveStatus = invClient.post().uri("/api/inventory/reserve")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(new Reserve(req.productId(), req.quantity()))
      .exchangeToMono(r -> r.toBodilessEntity())
      .block();
    if (reserveStatus == null || !reserveStatus.getStatusCode().is2xxSuccessful())
      return ResponseEntity.status(409).body("No hay stock");

    // 2) Persistir pago (local)
    var payment = createPayment(req);

    try {
      // 3) Crear factura
      var invoiceId = billClient.post().uri("/api/invoices")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(new CreateInvoice(payment.getId()))
        .retrieve()
        .bodyToMono(CreateInvoiceRes.class)
        .map(CreateInvoiceRes::invoiceId)
        .block();

      return ResponseEntity.ok(new CheckoutRes(payment.getId(), invoiceId, "APPROVED"));

    } catch (RuntimeException ex) {
      // compensación
      invClient.post().uri("/api/inventory/release")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(new Reserve(req.productId(), req.quantity()))
        .retrieve().toBodilessEntity().block();
      throw ex;
    }
  }

  @Transactional
  protected Payment createPayment(CheckoutReq req) {
    return repo.save(new Payment(null, req.customerId(), req.productId(), req.amount()));
  }
}
