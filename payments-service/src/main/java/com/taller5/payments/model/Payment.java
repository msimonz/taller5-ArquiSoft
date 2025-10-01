package com.taller5.payments.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="payment")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Payment {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private Long customerId;
  private Long productId;
  private Double amount;
}
