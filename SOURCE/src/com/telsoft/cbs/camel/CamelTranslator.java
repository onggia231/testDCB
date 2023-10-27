package com.telsoft.cbs.camel;

import com.telsoft.cbs.utils.CBTranslator;
import telsoft.gateway.core.dsp.Dispatcher;
import telsoft.gateway.core.dsp.DispatcherManager;
import telsoft.gateway.core.log.MessageContext;
import telsoft.gateway.core.message.GatewayMessage;

public class CamelTranslator extends CBTranslator {
    @Override
    public GatewayMessage processMessage(MessageContext msgContext, Dispatcher thr) throws Exception {
        return thr.processMessage(msgContext, null);
    }

    @Override
    public boolean canApply(DispatcherManager dispatcher) {
        return dispatcher instanceof CamelDispatcher;
    }
}
