package com.taller5.billing.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@FeignClient(name = "aggregatorDal", url = "${aggregator.url}")
public interface AggregatorBillingDalClient {

  @PostMapping("/dal/billing/invoices")
  CreateInvoiceRes create(@RequestBody CreateInvoiceReq req);

  @GetMapping("/dal/billing/invoices/{id}")
  InvoiceDto get(@PathVariable Long id);

  // --- DTOs ---
  record CreateInvoiceReq(Long paymentId, Long customerId, BigDecimal amount, String description) {}
  record CreateInvoiceRes(Long invoiceId) {}
  record InvoiceDto(Long id, Long paymentId, Long customerId, BigDecimal amount, String status, String description) {}
}
