package com.telsoft.cbs.modules.queue;

import telsoft.gateway.core.gw.MessageChannel;
import telsoft.gateway.core.gw.TranslateTable;
import telsoft.gateway.core.log.MessageContext;
import telsoft.gateway.core.message.GatewayMessage;

import java.util.Map;

public class DumpMessageChannel extends MessageChannel {
    @Override
    public String getProtocolName() {
        return "CAMEL";
    }

    @Override
    public void sendMessage(MessageContext record, GatewayMessage msg) {

    }

    @Override
    protected void processMessage(MessageContext ctx, GatewayMessage msg) throws Exception {

    }

    @Override
    public void handleException(MessageContext msgContext, Exception e) {

    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public String getClientAddress() {
        return "local";
    }

    @Override
    public Map getMappedValues(GatewayMessage gatewayMessage, TranslateTable table) {
        return null;
    }

    @Override
    public void run() {

    }
}
