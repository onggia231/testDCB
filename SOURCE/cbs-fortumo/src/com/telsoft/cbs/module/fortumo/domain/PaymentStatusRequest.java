package com.telsoft.cbs.module.fortumo.domain;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@XmlRootElement(name = "PaymentStatusRequest")
@XmlAccessorType(XmlAccessType.FIELD)

public class PaymentStatusRequest extends Request {
    private String paymentRequestId;
    private String purchaseTime;

//    @Override
//    public String toString() {
//        return "PaymentStatusRequest{" +
//                "paymentRequestId='" + paymentRequestId + '\'' +
//                ", purchaseTime='" + purchaseTime + '\'' +
//                '}';
//    }

    @Override
    public Response createResponse() {
        return new PaymentStatusResponse();
    }
}
