package com.telsoft.cbs.module.fortumo.domain;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@XmlRootElement(name = "ValidateOTPRequest")
@XmlAccessorType(XmlAccessType.FIELD)

public class ValidateOTPRequest extends Request {
    private UserAccount account;
    private String otp;
    private String sessionId;
    private String requestId;

    @Override
    public Response createResponse() {
        return new ValidateOTPResponse();
    }
}
