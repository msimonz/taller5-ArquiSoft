package com.taller5.billing.dto;

import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerNotification implements Serializable {
  private static final long serialVersionUID = 1L;
  private Long invoiceId;
  private Long customerId;
  private String customerEmail;
  private BigDecimal totalAmount;
  private String currency;
  private List<PurchasedItem> items;
  private String timestamp;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class PurchasedItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
  }
}
