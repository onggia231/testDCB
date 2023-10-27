package com.telsoft.cbs.module.fortumo.domain;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@XmlRootElement(name = "CheckEligibilityRequest")
@XmlAccessorType(XmlAccessType.FIELD)

public class CheckEligibilityRequest extends Request {
    private UserAccount account;

//    @Override
//    public String toString() {
//        return "CheckEligibilityRequest{" +
//                "account=" + account +
//                '}';
//    }

    @Override
    public Response createResponse() {
        return new CheckEligibilityResponse();
    }

}
