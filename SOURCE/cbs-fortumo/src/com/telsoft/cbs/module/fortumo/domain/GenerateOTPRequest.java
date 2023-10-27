package com.telsoft.cbs.module.fortumo.domain;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@XmlRootElement(name = "GenerateOTPRequest")
@XmlAccessorType(XmlAccessType.FIELD)

public class GenerateOTPRequest extends Request {
    private UserAccount account;
    private String message;
    private String sessionId;
    private String requestId;

    @Override
    public Response createResponse() {
        return new GenerateOTPResponse();
    }
}
