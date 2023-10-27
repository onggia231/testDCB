package com.telsoft.cbs.module.fortumo.domain;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@XmlRootElement(name = "AccountChangeRequestDTO")
@XmlAccessorType(XmlAccessType.FIELD)
public class AccountChangeRequestDTO extends Request{
    private String cbs_transaction_id;
    private String msisdn;

    @Override
    public Response createResponse() {
        return new AccountChangeResponse();
    }

//    @Override
//    public String toString() {
//        return "AccountChangeRequestDTO{" +
//                "cbs_transaction_id='" + cbs_transaction_id + '\'' +
//                ", msisdn='" + msisdn + '\'' +
//                '}';
//    }
}
