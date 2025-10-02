// dal/payments/Payment.java
package com.taller5.aggregator.dal.payments;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payment")
public class Payment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "customer_id", nullable = false) // <-- snake_case
  private Long customerId;

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Column(name = "amount", nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  // getters/setters
    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public Long getProductId() { return productId; }
    public BigDecimal getAmount() { return amount; }
    public void setId(Long id) { this.id = id; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
