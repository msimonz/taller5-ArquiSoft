package com.taller5.aggregator.dal;

public interface InventoryDAL {
  void reserve(long productId, int qty);
}