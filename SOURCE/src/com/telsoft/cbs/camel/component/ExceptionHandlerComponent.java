package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.CBCode;
import com.telsoft.cbs.domain.CBException;
import com.telsoft.cbs.domain.CBRequest;
import com.telsoft.cbs.domain.CBResponse;
import com.telsoft.cbs.utils.CbsLog;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.annotations.Component;
import telsoft.gateway.core.log.MessageContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Handle Error Component
 * <p>
 */

@Component("cbs-exception")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-exception",
        title = "Build response when error is occurred",
        syntax = "cbs-exception:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)
public class ExceptionHandlerComponent extends ProcessorComponent {
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        Exception ex = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
        exchange.removeProperty(Exchange.EXCEPTION_CAUGHT);

        if (ex instanceof CBException) {
            CBException exception = (CBException) ex;
            CBResponse response = request.createResponse();
            response.setCode(exception.getCode());
            response.setMessage(ex.getCause() == null ? null : ex.getCause().getMessage());
            if (messageContext.getTransID() != null)
                response.set(CbsContansts.TRANSACTION_ID, messageContext.getTransID());
            //IF idempotent, response same
            String refer_idempotent_id = null;
            try{
                Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
                refer_idempotent_id = (String) mapFullRequest.get(CbsContansts.REFERENCE_IDEMPOTENT_ID);
            }catch (Exception ignore){
                ;
            }
            if(refer_idempotent_id != null && !refer_idempotent_id.isEmpty())
                response.set(CbsContansts.TRANSACTION_ID, refer_idempotent_id);

            exchange.getMessage().setBody(response);
            messageContext.setClientResponse(response);
            messageContext.setStatus(response.getCode() == CBCode.OK ? MessageContext.STATUS_SUCCESS : MessageContext.STATUS_UNKNOWN);
//            CbsLog.error(messageContext, "ExceptionHandlerComponent", response.getCode(), "", ex.getMessage());
        } else {
            CBResponse response = request.createResponse();
            response.setCode(CBCode.INTERNAL_SERVER_ERROR);
            response.setMessage(ex.getMessage());
            exchange.getMessage().setBody(response);
            messageContext.setClientResponse(response);
            messageContext.setStatus(MessageContext.STATUS_UNKNOWN);
            CbsLog.error(messageContext, "ExceptionHandlerComponent", response.getCode(), "", ex.getMessage());
        }
    }
}
