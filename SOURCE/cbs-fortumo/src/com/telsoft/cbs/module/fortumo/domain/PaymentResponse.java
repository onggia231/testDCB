package com.telsoft.cbs.module.fortumo.domain;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentResponse implements Serializable {
    private static final long serialVersionUID = -6852463120979448565L;
    private String issuerPaymentId;
    private ResponseResult result;
}
