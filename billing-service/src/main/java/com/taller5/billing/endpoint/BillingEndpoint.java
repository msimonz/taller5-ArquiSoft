package com.taller5.billing.endpoint;

import com.taller5.billing.model.Invoice;
import com.taller5.billing.repository.InvoiceRepository;
import com.taller5.billing.soap.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ws.server.endpoint.annotation.*;

@Endpoint
@Component
public class BillingEndpoint {

  private static final String NAMESPACE = "http://taller5.com/soap/billing";
  private final InvoiceRepository repo;

  public BillingEndpoint(InvoiceRepository repo) {
    this.repo = repo;
  }

  @PayloadRoot(namespace = NAMESPACE, localPart = "CreateInvoiceRequest")
  @ResponsePayload
  @Transactional
  public CreateInvoiceResponse createInvoice(@RequestPayload CreateInvoiceRequest req) {
    CreateInvoiceResponse res = new CreateInvoiceResponse();
    if (req == null || req.paymentId == null) {
      res.status = "BAD_REQUEST";
      return res;
    }

    var existing = repo.findByPaymentId(req.paymentId).orElse(null);
    if (existing != null) {
      res.invoiceId = existing.getId();
      res.status = "EXISTS";
      return res;
    }

    Invoice inv = repo.save(new Invoice(null, req.paymentId));
    res.invoiceId = inv.getId();
    res.status = "CREATED";
    return res;
  }

  @PayloadRoot(namespace = NAMESPACE, localPart = "GetInvoiceRequest")
  @ResponsePayload
  public GetInvoiceResponse getInvoice(@RequestPayload GetInvoiceRequest req) {
    if (req == null || req.id == null) {
      throw new RuntimeException("id requerido");
    }
    var opt = repo.findById(req.id);
    if (opt.isEmpty()) {
      throw new RuntimeException("Invoice not found");
    }
    Invoice inv = opt.get();
    GetInvoiceResponse res = new GetInvoiceResponse();
    res.invoiceId = inv.getId();
    res.paymentId = inv.getPaymentId();
    return res;
  }
}
