package com.telsoft.cbs.module.cbsrest.domain;

import lombok.Getter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
public class Attribute {
    @XmlAttribute
    @Getter
    String key;

    @XmlValue
    @Getter
    String value;

    private Attribute() {
    }

    public Attribute(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return key + '=' + value;
    }
}
