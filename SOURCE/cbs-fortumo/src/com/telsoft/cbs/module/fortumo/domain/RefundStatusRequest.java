package com.telsoft.cbs.module.fortumo.domain;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@XmlRootElement(name = "RefundStatusRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class RefundStatusRequest extends Request {
    private String refundRequestId;
    private String refundTime;

//    @Override
//    public String toString() {
//        return "RefundStatusRequest{" +
//                "refundRequestId='" + refundRequestId + '\'' +
//                ", purchaseTime='" + refundTime + '\'' +
//                '}';
//    }

    @Override
    public Response createResponse() {
        return new RefundStatusResponse();
    }
}
