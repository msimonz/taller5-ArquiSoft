package com.taller5.aggregator.dal.billing;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class BillingDalService {
  private final InvoiceRepository repo;
  public BillingDalService(InvoiceRepository repo) { this.repo = repo; }

  @Transactional
  public CreateInvoiceRes create(CreateInvoiceReq req) {
    Invoice inv = new Invoice();
    inv.setPaymentId(req.paymentId());
    inv = repo.save(inv);
    return new CreateInvoiceRes(inv.getId());
  }

  @Transactional(readOnly = true)
  public InvoiceDto get(Long id) {
    Invoice inv = repo.findById(id).orElseThrow();
    return new InvoiceDto(inv.getId(), inv.getPaymentId());
  }

  // DTOs (que consumirá el billing-service vía Feign)
  public record CreateInvoiceReq(Long paymentId) {}
  public record CreateInvoiceRes(Long invoiceId) {}
  public record InvoiceDto(Long id, Long paymentId) {}
}
