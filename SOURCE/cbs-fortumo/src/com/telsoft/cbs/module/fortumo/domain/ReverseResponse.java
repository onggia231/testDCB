package com.telsoft.cbs.module.fortumo.domain;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@XmlRootElement(name = "ReverseResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReverseResponse extends Response {
    private String issuerReverseId;

//    @Override
//    public String toString() {
//        return "ReverseResponse{" +
//                "issuerReverseId='" + issuerReverseId + '\'' + ',' +
//                "result='" + getResult() + '\'' +
//                '}';
//    }
}
