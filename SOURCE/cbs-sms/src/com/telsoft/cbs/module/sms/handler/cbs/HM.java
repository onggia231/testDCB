package com.telsoft.cbs.module.sms.handler.cbs;

import com.telsoft.cbs.module.sms.handler.ChainCommandHandler;
import com.telsoft.cbs.module.sms.handler.CommandContext;
import com.telsoft.cbs.module.sms.handler.ConfigurableCommandHandler;
import com.telsoft.dictionary.DictionaryNode;

public class HM extends ChainCommandHandler implements ConfigurableCommandHandler {
    @Override
    public void doProcess(CommandContext context) {
        System.out.println("Khach hang thay doi han muc");
    }

    @Override
    public void configure(DictionaryNode config) throws Exception {

    }
}
