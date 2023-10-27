package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.CBCode;
import com.telsoft.cbs.domain.CBException;
import com.telsoft.cbs.domain.CBRequest;
import com.telsoft.cbs.utils.CbsLog;
import com.telsoft.cbs.utils.CbsUtils;
import com.telsoft.util.StringUtil;
import org.apache.axis.utils.StringUtils;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.annotations.Component;
import telsoft.gateway.core.cmp.gw_rtec.RTECMessage;
import telsoft.gateway.core.log.MessageContext;
import telsoft.gateway.core.message.MessageUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Read ppm display command from RTEC result
 * <p>
 */

@Component("cbs-read-ppm-display-result")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-read-ppm-display-result",
        title = "Read ppm display command from RTEC result",
        syntax = "cbs-read-ppm-display-result:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)

public class ReadPpmDisplayResultComponent extends ProcessorComponent {
    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        //todo Read response from CPS for ppm_display command, put to context
        String xmlResponse = exchange.getIn().getBody(String.class);
        try {
            RTECMessage rtecMessage = new RTECMessage(xmlResponse);
            String result = rtecMessage.getValue("cp_reply.result");
            if ("CPS-0000".equals(result)) {
                String subInfo = rtecMessage.getValue("cp_reply.sub_info");
                Map<String, String> mapInfo = new HashMap<>();
                MessageUtil.analyseMappedMessage(subInfo, ",", "=", mapInfo, true);
                CbsContansts.CPS_SUB_PAY_TYPE payType = CbsContansts.CPS_SUB_PAY_TYPE.getByCode(StringUtil.nvl(mapInfo.get("prepaid"), "1"));
                Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
                CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.SUB_PAY_TYPE, payType); //put to gw messageContext

                String strAcviveDate = StringUtil.nvl(mapInfo.get("activbdt"), "");
                Date subscriberActiveDate = null;
                try {
                    subscriberActiveDate = new SimpleDateFormat(CbsContansts.DATE_FORMAT_PPM_DISPLAY).parse(strAcviveDate);
                } catch (Exception ignore) {
                    log.warn("Can't parse active date from ppm_display response: " + subInfo);
                }
                if (subscriberActiveDate != null) {
                    Long deltaAcitveDate = CbsUtils.compareTwoDateByDays(new Date(), subscriberActiveDate);
                    exchange.setProperty(CbsContansts.SUB_ACTIVE_DATE, subscriberActiveDate);
                    CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.SUB_ACTIVE_DATE, subscriberActiveDate); //put to gw messageContext
                    exchange.setProperty(CbsContansts.SUB_DELTA_ACTIVE_DATE, deltaAcitveDate);
                    CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.SUB_DELTA_ACTIVE_DATE, deltaAcitveDate); //put to gw messageContext
                }

                // prepaid balance USCREDVO
                String strPrepaidBalance = mapInfo.get("uscredvo");
                if (!StringUtils.isEmpty(strPrepaidBalance)) {
                    try {
                        Long lPrepaidMainBalance = Long.parseLong(strPrepaidBalance);
                        exchange.setProperty(CbsContansts.SUB_PREPAID_MAIN_BALANCE, lPrepaidMainBalance);
                        CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.SUB_PREPAID_MAIN_BALANCE, lPrepaidMainBalance); //put to gw messageContext
                    } catch (Exception ignore) {
                        log.warn("Can't parse prepaid main balance (USCREDVO) field from ppm_display response: " + subInfo);
                    }
                }

            } else if ("CPS-1007".equals(result)) {
                throw new CBException(CBCode.USER_UNKNOWN);
            } else {
                throw new CBException(CBCode.INTERNAL_SERVER_ERROR);
            }
        } catch (CBException e) {
            throw e;
        } catch (Exception e) {
            CbsLog.error(messageContext, "ReadPpmDisplayResultComponent", CBCode.INTERNAL_SERVER_ERROR, "", e.getMessage());
            throw new CBException(CBCode.INTERNAL_SERVER_ERROR, e);
        }
    }
}
