package com.taller5.billing.soap;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "GetInvoiceRequest", namespace = "http://taller5.com/soap/billing")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetInvoiceRequest {
    @XmlElement(namespace = "http://taller5.com/soap/billing")
    public Long id;
}
