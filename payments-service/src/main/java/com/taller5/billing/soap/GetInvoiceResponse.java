package com.taller5.billing.soap;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "GetInvoiceResponse", namespace = "http://taller5.com/soap/billing")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetInvoiceResponse {
    @XmlElement(namespace = "http://taller5.com/soap/billing")
    public Long invoiceId;

    @XmlElement(namespace = "http://taller5.com/soap/billing")
    public Long paymentId;
}
