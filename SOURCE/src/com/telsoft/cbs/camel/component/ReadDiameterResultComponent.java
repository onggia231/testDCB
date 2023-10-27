package com.telsoft.cbs.camel.component;


import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.CBException;
import com.telsoft.cbs.domain.CBRequest;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.annotations.Component;
import telsoft.gateway.core.log.MessageContext;
import telsoft.gateway.core.message.MessageUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Read ppm display command from RTEC result
 * <p>
 */

@Component("cbs-read-diameter-result")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-read-diameter-result",
        title = "Read Diameter result",
        syntax = "cbs-read-diameter-result:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)

public class ReadDiameterResultComponent extends ProcessorComponent{
    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        String diameterResponse = exchange.getIn().getBody(String.class);
        Map mapResponse = new HashMap();
        MessageUtil.analyseMappedMessage(diameterResponse, ",", "=", mapResponse, true);
        Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
        String cpsResultCode = (String)mapResponse.get("result");

        /*//Test timeout
        if(cpsResultCode.contains("CPS-0000")){
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/

    }
}
