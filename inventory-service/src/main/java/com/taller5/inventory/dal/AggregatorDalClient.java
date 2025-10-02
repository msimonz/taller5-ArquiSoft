package com.taller5.inventory.dal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "aggregatorDal", url = "${aggregator.url}")
public interface AggregatorDalClient {

  @GetMapping("/dal/inventory/products")
  List<ProductDto> listProducts();

  @PostMapping("/dal/inventory/reserve")
  void reserve(@RequestBody ReserveReq req);

  @PostMapping("/dal/inventory/release")
  void release(@RequestBody ReserveReq req);

  // ===== DTOs del DAL =====
  record ProductDto(Long id, String name, BigDecimal price, Integer stock) {}
  record ReserveReq(Long productId, Integer quantity) {}
}
