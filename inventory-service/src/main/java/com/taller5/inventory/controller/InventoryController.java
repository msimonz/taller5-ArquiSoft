package com.taller5.inventory.controller;

import com.taller5.inventory.model.Product;
import com.taller5.inventory.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // ajusta en prod
public class InventoryController {

  private final ProductRepository repo;
  public InventoryController(ProductRepository repo) { this.repo = repo; }

  // 1) Catálogo
  @GetMapping("/products")
  public List<Product> list() { return repo.findAll(); }

  // 2) Reserva muy simple (descuenta stock si hay)
  public record ReserveReq(Long productId, Integer quantity) {}

  @PostMapping("/inventory/reserve")
  @Transactional
  public ResponseEntity<?> reserve(@RequestBody ReserveReq req) {
    if (req.productId() == null || req.quantity() == null || req.quantity() <= 0) {
      return ResponseEntity.badRequest().body("productId y quantity son requeridos");
    }
    var p = repo.findById(req.productId()).orElse(null);
    if (p == null) return ResponseEntity.notFound().build();
    if (p.getStock() < req.quantity()) return ResponseEntity.status(409).body("No hay stock");
    p.setStock(p.getStock() - req.quantity()); // JPA hace flush por dirty checking
    return ResponseEntity.ok().build();
  }

  // 3) Compensación (libera stock)
  @PostMapping("/inventory/release")
  @Transactional
  public ResponseEntity<?> release(@RequestBody ReserveReq req) {
    if (req.productId() == null || req.quantity() == null || req.quantity() <= 0) {
      return ResponseEntity.badRequest().body("productId y quantity son requeridos");
    }
    var p = repo.findById(req.productId()).orElse(null);
    if (p == null) return ResponseEntity.notFound().build();
    p.setStock(p.getStock() + req.quantity());
    return ResponseEntity.ok().build();
  }
}
