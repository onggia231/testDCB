package com.telsoft.cbs.modules.cps_anti_profit;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name = "request")
@XmlAccessorType(XmlAccessType.FIELD)
public class AntiProfitRequest implements Serializable {
    private static final long serialVersionUID = 8483584210101994677L;
    //"gamelistId": "0",
//        "hashstring": "694dc7a4ef68fee2300718ca27b69f67349a03ebe24929037319fd8876cd23e8",
//        "money": "50000",
//        "msisdn": "0934435389",
//        "partner_id": "000042",
//        "subId": "0"
    @Getter
    @Setter
    private String gamelistId;
    @Getter
    @Setter
    private String hashstring;
    @Getter
    @Setter
    private String money;
    @Getter
    @Setter
    private String msisdn;
    @Getter
    @Setter
    private String partner_id;
    @Getter
    @Setter
    private String subId;

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


    @Override
    public String toString() {
        return "AntiProfitRequest{" +
                "gamelistId='" + gamelistId + '\'' +
                ", hashstring='" + hashstring + '\'' +
                ", money='" + money + '\'' +
                ", msisdn='" + msisdn + '\'' +
                ", partner_id='" + partner_id + '\'' +
                ", subId='" + subId + '\'' +
                '}';
    }
}
