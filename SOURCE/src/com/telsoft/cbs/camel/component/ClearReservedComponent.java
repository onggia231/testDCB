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
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Clear charge reserved
 * <p>
 */

@Component("cbs-clear-reserved")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-clear-reserved",
        title = "Clear charge reserved",
        syntax = "cbs-clear-reserved:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)

@Slf4j
public class ClearReservedComponent extends ProcessorComponent {
    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        CBCommand command = (CBCommand) exchange.getProperty(CbsContansts.COMMAND);
        CBStore store = (CBStore) messageContext.getProperty(CbsContansts.STORE);
        Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
        Object reserved = mapFullRequest.get(CbsContansts.RESERVED_CHARGE_SUCCESS);
        // For Charge request
        if (command == CBCommand.CHARGE) {
            if(reserved != null && reserved.equals(true)){
                try (Connection connection = getManager().getConnection()) {
                    CheckTransactionLimitationComponent.rollbackReserved(connection, (Long) mapFullRequest.get(CbsContansts.AMOUNT_FULL_TAX),(String)mapFullRequest.get(CbsContansts.SUB_ID),store);
                    mapFullRequest.remove(CbsContansts.RESERVED_CHARGE_SUCCESS);
                } catch (Exception e) {
                    log.error("Clear reserved failed ", e);
                    CbsLog.error(messageContext, "ClearReservedComponent", CBCode.UNKNOWN, "Clear reserved failed", e.getMessage());
                }
            }

        }

        // Insert into CB_STORE_CHARGED
        if (command == CBCommand.REFUND) {
            try (Connection connection = getManager().getConnection()) {
                String transid = (String) messageContext.getProperty(CbsContansts.PAYMENT_TRANSACTION_ID);
                Date request_date = (Date) messageContext.getProperty(CbsContansts.PAYMENT_DATE);
                String status = messageContext.getStatus();

                if ("0".equals(status)) {
                    SUCCESS:
                    {
                        String sql = "UPDATE CB_STORE_CHARGED SET " +
                                "   REFUNDED = REFUNDED + ?," +
                                "   REVERSED_REFUND = REVERSED_REFUND - ? " +
                                "   WHERE TRANS_ID=? AND REQUEST_TIME >= ? AND REQUEST_TIME < ? + 1";
                        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                            stmt.setLong(1, (Long) mapFullRequest.get(CbsContansts.AMOUNT_FULL_TAX));
                            stmt.setLong(2, (Long) mapFullRequest.get(CbsContansts.AMOUNT_FULL_TAX));
                            stmt.setString(3, transid);
                            stmt.setDate(4, new java.sql.Date(request_date.getTime()));
                            stmt.setDate(5, new java.sql.Date(request_date.getTime()));
                            stmt.executeUpdate();
//                            connection.commit();
                        }
                    }
                } else {
                    FAILED:
                    {
                        String sql = "UPDATE CB_STORE_CHARGED SET " +
                                "   REVERSED_REFUND = REVERSED_REFUND - ? " +
                                "   WHERE TRANS_ID=? AND REQUEST_TIME >= ? AND REQUEST_TIME < ? + 1";
                        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

                            stmt.setLong(1, (Long) mapFullRequest.get(CbsContansts.AMOUNT_FULL_TAX));
                            stmt.setString(2, transid);
                            stmt.setDate(3, new java.sql.Date(request_date.getTime()));
                            stmt.setDate(4, new java.sql.Date(request_date.getTime()));
                            stmt.executeUpdate();
//                            connection.commit();
                        }
                    }
                }

            } catch (Exception e) {
                log.error("Clear reserved failed", e);
                CbsLog.error(messageContext, "ClearReservedComponent", CBCode.UNKNOWN, "Clear reserved failed", e.getMessage());
            }
        }
    }
}
