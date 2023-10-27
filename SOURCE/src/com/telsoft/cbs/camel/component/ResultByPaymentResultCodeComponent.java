package com.telsoft.cbs.camel.component;


import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.*;
import org.apache.camel.Exchange;
import org.apache.camel.component.cbor.CBORConstants;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.annotations.Component;
import telsoft.gateway.core.log.MessageContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Result Component build response message for sending to client by payment result
 * <p>
 */

@Component("cbs-result-by-payment-result")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-result-by-payment-result",
        title = "Build response message",
        syntax = "cbs-result-by-payment-result:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)

public class ResultByPaymentResultCodeComponent extends ProcessorComponent{
    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
        CBCode code = CBCode.valueOfCode((String) mapFullRequest.get(CbsContansts.PAYMENT_RESULT_CODE));

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
        //IF idempotent, response same
        String refer_idempotent_id = null;
        try{
//            Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
            refer_idempotent_id = (String) mapFullRequest.get(CbsContansts.REFERENCE_IDEMPOTENT_ID);
        }catch (Exception ignore){
            ;
        }
        if(refer_idempotent_id != null && !refer_idempotent_id.isEmpty())
            response.set(CbsContansts.TRANSACTION_ID, refer_idempotent_id);

        if (response.getCode() == CBCode.OK)
            messageContext.setStatus(MessageContext.STATUS_SUCCESS);
        else if (response.getCode() == CBCode.PROCESS_TIMEOUT)
            messageContext.setStatus(REQUEST_STATUS.TIMEOUT.getCode());
        else
            messageContext.setStatus(MessageContext.STATUS_UNKNOWN);

        messageContext.setClientResponse(response);
    }
}
