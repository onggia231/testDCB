package com.telsoft.cbs.module.fortumo.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
public class Attribute {
    @XmlAttribute
    String key;

    @XmlValue
    String value;

    private Attribute() {
    }

    public Attribute(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
