package com.taller5.aggregator.dal.payments;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payment")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "customer_id", nullable = false)
  private Long customerId;

  @Column(name = "customer_email", nullable = false)
  private String customerEmail;

  @Column(name = "amount", nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  @Column(name = "currency")
  private String currency;

  @Column(name = "description")
  private String description;

  @Column(name = "status")
  private String status;

  @Column(name = "created_at")
  private Instant createdAt;
}
