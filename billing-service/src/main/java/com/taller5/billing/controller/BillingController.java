package com.taller5.billing.controller;

import com.taller5.billing.client.AggregatorBillingDalClient;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/invoices")
public class BillingController {

  private final AggregatorBillingDalClient dal;

  public BillingController(AggregatorBillingDalClient dal) {
    this.dal = dal;
  }

  @PostMapping
  public AggregatorBillingDalClient.CreateInvoiceRes create(@RequestBody CreateInvoiceReq req) {
    var dalReq = new AggregatorBillingDalClient.CreateInvoiceReq(
        req.paymentId(), req.customerId(), req.amount(), req.description()
    );
    return dal.create(dalReq);
  }

  @GetMapping("/{id}")
  public AggregatorBillingDalClient.InvoiceDto get(@PathVariable Long id) {
    return dal.get(id);
  }

  // DTOs expuestos por billing-service (pueden ser iguales a los del DAL)
  public record CreateInvoiceReq(
      @NotNull Long paymentId,
      @NotNull Long customerId,
      @NotNull BigDecimal amount,
      String description
  ) {}
}
