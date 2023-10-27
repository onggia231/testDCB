package com.telsoft.cbs.utils;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.CBCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import telsoft.gateway.core.log.MessageContext;

import java.util.ArrayList;
import java.util.List;

public final class CbsLog {
    private static final Logger log = LoggerFactory.getLogger("CBS");

    public static void error(MessageContext messageContext, String context, CBCode code, String p1, Object p2) {
        if (log.isErrorEnabled()) {
            log.error("[{}]-[{}]-[{}] {}-{}:p1:{}:p2:{}", messageContext.getTransID(), messageContext.getProperty(CbsContansts.STORE_TRANSACTION_ID), messageContext.getProperty("source"), context, code, p1, p2);
        }
        List<String> exceptions = (List<String>) messageContext.getProperty(CbsContansts.LIST_EXCEPTIONS);
        if(exceptions == null){
            exceptions = new ArrayList<String>();
            messageContext.setProperty(CbsContansts.LIST_EXCEPTIONS,exceptions);
        }
        exceptions.add(String.format("%s-%s:p1:%s:p2:%s", context, code, p1, p2 ));
    }
}
