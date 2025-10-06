package com.taller5.billing.soap;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "CreateInvoiceRequest", namespace = "http://taller5.com/soap/billing")
@XmlAccessorType(XmlAccessType.FIELD)
public class CreateInvoiceRequest {
    @XmlElement(namespace = "http://taller5.com/soap/billing")
    public Long paymentId;
}
