package com.taller5.supplier.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierNotification {
  private Long supplierId;
  private String supplierName;
  private String supplierEmail;
  private Long invoiceId;
  private Long customerId;
  private String customerEmail;
  private List<ProductItem> products;
  private BigDecimal totalAmount;
  private String timestamp;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ProductItem {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
  }
}
