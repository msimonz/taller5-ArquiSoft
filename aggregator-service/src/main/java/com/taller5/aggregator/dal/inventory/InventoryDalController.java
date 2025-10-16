package com.taller5.aggregator.dal.inventory;

import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/dal/inventory")
@RequiredArgsConstructor
public class InventoryDalController {

  private final InventoryDalService service;

  // GET /dal/inventory/products
  @GetMapping("/products")
  public List<ProductDto> list() {
    return service.listProducts().stream()
        .map(p -> new ProductDto(
            p.getId(), 
            p.getName(), 
            p.getPrice(), 
            p.getStock(),
            p.getSupplier() != null ? new SupplierDto(
                p.getSupplier().getId(),
                p.getSupplier().getName(),
                p.getSupplier().getEmail()
            ) : null
        ))
        .toList();
  }

  // POST /dal/inventory/reserve {productId, quantity}
  @PostMapping("/reserve")
  public ResponseEntity<Void> reserve(@RequestBody ReserveReq req) {
    service.reserve(req.productId(), req.quantity());
    return ResponseEntity.ok().build();
  }

  // POST /dal/inventory/release {productId, quantity}
  @PostMapping("/release")
  public ResponseEntity<Void> release(@RequestBody ReserveReq req) {
    service.release(req.productId(), req.quantity());
    return ResponseEntity.ok().build();
  }

  // --- DTOs ---
  public record ProductDto(Long id, String name, BigDecimal price, Integer stock, SupplierDto supplier) {}
  public record SupplierDto(Long id, String name, String email) {}
  public record ReserveReq(long productId, int quantity) {}
}
