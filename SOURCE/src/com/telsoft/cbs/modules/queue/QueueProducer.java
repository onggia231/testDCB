package com.telsoft.cbs.modules.queue;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.CBCode;
import com.telsoft.cbs.domain.CBException;
import com.telsoft.cbs.utils.CbsLog;
import com.telsoft.util.AppException;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;
import telsoft.gateway.commons.GWUtil;
import telsoft.gateway.core.GatewayManager;
import telsoft.gateway.core.gw.MessageChannel;
import telsoft.gateway.core.log.MessageContext;
import telsoft.gateway.core.message.PlainMessage;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class QueueProducer extends DefaultProducer {
    public static final String LOCK = "$LOCK";
    private final ServerId serverId;
    private int miDispatchMessageTimeout = 30000;

    public QueueProducer(Endpoint endpoint, ServerId serverId) {
        super(endpoint);
        this.serverId = serverId;
    }

    protected static void merge(MessageContext parent, MessageContext child) {
        parent.getServerCommand().addAll(child.getServerCommand());
    }

    GatewayManager getGatewayManager() {
        return ((QueueEndPoint) getEndpoint()).getGatewayManager();
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        MessageContext messageContext = (MessageContext) exchange.getProperty(CbsContansts.CB_MSG_CONTEXT);
        MessageContext child = null;
        try {
            child = processMessage(messageContext, exchange.getIn().getBody(String.class));

            if (child.getException() != null) {
                throw child.getException();
            }
            exchange.getMessage().setBody(child.getClientResponse().getContent(), String.class);
            exchange.getMessage().setHeaders(exchange.getIn().getHeaders());
        } catch (Exception ex) {
            exchange.getMessage().setHeaders(exchange.getIn().getHeaders());
            String exp = GWUtil.decodeException(ex);
            CbsLog.error(messageContext, "QueueProducer.process", CBCode.INTERNAL_SERVER_ERROR, serverId.name(), exp);
            if (exp.equals("[GW]No dispatcher available to serve your request")) {
                throw new CBException(CBCode.CARRIER_MAINTENANCE);
            } else {
                throw new CBException(CBCode.INTERNAL_SERVER_ERROR);
            }
        } finally {
            if (messageContext != null && child != null)
                merge(messageContext, child);
        }
    }

    public MessageContext processMessage(MessageContext messageContext, String request) throws Exception {
        MessageContext childCtx = MessageContext.createMessageContext();
        childCtx.setProperty(MessageContext.REQUEST_ID, messageContext.getRequestID());
        childCtx.setRequest(new PlainMessage(request));
        childCtx.setProperty(MessageContext.SESSION_ID, "CAMEL");
        childCtx.setProperty(MessageContext.IPADDRESS, "local");
        childCtx.setProperty(MessageContext.GATEWAY_TYPE_ID, "-1");
        childCtx.setProperty(MessageContext.MESSAGE_CHANNEL, new DumpMessageChannel());
        childCtx.setAttribute(MessageContext.DISPATCHER_TYPE, "-1");
        childCtx.setAttribute(MessageChannel.INDEX_SERVER_ID, serverId.name());
        childCtx.setAttribute(MessageChannel.INDEX_ROUTE_ID, serverId.name());
        childCtx.setAttribute(MessageChannel.INDEX_CHANNEL, "CAMEL");
        childCtx.setAttribute("$SUB", "SUB");
        childCtx.setProperty(CbsContansts.STORE, messageContext.getProperty(CbsContansts.STORE));
        //Add mapFullRequest
        childCtx.setProperty(CbsContansts.MAP_FULL_REQUEST, messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST));

        CountDownLatch lockObject = new CountDownLatch(1);
        childCtx.setProperty(LOCK, lockObject);
        getGatewayManager().getQueueIN().attach(childCtx);
        if (miDispatchMessageTimeout > 0) {
            lockObject.await(miDispatchMessageTimeout, TimeUnit.MILLISECONDS);
            if (!childCtx.isProcessed()) {
                childCtx.setProcessed(true);
                throw new AppException("Wait message timeout");
            }
        } else {
            lockObject.wait();
        }
        /*if (childCtx.getException() != null) {
            throw childCtx.getException();
        }*/
        return childCtx;
    }

}
