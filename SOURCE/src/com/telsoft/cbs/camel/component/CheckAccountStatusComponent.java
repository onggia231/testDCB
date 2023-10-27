package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.domain.CBException;
import com.telsoft.cbs.domain.CBRequest;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.annotations.Component;
import telsoft.gateway.core.log.MessageContext;

import java.util.Map;

/**
 * The Check Account Status Component check current subscriber status before charge
 * <p>
 */

@Component("cbs-check-account-status")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-check-account-status",
        title = "Check Subscriber Current status",
        syntax = "cbs-check-account-status:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)

public class CheckAccountStatusComponent extends ProcessorComponent {
    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {

    }
}
