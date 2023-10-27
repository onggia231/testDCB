package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.*;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.annotations.Component;
import telsoft.gateway.core.log.MessageContext;

import java.util.Map;

/**
 * The Result Component build response message for sending to client
 * <p>
 */

@Component("cbs-result")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-result",
        title = "Build response message",
        syntax = "cbs-result:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)
public class ResultComponent extends ProcessorComponent {
    @UriParam(name = "code", displayName = "Code", description = "Result Code")
    CBCode code;


    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        CBCode code = CBCode.valueOf((String) parameters.get("code"));
        if (request == null) {
            throw new CBException(CBCode.INTERNAL_SERVER_ERROR);
        }
        CBResponse response = request.createResponse();
        response.setCode(code);
        exchange.getOut().setBody(response);
        Map<String, String> resultExtras = (Map<String, String>) exchange.getProperty(CbsContansts.RESULT_EXTRAS);
        if (resultExtras != null) {
            resultExtras.forEach((k, v) -> {
                response.set(k, v);
            });
        }

        if (messageContext.getTransID() != null)
            response.set(CbsContansts.TRANSACTION_ID, messageContext.getTransID());
        
        if (response.getCode() == CBCode.OK)
            messageContext.setStatus(MessageContext.STATUS_SUCCESS);
        else if (response.getCode() == CBCode.PROCESS_TIMEOUT)
            messageContext.setStatus(REQUEST_STATUS.TIMEOUT.getCode());
        else
            messageContext.setStatus(MessageContext.STATUS_UNKNOWN);

        messageContext.setClientResponse(response);
    }
}
