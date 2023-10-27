package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.*;
import com.telsoft.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.annotations.Component;
import telsoft.gateway.core.log.MessageContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

/**
 * Set refund reserved
 * <p>
 */

@Component("cbs-set-refund-reserved")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-set-refund-reserved",
        title = "Set refund reserved",
        syntax = "cbs-set-refund-reserved:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)

@Slf4j
public class SetReservedComponent extends ProcessorComponent {
    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        CBCommand command = (CBCommand) exchange.getProperty(CbsContansts.COMMAND);
        CBStore store = (CBStore) messageContext.getProperty(CbsContansts.STORE);

        // For Charge request
        // Insert into CB_STORE_CHARGED
        if (command == CBCommand.REFUND) {
            try (Connection connection = getManager().getConnection()) {
                String transid = (String) messageContext.getProperty(CbsContansts.PAYMENT_TRANSACTION_ID);
                Date request_date = (Date) messageContext.getProperty(CbsContansts.PAYMENT_DATE);

                String sql = "UPDATE CB_STORE_CHARGED SET " +
                        "   REVERSED_REFUND = REVERSED_REFUND + ? " +
                        "   WHERE TRANS_ID=? AND REQUEST_TIME >= ? AND REQUEST_TIME < ? + 1";


                try (PreparedStatement stmt = connection.prepareStatement(sql)) {

                    stmt.setLong(1, messageContext.getAmount());
                    stmt.setString(2, transid);
                    stmt.setDate(3, new java.sql.Date(request_date.getTime()));
                    stmt.setDate(4, new java.sql.Date(request_date.getTime()));
                    int i = stmt.executeUpdate();
//                    connection.commit();
                    if (i == 0) {
                        throw new CBException(CBCode.TRANSACTION_NOT_FOUND);
                    }
                }

                try {
                    // check refund
                    sql = "SELECT PRICE, REFUNDED, REVERSED_REFUND, RESPONSE_TIME FROM CB_STORE_CHARGED " +
                            "   WHERE TRANS_ID=? AND REQUEST_TIME >= ? AND REQUEST_TIME < ? + 1";
                    PreparedStatement stmt = connection.prepareStatement(sql);

                    stmt.setString(1, transid);
                    stmt.setDate(2, new java.sql.Date(request_date.getTime()));
                    stmt.setDate(3, new java.sql.Date(request_date.getTime()));
                    ResultSet rs = stmt.executeQuery();

                    if (!rs.next()) {
                        throw new CBException(CBCode.TRANSACTION_NOT_FOUND);
                    }

                    long chargedAmount = rs.getLong(1);
                    long refundedAmount = rs.getLong(2);
                    long reversedAmount = rs.getLong(3);
                    Timestamp responseTime = rs.getTimestamp(4);

                    if (chargedAmount <= 0) {
                        throw new CBException(CBCode.REFUND_AMOUNT_EXCEEDS_ORIGINAL);
                    }

                    if (refundedAmount + reversedAmount > chargedAmount) {
                        throw new CBException(CBCode.REFUND_AMOUNT_EXCEEDS_ORIGINAL);
                    }

                    Date now = new Date();
                    Date responseDate = new Date(responseTime.getTime());
                    if (DateUtil.addDay(responseDate, 180).compareTo(now) < 0) {
                        throw new CBException(CBCode.REFUND_WINDOW_EXPIRED);
                    }
                } catch (Exception ex) {
                    sql = "UPDATE CB_STORE_CHARGED SET " +
                            "   REVERSED_REFUND = REVERSED_REFUND - ? " +
                            "   WHERE TRANS_ID=? AND REQUEST_TIME >= ? AND REQUEST_TIME < ? + 1";
                    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                        stmt.setLong(1, messageContext.getAmount());
                        stmt.setString(2, transid);
                        stmt.setDate(3, new java.sql.Date(request_date.getTime()));
                        stmt.setDate(4, new java.sql.Date(request_date.getTime()));
                        stmt.executeUpdate();
//                        connection.commit();
                    }
                    throw ex;
                }
            } catch (CBException cbe) {
                throw cbe;
            } catch (Exception e) {
                throw new CBException(CBCode.INTERNAL_SERVER_ERROR);
            }
        }
    }
}
