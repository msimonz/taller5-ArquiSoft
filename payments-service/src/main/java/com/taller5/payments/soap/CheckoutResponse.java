package com.taller5.payments.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "CheckoutResponse", namespace = "http://taller5.com/soap/checkout")
@XmlAccessorType(XmlAccessType.FIELD)
public class CheckoutResponse {
    @XmlElement(namespace = "http://taller5.com/soap/checkout")
    public Long paymentId;
    @XmlElement(namespace = "http://taller5.com/soap/checkout")
    public Long invoiceId;
    @XmlElement(namespace = "http://taller5.com/soap/checkout")
    public String status;
}
