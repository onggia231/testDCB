package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.camel.service.cache.CacheService;
import com.telsoft.cbs.domain.*;
import com.telsoft.cbs.utils.CbsUtils;
import org.apache.camel.Exchange;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.annotations.Component;
import telsoft.gateway.core.log.MessageContext;

import java.sql.Connection;
import java.util.Map;

/**
 * The Check Subscriber/Content Blacklist/Whitelist Component
 * <p>
 */

@Component("cbs-check-access")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-check-access",
        title = "Check Isdn/Content in blacklist/whitelist",
        syntax = "cbs-check-access:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)

public class CheckAccessListComponent extends ProcessorComponent {

    @UriParam(name = "name", displayName = "ListName", description = "The name of access list")
    @Metadata(required = true)
    String name;

    @UriParam(name = "errorCode", displayName = "Error Code", description = "The error's code will be raised when subscriber/content is denied")
    @Metadata(required = true)
    CBCode errorCode;

    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        if(CbsUtils.isProcessTimeout(messageContext)){
            throw new CBException(CBCode.PROCESS_TIMEOUT);
        }

        String name = (String) parameters.get("name");
        CBCode code = CBCode.valueOf((String) parameters.get("errorCode"));

        CBStore store = (CBStore) messageContext.getProperty(CbsContansts.STORE);
        LIST_ACCESS_TYPE mode = LIST_ACCESS_TYPE.valueOfName(store.getAttributes().getProperty("running_mode"));

        CacheService cacheService = getService(CacheService.class);
        if (cacheService == null) {
            throw new CBException(CBCode.INTERNAL_SERVER_ERROR, new Exception("CacheService is not installed"));
        }

        Map mapAccessList = cacheService.getMap(CbsContansts.ACCESS_MAP);
        if (mapAccessList == null)
            throw new CBException(CBCode.INTERNAL_SERVER_ERROR, new Exception("CacheService is not installed correctly:" + CbsContansts.ACCESS_MAP));

        AccessList accessList = (AccessList) mapAccessList.get(name);

        //Bỏ qua access list khong ton tai hoac khac running mode
        if (accessList == null || accessList.getAccess_type() != mode) {
//            throw new CBException(code);
            return;
        }


        String subject;
        switch (accessList.getType()) {
            case ISDN: {
                subject = messageContext.getIsdn();
                break;
            }
            case CONTENT: {
                subject = messageContext.getPropertyNvl(CbsContansts.CONTENT_DESCRIPTION, "");
                break;
            }
            default:
                throw new CBException(code);
        }

        if (accessList.isCached()) {
            if (subject == null || !accessList.checkAccess(null, store.getStoreId(), subject)) {
                throw new CBException(code);
            }
        } else {
            try (Connection connection = getManager().getConnection()) {
                if (subject == null || !accessList.checkAccess(connection, store.getStoreId(), subject)) {
                    throw new CBException(code);
                }
            } catch (CBException e) {
                throw e;
            } catch (Exception e) {
                throw new CBException(CBCode.INTERNAL_SERVER_ERROR);
            }
        }
    }
}
