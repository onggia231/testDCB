package com.telsoft.cbs.module.fortumo.domain;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@XmlRootElement(name = "AccountProfileRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class AccountProfileRequest extends Request {
    private UserAccount account;

//    @Override
//    public String toString() {
//        return "AccountProfileRequest{" +
//                "account=" + account +
//                '}';
//    }

    @Override
    public Response createResponse() {
        return new AccountProfileResponse();
    }
}
