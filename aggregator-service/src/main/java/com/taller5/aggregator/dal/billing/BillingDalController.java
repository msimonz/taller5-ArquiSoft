package com.taller5.aggregator.dal.billing;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dal/billing/invoices")
public class BillingDalController {

  private final BillingDalService service;
  public BillingDalController(BillingDalService service) { this.service = service; }

  @PostMapping
  public ResponseEntity<BillingDalService.CreateInvoiceRes> create(
      @RequestBody BillingDalService.CreateInvoiceReq req) {
    return ResponseEntity.ok(service.create(req));
  }

  @GetMapping("/{id}")
  public ResponseEntity<BillingDalService.InvoiceDto> get(@PathVariable Long id) {
    return ResponseEntity.ok(service.get(id));
  }
}
