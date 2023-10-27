package com.telsoft.cbs.module.fortumo.domain;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@XmlRootElement(name = "SubmitMTResponse")
@XmlAccessorType(XmlAccessType.FIELD)

public class SubmitMTResponse extends Response {
    private String correlatorId;
}
