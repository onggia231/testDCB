package com.telsoft.cbs.module.fortumo.domain;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@XmlRootElement(name = "SubmitMORequestDTO")
@XmlAccessorType(XmlAccessType.FIELD)
public class SubmitMORequestDTO extends Request {
    private String cbs_transaction_id;
    private String msisdn;
    private String short_code;
    private String content;

    @Override
    public Response createResponse() {
        return new SubmitMOResponse();
    }

//    @Override
//    public String toString() {
//        return "SubmitMORequestDTO{" +
//                "cbs_transaction_id='" + cbs_transaction_id + '\'' +
//                ", msisdn='" + msisdn + '\'' +
//                ", short_code='" + short_code + '\'' +
//                ", content='" + content + '\'' +
//                '}';
//    }
}
