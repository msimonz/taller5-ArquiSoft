package com.taller5.billing.soap;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "CreateInvoiceResponse", namespace = "http://taller5.com/soap/billing")
@XmlAccessorType(XmlAccessType.FIELD)
public class CreateInvoiceResponse {
    @XmlElement(namespace = "http://taller5.com/soap/billing")
    public Long invoiceId;

    @XmlElement(namespace = "http://taller5.com/soap/billing")
    public String status; // "CREATED" or "EXISTS"
}
