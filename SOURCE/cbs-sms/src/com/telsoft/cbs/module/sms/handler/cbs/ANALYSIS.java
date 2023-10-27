package com.telsoft.cbs.module.sms.handler.cbs;

import com.telsoft.cbs.module.sms.handler.ChainCommandHandler;
import com.telsoft.cbs.module.sms.handler.CommandContext;
import com.telsoft.cbs.module.sms.handler.ConfigurableCommandHandler;
import com.telsoft.dictionary.DictionaryNode;
import com.telsoft.util.StringUtil;

public class ANALYSIS extends ChainCommandHandler implements ConfigurableCommandHandler {
    @Override
    public void doProcess(CommandContext context) {
        String content = context.getMessage().getContent();
        if (content.startsWith("SILENTSMS:")) {
            context.getAttributes().put("Code", "SILENTSMS");
        } else {
            String[] array = StringUtil.toStringArray(content, " ");
            if (array == null || array.length == 0)
                context.getAttributes().put("Code", "OTHER");
            else
                context.getAttributes().put("Code", array[0]);
        }
    }

    @Override
    public void configure(DictionaryNode config) throws Exception {

    }
}
