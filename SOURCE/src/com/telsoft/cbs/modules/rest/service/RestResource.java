package com.telsoft.cbs.modules.rest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.CBCode;
import com.telsoft.cbs.domain.CBRequest;
import com.telsoft.cbs.domain.CBResponse;
import com.telsoft.cbs.modules.rest.RestMessageChannel;
import com.telsoft.cbs.modules.rest.RestReceptionist;
import com.telsoft.cbs.modules.rest.domain.Request;
import com.telsoft.cbs.modules.rest.domain.Response;
import com.telsoft.cbs.utils.CbsUtils;
import com.telsoft.cbs.utils.JsonObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import telsoft.gateway.commons.GWUtil;
import telsoft.gateway.core.GWEvent;
import telsoft.gateway.core.gw.Gateway;
import telsoft.gateway.core.gw.MessageChannel;
import telsoft.gateway.core.log.MessageContext;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import java.util.concurrent.ConcurrentHashMap;

@Path("/")
@Produces({"application/xml"})
@Consumes({"application/xml"})
public class RestResource {
    private static final Logger log = LoggerFactory.getLogger(RestResource.class);

    @Context
    private RestReceptionist receptionist;

    private Gateway getGateway() {
        if (receptionist == null) {
            return null;
        }
        return receptionist.getGateway();
    }

    private void checkChannelThreshold() throws Exception {
        if (getGateway() == null) {
            throw new Exception("Gateway is null, the server may be started up incorrectly");
        }
        int iConnectionCount = getGateway().getChannelList().size();
        int iMaxConnection = getGateway().getMaxConnection();

        // MaxConnection >  0 : Limit connection count
        // MaxConnection <= 0 : Unlimit connection count
        if (iMaxConnection > 0 && iConnectionCount >= iMaxConnection) {
            // Log error
            String strError = String.format("Number of opened session (%d) exceeded the maximum session (%d)", iConnectionCount, iMaxConnection);
            throw new Exception(strError);
        }
    }

    private Response execute(Request request, HttpServletRequest httpRequest) throws RestException {
        //Log
        CbsUtils.logMonitorDebug(this.getGateway(), "Received message: ", request);
        if (receptionist != null) {
            MessageContext ctx = MessageContext.createMessageContext();
            ctx.setProperty(MessageContext.GATEWAY_ID, getGateway().getThreadID());
            CBRequest cbRequest = request.createCBRequest();
            Integer messageTimeout = (Integer) receptionist.getTimeoutTable().get(request.getName().name() + "_" + request.getStore_code());
            CBCode timeoutCode = (CBCode) receptionist.getTimeoutTableReturnCode().get(request.getName().name() + "_" + request.getStore_code());
            if (messageTimeout != null && messageTimeout > 0) {
                ctx.setProperty("TimeoutByCommand", messageTimeout);
            }
            if (timeoutCode != null) {
                ctx.setProperty("TimeoutByCommandReturnCode", timeoutCode);
            }
            try {
                checkChannelThreshold();
                RestMessageChannel channel = receptionist.createChannel();
                getGateway().addChannel(channel);

                ctx.setCommand(cbRequest.getCommandName());
                ctx.updateCommandID(getGateway());
                ctx.setRequest(cbRequest);
                ctx.setProperty(MessageContext.SESSION_ID, channel.getSessionID());
                ctx.setProperty(MessageContext.IPADDRESS, getRemoteAddr(httpRequest));
                ctx.setProperty("host", getLocalAddr(httpRequest));
                ctx.setProperty(MessageContext.GATEWAY_TYPE_ID, this.getGateway().getGatewayClassId());
                ctx.setAttribute(MessageContext.DISPATCHER_TYPE, "-1");
                ctx.setProperty("source", request.getSource());
                ctx.setProperty(CbsContansts.MAP_FULL_REQUEST, new ConcurrentHashMap());

                channel.setClientAddress(ctx.getPropertyNvl(MessageContext.IPADDRESS, ""));
                channel.startReceiptMessage();
                try {
                    ctx = channel.receivedMessage(ctx, cbRequest);
                } finally {
                    channel.close(MessageChannel.CLIENT_DISCONNECT, "Logged out");
                    getGateway().getGatewayManager().getEvent().fireGatewayEvent(GWEvent.GW_DISCONNECT, channel);
                }

                CBResponse cbResponse = (CBResponse) ctx.getClientResponse();
                if (ctx.getProperty(CbsContansts.TIMEOUT_RESPONSE) != null) {
                    cbResponse = (CBResponse) ctx.getProperty(CbsContansts.TIMEOUT_RESPONSE);
                }
                Response result = request.createResponse(cbResponse);
                //log
                CbsUtils.logMonitorDebug(this.getGateway(), "Response message: ", result);
                return result;
            } catch (Exception ex) {
                log.error("process message: ", ex);
                CBResponse cbResponse = cbRequest.createResponse();
                cbResponse.setCode(CBCode.INTERNAL_SERVER_ERROR);
                cbResponse.setMessage(GWUtil.decodeException(ex));
                ctx.setClientResponse(cbResponse);
                ctx.setStatus(MessageContext.STATUS_UNKNOWN);
                Response result = request.createResponse(cbResponse);
                //log
                CbsUtils.logMonitorDebug(this.getGateway(), "Response message: ", result);
                return result;
            } finally {
                getGateway().getGatewayManager().getLogger().attachLogRecord(ctx);
            }
        } else {
            throw new RestException(request, CBCode.INTERNAL_SERVER_ERROR);
        }
    }


    private String getLocalAddr(HttpServletRequest httpRequest) {
        return httpRequest.getLocalAddr();
    }

    private String getRemoteAddr(HttpServletRequest httpRequest) {
        String xForwardedFor = httpRequest.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && xForwardedFor.length() > 0)
            return xForwardedFor;
        return httpRequest.getRemoteAddr();
    }

    @Path("/execute")
    @POST
    public Response subscriberLookup(Request request, @Context HttpServletRequest httpRequest) throws RestException {
        return execute(request, httpRequest);
    }
}
