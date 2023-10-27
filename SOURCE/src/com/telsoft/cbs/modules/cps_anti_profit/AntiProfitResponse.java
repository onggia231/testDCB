package com.telsoft.cbs.modules.cps_anti_profit;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class AntiProfitResponse implements Serializable {

    private static final long serialVersionUID = 6710747630149931473L;
    @Getter
    @Setter
    private String msisdn;

    @Getter
    @Setter
    private String result;

    @Getter
    @Setter
    private String des;

    @Override
    public String toString() {
        return "AntiProfitResponse{" +
                "msisdn='" + msisdn + '\'' +
                ", result='" + result + '\'' +
                ", des='" + des + '\'' +
                '}';
    }

}
