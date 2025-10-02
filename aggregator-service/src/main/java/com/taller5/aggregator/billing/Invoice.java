package com.taller5.aggregator.billing;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="invoice")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Invoice {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name="payment_id", nullable=false, unique=true)
  private Long paymentId;
}
