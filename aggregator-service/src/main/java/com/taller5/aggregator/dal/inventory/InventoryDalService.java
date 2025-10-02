package com.taller5.aggregator.dal.inventory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryDalService {
  private final ProductRepository productRepo;

  @Transactional(readOnly = true)
  public List<Product> listProducts() {
    return productRepo.findAll();
  }

  @Transactional
  public void reserve(long productId, int qty) {
    Product p = productRepo.findById(productId)
        .orElseThrow(() -> new IllegalArgumentException("Producto no existe: " + productId));
    if (p.getStock() == null || p.getStock() < qty) {
      throw new IllegalStateException("Stock insuficiente");
    }
    p.setStock(p.getStock() - qty);
    productRepo.save(p);
  }

  @Transactional
  public void release(long productId, int qty) {
    Product p = productRepo.findById(productId)
        .orElseThrow(() -> new IllegalArgumentException("Producto no existe: " + productId));
    p.setStock((p.getStock() == null ? 0 : p.getStock()) + qty);
    productRepo.save(p);
  }
}
