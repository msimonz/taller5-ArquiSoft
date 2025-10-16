package com.taller5.aggregator.dto;

import java.math.BigDecimal;
import java.util.List;

public record CheckoutReq(
    Long customerId,
    String customerEmail,
    BigDecimal amount,
    String currency,
    String description,
    List<Item> items
) {
  public record Item(long productId, int quantity) {}
}
