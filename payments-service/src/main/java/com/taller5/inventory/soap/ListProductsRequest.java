package com.taller5.inventory.soap;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "ListProductsRequest", namespace = "http://taller5.com/soap/inventory")
@XmlAccessorType(XmlAccessType.FIELD)
public class ListProductsRequest {
    // empty request
}
