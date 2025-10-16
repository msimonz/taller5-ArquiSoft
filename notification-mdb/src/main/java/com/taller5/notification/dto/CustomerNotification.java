package com.taller5.notification.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class CustomerNotification implements Serializable {
  private static final long serialVersionUID = 1L;
  
  private Long invoiceId;
  private Long customerId;
  private String customerEmail;
  private BigDecimal totalAmount;
  private String currency;
  private List<PurchasedItem> items;
  private String timestamp;

  public CustomerNotification() {}

  // Getters and Setters
  public Long getInvoiceId() { return invoiceId; }
  public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }

  public Long getCustomerId() { return customerId; }
  public void setCustomerId(Long customerId) { this.customerId = customerId; }

  public String getCustomerEmail() { return customerEmail; }
  public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

  public BigDecimal getTotalAmount() { return totalAmount; }
  public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

  public String getCurrency() { return currency; }
  public void setCurrency(String currency) { this.currency = currency; }

  public List<PurchasedItem> getItems() { return items; }
  public void setItems(List<PurchasedItem> items) { this.items = items; }

  public String getTimestamp() { return timestamp; }
  public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

  public static class PurchasedItem implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    public PurchasedItem() {}

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
  }
}
