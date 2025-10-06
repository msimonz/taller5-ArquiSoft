package com.taller5.inventory.controller;

import com.taller5.inventory.model.Product;
import com.taller5.inventory.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class InventoryController {

  private final ProductRepository repo;
  public InventoryController(ProductRepository repo) { this.repo = repo; }

  @GetMapping("/products")
  public List<Product> list() { return repo.findAll(); }

  public record ReserveReq(Long productId, Integer quantity) {}

  @PostMapping("/inventory/reserve")
  public ResponseEntity<?> reserve(@RequestBody ReserveReq req) {
    if (req == null || req.productId() == null || req.quantity() == null || req.quantity() <= 0) {
      return ResponseEntity.badRequest().body("productId y quantity son requeridos");
    }
    var p = repo.findById(req.productId()).orElse(null);
    if (p == null) return ResponseEntity.notFound().build();
    Integer stock = p.getStock() == null ? 0 : p.getStock();
    if (stock < req.quantity()) return ResponseEntity.status(409).body("No hay stock");
    p.setStock(stock - req.quantity());
    repo.save(p); // persistimos aquí; si usas optimistic locking, L2 detectará versión
    return ResponseEntity.ok().build();
  }

  @PostMapping("/inventory/release")
  public ResponseEntity<?> release(@RequestBody ReserveReq req) {
    if (req == null || req.productId() == null || req.quantity() == null || req.quantity() <= 0) {
      return ResponseEntity.badRequest().body("productId y quantity son requeridos");
    }
    var p = repo.findById(req.productId()).orElse(null);
    if (p == null) return ResponseEntity.notFound().build();
    Integer stock = p.getStock() == null ? 0 : p.getStock();
    p.setStock(stock + req.quantity());
    repo.save(p);
    return ResponseEntity.ok().build();
  }
}
