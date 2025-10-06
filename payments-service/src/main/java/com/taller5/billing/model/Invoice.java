package com.taller5.billing.model;

import jakarta.persistence.*;

@Entity
@Table(name = "invoice")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // mapea a payment_id en la BDD
    @Column(name = "payment_id", nullable = false, unique = true)
    private Long paymentId;

    public Invoice() {}

    public Invoice(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getId() { return id; }
    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
}
