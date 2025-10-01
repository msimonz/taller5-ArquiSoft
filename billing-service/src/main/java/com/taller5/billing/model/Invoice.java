package com.taller5.billing.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "invoice")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Invoice {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;          // invoice id (PK autoincrement)

  @Column(name = "payment_id", nullable = false, unique = true)
  private Long paymentId;   // id del pago (único)
}
