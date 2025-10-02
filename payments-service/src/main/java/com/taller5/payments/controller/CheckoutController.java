// controller/CheckoutController.java
package com.taller5.payments.controller;

import com.taller5.payments.client.PaymentsDalClient;
import com.taller5.payments.dto.ChargeRequest;
import com.taller5.payments.dto.PaymentDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class CheckoutController {

  private final PaymentsDalClient dal;

  public CheckoutController(PaymentsDalClient dal) {
    this.dal = dal;
  }

  @PostMapping("/charge")
  public ResponseEntity<PaymentDTO> charge(@RequestBody ChargeRequest req) {
    return ResponseEntity.ok(dal.charge(req));
  }

  @GetMapping("/{id}")
  public ResponseEntity<PaymentDTO> get(@PathVariable Long id) {
    return ResponseEntity.ok(dal.findById(id));
  }

  @GetMapping
  public ResponseEntity<List<PaymentDTO>> list() {
    return ResponseEntity.ok(dal.findAll());
  }
}
