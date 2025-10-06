package com.taller5.payments.controller;

import com.taller5.dal.TransactionalDal;
import com.taller5.dal.TransactionalDal.CheckoutResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CheckoutController {

  private final TransactionalDal dal;

  public CheckoutController(TransactionalDal dal) {
    this.dal = dal;
  }

  public record CheckoutReq(Long customerId, Long productId, Integer quantity, Double amount) {}
  public record CheckoutRes(Long paymentId, Long invoiceId, String status) {}

  @PostMapping("/checkout")
  public ResponseEntity<?> checkout(@RequestBody CheckoutReq req) {
    if (req == null
        || req.customerId() == null
        || req.productId() == null
        || req.quantity() == null
        || req.quantity() <= 0
        || req.amount() == null) {
      return ResponseEntity.badRequest().body("Datos incompletos");
    }

    try {
      CheckoutResult res = dal.checkout(
          req.customerId(),
          req.productId(),
          req.quantity(),
          BigDecimal.valueOf(req.amount())
      );

      var body = new CheckoutRes(res.paymentId(), res.invoiceId(), res.status());
      return ResponseEntity.ok(body);

    } catch (IllegalStateException ex) {
      // ej: stock insuficiente
      return ResponseEntity.status(409).body(ex.getMessage());
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(ex.getMessage());
    } catch (Exception ex) {
      // loguear si quieres
      return ResponseEntity.status(500).body("Error interno: " + ex.getMessage());
    }
  }
}
