package com.telsoft.cbs.module.fortumo.domain;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class CurrencyAmount implements Serializable {
    private static final long serialVersionUID = -3676418211917164582L;
    private String currency;
    private String amount;
}
