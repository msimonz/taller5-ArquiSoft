package com.taller5.aggregator.dal.impl;

import com.taller5.aggregator.dal.PaymentsDAL;
import com.taller5.aggregator.payments.Payment;
import com.taller5.aggregator.payments.PaymentRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class PaymentsDALImpl implements PaymentsDAL {

    private final PaymentRepository paymentRepository;

    public PaymentsDALImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public long createPayment(long customerId, long productId, double amount) {
        Payment p = new Payment();           // ctor vacío
        p.setCustomerId(customerId);
        p.setProductId(productId);
        p.setAmount(amount);
        // setea aquí cualquier otro campo NOT NULL…
        return paymentRepository.saveAndFlush(p).getId();
    }
}
