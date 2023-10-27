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
public class RefundResponse extends Response {
    private String issuerRefundId;

//    @Override
//    public String toString() {
//        return "RefundResponse{" +
//                "issuerRefundId='" + issuerRefundId + '\'' + ',' +
//                "result='" + getResult() + '\'' +
//                '}';
//    }
}
