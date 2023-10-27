package com.telsoft.cbs.module.fortumo.domain;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@XmlRootElement(name = "AuthResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class AuthResponse extends Response {
    private String issuerPaymentId;

//    @Override
//    public String toString() {
//        return "AuthResponse{" +
//                "issuerPaymentId='" + issuerPaymentId + '\'' + ',' +
//                "result='" + getResult() + '\'' +
//                '}';
//    }
}
