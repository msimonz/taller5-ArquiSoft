package com.taller5.inventory.controller;

import com.taller5.inventory.dal.AggregatorDalClient;
import com.taller5.inventory.dal.AggregatorDalClient.ProductDto;
import com.taller5.inventory.dal.AggregatorDalClient.ReserveReq;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class InventoryController {

  private final AggregatorDalClient dal;

  public InventoryController(AggregatorDalClient dal) {
    this.dal = dal;
  }

  @GetMapping("/products")
  public List<ProductDto> list() {
    return dal.listProducts();
  }

  @PostMapping("/inventory/reserve")
  public ResponseEntity<Void> reserve(@RequestBody ReserveReq req) {
    dal.reserve(req);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/inventory/release")
  public ResponseEntity<Void> release(@RequestBody ReserveReq req) {
    dal.release(req);
    return ResponseEntity.ok().build();
  }
}
