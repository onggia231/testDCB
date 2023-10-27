package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.utils.CbsLog;
import com.telsoft.cbs.utils.DbUtils;
import com.telsoft.cbs.domain.*;
import com.telsoft.database.Database;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.annotations.Component;
import telsoft.gateway.core.log.MessageContext;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * The Get Profile Component
 * <p>
 */

@Slf4j
@Component("cbs-get-profile")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-get-profile",
        title = "Get subscriber information",
        syntax = "cbs-get-profile:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)

public class GetProfileComponent extends ProcessorComponent {
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
                String subStatus = DbUtils.getValue(connection,
                        "CB_SUBSCRIBER",
                        "vasgate_status",
                        String.format("ISDN='%s' AND STATUS='1'", isdn));
                if (subStatus == null) {
                    subStatus = "";
//                    throw new CBException(CBCode.USER_UNKNOWN);
                }

                /*// Get subscriber information on specific store
                Vector vtData = Database.executeQuery(connection, String.format("SELECT SUB_ID,STORE_ID,YEARLY_LIMIT, " +
                        "MONTHLY_LIMIT, WEEKLY_LIMIT, DAILY_LIMIT, TRANS_LIMIT, TO_CHAR(REG_DATE,'yyyy/mm/dd HH24:mi:ss') FROM CB_SUB_STORE  " +
                        "WHERE SUB_ID='%s' AND STORE_ID='%s' ", subId, store.getStoreId()));
                if (vtData == null || vtData.size() == 0)
                    throw new CBException(CBCode.USER_UNKNOWN);

                Vector row = (Vector) vtData.get(0);
                Map mp = new HashMap();
                mp.put("yearlyLimit", row.get(2));
                mp.put("monthLyLimit", row.get(3));
                mp.put("weeklyLimit", row.get(4));
                mp.put("dailyLimit", row.get(5));
                mp.put("transactionLimit", row.get(6));
                mp.put("reg_date", row.get(7));

                exchange.setProperty(CbsContansts.RESULT_EXTRAS, mp);*/

                String accountStatus = "active";
                if("1".equals(subStatus) || "2".equals(subStatus)){
                    accountStatus = "invalid";
                }
                if("5".equals(subStatus)){
                    accountStatus = "inactive";
                }
                Map mp = new HashMap();
                mp.put("account-status", accountStatus);
                exchange.setProperty(CbsContansts.RESULT_EXTRAS, mp);
                exchange.setProperty("account-status", accountStatus);
            }
        } catch (CBException cbse) {
            throw cbse;
        } catch (Exception e) {
            CbsLog.error(messageContext, "GetProfileComponent", CBCode.INTERNAL_SERVER_ERROR, "", e.getMessage());
            throw new CBException(CBCode.INTERNAL_SERVER_ERROR, e);
        }
    }
}
