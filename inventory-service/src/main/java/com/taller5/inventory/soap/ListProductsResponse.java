package com.taller5.inventory.soap;

import jakarta.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "ListProductsResponse", namespace = "http://taller5.com/soap/inventory")
@XmlAccessorType(XmlAccessType.FIELD)
public class ListProductsResponse {

    @XmlElement(name = "product", namespace = "http://taller5.com/soap/inventory")
    public List<ProductSoap> product;
}
