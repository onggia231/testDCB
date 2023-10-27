package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.utils.DbUtils;
import com.telsoft.cbs.domain.*;
import com.telsoft.database.Database;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.annotations.Component;
import telsoft.gateway.core.log.MessageContext;

import java.sql.Connection;
import java.util.Map;
import java.util.Vector;

/**
 * The MT SMS Queue Component
 * <p>
 */

@Slf4j
@Component("cbs-mt-queue")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-mt-queue",
        title = "Put message to MT queue",
        syntax = "cbs-mt-queue:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)

public class EnMTQueueComponent extends ProcessorComponent {
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
                // if sub_id is null, add new subscriber into CB_SUBSCRIBER
                String isdn = messageContext.getIsdn();
                String content = request.get(CbsContansts.CONTENT);
                if (content == null || content.length() == 0) {
                    throw new CBException(CBCode.PARAMETER_ERROR);
                }

                if (content.length() > 1000) {
                    content = content.substring(0, 1000);
                }

                String subId = DbUtils.getValue(connection,
                        "CB_SUBSCRIBER",
                        "ID",
                        String.format("ISDN='%s' AND STATUS='1'", isdn));
                if (subId == null) {
                    throw new CBException(CBCode.USER_UNKNOWN);
                }

                // Get subscriber information on specific store
                Vector vtData = Database.executeQuery(connection, String.format("SELECT 1 FROM CB_SUB_STORE  " +
                        "WHERE SUB_ID='%s' AND STORE_ID='%s' ", subId, store.getStoreId()));
                if (vtData == null || vtData.size() == 0)
                    throw new CBException(CBCode.USER_UNKNOWN);

                // Get subscriber information on specific store
                Database.executeUpdate(connection, String.format(
                        "INSERT INTO CB_MT_QUEUE(ID, ISDN, STORE_CODE,CONTENT,TRANS_ID) " +
                                "VALUES (SEQ_MT_QUEUE.nextval, '%s', '%s', '%s', '%s')", isdn, store.getStoreCode(), content, messageContext.getTransID()));
                exchange.getOut().setBody("OK");
            }
        } catch (CBException cbse) {
            throw cbse;
        } catch (Exception e) {
            throw new CBException(CBCode.INTERNAL_SERVER_ERROR, e);
        }
    }
}
