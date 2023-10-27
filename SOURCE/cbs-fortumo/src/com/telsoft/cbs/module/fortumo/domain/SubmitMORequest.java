package com.telsoft.cbs.module.fortumo.domain;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@XmlRootElement(name = "SubmitMORequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class SubmitMORequest extends Request {
    private UserAccount account;
    private String messageId;
    private String destination;
    private String message;
    private String country;
    private String network;

    @Override
    public Response createResponse() {
        return new SubmitMOResponse();
    }

//    @Override
//    public String toString() {
//        return "SubmitMORequest{" +
//                "account=" + account +
//                ", messageId='" + messageId + '\'' +
//                ", destination='" + destination + '\'' +
//                ", message='" + message + '\'' +
//                ", country='" + country + '\'' +
//                ", network='" + network + '\'' +
//                '}';
//    }
}
