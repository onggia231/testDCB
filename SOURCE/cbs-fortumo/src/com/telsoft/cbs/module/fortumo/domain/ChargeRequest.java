package com.telsoft.cbs.module.fortumo.domain;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@XmlRootElement(name = "ChargeRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class ChargeRequest extends Request {
    private String authCorrelationId;
    private String authPurchaseTime;
    private String storeTransactionId;
    //private String issuerPaymentId;

    @Override
    public Response createResponse() {
        return new ChargeResponse();
    }

//    @Override
//    public String toString() {
//        return "ChargeRequest{" +
//                "authCorrelationId='" + authCorrelationId + '\'' +
//                ", authPurchaseTime='" + authPurchaseTime + '\'' +
//                '}';
//    }
}
