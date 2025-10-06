package com.taller5.inventory.soap;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "ReleaseResponse", namespace = "http://taller5.com/soap/inventory")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReleaseResponse {
    @XmlElement(namespace = "http://taller5.com/soap/inventory")
    public String status; // e.g. "OK", "NOT_FOUND"
}
