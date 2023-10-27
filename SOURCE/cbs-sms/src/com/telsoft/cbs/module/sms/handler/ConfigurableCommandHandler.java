package com.telsoft.cbs.module.sms.handler;

import com.telsoft.dictionary.DictionaryNode;

public interface ConfigurableCommandHandler {
    void configure(DictionaryNode config) throws Exception;
}
