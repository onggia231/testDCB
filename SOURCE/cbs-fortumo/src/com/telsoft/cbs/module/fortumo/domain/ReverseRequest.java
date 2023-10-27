package com.telsoft.cbs.module.fortumo.domain;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@XmlRootElement(name = "ReverseRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReverseRequest extends Request {
    private String authCorrelationId;
    private String authPurchaseTime;
    private String storeTransactionId;
    //private String issuerPaymentId;

    @Override
    public Response createResponse() {
        return new ReverseResponse();
    }

//    @Override
//    public String toString() {
//        return "ReverseRequest{" +
//                "authCorrelationId='" + authCorrelationId + '\'' +
//                ", authPurchaseTime='" + authPurchaseTime + '\'' +
//                '}';
//    }
}
