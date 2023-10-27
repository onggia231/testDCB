package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.CBCode;
import com.telsoft.cbs.domain.CBException;
import com.telsoft.cbs.domain.CBRequest;
import com.telsoft.cbs.domain.CBResponse;
import com.telsoft.util.StringUtil;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.annotations.Component;
import telsoft.gateway.core.cmp.gw_rtec.RTECMessage;
import telsoft.gateway.core.log.MessageContext;
import telsoft.gateway.core.message.MessageUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * The Account Profile Component build the get profile response message
 * <p>
 */

@Component("cbs-result-account-profile")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-result-account-profile",
        title = "Build response message for Account Profile request",
        syntax = "cbs-result-account-profile:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)
public class BuildAccountProfileResultComponent extends ResultComponent {
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        super.process(request, exchange, parameters, messageContext);
        CBResponse response = (CBResponse) exchange.getMessage().getBody();
        String xmlResponse = exchange.getIn().getBody(String.class);
        try {
            RTECMessage rtecMessage = new RTECMessage(xmlResponse);
            String subInfo = rtecMessage.getValue("cp_reply.sub_info");
            Map<String, String> mapInfo = new HashMap<>();
            MessageUtil.analyseMappedMessage(subInfo, ",", "=", mapInfo, true);

            CbsContansts.CPS_SUB_PAY_TYPE payType = CbsContansts.CPS_SUB_PAY_TYPE.getByCode(StringUtil.nvl(mapInfo.get("prepaid"), "1"));
            String prepaid = StringUtil.nvl(mapInfo.get("prepaid"), "1");

            response.set("account-type", payType.name().toLowerCase());
            if("false".equals(exchange.getProperty("eligibility")) || !"active".equals(exchange.getProperty("account-status"))){
                response.set("eligibility", "false");
            }else {
                response.set("eligibility", "true");
            }
            response.set("account-status", exchange.getProperty("account-status"));
            response.set("msisdn", exchange.getProperty(CbsContansts.MSISDN));
        } catch (Exception e) {
            throw new CBException(CBCode.INTERNAL_SERVER_ERROR);
        }
    }
}
