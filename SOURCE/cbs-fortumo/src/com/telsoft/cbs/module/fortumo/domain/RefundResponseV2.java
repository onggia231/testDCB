package com.telsoft.cbs.module.fortumo.domain;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@XmlRootElement(name = "RefundResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class RefundResponseV2 extends Response {
    private String issuerRefundId;

//    @Override
//    public String toString() {
//        return "RefundResponseV2{" +
//                "issuerRefundId='" + issuerRefundId + '\'' + ',' +
//                "result='" + getResult() + '\'' +
//                '}';
//    }
}
