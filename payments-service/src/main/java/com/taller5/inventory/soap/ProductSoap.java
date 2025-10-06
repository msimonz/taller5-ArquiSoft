package com.taller5.inventory.soap;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class ProductSoap {
    @XmlElement(namespace = "http://taller5.com/soap/inventory")
    public Long id;
    @XmlElement(namespace = "http://taller5.com/soap/inventory")
    public String name;
    @XmlElement(namespace = "http://taller5.com/soap/inventory")
    public Double price;
    @XmlElement(namespace = "http://taller5.com/soap/inventory")
    public Integer stock;
}
