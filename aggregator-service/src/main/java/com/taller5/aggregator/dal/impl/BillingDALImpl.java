package com.taller5.aggregator.dal.impl;

import com.taller5.aggregator.billing.Invoice;
import com.taller5.aggregator.billing.InvoiceRepository;
import com.taller5.aggregator.dal.BillingDAL;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class BillingDALImpl implements BillingDAL {

    private final InvoiceRepository invoiceRepository;

    public BillingDALImpl(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    public long createInvoice(long paymentId) {
        Invoice inv = new Invoice();         // ctor vacío
        inv.setPaymentId(paymentId);         // set obligatorio
        // setea aquí cualquier otro campo NOT NULL…
        return invoiceRepository.saveAndFlush(inv).getId();
    }
}
