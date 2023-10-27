package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.domain.ActionCode;
import com.telsoft.cbs.domain.CBException;
import com.telsoft.cbs.domain.CBRequest;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.annotations.Component;
import telsoft.gateway.core.log.MessageContext;

import java.util.Map;

/**
 * The Action Log Component write subscriber action log into local database
 * <p>
 */

@Component("cbs-action-log")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-action-log",
        title = "Log subscriber action",
        syntax = "cbs-action-log:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)

public class ActionLogComponent extends ProcessorComponent {
    @UriParam(name = "code", displayName = "Action", description = "Subscriber action")
    ActionCode code;

    @UriParam(name = "message", displayName = "Custom message", description = "Custom message")
    String message;


    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {

    }
}
