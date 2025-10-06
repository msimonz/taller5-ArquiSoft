package com.taller5.billing.repository;

import com.taller5.billing.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
  Optional<Invoice> findByPaymentId(Long paymentId);
}
