// dal/payments/PaymentsDalController.java
package com.taller5.aggregator.dal.payments;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/dal/payments")
@Validated
public class PaymentsDalController {

  record ChargeRequest(
      @NotNull @JsonAlias({"customerId","customer_id"}) Long customerId,
      @NotNull @JsonAlias({"customerEmail","customer_email"}) String customerEmail,
      @NotNull @JsonAlias({"amount"})                   BigDecimal amount
  ) {}

  // te conviene devolver el id creado
  record PaymentDTO(Long id, Long customerId, String customerEmail, BigDecimal amount, String status) {
    static PaymentDTO of(Payment p){ return new PaymentDTO(p.getId(), p.getCustomerId(), p.getCustomerEmail(), p.getAmount(), p.getStatus()); }
  }

  private final PaymentsDalService svc;
  public PaymentsDalController(PaymentsDalService svc){ this.svc = svc; }

  @PostMapping("/charge")
  public PaymentDTO charge(@RequestBody @Validated ChargeRequest req) {
    System.out.printf("DAL /charge => customerId=%s, customerEmail=%s, amount=%s%n",
        req.customerId(), req.customerEmail(), req.amount());
    return PaymentDTO.of(svc.charge(req.customerId(), req.customerEmail(), req.amount()));
  }

  @GetMapping("/{id}")
  public PaymentDTO get(@PathVariable Long id) {
    var p = svc.get(id);
    return p == null ? null : PaymentDTO.of(p);
  }

  @GetMapping
  public List<PaymentDTO> list() {
    return svc.list().stream().map(PaymentDTO::of).toList();
  }
}
