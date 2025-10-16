package com.taller5.billing.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "aggregatorDal", url = "${aggregator.url}")
public interface AggregatorBillingDalClient {

  @PostMapping("/dal/billing/invoices")
  CreateInvoiceRes create(@RequestBody CreateInvoiceReq req);

  @GetMapping("/dal/billing/invoices/{id}")
  InvoiceDto get(@PathVariable Long id);

  // --- DTOs ---
  record CreateInvoiceReq(
      Long paymentId, 
      Long customerId, 
      String customerEmail,
      BigDecimal totalAmount,
      List<InvoiceItemReq> items) {}

  record InvoiceItemReq(
      Long productId,
      String productName,
      Long supplierId,
      String supplierName,
      Integer quantity,
      BigDecimal unitPrice,
      BigDecimal subtotal) {}

  record CreateInvoiceRes(Long invoiceId, InvoiceDto invoice) {}
  
  record InvoiceDto(
      Long id, 
      Long paymentId, 
      Long customerId,
      String customerEmail,
      BigDecimal totalAmount,
      List<InvoiceItemDto> items,
      String createdAt) {}

  record InvoiceItemDto(
      Long id,
      Long productId,
      String productName,
      Long supplierId,
      String supplierName,
      Integer quantity,
      BigDecimal unitPrice,
      BigDecimal subtotal) {}
}
