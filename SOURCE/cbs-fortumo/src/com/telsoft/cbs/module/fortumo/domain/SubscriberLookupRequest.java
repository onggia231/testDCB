package com.telsoft.cbs.module.fortumo.domain;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@XmlRootElement(name = "SubscriberLookupRequest")
@XmlAccessorType(XmlAccessType.FIELD)

public class SubscriberLookupRequest extends Request {
    private UserAccount account;

//    @Override
//    public String toString() {
//        return "SubscriberLookupRequest{" +
//                "account=" + account +
//                '}';
//    }

    @Override
    public <T extends Response> T createResponse() {
        return (T) new SubscriberLookupResponse();
    }
}
