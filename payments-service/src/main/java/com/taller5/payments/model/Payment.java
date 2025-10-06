package com.taller5.payments.model;

import jakarta.persistence.*;

@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    // ↓ sin precision ni scale ↓
    @Column(name = "amount", nullable = false)
    private Double amount;

    public Payment() {}

    public Payment(Long customerId, Long productId, Double amount) {
        this.customerId = customerId;
        this.productId = productId;
        this.amount = amount;
    }

    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public Long getProductId() { return productId; }
    public Double getAmount() { return amount; }

    public void setId(Long id) { this.id = id; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public void setAmount(Double amount) { this.amount = amount; }
}
