package com.telsoft.cbs.modules.soap;

import com.telsoft.cbs.utils.CBTranslator;
import telsoft.gateway.core.dsp.Dispatcher;
import telsoft.gateway.core.dsp.DispatcherManager;
import telsoft.gateway.core.log.MessageContext;
import telsoft.gateway.core.message.GatewayMessage;
import telsoft.gateway.core.message.PlainMessage;

public class AntiProfitTranslator extends CBTranslator {
    @Override
    public GatewayMessage processMessage(MessageContext msgContext, Dispatcher thr) throws Exception {
        PlainMessage plainMessage = (PlainMessage) msgContext.getRequest();

        return thr.processMessage(msgContext, plainMessage);
    }

    @Override
    public boolean canApply(DispatcherManager dispatcher) {
        return dispatcher instanceof AntiProfitDispatcher;
    }
}
