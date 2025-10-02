package com.taller5.aggregator.controller;

import com.taller5.aggregator.service.CheckoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agg")
@CrossOrigin(origins = "*")
public class AggregatorController {
  private final CheckoutService service;
  public AggregatorController(CheckoutService service){ this.service = service; }

  @PostMapping("/checkout")
  public ResponseEntity<?> checkout(@RequestBody CheckoutService.CheckoutReq req) {
    try {
      var res = service.checkout(req);
      return ResponseEntity.ok(res);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (IllegalStateException e) {
      return ResponseEntity.status(409).body(e.getMessage()); // No hay stock
    }
  }
}
