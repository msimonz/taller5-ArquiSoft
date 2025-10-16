package com.taller5.aggregator.dal.billing;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoice")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Invoice {
  @Id 
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "payment_id", nullable = true)
  private Long paymentId;

  @Column(name = "customer_id", nullable = false)
  private Long customerId;

  @Column(name = "customer_email", nullable = false)
  private String customerEmail;

  @Column(name = "total_amount", nullable = false)
  private BigDecimal totalAmount;

  @Column(name = "created_at")
  private Instant createdAt;

  @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<InvoiceItem> items = new ArrayList<>();

  public void addItem(InvoiceItem item) {
    items.add(item);
    item.setInvoice(this);
  }
}
