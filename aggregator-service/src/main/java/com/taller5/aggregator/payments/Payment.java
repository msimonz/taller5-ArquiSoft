package com.taller5.aggregator.payments;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="payment")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Payment {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name="customer_id", nullable=false) private Long customerId;
  @Column(name="product_id",  nullable=false) private Long productId;
  @Column(nullable=false) private Double amount;
}
