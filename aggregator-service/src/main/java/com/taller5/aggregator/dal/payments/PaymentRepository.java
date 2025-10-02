// dal/payments/PaymentRepository.java
package com.taller5.aggregator.dal.payments;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {}
