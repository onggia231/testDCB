package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.*;
import com.telsoft.cbs.utils.CbsLog;
import com.telsoft.cbs.utils.CbsUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.annotations.Component;
import telsoft.gateway.core.log.MessageContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Update total spent into database
 * <p>
 */

@Slf4j
@Component("cbs-update-total-spent")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-update-total-spent",
        title = "Update total spent into database",
        syntax = "cbs-update-total-spent:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)

public class UpdateTotalSpentComponent extends ProcessorComponent {
    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        CBCommand command = (CBCommand) exchange.getProperty(CbsContansts.COMMAND);
        CBStore store = (CBStore) messageContext.getProperty(CbsContansts.STORE);
        Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);

        //20210817 - fix not update when timeout
        CBResponse flow_response = (CBResponse) messageContext.getClientResponse();
//        if (mapFullRequest.get(CbsContansts.REFERENCE_IDEMPOTENT_ID) == null && MessageContext.STATUS_SUCCESS.equals(messageContext.getStatus())) {
        if (mapFullRequest.get(CbsContansts.REFERENCE_IDEMPOTENT_ID) == null && flow_response != null && CBCode.OK == flow_response.getCode()) {
            if (command == CBCommand.CHARGE) {
                Object reserved = mapFullRequest.get(CbsContansts.RESERVED_CHARGE_SUCCESS);
                Long amountFullTax = (Long) mapFullRequest.get(CbsContansts.AMOUNT_FULL_TAX);

                try (Connection connection = getManager().getConnection()) {

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

                            if (reserved != null && reserved.equals(true)) {
                                stmt.setLong(5, amountFullTax);
                            } else {
                                stmt.setLong(5, 0);
                            }
                            stmt.setString(6, (String) exchange.getProperty(CbsContansts.SUB_ID));
                            stmt.setString(7, store.getStoreId());
                            stmt.executeUpdate();
//                            connection.commit();
                            mapFullRequest.remove(CbsContansts.RESERVED_CHARGE_SUCCESS);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error when UpdateTotalSpentComponent for charge", e);
                    CbsLog.error(messageContext, "UpdateTotalSpentComponent.UPDATE_CB_SUB_STORE", CBCode.INTERNAL_SERVER_ERROR, "", e.getMessage());
                }

            }
            if (command == CBCommand.REVERSE || command == CBCommand.REFUND) {

                Long amountFullTax = (Long) mapFullRequest.get(CbsContansts.AMOUNT_FULL_TAX);

                Date currentDate = new Date();
                Date firstDateOfDayCurrent = CbsUtils.getFirstDateOf(currentDate, Calendar.HOUR_OF_DAY);
                Date firstDateOfWeekCurrent = CbsUtils.getFirstDateOf(currentDate,Calendar.DAY_OF_WEEK);
                Date firstDateOfMonthCurrent = CbsUtils.getFirstDateOf(currentDate,Calendar.DAY_OF_MONTH);
                Date firstDateOfYearCurrent = CbsUtils.getFirstDateOf(currentDate,Calendar.DAY_OF_YEAR);

                Timestamp paymentDate = (Timestamp) exchange.getProperty(CbsContansts.PAYMENT_REQUEST_TIME);
                Date firstDateOfDay = CbsUtils.getFirstDateOf(paymentDate,Calendar.HOUR_OF_DAY);
                Date firstDateOfWeek = CbsUtils.getFirstDateOf(paymentDate,Calendar.DAY_OF_WEEK);
                Date firstDateOfMonth = CbsUtils.getFirstDateOf(paymentDate,Calendar.DAY_OF_MONTH);
                Date firstDateOfYear = CbsUtils.getFirstDateOf(paymentDate,Calendar.DAY_OF_YEAR);

                try (Connection connection = getManager().getConnection()) {
                    CLEAR_CB_SUB_STORE:
                    {
                        String sql = "UPDATE CB_SUB_STORE SET " +
                                "   TOTAL_YEAR = TOTAL_YEAR - ?," +
                                "   TOTAL_MONTH = TOTAL_MONTH - ?," +
                                "   TOTAL_WEEK = TOTAL_WEEK - ?," +
                                "   TOTAL_DAY = TOTAL_DAY - ? " +
                                "   WHERE SUB_ID = ? AND STORE_ID = ?";
                        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                            boolean change = false;
                            if(paymentDate != null && firstDateOfYearCurrent.compareTo(firstDateOfYear) == 0) {
                                stmt.setLong(1, amountFullTax);
                                change = true;
                            }else{
                                stmt.setLong(1, 0);
                            }
                            if(paymentDate != null && firstDateOfMonthCurrent.compareTo(firstDateOfMonth) == 0) {
                                stmt.setLong(2, amountFullTax);
                                change = true;
                            }else{
                                stmt.setLong(2, 0);
                            }
                            if(paymentDate != null && firstDateOfWeekCurrent.compareTo(firstDateOfWeek) == 0) {
                                stmt.setLong(3, amountFullTax);
                                change = true;
                            }else{
                                stmt.setLong(3, 0);
                            }
                            if(paymentDate != null && firstDateOfDayCurrent.compareTo(firstDateOfDay) == 0) {
                                stmt.setLong(4, amountFullTax);
                                change = true;
                            }else{
                                stmt.setLong(4, 0);
                            }

                            stmt.setString(5, (String) exchange.getProperty(CbsContansts.SUB_ID));
                            stmt.setString(6, store.getStoreId());

                            if(change) {
                                stmt.executeUpdate();
//                                connection.commit();
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Error when UpdateTotalSpentComponent for refund", e);
                    CbsLog.error(messageContext, "UpdateTotalSpentComponent.CLEAR_CB_SUB_STORE", CBCode.INTERNAL_SERVER_ERROR, "", e.getMessage());
                }

            }
        }
    }
}
