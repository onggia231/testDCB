package com.telsoft.cbs.modules.selfcare.rest;

import com.telsoft.cbs.domain.CBCode;
import com.telsoft.cbs.domain.CBResponse;
import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name = "request")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetOTPRequest implements Serializable {

    private static final long serialVersionUID = 3006798162011552155L;
    @Getter
    @Setter
    private String msisdn;

    @Getter
    @Setter
    private String otp;
//
//    public static GetOTPResponse createResponse(GetOTPRequest request) {
//        if (request != null)
//            return request.createResponse();
//        return null;
//    }
//
//
//    public GetOTPResponse createResponse(CBResponse cbResponse) {
//        GetOTPResponse response = createResponse();
//        response.setCode(cbResponse.getCode().getCode());
//
//        if (cbResponse.getMessage() != null) {
//            response.setDescription(cbResponse.getMessage());
//        } else {
//            response.setDescription(CBCode.getDescription(response.getCode()));
//        }
//        return response;
//    }


    public GetOTPResponse createResponse() {
        GetOTPResponse response = new GetOTPResponse();
        response.setOtp(getOtp());
        return response;
    }
}
