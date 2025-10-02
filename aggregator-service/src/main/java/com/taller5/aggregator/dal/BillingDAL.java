package com.taller5.aggregator.dal;

public interface BillingDAL {
  long createInvoice(long paymentId);
}