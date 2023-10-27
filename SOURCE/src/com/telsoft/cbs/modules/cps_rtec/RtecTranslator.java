package com.telsoft.cbs.modules.cps_rtec;

import com.telsoft.cbs.utils.CBTranslator;
import telsoft.gateway.core.cmp.gw_rtec.RTECMessage;
import telsoft.gateway.core.dsp.Dispatcher;
import telsoft.gateway.core.dsp.DispatcherManager;
import telsoft.gateway.core.log.MessageContext;
import telsoft.gateway.core.message.GatewayMessage;
import telsoft.gateway.core.message.PlainMessage;

public class RtecTranslator extends CBTranslator {
    @Override
    public GatewayMessage processMessage(MessageContext msgContext, Dispatcher thr) throws Exception {
        PlainMessage plainMessage = (PlainMessage) msgContext.getRequest();

        RTECMessage request = new RTECMessage(plainMessage.getContent());
        return thr.processMessage(msgContext, request);
    }

    @Override
    public boolean canApply(DispatcherManager dispatcher) {
        return dispatcher instanceof CpsRtecDispatcher;
    }
}
