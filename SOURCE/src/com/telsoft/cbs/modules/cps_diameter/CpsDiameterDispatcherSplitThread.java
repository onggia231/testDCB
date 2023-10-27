package com.telsoft.cbs.modules.cps_diameter;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.CBCode;
import com.telsoft.cbs.domain.CBException;
import com.telsoft.cbs.domain.CBStore;
import com.telsoft.cbs.utils.CbsUtils;
import com.telsoft.cbs.utils.DbUtils;
import com.telsoft.util.AppException;
import com.telsoft.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import org.jdiameter.api.*;
import org.jdiameter.client.api.IMessage;
import org.jdiameter.client.api.io.IConnectionListener;
import org.jdiameter.client.api.io.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import telsoft.gateway.commons.GWUtil;
import telsoft.gateway.core.dsp.Dispatcher;
import telsoft.gateway.core.dsp.DispatcherManager;
import telsoft.gateway.core.excp.CommandFailureException;
import telsoft.gateway.core.log.MessageContext;
import telsoft.gateway.core.log.ServerCommand;
import telsoft.gateway.core.message.GatewayMessage;
import telsoft.gateway.core.message.MessageUtil;
import telsoft.gateway.core.message.PlainMessage;
import telsoft.jdiameter.message.DiameterMessage;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * <p>Title: DIAMETER Protocol</p>
 * <p/>
 * <p>Description: A part TELSOFT Gateway System</p>
 * <p/>
 * <p>Copyright: Copyright (c) 2009</p>
 * <p/>
 * <p>Company: TELSOFT</p>
 *
 * @author Nguyen Cong Khanh, Do Khac Sy
 * @version 1.0
 */
public class CpsDiameterDispatcherSplitThread extends Dispatcher implements IConnectionListener {
    private static final Logger logger = LoggerFactory.getLogger(CpsDiameterDispatcherSplitThread.class);
    @Getter
    @Setter
    private ClientConfiguration config;
    private Stack stack;
    private SessionFactory factory;
    @Getter
    @Setter
    private String realm;
    @Getter
    @Setter
    private String serverHost;
    @Getter
    @Setter
    private String username = null;
    @Getter
    @Setter
    private String password = null;
    @Getter
    @Setter
    private String serverRealm;
    private boolean stackClosed;


    /**
     * @param mgr DispatcherManager
     */
    protected CpsDiameterDispatcherSplitThread(DispatcherManager mgr) {
        super(mgr);
    }

    /**
     *
     */
    public void close() {
        if (stack != null) {

            try {
                logMonitor("Trying to logout");
                logout();
                logMonitor("Logged out");
            } catch (Exception ex) {
                logger.error("Error when logout", ex);
                logError(GWUtil.decodeException(ex));
            }
            try {
                stack.stop(10000, TimeUnit.MILLISECONDS /**, DisconnectCause.DO_NOT_WANT_TO_TALK_TO_YOU*/);
                stack.destroy();
            } catch (Exception ex) {
                logger.error("Error when destroy stack", ex);
                logError(GWUtil.decodeException(ex));
            } finally {
                stack = null;
            }
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


    /**
     * @return String
     */
    public String getProtocolName() {
        return "DIAMETER";
    }

    public boolean isOpen() {
        return !stackClosed && stack != null && stack.isActive() && super.isOpen();
    }


    /**
     * @throws Exception
     */
    public void open() throws Exception {
        logMonitor("Trying to connect..");
        stack = new org.jdiameter.client.impl.StackImpl();
        factory = stack.init(config);

        MetaData metaData = stack.getMetaData();
        if (metaData.getStackType() != StackType.TYPE_CLIENT || metaData.getMinorVersion() <= 0) {
            throw new Exception("Incorrect driver");
        }
        stackClosed = false;
        stack.start(Mode.ANY_PEER, 30000, TimeUnit.MILLISECONDS);
        logMonitor("Connection created");


        logMonitor("Trying to login");
        if (login()) {
            logMonitor("Logged in");
        }
    }

    /**
     * @return Stack
     */
    public Stack getStack() {
        return stack;
    }

    public GatewayMessage processMessage(MessageContext messageContext, GatewayMessage gatewayMessage) throws Exception {
        CBStore store = (CBStore) messageContext.getProperty(CbsContansts.STORE);
        Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
        List<Map> listCPSDiameterCall = (List<Map>) mapFullRequest.get(CbsContansts.LIST_CPS_DIAMETER_CDR);
        if (listCPSDiameterCall == null) {
            listCPSDiameterCall = new ArrayList<>();
            mapFullRequest.put(CbsContansts.LIST_CPS_DIAMETER_CDR, listCPSDiameterCall);
        }
        ServerCommand sc = messageContext.addServerInfo(this);
        try {
            PlainMessage request = (PlainMessage) gatewayMessage;

            Map mapCPSTransaction = new HashMap();
            listCPSDiameterCall.add(mapCPSTransaction);
            Map mapRequest = new HashMap();
            MessageUtil.analyseMappedMessage(request.getContent(), ",", "=", mapRequest, true);
            String command = ((String) mapRequest.get(CbsContansts.COMMAND)).toLowerCase();
            DiameterMessage diameterMessage;
            String isdn = (String) mapRequest.get(CbsContansts.MSISDN);
            String seq = (String) mapRequest.get("seq");
            String b_isdn = (String) mapRequest.get(CbsContansts.CPS_B_ISDN);
            String extraInfo = (String) mapRequest.get(CbsContansts.CPS_EXTRA_INFO);

            CbsUtils.putValueIntoMapCheckNullValue(mapCPSTransaction, CbsContansts.COMMAND, command);

            //Gen session User-Package_name-Shortcode.service.session
            String cpsPackageName = StringUtil.nvl(store.getAttributes().getProperty(CbsContansts.CPS_PARAM_PACKAGE_NAME), "");
            String shortCode = StringUtil.nvl(store.getAttributes().getProperty(CbsContansts.CPS_PARAM_SHORT_CODE), "");
            String storeTransactionID = (String) mapFullRequest.get(CbsContansts.STORE_TRANSACTION_ID);
            String session = String.format("%s-%s-%s.%s.%s.%s.%s", username,cpsPackageName,shortCode, store.getStoreCode(), isdn, storeTransactionID, seq);

            switch (command) {
                case "two_step_debit_init": {
                    long amount = Long.parseLong((String) mapRequest.get("amount"));
                    diameterMessage = twoStepDebitInit(store, sc, session, isdn, amount, seq, b_isdn);
                    break;
                }
                case "two_step_debit_commit": {
                    long amount = Long.parseLong((String) mapRequest.get("amount"));
                    diameterMessage = twoStepDebitCommit(store, sc, session, isdn, amount, seq, b_isdn);
                    break;
                }
/*            case "direct_debit": {
                long amount = Long.parseLong((String) mapRequest.get("amount"));
                diameterMessage = directDebit(store, sc, session, isdn, amount, seq, b_isdn);
                break;
            }*/
/*            case "refund": {
                String refund_information = (String) mapRequest.get("refund_information");
                diameterMessage = refund(store, sc, session, isdn, refund_information, seq, b_isdn);
                break;
            }*/
                case "direct_debit": {
                    Long amountFullTax = (Long) mapFullRequest.get(CbsContansts.AMOUNT_FULL_TAX);
                    CbsUtils.putValueIntoMapCheckNullValue(mapCPSTransaction, CbsContansts.CPS_CATEGORYID, CbsContansts.CPS_CATEGORY.CHARGE.getCategoryId());
                    diameterMessage = directDebitDiameter(store, sc, session, isdn, amountFullTax, seq, b_isdn, extraInfo);
                    break;
                }
                case "refund": {
                    String refund_information = (String) mapRequest.get("refund_information");
                    CbsUtils.putValueIntoMapCheckNullValue(mapCPSTransaction, CbsContansts.CPS_CATEGORYID, CbsContansts.CPS_CATEGORY.REFUND.getCategoryId());
                    diameterMessage = refundDiameter(store, sc, session, isdn, refund_information, seq, b_isdn, extraInfo, CbsContansts.CPS_CATEGORY.REFUND);
                    break;
                }
                case "cancel": {
                    String refund_information = (String) mapRequest.get("refund_information");
                    CbsUtils.putValueIntoMapCheckNullValue(mapCPSTransaction, CbsContansts.CPS_CATEGORYID, CbsContansts.CPS_CATEGORY.CANCEL.getCategoryId());
                    diameterMessage = refundDiameter(store, sc, session, isdn, refund_information, seq, b_isdn, extraInfo, CbsContansts.CPS_CATEGORY.CANCEL);
                    break;
                }
                default:
                    throw new CommandFailureException("Command '" + command + "' not supported");
            }
            sc.setServerResponse(diameterMessage);


            //Push cps request time/ response time
            CbsUtils.putValueIntoMapCheckNullValue(mapCPSTransaction, CbsContansts.CPS_REQUEST_TIME, sc.dtServerRequestTime);
            CbsUtils.putValueIntoMapCheckNullValue(mapCPSTransaction, CbsContansts.CPS_R_RESPONSE_TIME, sc.dtServerResponseTime);

            // Read response
            Object cpsResultCode = null;
            Object INResultCode = null;
            ;
            String strDescription = null;
            ;
            Object code = null;
            ;
            String refund_information = null;
            ;

            if (diameterMessage != null) {
                cpsResultCode = diameterMessage.getValue("1021");
                INResultCode = diameterMessage.getValue("268");
                strDescription = (String) diameterMessage.getValue("1011");
                code = diameterMessage.getValue("186");
                refund_information = (String) diameterMessage.getValue("456.2022");
            }
            // push cps response
            CbsUtils.putValueIntoMapCheckNullValue(mapCPSTransaction, CbsContansts.CPS_R_IN_RESULT_CODE, INResultCode);
            CbsUtils.putValueIntoMapCheckNullValue(mapCPSTransaction, CbsContansts.CPS_R_RESULT_CODE, cpsResultCode);
            CbsUtils.putValueIntoMapCheckNullValue(mapCPSTransaction, CbsContansts.CPS_R_DESCRIPTION, strDescription);

            if ("CPS-2011".equals(cpsResultCode)) {
                throw new Exception("Session expired");
            }

            Map mapResponse = new HashMap();
            mapResponse.put("cps_transaction_id", seq);
            mapResponse.put(CbsContansts.COMMAND, command);
            mapResponse.put("result", cpsResultCode);
            mapResponse.put("code", code);
            if (refund_information != null && refund_information.length() > 0) {
                mapResponse.put("refund_information", refund_information);
                CbsUtils.putValueIntoMapCheckNullValue(mapCPSTransaction, CbsContansts.CPS_R_REFUND_INFORMATION, refund_information);
                CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.CPS_R_REFUND_INFORMATION, refund_information);
            }
            PlainMessage response = new PlainMessage(MessageUtil.convertMaptoString(mapResponse, ","));
            CbsUtils.setMessageServerCommand(sc,(String)cpsResultCode, null);
            return response;
        } catch (Exception ex) {
            CbsUtils.setMessageServerCommand(sc,"Error when call Diameter command. Exception: ", ex);
            throw ex;
        }
    }

    public void connectionOpened(String string) {
        logMonitor("Connection open");
    }

    public void connectionClosed(String string, List list) {
        logMonitor("Connection closed");
        stackClosed = true;
    }

    public void messageReceived(String string, IMessage iMessage) {
    }

    public void internalError(String string, IMessage iMessage, TransportException transportException) {
        logMonitor("internalError");
    }

    public boolean login() throws Exception {
        Session session = factory.getNewSession();
        try {

            Request requestLogin = session.createRequest(265,
                    org.jdiameter.api.ApplicationId.createByAccAppId(0, 4),
                    serverRealm, serverHost);

            requestLogin.getAvps().addAvp(1, username, true, false, false);
            requestLogin.getAvps().addAvp(2, password, true, false, true);

//            requestLogin.getAvps().addAvp(264, "127.0.0.1", true, false, true);
//            requestLogin.getAvps().addAvp(296, realm, true, false, true);
//
//            requestLogin.getAvps().addAvp(293, serverHost, true, false, true);
//            requestLogin.getAvps().addAvp(283, serverRealm, true, false, true);

            Future<Message> future = session.send(requestLogin);
            Answer response = (Answer) future.get();
            Avp avpResultCode = response.getAvps().getAvp(1021);
            String resultCode = avpResultCode.getUTF8String();

            if (resultCode.equals("CPS-0000")) {
                return true;
            } else {
                throw new Exception("Login result: " + resultCode);
            }
        } finally {
            session.release();
        }
    }

    public boolean logout() throws Exception {
        Session session = factory.getNewSession();
        try {

            Request requestLogout = session.createRequest(282,
                    org.jdiameter.api.ApplicationId.createByAccAppId(0,
                            4),
                    serverRealm, serverHost);

//            requestLogin.getAvps().addAvp(264, "127.0.0.1", true, false, true);
//            requestLogin.getAvps().addAvp(296, realm, true, false, true);
//
//            requestLogin.getAvps().addAvp(293, serverHost, true, false, true);
//            requestLogin.getAvps().addAvp(283, serverRealm, true, false, true);

            Future<Message> future = session.send(requestLogout);
            Answer response = (Answer) future.get();
            return true;
//            Avp avpResultCode = response.getAvps().getAvp(1021);
//            String resultCode = avpResultCode.getUTF8String();
//            if (resultCode.equals("CPS-0000")) {
//                return true;
//            } else {
//                return false;
//            }
        } finally {
            session.release();
        }
    }


    public DiameterMessage twoStepDebitInit(CBStore store, ServerCommand sc, String sessionId, String isdn, long amount, String seq, String calling) throws Exception {
        isdn = GWUtil.correctISDN(isdn);
        Session session = factory.getNewSession(sessionId);
        try {

            Request request = session.createRequest(272,
                    org.jdiameter.api.ApplicationId.createByAccAppId(0, 4),
                    serverRealm, serverHost);

            AvpSet avpSet = request.getAvps();

            //add auth_application_id: default=4
            avpSet.addAvp(Avp.AUTH_APPLICATION_ID, 4L, true, false, true);

            //service context id: default=6.32260@3gpp.org
            //AvpCode.SERVICE_CONTEXT_ID: 461
            avpSet.addAvp(461, "6.32260@3gpp.org", true, false, false);
            //CC_REQUEST_TYPE: 416
            avpSet.addAvp(416, 1, true, false); // TWO_STEP_INIT
            //CC_REQUEST_NUMBER: 415
            avpSet.addAvp(415, 0, true, false, true);

            //add isdn
            //SUBSCRIPTION_ID: 443
            AvpSet subId = request.getAvps().addGroupedAvp(443);
            //SUBSCRIPTION_ID_TYPE: 450
            subId.addAvp(450, 0, true, false);
            //SUBSCRIPTION_ID_DATA: 444
            subId.addAvp(444, isdn, true, false, false);

            //add amount
            //SERVICE_IDENTIFIER: 439
            avpSet.addAvp(439, 4, true, false);

            //REQUESTED_SERVICE_UNIT: 437
            AvpSet request_service_unit = avpSet.addGroupedAvp(437);
            AvpSet ccMoney = request_service_unit.addGroupedAvp(413); // CC-Money
            AvpSet ccMoney_Unit_Value = ccMoney.addGroupedAvp(445);
            ccMoney_Unit_Value.addAvp(447, amount, true, false);

            //add avp of CPS: cp_id, sp_id,...
            applySpCp(store, request, seq, calling);

            DiameterMessage diameterRequest = new DiameterMessage((IMessage) request);
            sc.setServerRequest(diameterRequest);
            if (getDispatcherManager().isLogCommandProcess()) {
                logSend(diameterRequest.getContent());
            }
            Future<Message> future = session.send(request);
            Answer response = (Answer) future.get();
            if (response == null) {
                throw new Exception("No response from diameter");
            }
            if (getDispatcherManager().isLogCommandProcess()) {
                logReceive(new DiameterMessage((IMessage) response).getContent());
            }

//            String resultCode = response.getAvps().getAvp(1021).getUTF8String();
//            if ("CPS-2011".equals(resultCode)) {
//                reConnect();
//                return twoStepDebitInit(sessionId, isdn, amount, seq, calling);
//            }

//            if (!"CPS-0000".equals(resultCode)) {
//                throw new Exception(resultCode);
//            }
            return new DiameterMessage((IMessage) response);
        } finally {
            session.release();
        }

    }


    public DiameterMessage twoStepDebitCommit(CBStore store, ServerCommand sc, String sessionId, String isdn, long amount, String seq, String calling) throws Exception {
        isdn = GWUtil.correctISDN(isdn);
        Session session = factory.getNewSession(sessionId);
        try {

            Request request = session.createRequest(272,
                    org.jdiameter.api.ApplicationId.createByAccAppId(0, 4),
                    serverRealm, serverHost);

            AvpSet avpSet = request.getAvps();

            //add auth_application_id: default=4
            avpSet.addAvp(Avp.AUTH_APPLICATION_ID, 4L, true, false, true);

            //service context id: default=6.32260@3gpp.org
            //AvpCode.SERVICE_CONTEXT_ID: 461
            avpSet.addAvp(461, "6.32260@3gpp.org", true, false, false);
            //CC_REQUEST_TYPE: 416
            avpSet.addAvp(416, 3, true, false); // TWO_STEP_INIT
            //CC_REQUEST_NUMBER: 415
            avpSet.addAvp(415, 1, true, false, true);

            //add isdn
            //SUBSCRIPTION_ID: 443
            AvpSet subId = request.getAvps().addGroupedAvp(443);
            //SUBSCRIPTION_ID_TYPE: 450
            subId.addAvp(450, 0, true, false);
            //SUBSCRIPTION_ID_DATA: 444
            subId.addAvp(444, isdn, true, false, false);

            //add amount
            //SERVICE_IDENTIFIER: 439
            avpSet.addAvp(439, 4, true, false);

            //REQUESTED_SERVICE_UNIT: 437
            AvpSet request_service_unit = avpSet.addGroupedAvp(446);
            AvpSet ccMoney = request_service_unit.addGroupedAvp(413); // CC-Money
            AvpSet ccMoney_Unit_Value = ccMoney.addGroupedAvp(445);
            ccMoney_Unit_Value.addAvp(447, amount, true, false);

            //add avp of CPS: cp_id, sp_id,...
            applySpCp(store, request, seq, calling);

            DiameterMessage diameterRequest = new DiameterMessage((IMessage) request);
            sc.setServerRequest(diameterRequest);
            if (getDispatcherManager().isLogCommandProcess()) {
                logSend(diameterRequest.getContent());
            }
            Future<Message> future = session.send(request);
            Answer response = (Answer) future.get();
            if (response == null) {
                throw new Exception("No response from diameter");
            }
            if (getDispatcherManager().isLogCommandProcess()) {
                logReceive(new DiameterMessage((IMessage) response).getContent());
            }

            String resultCode = response.getAvps().getAvp(1021).getUTF8String();
//            if ("CPS-2011".equals(resultCode)) {
//                reConnect();
//                return twoStepDebitCommit(sessionId, isdn, amount, seq, calling);
//            }
//            if (!"CPS-0000".equals(resultCode)) {
//                throw new Exception(resultCode);
//            }
            return new DiameterMessage((IMessage) response);
        } finally {
            session.release();
        }
    }


    public DiameterMessage directDebit(CBStore store, ServerCommand sc, String sessionId, String isdn, long amount, String seq, String calling) throws Exception {
        isdn = GWUtil.correctISDN(isdn);
        Session session = factory.getNewSession(sessionId);
        try {

            Request request = session.createRequest(272,
                    org.jdiameter.api.ApplicationId.createByAccAppId(0, 4),
                    serverRealm, serverHost);

            AvpSet avpSet = request.getAvps();

            //add auth_application_id: default=4
            avpSet.addAvp(Avp.AUTH_APPLICATION_ID, 4L, true, false, true);

            //service context id: default=6.32260@3gpp.org
            //AvpCode.SERVICE_CONTEXT_ID: 461
            avpSet.addAvp(461, "6.32260@3gpp.org", true, false, false);
            //CC_REQUEST_TYPE: 416
            avpSet.addAvp(416, 4, true, false); // TWO_STEP_INIT
            //CC_REQUEST_NUMBER: 415
            avpSet.addAvp(415, 0, true, false, true);

            //REQUESTED_ACTION
            avpSet.addAvp(Avp.REQUESTED_ACTION, 0, true, false); // DIRECT DEBIT

            //add isdn
            //SUBSCRIPTION_ID: 443
            AvpSet subId = request.getAvps().addGroupedAvp(443);
            //SUBSCRIPTION_ID_TYPE: 450
            subId.addAvp(450, 0, true, false);
            //SUBSCRIPTION_ID_DATA: 444
            subId.addAvp(444, isdn, true, false, false);

            AvpSet mscc = avpSet.addGroupedAvp(Avp.MULTIPLE_SERVICES_CREDIT_CONTROL);
            //add amount
            //SERVICE_IDENTIFIER: 439
            mscc.addAvp(439, 4, true, false);

            //REQUESTED_SERVICE_UNIT: 437
            AvpSet request_service_unit = mscc.addGroupedAvp(437);
            AvpSet ccMoney = request_service_unit.addGroupedAvp(413); // CC-Money
            AvpSet ccMoney_Unit_Value = ccMoney.addGroupedAvp(445);
            ccMoney_Unit_Value.addAvp(447, amount, true, false);

            //add avp of CPS: cp_id, sp_id,...
            applySpCp(store, request, seq, calling);

            DiameterMessage diameterRequest = new DiameterMessage((IMessage) request);
            sc.setServerRequest(diameterRequest);
            if (getDispatcherManager().isLogCommandProcess()) {
                logSend(diameterRequest.getContent());
            }
            Future<Message> future = session.send(request);
            Answer response = (Answer) future.get();
            if (response == null) {
                throw new Exception("No response from diameter");
            }
            if (getDispatcherManager().isLogCommandProcess()) {
                logReceive(new DiameterMessage((IMessage) response).getContent());
            }

            String resultCode = response.getAvps().getAvp(1021).getUTF8String();
//            if ("CPS-2011".equals(resultCode)) {
//                reConnect();
//                return directDebit(sessionId, isdn, amount, seq, calling);
//            }

            return new DiameterMessage((IMessage) response);
        } finally {
            session.release();
        }
    }

    public DiameterMessage refund(CBStore store, ServerCommand sc, String sessionId, String isdn, String refund_information, String seq, String calling) throws Exception {
        isdn = GWUtil.correctISDN(isdn);
        Session session = factory.getNewSession(sessionId);
        try {

            Request request = session.createRequest(272,
                    org.jdiameter.api.ApplicationId.createByAccAppId(0, 4),
                    serverRealm, serverHost);

            AvpSet avpSet = request.getAvps();

            //add auth_application_id: default=4
            avpSet.addAvp(Avp.AUTH_APPLICATION_ID, 4L, true, false, true);

            //service context id: default=6.32260@3gpp.org
            //AvpCode.SERVICE_CONTEXT_ID: 461
            avpSet.addAvp(Avp.SERVICE_CONTEXT_ID, "6.32260@3gpp.org", true, false, false);
            //CC_REQUEST_TYPE: 416
            avpSet.addAvp(Avp.CC_REQUEST_TYPE, 4, true, false);
            //CC_REQUEST_NUMBER: 415
            avpSet.addAvp(Avp.CC_REQUEST_NUMBER, 0, true, false, true);

            //REQUESTED_ACTION
            avpSet.addAvp(Avp.REQUESTED_ACTION, 1, true, false); // REFUND

            //add isdn
            //SUBSCRIPTION_ID: 443
            AvpSet subId = request.getAvps().addGroupedAvp(Avp.SUBSCRIPTION_ID);
            //SUBSCRIPTION_ID_TYPE: 450
            subId.addAvp(Avp.SUBSCRIPTION_ID_TYPE, 0, true, false);
            //SUBSCRIPTION_ID_DATA: 444
            subId.addAvp(Avp.SUBSCRIPTION_ID_DATA, isdn, true, false, false);

            AvpSet mscc = avpSet.addGroupedAvp(Avp.MULTIPLE_SERVICES_CREDIT_CONTROL);
            //add amount
            //SERVICE_IDENTIFIER: 439
            mscc.addAvp(Avp.SERVICE_IDENTIFIER_CCA, 4, true, false);

            //REFUND INFORMATION
            mscc.addAvp(Avp.REFUND_INFORMATION, refund_information, true, false, false);

            //add avp of CPS: cp_id, sp_id,...
            applySpCp(store, request, seq, calling);

            DiameterMessage diameterRequest = new DiameterMessage((IMessage) request);
            sc.setServerRequest(diameterRequest);
            if (getDispatcherManager().isLogCommandProcess()) {
                logSend(diameterRequest.getContent());
            }
            Future<Message> future = session.send(request);
            Answer response = (Answer) future.get();
            if (response == null) {
                throw new Exception("No response from diameter");
            }
            if (getDispatcherManager().isLogCommandProcess()) {
                logReceive(new DiameterMessage((IMessage) response).getContent());
            }

            String resultCode = response.getAvps().getAvp(1021).getUTF8String();
//            if ("CPS-2011".equals(resultCode)) {
//                reConnect();
//                return directDebit(sessionId, isdn, amount, seq, calling);
//            }

            return new DiameterMessage((IMessage) response);
        } finally {
            session.release();
        }
    }

    private void applySpCp(CBStore store, Request request, String seq, String calling) {
        //cp
        request.getAvps().addAvp(10111, StringUtil.nvl(store.getAttributes().getProperty("cp"), ""), true);

        //sp
        request.getAvps().addAvp(10112, StringUtil.nvl(store.getAttributes().getProperty("sp"), ""), true);

//        // category id: fix theo lenh
        request.getAvps().addAvp(10113, StringUtil.nvl(store.getAttributes().getProperty("category_id"), ""), true);

        // content id (sequence)
        request.getAvps().addAvp(10114, StringUtil.nvl(seq, ""), true);

        // shortcode
        request.getAvps().addAvp(10115, StringUtil.nvl(store.getAttributes().getProperty(CbsContansts.CPS_PARAM_SHORT_CODE), ""), true);

        // calling
        request.getAvps().addAvp(10116, StringUtil.nvl(calling, ""), true, false, true);
    }

    //Google flow
    public DiameterMessage directDebitDiameter(CBStore store, ServerCommand sc, String sessionId, String isdn, long amount, String seq, String b_isdn, String extraInfo) throws Exception {
        isdn = GWUtil.correctISDN(isdn);
        Session session = factory.getNewSession(sessionId);
        try {

            Request request = session.createRequest(272,
                    org.jdiameter.api.ApplicationId.createByAccAppId(0, 4),
                    serverRealm, serverHost);

            AvpSet avpSet = request.getAvps();

            //add auth_application_id: default=4
            avpSet.addAvp(Avp.AUTH_APPLICATION_ID, 4L, true, false, true);

            //service context id: default=6.32260@3gpp.org
            //AvpCode.SERVICE_CONTEXT_ID: 461
            avpSet.addAvp(461, "6.32260@3gpp.org", true, false, false);
            //CC_REQUEST_TYPE: 416
            avpSet.addAvp(416, 4, true, false); // TWO_STEP_INIT
            //CC_REQUEST_NUMBER: 415
            avpSet.addAvp(415, 0, true, false, true);

            //REQUESTED_ACTION
            avpSet.addAvp(Avp.REQUESTED_ACTION, 0, true, false); // DIRECT DEBIT

            //add isdn
            //SUBSCRIPTION_ID: 443
            AvpSet subId = request.getAvps().addGroupedAvp(443);
            //SUBSCRIPTION_ID_TYPE: 450
            subId.addAvp(450, 0, true, false);
            //SUBSCRIPTION_ID_DATA: 444
            subId.addAvp(444, isdn, true, false, false);

            AvpSet mscc = avpSet.addGroupedAvp(Avp.MULTIPLE_SERVICES_CREDIT_CONTROL);
            //add amount
            //SERVICE_IDENTIFIER: 439
            mscc.addAvp(439, 4, true, false);

            //REQUESTED_SERVICE_UNIT: 437
            AvpSet request_service_unit = mscc.addGroupedAvp(437);
            AvpSet ccMoney = request_service_unit.addGroupedAvp(413); // CC-Money
            AvpSet ccMoney_Unit_Value = ccMoney.addGroupedAvp(445);
            ccMoney_Unit_Value.addAvp(447, amount, true, false);

            //b_isdn
            request.getAvps().addAvp(10116, StringUtil.nvl(b_isdn, ""), true, false, true);
            // category id
            request.getAvps().addAvp(10113, CbsContansts.CPS_CATEGORY.CHARGE.getCategoryId(), true);

            //add avp of CPS: cp_id, sp_id,...
            applyStoreParameter(request, store);

            // content id (sequence)
            request.getAvps().addAvp(10114, StringUtil.nvl(seq, ""), true);

            //EXTRA INFO
            request.getAvps().addAvp(10117, StringUtil.nvl(extraInfo, ""), true);

            DiameterMessage diameterRequest = new DiameterMessage((IMessage) request);
            sc.setServerRequest(diameterRequest);
            if (getDispatcherManager().isLogCommandProcess()) {
                logSend(diameterRequest.getContent());
            }
            Future<Message> future = session.send(request);
            Answer response = (Answer) future.get();

            DiameterMessage result = null;
            if (response == null) {
//                throw new AppException("No response from diameter");
                logReceive("No response from diameter");
            }
            //fixme: Test
            if (response != null) {
                result = new DiameterMessage((IMessage) response);
                if (getDispatcherManager().isLogCommandProcess()) {
                    logReceive(result.getContent());
                }
            }


//            String resultCode = response.getAvps().getAvp(1021).getUTF8String();
//            if ("CPS-2011".equals(resultCode)) {
//                reConnect();
//                return directDebit(sessionId, isdn, amount, seq, calling);
//            }

//            return new DiameterMessage((IMessage) response);
            return result;
        } finally {
            session.release();
        }
    }

    public DiameterMessage refundDiameter(CBStore store, ServerCommand sc, String sessionId, String isdn, String refund_information, String seq, String b_isdn, String extraInfo, CbsContansts.CPS_CATEGORY cpsCategory) throws Exception {
        isdn = GWUtil.correctISDN(isdn);
        Session session = factory.getNewSession(sessionId);
        try {

            Request request = session.createRequest(272,
                    org.jdiameter.api.ApplicationId.createByAccAppId(0, 4),
                    serverRealm, serverHost);

            AvpSet avpSet = request.getAvps();

            //add auth_application_id: default=4
            avpSet.addAvp(Avp.AUTH_APPLICATION_ID, 4L, true, false, true);

            //service context id: default=6.32260@3gpp.org
            //AvpCode.SERVICE_CONTEXT_ID: 461
            avpSet.addAvp(Avp.SERVICE_CONTEXT_ID, "6.32260@3gpp.org", true, false, false);
            //CC_REQUEST_TYPE: 416
            avpSet.addAvp(Avp.CC_REQUEST_TYPE, 4, true, false);
            //CC_REQUEST_NUMBER: 415
            avpSet.addAvp(Avp.CC_REQUEST_NUMBER, 0, true, false, true);

            //REQUESTED_ACTION
            avpSet.addAvp(Avp.REQUESTED_ACTION, 1, true, false); // REFUND

            //add isdn
            //SUBSCRIPTION_ID: 443
            AvpSet subId = request.getAvps().addGroupedAvp(Avp.SUBSCRIPTION_ID);
            //SUBSCRIPTION_ID_TYPE: 450
            subId.addAvp(Avp.SUBSCRIPTION_ID_TYPE, 0, true, false);
            //SUBSCRIPTION_ID_DATA: 444
            subId.addAvp(Avp.SUBSCRIPTION_ID_DATA, isdn, true, false, false);

            AvpSet mscc = avpSet.addGroupedAvp(Avp.MULTIPLE_SERVICES_CREDIT_CONTROL);
            //add amount
            //SERVICE_IDENTIFIER: 439
            mscc.addAvp(Avp.SERVICE_IDENTIFIER_CCA, 4, true, false);

            //REFUND INFORMATION
            mscc.addAvp(Avp.REFUND_INFORMATION, refund_information, 10415, true, false, true);

            //b_isdn
            request.getAvps().addAvp(10116, StringUtil.nvl(b_isdn, ""), true, false, true);
            // category id
            request.getAvps().addAvp(10113, cpsCategory.getCategoryId(), true);

            //add avp of CPS: cp_id, sp_id,...
            applyStoreParameter(request, store);

            // content id (sequence)
            request.getAvps().addAvp(10114, StringUtil.nvl(seq, ""), true);

            //EXTRA INFO
            request.getAvps().addAvp(10117, StringUtil.nvl(extraInfo, ""), true);

            DiameterMessage diameterRequest = new DiameterMessage((IMessage) request);
            sc.setServerRequest(diameterRequest);
            if (getDispatcherManager().isLogCommandProcess()) {
                logSend(diameterRequest.getContent());
            }
            Future<Message> future = session.send(request);
            Answer response = (Answer) future.get();
            DiameterMessage result = null;
            if (response == null) {
//                throw new AppException("No response from diameter");
                logReceive("No response from diameter");
            }

            if (response != null) {
                result = new DiameterMessage((IMessage) response);
                if (getDispatcherManager().isLogCommandProcess()) {
                    logReceive(result.getContent());
                }
            }

            return result;
        } finally {
            session.release();
        }
    }

    private void applyStoreParameter(Request request, CBStore store) {
        // shortcode
        request.getAvps().addAvp(10115, StringUtil.nvl(store.getAttributes().getProperty(CbsContansts.CPS_PARAM_SHORT_CODE), ""), true);

        //cp
        request.getAvps().addAvp(10111, StringUtil.nvl(store.getAttributes().getProperty("cp"), ""), true);

        //sp
        request.getAvps().addAvp(10112, StringUtil.nvl(store.getAttributes().getProperty("sp"), ""), true);
    }
}
