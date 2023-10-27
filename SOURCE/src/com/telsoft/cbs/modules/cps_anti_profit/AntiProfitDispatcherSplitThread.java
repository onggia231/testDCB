package com.telsoft.cbs.modules.cps_anti_profit;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.utils.CbsUtils;
import lombok.Getter;
import lombok.Setter;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import telsoft.gateway.core.dsp.Dispatcher;
import telsoft.gateway.core.dsp.DispatcherManager;
import telsoft.gateway.core.log.MessageContext;
import telsoft.gateway.core.log.ServerCommand;
import telsoft.gateway.core.message.GatewayMessage;
import telsoft.gateway.core.message.MessageUtil;
import telsoft.gateway.core.message.PlainMessage;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

public class AntiProfitDispatcherSplitThread extends Dispatcher {
    private final static Logger log = LoggerFactory.getLogger(AntiProfitDispatcherSplitThread.class);
    @Getter
    @Setter
    private String username;

    @Getter
    @Setter
    private String password;

    @Getter
    @Setter
    private String host;

    @Getter
    @Setter
    private String uri;

    @Getter
    @Setter
    private int port;

    @Getter
    @Setter
    private int retryCount;

    @Getter
    @Setter
    private int retryDelay;

    @Getter
    @Setter
    private int miTimeout;

    @Setter
    @Getter
    private String gameListId;

    @Setter
    @Getter
    private String partnerId;

    @Setter
    @Getter
    private String subId;

    private Client client;

//    private AntiProfitServiceSoapBindingStub bindingStub;

    protected AntiProfitDispatcherSplitThread(DispatcherManager mgr) {
        super(mgr);
    }


    @Override
    public void open() throws Exception {
        client = ClientBuilder.newClient();
        logMonitor("Prepared client [" + this._getSequence() + "]");
    }

    @Override
    public void close() {
        if (client != null) {
            try {
                client.close();
            }catch (Exception ex){
                //ignore;
            }
            client = null;
        }
        logMonitor("Closed client [" + this._getSequence() + "]");
    }

    @Override
    public boolean isOpen() {
        return super.isOpen() && client != null;
    }

    @Override
    public String getProtocolName() {
        return "Anti-Profit";
    }

    @Override
    public GatewayMessage processMessage(MessageContext msgContext, GatewayMessage gatewayMessage) throws Exception {
//
        ServerCommand sc = null;
        if (msgContext != null) {
            sc = msgContext.addServerInfo(this);
            sc.setServerRequest(gatewayMessage);
        }
        String result = "";
        String url = getHost() + (getPort() < 0 ? "" : (":" + getPort())) + getUri();
        PlainMessage message = null;
        Map<String, String> msgMap = new HashMap<>();
        MessageUtil.analyseMappedMessage(gatewayMessage.getContent(), ",", "=", msgMap, true);
        String money = msgMap.get("money");
        String msisdn = msgMap.get("msisdn");
//        String money = "50000";
//        String msisdn = "0934435389";

        String hashString = msisdn + money + partnerId + gameListId + subId + username + password;

        String sha256hex = DigestUtils.sha256Hex(hashString);


        com.telsoft.cbs.modules.cps_anti_profit.AntiProfitRequest antiProfitRequest = new AntiProfitRequest();
        antiProfitRequest.setGamelistId(gameListId);
        antiProfitRequest.setHashstring(sha256hex);
//        antiProfitRequest.setHashstring("694dc7a4ef68fee2300718ca27b69f67349a03ebe24929037319fd8876cd23e8");
        antiProfitRequest.setPartner_id(partnerId);
        antiProfitRequest.setMsisdn(msisdn);
        antiProfitRequest.setSubId(subId);
        antiProfitRequest.setMoney(money);


//        AntiProfitRequest antiProfitRequest = new AntiProfitRequest();
//        antiProfitRequest.setGamelistId("0");
//        antiProfitRequest.setHashstring("694dc7a4ef68fee2300718ca27b69f67349a03ebe24929037319fd8876cd23e8");
//        antiProfitRequest.setPartner_id("000042");
//        antiProfitRequest.setMsisdn("0934435389");
//        antiProfitRequest.setSubId("0");
//        antiProfitRequest.setMoney("50000");
        try {
            Invocation.Builder invocationBuilder;
//            WebTarget target = client.target("http://mpay.mobifone.vn/checktrucloiapi/services/check");
            WebTarget target = client.target(url);
            invocationBuilder = target.request(MediaType.APPLICATION_JSON);

            Response response = null;
            boolean bSuccessCall = false;
            for (int i = 0; i <= retryCount; i++) {
                if ("POST".equalsIgnoreCase("POST") && antiProfitRequest != null) {
                    logSend(antiProfitRequest.toString());
                    response = invocationBuilder.method("POST", Entity.entity(antiProfitRequest, MediaType.APPLICATION_JSON));
                } else {
                    response = invocationBuilder.method("POST");
                }

                if (response != null && response.getStatus() == 200) {
                    bSuccessCall = true;
                    break;
                } else {
                    logMonitor("Warn: The request failed to " + url + ". Try again after " + retryDelay + " ms");
                    Thread.sleep(retryDelay);
                }
            }

            if (!bSuccessCall) {
                throw new RuntimeException("Failed: " + response == null ? " no response." : "HTTP Code:" + response.getStatus());
            }

            AntiProfitResponse antiProfitResponse = response.readEntity(AntiProfitResponse.class);

            Map mapResponse = new HashMap();
            result = antiProfitResponse.getResult();
            mapResponse.put("result", "" + result);
            mapResponse.put("msisdn", "" + antiProfitResponse.getMsisdn());
            mapResponse.put("des", "" + antiProfitResponse.getDes());
            message = new PlainMessage(MessageUtil.convertMaptoString(mapResponse, ","));
            logReceive(antiProfitResponse.toString());
            CbsUtils.setMessageServerCommand(sc,result, null);
        } catch (Exception e) {
            logMonitor("Error when call check-truc-loi-api at " + url + " Exception: " + e.getMessage());
            log.error("Error when call check-truc-loi-api at " + url, e);
            CbsUtils.setMessageServerCommand(sc,"Error when call check-truc-loi-api at " + url + " Exception: ", e);
            throw e;
        }

        // Add server response
        if (sc != null) {
            sc.setServerResponse(message);
        }
        updateKeepAliveTime();

//        PlainMessage message = null;
//        Map mapResponse = new HashMap();
//        mapResponse.put("result", "0");
//        message = new PlainMessage(MessageUtil.convertMaptoString(mapResponse, ","));
        // Return
        return message;
    }

    public void keepAlive() throws Exception {
        if(client == null){
            throw new Exception("Anti-Profit keepAlive failed");
        }
    }

    public void logProcess(String log) {
        if (getDispatcherManager().isDebug()) {
            logMonitor(log);
        }
    }

    public void logSend(String log) {
        logProcess("SEND --> " + log);
    }

    public void logReceive(String log) {
        logProcess("RECV <-- " + log);
    }

    public void logFailed(String str) {
        logProcess("FAILED <-- " + str);
    }

    public void logError(String log) {
        logMonitor("Error occured: " + log);
    }
}
