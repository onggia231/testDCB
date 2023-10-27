package com.telsoft.cbs.modules.cps_rtec;

import com.telsoft.cbs.camel.CBProducer;
import com.telsoft.cbs.domain.CBCode;
import com.telsoft.cbs.domain.CBException;
import com.telsoft.cbs.domain.CBRequest;
import com.telsoft.util.StringUtil;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import telsoft.gateway.core.cmp.gw_rtec.RTECMessage;
import telsoft.gateway.core.cmp.in_rtec.api.HTTPTransport;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class RtecProducer extends CBProducer {
    private final String username;
    private final String password;
    private final int port;
    private final String host;
    private final String uri;
    private final int timeout;
    private HTTPTransport transport;
    private String sessionId = null;

    // URL rtec://host:port/
    public RtecProducer(Endpoint endpoint, Map<String, Object> parameters) throws URISyntaxException {
        super(endpoint);
        this.username = (String) parameters.get("username");
        this.password = (String) parameters.get("password");
        this.timeout = Integer.parseInt(StringUtil.nvl(parameters.get("timeout"), "30000"));

        URI uri = new URI(endpoint.getEndpointUri());
        this.uri = uri.getPath();
        this.host = uri.getHost();
        this.port = uri.getPort();
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        transport = new HTTPTransport(host, port, uri, 3);
        transport.setResponseTimeout(timeout);
        transport.connect();

        RTECMessage loginRequest = new RTECMessage();
        loginRequest.setValue("cp_request.login", username);
        loginRequest.setValue("cp_request.command", "login");
        loginRequest.setValue("cp_request.password", password);
        loginRequest.setValue("cp_request.session", "0");
        sessionId = null;
        RTECMessage loginResponse = process(loginRequest);
        String result = loginResponse.getValue("cp_reply.result");
        if (result == null || !result.equals("0")) {
            throw new Exception("Rtec login failed");
        }

        sessionId = loginResponse.getValue("cp_reply.session");
    }

    @Override
    protected void doStop() throws Exception {
        RTECMessage logoutRequest = new RTECMessage();
        logoutRequest.setValue("cp_request.command", "logout");
        process(logoutRequest);
        transport.close();
        super.doStop();
    }

    @Override
    public void process(CBRequest request, Exchange exchange) throws CBException {
        String s = exchange.getIn().getBody(String.class);
        try {
            RTECMessage response = process(new RTECMessage(s));
            exchange.getMessage().setBody(response.getContent());
        } catch (Exception e) {
            throw new CBException(CBCode.INTERNAL_SERVER_ERROR);
        }
    }

    public RTECMessage process(RTECMessage request) throws Exception {
        if (sessionId != null) {
            request.setValue("cp_request.session", sessionId);
        }
        return transport.request(request);
    }
}
