package com.taller5.aggregator.dal.inventory;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "supplier")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Supplier {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  private String email;
}
