package com.telsoft.cbs.utils;

import telsoft.gateway.core.gw.MessageChannel;
import telsoft.gateway.core.translator.Translator;

public abstract class CBTranslator implements Translator {
    @Override
    public void init(String strParameter) throws Exception {

    }

    @Override
    public Class[] getSupportedChannelClasses() {
        return new Class[]{MessageChannel.class};
    }
}
