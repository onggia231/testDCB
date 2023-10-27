package com.telsoft.cbs.modules.cps_rtec;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.CBStore;
import com.telsoft.cbs.utils.CbsUtils;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import telsoft.gateway.commons.GWUtil;
import telsoft.gateway.core.cmp.gw_rtec.RTECMessage;
import telsoft.gateway.core.cmp.in_rtec.RTECDispatcherSplitThread;
import telsoft.gateway.core.cmp.in_rtec.api.HTTPTransport;
import telsoft.gateway.core.dsp.DispatcherManager;
import telsoft.gateway.core.excp.CommandFailureException;
import telsoft.gateway.core.log.MessageContext;
import telsoft.gateway.core.log.ServerCommand;
import telsoft.gateway.core.message.GatewayMessage;

import java.lang.reflect.Field;

public class CpsRtecDispatcherSplitThread extends RTECDispatcherSplitThread {
    private final static Logger log = LoggerFactory.getLogger(CpsRtecDispatcherSplitThread.class);
    @Getter
    @Setter
    private String username;

    @Getter
    @Setter
    private String password;

    private String sessionId;
    private HTTPTransport transport_;

    public CpsRtecDispatcherSplitThread(DispatcherManager mgr) {
        super(mgr);
    }

    public void open() throws Exception {
        super.open();
        Field field = getClass().getSuperclass().getDeclaredField("transport");
        field.setAccessible(true);
        transport_ = (HTTPTransport) field.get(this);

        RTECMessage loginRequest = RTECMessageUtil.createLoginRequest(username, password);
        sessionId = null;
        RTECMessage loginResponse = process(loginRequest);
        String result = loginResponse.getValue("cp_reply.result");
        if (result == null || !result.equals("CPS-0000")) {
            throw new Exception("Cannot login, login code:" + result);
        }
        sessionId = loginResponse.getValue("cp_reply.session");

        logMonitor("Logined. SessionId = " + sessionId);
    }

    public void close() {
        if (transport_ != null) {
            if (sessionId != null && sessionId.length() > 0) {
                try {
                    RTECMessage logoutRequest = RTECMessageUtil.createLogoutRequest(sessionId);
                    process(logoutRequest);
                } catch (Exception ex) {
                    log.error("Close rtec connection error", ex);
                }
            }
        }
        super.close();
    }

    @Override
    public boolean isOpen() {
        return super.isOpen() && sessionId != null && sessionId.length() > 0;
    }

    @Override
    public GatewayMessage processMessage(MessageContext msgContext, GatewayMessage message) throws Exception {
        ServerCommand sc = msgContext.addServerInfo(this);
        sc.setServerRequest(message);
        RTECMessage response = null;

        try {
            CBStore store = (CBStore) msgContext.getProperty(CbsContansts.STORE);
            String isdn = (String) message.getValue("cp_request.user_id");
            isdn = GWUtil.correctISDN(isdn);
            message.setValue("cp_request.user_id", isdn);
            response = process((RTECMessage) message);
            String result = response.getValue("cp_reply.result");
            CbsUtils.setMessageServerCommand(sc,result, null);
        } catch (Exception var9) {
            CbsUtils.setMessageServerCommand(sc,"Error when call RTEC command. Exception: ", var9);
            if (this.getExceptionFilter().isFatal(var9)) {
                throw var9;
            }

            throw new CommandFailureException(var9.getLocalizedMessage());
        } finally {
            sc.setServerResponse(response);
        }

        return response;
    }

    public RTECMessage process(RTECMessage request) throws Exception {
        if (sessionId != null) {
            request.setValue("cp_request.session", sessionId);
        }
        return transport_.request(request);
    }

    public void keepAlive() throws Exception {
        RTECMessage resquest = new RTECMessage(getKeepaliveCommand());
        if (getDispatcherManager().isDebug()) {
            logMonitor("Send keepalive : " + resquest.getContent());
        }
        RTECMessage response = process(resquest);
        if (getDispatcherManager().isDebug()) {
            logMonitor("Keepalive response : " + response.getContent());
        }
        String result = response.getValue("cp_reply.result");
        if (result == null || !result.equals("CPS-0000")) {
            throw new Exception("Rtec keepAlive failed");
        }
    }

}
