package com.telsoft.cbs.module.fortumo.domain;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@XmlRootElement(name = "RefundRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class RefundRequestV2 extends Request {
    private String authCorrelationId;
    private String authPurchaseTime;
    //private String issuerPaymentId;
    private String refundReason;
    private Double refundAmount;

//    @Override
//    public String toString() {
//        return "RefundRequestV2{" +
//                "authCorrelationId='" + authCorrelationId + '\'' +
//                ", authPurchaseTime='" + authPurchaseTime + '\'' +
//                ", refundReason='" + refundReason + '\'' +
//                ", refundAmount='" + refundAmount + '\'' +
//                '}';
//    }

    @Override
    public <T extends Response> T createResponse() {
        return null;
    }
}
