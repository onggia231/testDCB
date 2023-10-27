package com.telsoft.cbs.utils;

import com.telsoft.cbs.modules.cps_anti_profit.AntiProfitRequest;
import com.telsoft.cbs.modules.cps_anti_profit.AntiProfitResponse;
import org.apache.commons.codec.digest.DigestUtils;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class testRest {


    public static void main(String[] args) {
/*
POST http://mpay.mobifone.vn/checktrucloiapi/services/check
Content-Type:application/json
Accept:application/json

{
  "gamelistId": "0",
  "hashstring": "694dc7a4ef68fee2300718ca27b69f67349a03ebe24929037319fd8876cd23e8",
  "money": "50000",
  "msisdn": "0934435389",
  "partner_id": "000042",
  "subId": "0"
}

 */
        String hash = DigestUtils.sha256Hex(generateStringToHash());
        Client client = ClientBuilder.newClient();
        AntiProfitRequest antiProfitRequest = new AntiProfitRequest();
        antiProfitRequest.setGamelistId("0");
        antiProfitRequest.setHashstring("694dc7a4ef68fee2300718ca27b69f67349a03ebe24929037319fd8876cd23e8");
        antiProfitRequest.setPartner_id("000042");
        antiProfitRequest.setMsisdn("0934435389");
        antiProfitRequest.setSubId("0");
        antiProfitRequest.setMoney("50000");
        try {
            Invocation.Builder invocationBuilder;
            WebTarget target = client.target("http://mpay.mobifone.vn/checktrucloiapi/services/check");
            invocationBuilder = target.request(MediaType.APPLICATION_JSON);
//            if ("10.10.10.1" != null) {
//                invocationBuilder.header("X-Forward-For", "10.10.10.1");
//            }

            Response response;
            if ("POST".equalsIgnoreCase("POST") && antiProfitRequest != null) {
                response = invocationBuilder.method("POST", Entity.entity(antiProfitRequest, MediaType.APPLICATION_JSON));
            } else {
                response = invocationBuilder.method("POST");
            }

            if (response.getStatus() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
            }

            System.out.println(response.getHeaders().toString());

            AntiProfitResponse antiProfitResponse = response.readEntity(AntiProfitResponse.class);
            System.out.println(antiProfitResponse.getMsisdn());
            System.out.println(antiProfitResponse.getResult());
            System.out.println(antiProfitResponse.getDes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String generateStringToHash() {
        return "0934435389" + "50000" + "000042" + "0" + "0" + "globalpay" + "3246dfbc0053bb5c5befac430696ea08";

    }


}
