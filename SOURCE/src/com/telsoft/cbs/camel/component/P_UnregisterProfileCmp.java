package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.utils.DbUtils;
import com.telsoft.cbs.domain.CBCode;
import com.telsoft.cbs.domain.CBException;
import com.telsoft.cbs.domain.CBRequest;
import com.telsoft.cbs.domain.CBStore;
import com.telsoft.database.Database;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.annotations.Component;
import telsoft.gateway.core.log.MessageContext;

import java.sql.Connection;
import java.util.Map;

/**
 * The UnRegister Profile Component
 * <p>
 */

@Slf4j
@Component("cbs-unregister-profile")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-unregister-profile",
        title = "Unregister subscriber information from local database",
        syntax = "cbs-unregister-profile:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)

public class P_UnregisterProfileCmp extends ProcessorComponent {

    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        try {
            try (Connection connection = getManager().getConnection()) {

                /**
                 * Get Store ID, default limitations from CB_STORE
                 * {@link CheckStoreComponent#process(CBRequest, Exchange, Map, MessageContext)}
                 */

                CBStore store = (CBStore) messageContext.getProperty(CbsContansts.STORE);
                if (store == null) {
                    throw new CBException(CBCode.STORE_UNKNOWN);
                }

                // Get sub_id from CB_SUBSCRIBER
                // if sub_id is null, user is not registered yet
                String isdn = messageContext.getIsdn();
                String subId = DbUtils.getValue(connection,
                        "CB_SUBSCRIBER",
                        "ID",
                        String.format("ISDN='%s' AND STATUS='1'", isdn));

                if (subId == null) {
                    throw new CBException(CBCode.USER_UNKNOWN);
                }

                // remove SUB_ID, STORE_ID from CB_SUB_STORE
                String registered = DbUtils.getValue(connection,
                        "CB_SUB_STORE",
                        "1",
                        String.format("SUB_ID='%s' AND STORE_ID='%s'", subId, store.getStoreId()));

                if (registered == null) {
                    throw new CBException(CBCode.USER_UNKNOWN);
                } else {
                    Database.executeUpdate(connection,
                            String.format("DELETE CB_SUB_STORE WHERE SUB_ID = '%s' AND STORE_ID='%s'", subId, store.getStoreId()));
                }
                exchange.getOut().setBody("OK");
            }
        } catch (CBException cbse) {
            throw cbse;
        } catch (Exception e) {
            throw new CBException(CBCode.INTERNAL_SERVER_ERROR, e);
        }
    }
}
