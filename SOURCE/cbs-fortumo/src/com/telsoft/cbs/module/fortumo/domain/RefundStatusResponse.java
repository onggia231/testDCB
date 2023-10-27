package com.telsoft.cbs.module.fortumo.domain;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@XmlRootElement(name = "RefundStatusResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class RefundStatusResponse extends Response {
    private RefundResponse refundResponse;
}
