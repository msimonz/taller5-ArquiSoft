package com.taller5.aggregator.dal.billing;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "invoice")
public class Invoice {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "payment_id", nullable = false)
  private Long paymentId;


  // getters/setters
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getPaymentId() { return paymentId; }
  public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
}
