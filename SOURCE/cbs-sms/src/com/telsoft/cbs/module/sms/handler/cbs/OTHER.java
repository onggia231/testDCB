package com.telsoft.cbs.module.sms.handler.cbs;

import com.telsoft.cbs.module.sms.handler.ChainCommandHandler;
import com.telsoft.cbs.module.sms.handler.CommandContext;
import com.telsoft.cbs.module.sms.handler.ConfigurableCommandHandler;
import com.telsoft.dictionary.DictionaryNode;

public class OTHER extends ChainCommandHandler implements ConfigurableCommandHandler {
    @Override
    public void doProcess(CommandContext context) {
        System.out.println("Cu phap khong dung dinh dang");
    }

    @Override
    public void configure(DictionaryNode config) throws Exception {

    }
}
