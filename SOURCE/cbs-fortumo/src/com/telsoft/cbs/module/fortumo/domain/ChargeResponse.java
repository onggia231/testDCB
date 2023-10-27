package com.telsoft.cbs.module.fortumo.domain;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@XmlRootElement(name = "ChargeResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class ChargeResponse extends Response {
    private String issuerChargeId;

//    @Override
//    public String toString() {
//        return "ChargeResponse{" +
//                "issuerChargeId='" + issuerChargeId + '\'' + ',' +
//                "result='" + getResult() + '\'' +
//                '}';
//    }
}
