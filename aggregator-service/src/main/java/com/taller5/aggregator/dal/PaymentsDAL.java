package com.taller5.aggregator.dal;

public interface PaymentsDAL {
  long createPayment(long customerId, long productId, double amount);
}
