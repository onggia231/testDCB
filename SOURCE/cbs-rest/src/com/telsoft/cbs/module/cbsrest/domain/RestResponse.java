package com.telsoft.cbs.module.cbsrest.domain;

import com.telsoft.httpservice.domain.XmlEnt;
import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class RestResponse extends XmlEnt implements Serializable, com.telsoft.httpservice.domain.RestResponse {
    private static final long serialVersionUID = -6030745052849218251L;
    @Getter
    @Setter
    @XmlElement(name = "parameter")
    private List<Attribute> parameters = new ArrayList<>();

    @Getter
    @Setter
    private CBCommand name;

    @Getter
    @Setter
    private int code;
    @Getter
    @Setter
    private String description;
    @Getter
    @Setter
    private String isdn;
    @Getter
    @Setter
    private String transaction_id;
    @Getter
    @Setter
    private String source;

    public CBCode getCBCode() {
        return CBCode.getCode(getCode());
    }

//    @Override
//    public String toString() {
//        return "{" +
//                "parameters=" + parameters +
//                ", name=" + name +
//                ", code=" + code +
//                ", description='" + description + '\'' +
//                ", isdn='" + isdn + '\'' +
//                ", transaction_id='" + transaction_id + '\'' +
//                ", source='" + source + '\'' +
//                '}';
//    }

    public String getParameter(String name) {
        for (Attribute attribute : getParameters()) {
            if (attribute.key.equals(name))
                return attribute.value;
        }
        return null;
    }
}
