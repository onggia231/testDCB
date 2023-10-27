package com.telsoft.cbs.camel.component;


import com.telsoft.cbs.domain.CBCode;
import com.telsoft.cbs.domain.CBException;
import com.telsoft.cbs.domain.CBRequest;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.annotations.Component;
import telsoft.gateway.core.log.MessageContext;

import java.util.Map;

/**
 * Throw a CBException
 * <p>
 */

@Component("cbs-throw-cbexception")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-throw-cbexception",
        title = "Throw a CBException",
        syntax = "cbs-throw-cbexception:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)

public class ThrowCBExceptionComponent extends ProcessorComponent {
    @UriParam(name = "code", displayName = "Code", description = "Result Code")
    CBCode code;


    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        CBCode code = CBCode.valueOf((String) parameters.get("code"));
        throw new CBException(code);
    }
}
