package com.taller5.billing.soap;

import jakarta.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {
    public ObjectFactory() {}

    public CreateInvoiceRequest createCreateInvoiceRequest() { return new CreateInvoiceRequest(); }
    public CreateInvoiceResponse createCreateInvoiceResponse() { return new CreateInvoiceResponse(); }
    public GetInvoiceRequest createGetInvoiceRequest() { return new GetInvoiceRequest(); }
    public GetInvoiceResponse createGetInvoiceResponse() { return new GetInvoiceResponse(); }
}
