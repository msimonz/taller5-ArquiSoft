package com.taller5.payments.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "CheckoutRequest", namespace = "http://taller5.com/soap/checkout")
@XmlAccessorType(XmlAccessType.FIELD)
public class CheckoutRequest {
    @XmlElement(namespace = "http://taller5.com/soap/checkout")
    public Long customerId;
    @XmlElement(namespace = "http://taller5.com/soap/checkout")
    public Long productId;
    @XmlElement(namespace = "http://taller5.com/soap/checkout")
    public Integer quantity;
    @XmlElement(namespace = "http://taller5.com/soap/checkout")
    public Double amount;
}
