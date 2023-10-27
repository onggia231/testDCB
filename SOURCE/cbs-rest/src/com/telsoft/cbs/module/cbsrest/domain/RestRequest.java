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

@XmlRootElement(name = "request")
@XmlAccessorType(XmlAccessType.FIELD)
public class RestRequest extends XmlEnt implements Serializable, com.telsoft.httpservice.domain.RestRequest {
    private static final long serialVersionUID = -4851826989092673084L;
    @Getter
    @Setter
    @XmlElement(name = "parameter")
    private List<Attribute> parameters = new ArrayList<>();
    @Getter
    @Setter
    private CBCommand name;
    @Getter
    @Setter
    private String isdn;
    @Getter
    @Setter
    private String transaction_id;
    @Getter
    @Setter
    private String store_code;
    @Getter
    @Setter
    private String source;

    public static RestResponse createResponse(RestRequest request) {
        if (request != null)
            return request.createResponse();
        return null;
    }

    public String getParameter(String name) {
        for (Attribute attribute : getParameters()) {
            if (attribute.key.equals(name))
                return attribute.value;
        }
        return null;
    }

//    @Override
//    public String toString() {
//        return "{" +
//                "parameters=" + parameters +
//                ", name=" + name +
//                ", isdn='" + isdn + '\'' +
//                ", transaction_id='" + transaction_id + '\'' +
//                ", store_code='" + store_code + '\'' +
//                ", source='" + source + '\'' +
//                '}';
//    }

    public RestResponse createResponse() {
        RestResponse response = new RestResponse();
        response.setName(getName());
        response.setIsdn(getIsdn());
        response.setSource(getSource());
        response.setTransaction_id(getTransaction_id());
        return response;
    }
}
