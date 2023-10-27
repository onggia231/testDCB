package com.telsoft.cbs.module.fortumo.domain;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@XmlRootElement(name = "SubmitDNRequest")
@XmlAccessorType(XmlAccessType.FIELD)

public class SubmitDNRequest extends Request {
    private String messageId;
    private String correlationId;
    private String status;
    private String message;
    private String country;
    private String network;

    @Override
    public Response createResponse() {
        return new SubmitDNResponse();
    }
}
