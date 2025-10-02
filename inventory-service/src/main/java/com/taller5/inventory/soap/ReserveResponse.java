package com.taller5.inventory.soap;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "ReserveResponse", namespace = "http://taller5.com/soap/inventory")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReserveResponse {
    @XmlElement(namespace = "http://taller5.com/soap/inventory")
    public String status; // e.g. "OK", "NO_STOCK", "NOT_FOUND"
}
