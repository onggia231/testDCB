package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.utils.CbsUtils;
import com.telsoft.cbs.utils.DbUtils;
import com.telsoft.cbs.domain.CBCode;
import com.telsoft.cbs.domain.CBException;
import com.telsoft.cbs.domain.CBRequest;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.annotations.Component;
import telsoft.gateway.core.log.MessageContext;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Check Subscriber Registered Component
 * IN: Isdn from MessageContext
 * OUT: Set sub_id in MessageContext.property, exchange.property
 * ERROR: USER_UNKNOWN if there is no isdn in CB_SUBSCRIBER
 * ERROR: INTERNAL_SERVER_ERROR : other error occurred
 * <p>
 */

@Component("cbs-sub-registered")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-sub-registered",
        title = "Check Subscriber is registered",
        syntax = "cbs-sub-registered:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)

public class CheckSubscriberRegisteredComponent extends ProcessorComponent {
    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        try {
            try (Connection connection = getManager().getConnection()) {
                // Get sub_id from CB_SUBSCRIBER
                // if sub_id is null, user is not registered yet
                String isdn = messageContext.getIsdn();
                String subId = DbUtils.getValue(connection,
                        "CB_SUBSCRIBER",
                        "ID",
                        String.format("ISDN='%s' AND STATUS='1'", isdn));

                if (subId == null) {
                    throw new CBException(CBCode.USER_UNKNOWN);
                }
                messageContext.setProperty(CbsContansts.SUB_ID, subId);
                exchange.setProperty(CbsContansts.SUB_ID, subId);
                Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
                CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest,CbsContansts.SUB_ID, subId);
            }
        } catch (CBException cbse) {
            throw cbse;
        } catch (Exception e) {
            throw new CBException(CBCode.INTERNAL_SERVER_ERROR, e);
        }
    }
}
