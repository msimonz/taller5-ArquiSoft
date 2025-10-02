package com.taller5.inventory.soap;

import jakarta.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {
    public ObjectFactory() {}

    public ReserveRequest createReserveRequest() { return new ReserveRequest(); }
    public ReserveResponse createReserveResponse() { return new ReserveResponse(); }
    public ReleaseRequest createReleaseRequest() { return new ReleaseRequest(); }
    public ReleaseResponse createReleaseResponse() { return new ReleaseResponse(); }
    public ListProductsRequest createListProductsRequest() { return new ListProductsRequest(); }
    public ListProductsResponse createListProductsResponse() { return new ListProductsResponse(); }
    public ProductSoap createProductSoap() { return new ProductSoap(); }
}
