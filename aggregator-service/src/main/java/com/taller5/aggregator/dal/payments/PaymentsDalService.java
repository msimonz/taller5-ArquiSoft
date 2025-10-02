// dal/payments/PaymentsDalService.java
package com.taller5.aggregator.dal.payments;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentsDalService {
  private final PaymentRepository repo;
  public PaymentsDalService(PaymentRepository repo) { this.repo = repo; }

  @Transactional
  public Payment charge(Long customerId, Long productId, java.math.BigDecimal amount) {
    // aquí harías lógica de cobro real si la hubiera; por ahora persistimos
    Payment p = new Payment();
    p.setCustomerId(customerId);
    p.setProductId(productId);
    p.setAmount(amount);
    return repo.save(p);
  }

  public Payment get(Long id) { return repo.findById(id).orElse(null); }
  public List<Payment> list() { return repo.findAll(); }
}
