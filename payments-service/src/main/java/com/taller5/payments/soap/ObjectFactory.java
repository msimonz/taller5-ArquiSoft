package com.taller5.payments.soap;

import jakarta.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {
    public ObjectFactory() {}

    public CheckoutRequest createCheckoutRequest() {
        return new CheckoutRequest();
    }

    public CheckoutResponse createCheckoutResponse() {
        return new CheckoutResponse();
    }
}
