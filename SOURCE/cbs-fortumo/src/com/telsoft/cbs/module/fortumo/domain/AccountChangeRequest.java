package com.telsoft.cbs.module.fortumo.domain;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@XmlRootElement(name = "AccountChangeRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class AccountChangeRequest extends Request {
    private String messageId;
    private UserAccount account;

    @Override
    public Response createResponse() {
        return new AccountChangeResponse();
    }

//    @Override
//    public String toString() {
//        return "AccountChangeRequest{" +
//                "messageId='" + messageId + '\'' +
//                ", account=" + account +
//                '}';
//    }
}
