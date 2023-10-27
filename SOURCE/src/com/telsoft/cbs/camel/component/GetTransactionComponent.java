package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.CBCode;
import com.telsoft.cbs.domain.CBCommand;
import com.telsoft.cbs.domain.CBException;
import com.telsoft.cbs.domain.CBRequest;
import com.telsoft.cbs.utils.CbsLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.annotations.Component;
import org.apache.commons.dbutils.DbUtils;
import telsoft.gateway.core.log.MessageContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * The Get Profile Component
 * <p>
 */

@Slf4j
@Component("cbs-get-transaction")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-get-transaction",
        title = "Get subscriber information",
        syntax = "cbs-get-transaction:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)
public class GetTransactionComponent extends ProcessorComponent {
    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        try {
            try (Connection connection = getManager().getConnection()) {
                String transid = (String) messageContext.getProperty(CbsContansts.PAYMENT_TRANSACTION_ID);
                Date request_date = (Date) messageContext.getProperty(CbsContansts.PAYMENT_DATE);
                CBCommand command = (CBCommand) messageContext.getProperty(CbsContansts.PAYMENT_COMMAND);


                String sql = "SELECT " +
                        "(select ISDN from CB_SUBSCRIBER WHERE CB_SUBSCRIBER.ID = SUB_ID) ISDN," +
                        "(select STORE_CODE from CB_STORE WHERE CB_STORE.ID = STORE_ID) STORE," +
                        "TO_CHAR(REQUEST_TIME,'yyyy/MM/dd HH24:mi:ss'), " +
                        "COMMAND, " +
                        "STATUS," +
                        "CLIENT_TRANS_ID," +
                        "TO_CHAR(RESPONSE_TIME,'yyyy/MM/dd HH24:mi:ss')," +
                        "PRICE," +
                        "REFUNDED" +
                        " FROM CB_STORE_CHARGED  " +
                        "WHERE TRANS_ID=? AND REQUEST_TIME >= ? AND REQUEST_TIME < ? + 1";

                ResultSet rs = null;
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, transid);
                    statement.setDate(2, new java.sql.Date(request_date.getTime()));
                    statement.setDate(3, new java.sql.Date(request_date.getTime()));

                    rs = statement.executeQuery();
                    if (!rs.next())
                        throw new CBException(CBCode.TRANSACTION_NOT_FOUND);

                    Map mp = new HashMap();
                    mp.put("isdn", rs.getString(1));
                    mp.put("store", rs.getString(2));
                    mp.put("request_time", rs.getString(3));
                    mp.put("transaction_id", rs.getString(6));
                    mp.put("response_time", rs.getString(7));
                    mp.put("amount", rs.getString(8));
                    long refunded = rs.getLong(9);
                    long amount = rs.getLong(8);
                    String status = rs.getString(5);

                    if (status.equals("0")) {
                        if (refunded > 0) {
                            if (refunded == amount)
                                mp.put("status", "REFUND");
                            else {
                                mp.put("status", "PARTLY REFUND");
                                mp.put("refunded", String.valueOf(refunded));
                            }
                        } else
                            mp.put("status", "SUCCESS");
                    } else {
                        mp.put("status", "FAILED");
                    }
                    exchange.setProperty(CbsContansts.RESULT_EXTRAS, mp);
                    exchange.getOut().setBody(mp);
                } finally {
                    DbUtils.closeQuietly(rs);
                }
            }
        } catch (CBException cbse) {
            throw cbse;
        } catch (Exception e) {
            CbsLog.error(messageContext, "GetTransactionComponent", CBCode.INTERNAL_SERVER_ERROR, "", e.getMessage());
            throw new CBException(CBCode.INTERNAL_SERVER_ERROR, e);
        }
    }
}
