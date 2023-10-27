package com.telsoft.cbs.module.fortumo.domain;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@XmlRootElement(name = "SubmitMTRequest")
@XmlAccessorType(XmlAccessType.FIELD)

public class SubmitMTRequest extends Request {
    private String messageId;
    private String message;
    private UserAccount account;
    private String originator;
    private Integer validity;

    @Override
    public Response createResponse() {
        return new SubmitMTResponse();
    }
}
