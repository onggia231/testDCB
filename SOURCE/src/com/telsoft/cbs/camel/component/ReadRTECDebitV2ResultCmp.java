package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.CBCode;
import com.telsoft.cbs.domain.CBException;
import com.telsoft.cbs.domain.CBRequest;
import com.telsoft.cbs.utils.CbsUtils;
import com.telsoft.util.StringUtil;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.annotations.Component;
import telsoft.gateway.core.cmp.gw_rtec.RTECMessage;
import telsoft.gateway.core.log.MessageContext;
import telsoft.gateway.core.message.MessageUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Read debit_v2 command from RTEC result
 * <p>
 */

@Component("cbs-read-rtec-debit-v2-result")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-read-rtec-debit-v2-result",
        title = "Read debit_v2 command from RTEC result",
        syntax = "cbs-read-rtec-debit-v2-result:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)

public class ReadRTECDebitV2ResultCmp extends ProcessorComponent {

    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        String xmlResponse = exchange.getIn().getBody(String.class);
        try {
            RTECMessage rtecMessage = new RTECMessage(xmlResponse);
            String result = rtecMessage.getValue("cp_reply.result");
            if ("CPS-0000".equals(result)) {
                String chargeInfo = rtecMessage.getValue("cp_reply.charge_id");
                Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
                CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.CPS_R_REFUND_INFORMATION, chargeInfo); //put to gw messageContext
            }
        } catch (CBException e) {
            throw e;
        } catch (Exception e) {
            throw new CBException(CBCode.INTERNAL_SERVER_ERROR);
        }
    }
}

