package com.telsoft.cbs.module.sms.handler.cbs;

import com.telsoft.cbs.module.cbsrest.client.CBSClient;
import com.telsoft.cbs.module.cbsrest.domain.RestResponse;
import com.telsoft.cbs.module.sms.handler.ChainCommandHandler;
import com.telsoft.cbs.module.sms.handler.CommandContext;
import com.telsoft.cbs.module.sms.handler.ConfigurableCommandHandler;
import com.telsoft.cbs.module.sms.handler.SessionAware;
import com.telsoft.dictionary.DictionaryNode;

public class REGISTER extends ChainCommandHandler implements ConfigurableCommandHandler, SessionAware {
    private CBSClient cbsClient;
    private String url;
    private String source;
    private String storeCode;

    @Override
    public void doProcess(CommandContext context) {
        RestResponse response = cbsClient.registerProfile(null, context.getMessage().getOriginator(), "");
    }

    @Override
    public void configure(DictionaryNode config) throws Exception {
        url = config.getStringIgnoreCase("Url");
        source = config.getStringIgnoreCase("Source");
        storeCode = config.getStringIgnoreCase("storeCode");
    }


    public void start() throws Exception {
        cbsClient = new CBSClient(this.source, this.storeCode, url);
        cbsClient.start();

    }

    public void stop() {
        cbsClient.stop();
    }

}
