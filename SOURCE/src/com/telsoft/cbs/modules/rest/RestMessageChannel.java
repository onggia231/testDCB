package com.telsoft.cbs.modules.rest;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.CBCode;
import com.telsoft.cbs.domain.CBRequest;
import com.telsoft.cbs.domain.CBResponse;
import com.telsoft.cbs.domain.FLOW_STATUS;
import com.telsoft.cbs.utils.CbsLog;
import com.telsoft.cbs.utils.CbsUtils;
import telsoft.gateway.commons.GWUtil;
import telsoft.gateway.commons.SerializableCloner;
import telsoft.gateway.core.gw.Gateway;
import telsoft.gateway.core.gw.MessageChannel;
import telsoft.gateway.core.gw.TranslateTable;
import telsoft.gateway.core.gw.UserRight;
import telsoft.gateway.core.log.MessageContext;
import telsoft.gateway.core.message.GatewayMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Title: Core Gateway System</p>
 *
 * <p>Description: A part of TELSOFT Gateway System</p>
 *
 * <p>Copyright: Copyright (c) 2009</p>
 *
 * <p>Company: TELSOFT</p>
 *
 * @author Nguyen Cong Khanh
 * @version 1.0
 */
public class RestMessageChannel extends MessageChannel {
    private String address;

    protected RestMessageChannel(Gateway gateway) throws Exception {
        super(gateway);
    }

    protected RestMessageChannel() {
        super();
    }

    @Override
    public String getProtocolName() {
        return "REST";
    }

    @Override
    public void sendMessage(MessageContext record, GatewayMessage msg) {

    }

    @Override
    public String getUserID() {
        return "";
    }

    @Override
    public void prepareMessage(MessageContext msg) throws Exception {
        super.prepareMessage(msg);
        msg.setAttribute(MessageChannel.INDEX_ROUTE_ID, msg.getAttribute(MessageChannel.INDEX_SERVER_ID));
    }

    @Override
    protected void processMessage(MessageContext ctx, GatewayMessage msg) throws Exception {
        //todo Đổi timeout theo từng lệnh gọi của từng kho
        int messageTimeout = getMessageTimeout();
        Integer timeoutCommand = (Integer) ctx.getProperty("TimeoutByCommand");
        if(timeoutCommand != null && timeoutCommand >0){
            messageTimeout = timeoutCommand;
        }
        attachMessage(ctx, messageTimeout, true);
    }

    @Override
    public void handleException(MessageContext msgContext, Exception e) {
        if (msgContext != null) {
            CBRequest cbRequest = (CBRequest) msgContext.getRequest();
            CBResponse cbResponse = cbRequest.createResponse();

            String exp = GWUtil.decodeException(e);
            if (exp.equals("[GW]No dispatcher available to serve your request")) {
                cbResponse.setCode(CBCode.CARRIER_MAINTENANCE);
                msgContext.setClientResponse(cbResponse);
                msgContext.setStatus(MessageContext.STATUS_UNKNOWN);
                CbsLog.error(msgContext, "RestMessageChannel.handleException", CBCode.CARRIER_MAINTENANCE, "", exp);

            } else if(exp.contains("Wait message timeout")){
                //Add response for timeout only
                CBResponse timeoutResponse = cbRequest.createResponse();

                //fixme Change to right code
                CBCode timeoutCode = (CBCode) msgContext.getProperty("TimeoutByCommandReturnCode");
                if(timeoutCode != null){
//                    cbResponse.setCode(timeoutCode);
//                    cbResponse.setMessage(CBCode.getDescription(timeoutCode));
                    timeoutResponse.setCode(timeoutCode);
                    timeoutResponse.setMessage(CBCode.getDescription(timeoutCode));
                }else {
//                    cbResponse.setCode(CBCode.PROCESS_TIMEOUT);
//                    cbResponse.setMessage(CBCode.getDescription(CBCode.PROCESS_TIMEOUT));
                    timeoutResponse.setCode(CBCode.PROCESS_TIMEOUT);
                    timeoutResponse.setMessage(CBCode.getDescription(CBCode.PROCESS_TIMEOUT));
                }
                if (msgContext.getTransID() != null) {
//                    cbResponse.set(CbsContansts.TRANSACTION_ID, msgContext.getTransID());
                    timeoutResponse.set(CbsContansts.TRANSACTION_ID, msgContext.getTransID());
                }
                //todo mark timeout
                Map mapFullRequest = (ConcurrentHashMap) msgContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
                CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest,CbsContansts.FLOW_STATUS, CBCode.PROCESS_TIMEOUT);
//                CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest,CbsContansts.TIMEOUT_RESPONSE, cbResponse);
                CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest,CbsContansts.TIMEOUT_RESPONSE, timeoutResponse);

                msgContext.setProperty(CbsContansts.TIMEOUT_RESPONSE,timeoutResponse);
                CbsLog.error(msgContext, "RestMessageChannel.handleException", CBCode.PROCESS_TIMEOUT, "", exp);
            }
            else {
                cbResponse.setCode(CBCode.INTERNAL_SERVER_ERROR);
                cbResponse.setMessage(exp);
                msgContext.setClientResponse(cbResponse);
                msgContext.setStatus(MessageContext.STATUS_UNKNOWN);
                CbsLog.error(msgContext, "RestMessageChannel.handleException", CBCode.INTERNAL_SERVER_ERROR, "", exp);
            }

//            msgContext.setClientResponse(cbResponse);
            //fixme: Status = 2 timeout
//            msgContext.setStatus(MessageContext.STATUS_UNKNOWN);
        }
    }

    @Override
    public UserRight getUserRight() throws Exception {
        return new UserRight();
    }

    /**
     * @return boolean
     */
    public boolean isOpen() {
        return true;
    }

    @Override
    public String getClientAddress() {
        return address;
    }

    public void setClientAddress(String address) {
        this.address = address;
    }

    @Override
    public Map getMappedValues(GatewayMessage gatewayMessage, TranslateTable table) {
        if (!(gatewayMessage instanceof CBRequest)) {
            throw new RuntimeException("Message no compatiable with CBRequest");
        }

        CBRequest msg = (CBRequest) gatewayMessage;
        HashMap mapReturn = new HashMap();

        for (String strDescKey : table.keyDestSet()) {
            for (String strKey : table.getKeySource(strDescKey)) {
                String val = msg.get(strKey);
                if (val != null) {
                    mapReturn.put(strDescKey, val);
                    break;
                }
            }
        }
        return mapReturn;
    }

    @Override
    public void run() {

    }

    /**
     * @throws Exception
     */
    public void startReceiptMessage() throws Exception {
        super.startReceiptMessage();
    }

    /**
     *
     */
    public void stopReceiptMessage() {
        super.stopReceiptMessage();
    }
}
