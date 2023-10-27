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
public class RefundRequest extends Request {
    private String authCorrelationId;
    private String authPurchaseTime;
    private String storeTransactionId;
    //private String issuerPaymentId;
    private String refundReason;

    @Override
    public Response createResponse() {
        return new RefundResponse();
    }

//    @Override
//    public String toString() {
//        return "RefundRequest{" +
//                "authCorrelationId='" + authCorrelationId + '\'' +
//                ", authPurchaseTime='" + authPurchaseTime + '\'' +
//                ", refundReason='" + refundReason + '\'' +
//                '}';
//    }
}
