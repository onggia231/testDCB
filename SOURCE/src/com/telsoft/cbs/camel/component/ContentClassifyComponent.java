package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.CBCode;
import com.telsoft.cbs.domain.CBException;
import com.telsoft.cbs.domain.CBRequest;
import com.telsoft.cbs.domain.CONTENT_TYPE;
import com.telsoft.cbs.utils.CbsLog;
import com.telsoft.cbs.utils.CbsUtils;
import com.telsoft.util.StringUtil;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.annotations.Component;
import telsoft.gateway.core.log.MessageContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Classify the Content
 * <p>
 */

@Component("cbs-content-classify")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-content-classify",
        title = "Classify the Content",
        syntax = "cbs-content-classify:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)

public class ContentClassifyComponent extends ProcessorComponent{

    @UriParam(name = "contentRegex", displayName = "Content Regex", description = "Content Regex")
    String contentRegex;

    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        try {
            String contentRegex = StringUtil.nvl((String) parameters.get("contentRegex"), "^GOOGLE - [\\s\\S]+");
            Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
            String contentDescription = StringUtil.nvl((String) mapFullRequest.get(CbsContansts.CONTENT_DESCRIPTION), "");
            String contentType = CONTENT_TYPE.OTHER_CONTENT.getCode();
            //fixme Doi lai cho chinh xac khi co thong tin (Phan biet noi dung)
            if (contentDescription != null && contentDescription.matches(contentRegex)) {
                contentType = CONTENT_TYPE.GOOGLE_CONTENT.getCode();
            }
            CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.CPS_B_ISDN, contentType);
            CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.CONTENT_TYPE, contentType);
        }catch (Exception ex)
        {
            CbsLog.error(messageContext, "ContentClassifyComponent", CBCode.UNKNOWN, "", ex.getMessage());
            throw ex;
        }
    }
}
