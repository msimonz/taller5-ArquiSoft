package com.taller5.inventory.soap;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "ReleaseRequest", namespace = "http://taller5.com/soap/inventory")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReleaseRequest {
    @XmlElement(namespace = "http://taller5.com/soap/inventory")
    public Long productId;

    @XmlElement(namespace = "http://taller5.com/soap/inventory")
    public Integer quantity;
}
