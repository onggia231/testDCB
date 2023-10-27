package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.*;
import com.telsoft.cbs.utils.CbsLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.annotations.Component;
import telsoft.gateway.core.log.MessageContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Transaction Log Component write transaction log into local database
 * <p>
 */

@Component("cbs-transaction-log")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-transaction-log",
        title = "Log subscriber transaction",
        syntax = "cbs-transaction-log:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)

@Slf4j
public class TransactionLogComponent extends ProcessorComponent {
    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        CBCommand command = (CBCommand) exchange.getProperty(CbsContansts.COMMAND);
        CBStore store = (CBStore) messageContext.getProperty(CbsContansts.STORE);
        Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
        Object reserved = mapFullRequest.get(CbsContansts.RESERVED_CHARGE_SUCCESS);
        Long amountFullTax = (Long) mapFullRequest.get(CbsContansts.AMOUNT_FULL_TAX);
        // For Charge request
        // Insert into CB_STORE_CHARGED
        if (command == CBCommand.CHARGE) {
            try (Connection connection = getManager().getConnection()) {
                INSERT_CB_STORE_CHARGED:
                {
                    String sql = "INSERT INTO CB_STORE_CHARGED (SUB_ID, STORE_ID, TRANS_ID, REQUEST_TIME, COMMAND, STATUS, REQUEST, RESPONSE,\n" +
                            "                              CLIENT_TRANS_ID, RESPONSE_TIME, PRICE)\n" +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement stmt = connection.prepareStatement(sql)) {

                        stmt.setString(1, (String) exchange.getProperty(CbsContansts.SUB_ID));
                        stmt.setString(2, (String) store.getStoreId());
                        stmt.setString(3, messageContext.getTransID());
                        stmt.setTimestamp(4, new Timestamp(messageContext.getClientRequestDate().getTime()));
                        stmt.setString(5, command.name());
                        stmt.setString(6, messageContext.getStatus());
                        stmt.setString(7, messageContext.getRequest().getContent());
                        stmt.setString(8, messageContext.getClientResponse().getContent());
                        stmt.setString(9, (String) messageContext.getOrigin());
                        stmt.setTimestamp(10, new Timestamp(messageContext.getClientResponseDate().getTime()));
                        stmt.setLong(11, amountFullTax);
                        stmt.executeUpdate();
//                        connection.commit();
                    }
                }

                UPDATE_CB_SUB_STORE:
                {
                    String sql = "UPDATE CB_SUB_STORE SET " +
                            "   TOTAL_YEAR = TOTAL_YEAR + ?," +
                            "   TOTAL_MONTH = TOTAL_MONTH + ?," +
                            "   TOTAL_WEEK = TOTAL_WEEK + ?," +
                            "   TOTAL_DAY = TOTAL_DAY + ?, " +
                            "   RESERVED_CHARGE = RESERVED_CHARGE - ? " +
                            "   WHERE SUB_ID = ? AND STORE_ID = ?";
                    try (PreparedStatement stmt = connection.prepareStatement(sql)) {

                        stmt.setLong(1, amountFullTax);
                        stmt.setLong(2, amountFullTax);
                        stmt.setLong(3, amountFullTax);
                        stmt.setLong(4, amountFullTax);

                        if(reserved != null && reserved.equals(true)){
                            stmt.setLong(5, amountFullTax);
                        }else{
                            stmt.setLong(5, 0);
                        }
                        stmt.setString(6, (String) exchange.getProperty(CbsContansts.SUB_ID));
                        stmt.setString(7, store.getStoreId());
                        stmt.executeUpdate();
//                        connection.commit();
                        mapFullRequest.remove(CbsContansts.RESERVED_CHARGE_SUCCESS);
                    }
                }

            } catch (Exception e) {
                log.error("Store charge request failed", e);
                CbsLog.error(messageContext, "TransactionLogComponent", CBCode.UNKNOWN, "", e.getMessage());
            }
        }


//        // for Refund request
//        // increase refundED value to CB_STORE_CHARGED, deduct REVERSED_REFUND
//        if (command == CBCommand.REFUND) {
//            try (Connection connection = getManager().getConnection()) {
//                String sql = "UPDATE CB_STORE_CHARGED SET REFUNDED = REFUNDED + ?, REVERSED_REFUND = REVERSED_REFUND - ?" +
//                        " WHERE TRANS_ID = ? AND STORE_ID = ?";
//                PreparedStatement stmt = connection.prepareStatement(sql);
//                stmt.setLong(1, amountFullTax);
//                stmt.setLong(2, amountFullTax);
//                stmt.setString(3, (String) messageContext.getProperty(CbsContansts.PAYMENT_TRANSACTION_ID));
//                stmt.setString(4, store.getStoreId());
//                stmt.executeUpdate();
//                connection.commit();
//            } catch (Exception e) {
//                log.error("Store refund request failed", e);
//            }
//        }
    }
}
