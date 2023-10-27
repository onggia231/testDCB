package com.telsoft.cbs.modules.rest.domain;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.*;
import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "request")
@XmlAccessorType(XmlAccessType.FIELD)
public class Request implements Serializable {
    private static final long serialVersionUID = -173039793964931121L;
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

    public static Response createResponse(Request request) {
        if (request != null)
            return request.createResponse();
        return null;
    }

    public CBRequest createCBRequest() {
        CBRequest cbRequest = new CBRequest();
        cbRequest.set(CbsContansts.MSISDN, getIsdn());
        cbRequest.set(CbsContansts.ORIGINATOR, getSource());
        cbRequest.set(CbsContansts.CLIENT_TRANSACTION_ID, getTransaction_id());
        cbRequest.set(CbsContansts.STORE_CODE, getStore_code());
        cbRequest.setCommand(getName());
        if (parameters != null) {
            parameters.forEach((a) -> {
                cbRequest.set(a.key, a.value);
            });
        }
        return cbRequest;
    }

    public Response createResponse(CBResponse cbResponse) {
        Response response = createResponse();
        response.setCode(cbResponse.getCode().getCode());

        if (cbResponse.getMessage() != null) {
            response.setDescription(cbResponse.getMessage());
        } else {
            response.setDescription(CBCode.getDescription(response.getCode()));
        }

        Map map = cbResponse.getValues();
        map.forEach((k, v) -> {
            if ("code".equals(k) || "msg".equals(k))
                return;
            response.getParameters().add(new Attribute((String) k, String.valueOf(v)));
        });
        return response;
    }

    public Response createResponse() {
        Response response = new Response();
        response.setName(getName());
        response.setIsdn(getIsdn());
        response.setSource(getSource());
        response.setTransaction_id(getTransaction_id());
        return response;
    }
}
