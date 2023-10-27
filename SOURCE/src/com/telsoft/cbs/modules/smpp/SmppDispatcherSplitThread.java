package com.telsoft.cbs.modules.smpp;

import org.jsmpp.extra.SessionState;
import org.jsmpp.session.SMPPSession;
import telsoft.gateway.core.dsp.Dispatcher;
import telsoft.gateway.core.dsp.DispatcherManager;
import telsoft.gateway.core.log.MessageContext;
import telsoft.gateway.core.message.GatewayMessage;
import telsoft.gateway.core.message.PlainMessage;

/**
 * <p>Title: IN SOAP Protocol</p>
 * <p/>
 * <p>Description: A part of TELSOFT Gateway System</p>
 * <p/>
 * <p>Copyright: Copyright (c) 2009</p>
 * <p/>
 * <p>Company: TELSOFT</p>
 *
 * @author Nguyen Cong Khanh
 * @version 1.0
 */
public class SmppDispatcherSplitThread extends Dispatcher {
    private SMPPSession smppSession;

    /**
     * @param mmgr DispatcherManager
     */
    public SmppDispatcherSplitThread(DispatcherManager mmgr) {
        super(mmgr);
    }

    public void close() {
    }

    /**
     * @return String
     */
    public String getProtocolName() {
        return "SMPP";
    }

    /**
     * @throws Exception
     */
    public void open() throws Exception {
    }

    /**
     * @return boolean
     * @throws Exception
     */
    public boolean isOpen() {
        return super.isOpen() && (smppSession != null) && smppSession.getSessionState() != SessionState.CLOSED;
    }

    /**
     * @throws Exception
     */
    public void keepAlive() throws Exception {
    }

    public GatewayMessage processMessage(MessageContext messageContext, GatewayMessage gatewayMessage) throws Exception {
        PlainMessage response = new PlainMessage("OK,1234567890");
        return response;
    }

    public SMPPSession getSmppSession() {
        return smppSession;
    }

    public void setSmppSession(SMPPSession smppSession) {
        this.smppSession = smppSession;
    }
}
