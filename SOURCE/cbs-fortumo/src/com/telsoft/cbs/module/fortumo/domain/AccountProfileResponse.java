package com.telsoft.cbs.module.fortumo.domain;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Getter
@Setter
@XmlRootElement(name = "AccountProfileResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class AccountProfileResponse extends Response {

    @XmlElement(name = "attribute")
    private List<Attribute> attributes;
}
