package com.taller5.aggregator.dal.impl;

import com.taller5.aggregator.dal.InventoryDAL;
import com.taller5.aggregator.inventory.ProductRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class InventoryDALImpl implements InventoryDAL {

    private final ProductRepository repo;

    public InventoryDALImpl(ProductRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public void reserve(long productId, int quantity) {
        int updated = repo.tryReserve(productId, quantity);
        if (updated == 0) {
            throw new IllegalStateException("Sin stock suficiente o hubo colisión concurrente");
        }
    }
}
