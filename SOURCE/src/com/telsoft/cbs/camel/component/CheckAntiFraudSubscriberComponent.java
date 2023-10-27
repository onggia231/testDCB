package com.telsoft.cbs.camel.component;


import com.telsoft.cbs.domain.CBException;
import com.telsoft.cbs.domain.CBRequest;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.annotations.Component;
import telsoft.gateway.core.log.MessageContext;

import java.util.Map;

/**
 * Check anti-fraud
 * <p>
 */

@Component("cbs-check-anti-fraud")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-check-anti-fraud",
        title = "Check anti-fraud",
        syntax = "cbs-check-anti-fraud:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)
public class CheckAntiFraudSubscriberComponent extends ProcessorComponent {
    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        //todo Call API anti-fraud to 9029 system of Mobifone
    }
}
