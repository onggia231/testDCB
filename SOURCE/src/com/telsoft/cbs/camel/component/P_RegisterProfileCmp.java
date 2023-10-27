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

/**
 * The Register Profile Component
 * <p>
 */

@Slf4j
@Component("cbs-register-profile")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-register-profile",
        title = "Register subscriber information in local database",
        syntax = "cbs-register-profile:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)

public class P_RegisterProfileCmp extends ProcessorComponent {
    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        try {
            log.debug("[{}]Get connection", messageContext.getIsdn());
            try (Connection connection = getManager().getConnection()) {

                /**
                 * Get Store ID, default limitations from CB_STORE
                 * {@link CheckStoreComponent#process(CBRequest, Exchange, Map, MessageContext)}
                 */

                log.debug("[{}]Get store", messageContext.getIsdn());
                CBStore store = (CBStore) messageContext.getProperty(CbsContansts.STORE);
                if (store == null) {
                    throw new CBException(CBCode.STORE_UNKNOWN);
                }

                // Get sub_id from CB_SUBSCRIBER
                // if sub_id is null, add new subscriber into CB_SUBSCRIBER
                log.debug("[{}]Get subscriber from CB_SUBSCRIBER", messageContext.getIsdn());
                String isdn = messageContext.getIsdn();
                String subId = DbUtils.getValue(connection,
                        "CB_SUBSCRIBER",
                        "ID",
                        String.format("ISDN='%s' AND STATUS='1'", isdn));

                boolean save = connection.getAutoCommit();
                try {
                    connection.setAutoCommit(false);
                    if (subId == null) {
                        log.debug("[{}]Get SEQ_SUBSCRIBER", messageContext.getIsdn());
                        subId = Database.getSequenceValue(connection, "SEQ_SUBSCRIBER");

                        log.debug("[{}]INSERT INTO CB_SUBSCRIBER", messageContext.getIsdn());
                        Database.executeUpdate(connection, String.format("INSERT INTO CB_SUBSCRIBER (ID,ISDN,REG_DATE,SUB_TYPE,STATUS) " +
                                "VALUES ('%s','%s',sysdate,null,'1')", subId, isdn));
                    }
                    // insert SUB_ID, STORE_ID, default limitations,reg_date into CB_SUB_STORE
                    try {
                        log.debug("[{}]Get Subscriber in CB_SUB_STORE", messageContext.getIsdn());
                        String registered = DbUtils.getValue(connection,
                                "CB_SUB_STORE",
                                "1",
                                String.format("SUB_ID='%s' AND STORE_ID='%s'", subId, store.getStoreId()));

                        if (registered == null) {
                            log.debug("[{}]Insert CB_SUB_STORE", messageContext.getIsdn());
                            Database.executeUpdate(connection, String.format("INSERT INTO CB_SUB_STORE (SUB_ID,STORE_ID,YEARLY_LIMIT, MONTHLY_LIMIT, WEEKLY_LIMIT, DAILY_LIMIT, TRANS_LIMIT, REG_DATE) " +
                                    "VALUES ('%s','%s',%d,%d,%d,%d,%d,sysdate)", subId, store.getStoreId(), store.getYearlyLimits(), store.getMonthlyLimits(), store.getWeeklyLimits(), store.getDailyLimits(), store.getTransactionLimits()));
                        } else {
                            log.debug("[{}]User existed", messageContext.getIsdn());
                            throw new CBException(CBCode.USER_EXISTED);
                        }
                        connection.commit();
                        log.debug("[{}]OK", messageContext.getIsdn());
                        exchange.getOut().setBody("OK");
                    } catch (Exception ex) {
                        connection.rollback();
                        throw ex;
                    }
                } finally {
                    connection.setAutoCommit(save);
                }
            }
        } catch (CBException cbse) {
            throw cbse;
        } catch (Exception e) {
            throw new CBException(CBCode.INTERNAL_SERVER_ERROR, e);
        }
    }
}
