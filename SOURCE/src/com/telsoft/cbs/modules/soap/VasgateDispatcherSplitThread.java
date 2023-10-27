package com.telsoft.cbs.modules.soap;

import lombok.Getter;
import lombok.Setter;
import org.apache.axis.AxisFault;
import org.apache.axis.utils.JavaUtils;
import org.apache.axis.utils.XMLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soapserversimulator.VasgateServiceSoapBindingStub;
import telsoft.gateway.core.dsp.Dispatcher;
import telsoft.gateway.core.dsp.DispatcherManager;
import telsoft.gateway.core.excp.CommandFailureException;
import telsoft.gateway.core.log.MessageContext;
import telsoft.gateway.core.log.ServerCommand;
import telsoft.gateway.core.message.GatewayMessage;
import telsoft.gateway.core.message.MessageUtil;
import telsoft.gateway.core.message.PlainMessage;

import java.util.HashMap;
import java.util.Map;

public class VasgateDispatcherSplitThread extends Dispatcher {
    private final static Logger log = LoggerFactory.getLogger(VasgateDispatcherSplitThread.class);
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
    private int miTimeout;

    private VasgateServiceSoapBindingStub bindingStub;

    protected VasgateDispatcherSplitThread(DispatcherManager mgr) {
        super(mgr);
    }

    public static String dumpToString(AxisFault af) {
        StringBuffer buf = new StringBuffer("AxisFault");
        buf.append(JavaUtils.LS);
        buf.append(" faultCode: ");
        buf.append(XMLUtils.xmlEncodeString(af.getFaultCode().toString()));
        buf.append(JavaUtils.LS);
        buf.append(" faultSubcode: ");
        if (af.getFaultSubCodes() != null) {
            for (int i = 0; i < af.getFaultSubCodes().length; i++) {
                buf.append(JavaUtils.LS);
                buf.append(af.getFaultSubCodes()[i].toString());
            }
        }
        buf.append(JavaUtils.LS);
        buf.append(" faultString: ");
        try {
            buf.append(XMLUtils.xmlEncodeString(af.getFaultString()));
        } catch (RuntimeException re) {
            buf.append(re.getMessage());
        }
        buf.append(JavaUtils.LS);
        buf.append(" faultActor: ");
        buf.append(XMLUtils.xmlEncodeString(af.getFaultActor()));
        buf.append(JavaUtils.LS);
        buf.append(" faultNode: ");
        buf.append(XMLUtils.xmlEncodeString(af.getFaultNode()));
        buf.append(JavaUtils.LS);
        return buf.toString();
    }

    @Override
    public boolean isOpen() {
        return super.isOpen() && bindingStub != null;
    }

    @Override
    public void open() throws Exception {
        try {
            bindingStub = null;

            try {
                java.net.URL endpoint = new java.net.URL("http://" + host + ":" + port + uri);
                bindingStub = (soapserversimulator.VasgateServiceSoapBindingStub)
                        new soapserversimulator.VasgateServiceServiceLocator().getVasgateService(endpoint);
            } catch (javax.xml.rpc.ServiceException jre) {
                throw new Exception("JAX-RPC ServiceException caught: " + jre);
            }

            if (bindingStub == null) {
                throw new Exception("Vasgate Binding Stub is null");
            }

            bindingStub.setTimeout(miTimeout);
            bindingStub._setProperty("javax.xml.rpc.session.maintain", Boolean.FALSE);
        } catch (Exception ex) {
            throw ex;
        }
    }

    @Override
    public void close() {
        if (bindingStub != null) {
            try {
                bindingStub = null;
            } catch (Exception ex) {
                log.error("Close connection error", ex);
            }
        }
    }

    @Override
    public String getProtocolName() {
        return "Vasgate";
    }

    @Override
    public GatewayMessage processMessage(MessageContext msgContext, GatewayMessage gatewayMessage) throws Exception {
        int result = 3;
        // Add server request
        ServerCommand sc = null;
        if (msgContext != null) {
            sc = msgContext.addServerInfo(this);
            sc.setServerRequest(gatewayMessage);
        }
        PlainMessage response = null;

        Map<String, String> msgMap = new HashMap<>();
        MessageUtil.analyseMappedMessage(gatewayMessage.getContent(), ",", "=", msgMap, true);
        String command = msgMap.get("command").toLowerCase();
        String msisdn = msgMap.get("msisdn").toLowerCase();
        String action = msgMap.get("action").toLowerCase();
        try {
            if ("check".equals(command) && ("delete".equals(action) || "register".equals(action))) {
                Map mapResponse = new HashMap();
                result = bindingStub.vasgate(msisdn, action);
                mapResponse.put("result", "" + result);
                response = new PlainMessage(MessageUtil.convertMaptoString(mapResponse, ","));
                logMonitor("Action: " + action.toUpperCase() + " || Result: " + result);
            } else {
                throw new CommandFailureException("Command not supported");
            }
        } catch (AxisFault af) {
            log.error("SOAP invoke error, full trace", af);
            Throwable throwable = af.getCause();
            if (this.getExceptionFilter().isFatal(throwable)) {
                throw new Exception(dumpToString(af));
            } else {
                throw new CommandFailureException(throwable.getLocalizedMessage());
            }
        } catch (java.rmi.RemoteException ex) {
            if (ex != null) {
                throw new Exception(ex.getMessage());
            } else {
                throw ex;
            }
        }

        // Add server response
        if (sc != null) {
            sc.setServerResponse(gatewayMessage);
        }
        updateKeepAliveTime();

        // Return
        return response;
    }

    public void keepAlive() throws Exception {
        SOAPMessage resquest = new SOAPMessage(getKeepaliveCommand());
        if (getDispatcherManager().isDebug()) {
            logMonitor("Send keepalive : " + resquest.getContent());
        }
        int result = bindingStub.vasgate("0", "0");
        logMonitor("Keep Alive succeed");
    }
}
