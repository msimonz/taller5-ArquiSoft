package com.taller5.billing.controller;

import com.taller5.billing.model.Invoice;
import com.taller5.billing.repository.InvoiceRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class BillingController {
  private final InvoiceRepository repo;

  public BillingController(InvoiceRepository repo) { this.repo = repo; }

  public record CreateInvoiceReq(Long paymentId) {}
  public record CreateInvoiceRes(Long invoiceId) {}

  // Crea una factura para un paymentId (idempotente: si ya existe, devuelve la existente)
  @PostMapping("/invoices")
  @Transactional
  public ResponseEntity<?> create(@RequestBody CreateInvoiceReq req) {
    if (req.paymentId() == null) return ResponseEntity.badRequest().body("paymentId requerido");
    var existing = repo.findByPaymentId(req.paymentId()).orElse(null);
    if (existing != null) return ResponseEntity.ok(new CreateInvoiceRes(existing.getId()));

    var inv = repo.save(new Invoice(null, req.paymentId()));
    return ResponseEntity.ok(new CreateInvoiceRes(inv.getId()));
  }

  // Consultar por id (útil para pruebas)
  @GetMapping("/invoices/{id}")
  public ResponseEntity<?> get(@PathVariable Long id) {
    return repo.findById(id).<ResponseEntity<?>>map(ResponseEntity::ok)
      .orElseGet(() -> ResponseEntity.notFound().build());
  }
}
