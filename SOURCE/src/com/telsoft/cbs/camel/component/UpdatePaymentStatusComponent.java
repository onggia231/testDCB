package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.*;
import com.telsoft.cbs.utils.CbsLog;
import com.telsoft.cbs.utils.CbsUtils;
import com.telsoft.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.annotations.Component;
import telsoft.gateway.core.log.MessageContext;

import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Update payment status
 * <p>
 */

@Slf4j
@Component("cbs-update-payment-status")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-update-payment-status",
        title = "Update payment status",
        syntax = "cbs-update-payment-status:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)

public class UpdatePaymentStatusComponent extends ProcessorComponent {
    @UriParam(name = "newPaymentStatus", displayName = "NewPaymentStatus", description = "New Payment Status")
    PaymentStatus newPaymentStatus;

    @UriParam(name = "currentPaymentStatus", displayName = "CurrentPaymentStatus", description = "New Payment Status")
    PaymentStatus currentPaymentStatus;

    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
//        int newPaymentStatus = Integer.parseInt(StringUtil.nvl((String) parameters.get("newPaymentStatus"), "0"));
        PaymentStatus newPaymentStatus = PaymentStatus.valueOf((String) parameters.get("newPaymentStatus"));
//        int currentPaymentStatus = Integer.parseInt(StringUtil.nvl((String) parameters.get("currentPaymentStatus"), "0"));
        PaymentStatus currentPaymentStatus = PaymentStatus.valueOf((String) parameters.get("currentPaymentStatus"));

        CBCommand command = (CBCommand) exchange.getProperty(CbsContansts.COMMAND);
        CBStore store = (CBStore) messageContext.getProperty(CbsContansts.STORE);
        Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
        String storeTransactionID = (String) mapFullRequest.get(CbsContansts.STORE_TRANSACTION_ID);

        String sql = "UPDATE cb_request_charge SET payment_status = ? " +
                " WHERE request_time = ? and transaction_id = ? and payment_status = ?";

        try (Connection connection = getManager().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, newPaymentStatus.getCode());
            stmt.setTimestamp(2, (Timestamp) mapFullRequest.get(CbsContansts.PAYMENT_REQUEST_TIME));
            stmt.setString(3, (String) mapFullRequest.get(CbsContansts.PAYMENT_TRANSACTION_ID));
            stmt.setInt(4, currentPaymentStatus.getCode());
            int iResult = stmt.executeUpdate();
            if(iResult >= 0){
                exchange.getIn().setBody("UPDATE_SUCCESS");
                //payment_status
                messageContext.setProperty(CbsContansts.PAYMENT_STATUS, newPaymentStatus.getCode());
                exchange.setProperty(CbsContansts.PAYMENT_STATUS, newPaymentStatus.getCode());
                CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.PAYMENT_STATUS, newPaymentStatus.getCode());
            }else{
                String current_payment_status = getCurrentPaymentStatus(connection,
                        (Timestamp) mapFullRequest.get(CbsContansts.PAYMENT_REQUEST_TIME),
                        (String) mapFullRequest.get(CbsContansts.PAYMENT_TRANSACTION_ID));
                exchange.getIn().setBody("UPDATE_FAILURE=" + current_payment_status);
                //payment_status
                messageContext.setProperty(CbsContansts.PAYMENT_STATUS, current_payment_status);
                exchange.setProperty(CbsContansts.PAYMENT_STATUS, current_payment_status);
                CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.PAYMENT_STATUS, current_payment_status);
            }

        } catch (CBException cbe) {
            throw cbe;
        } catch (Exception e) {
            log.error("Error when UpdatePaymentStatus", e);
            CbsLog.error(messageContext, "UpdatePaymentStatus", CBCode.INTERNAL_SERVER_ERROR, "", e.getMessage());
            throw new CBException(CBCode.INTERNAL_SERVER_ERROR);
        }
    }

    private String getCurrentPaymentStatus(Connection connection, Timestamp requestTime, String transactionId) throws Exception {
        String result = null;
        String sql = "SELECT payment_status FROM cb_request_charge WHERE request_time = ? and transaction_id = ? ";
        try(PreparedStatement stmt = connection.prepareStatement(sql)){
            stmt.setTimestamp(1,requestTime);
            stmt.setString(1,transactionId);
            try(ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    result = rs.getString("payment_status");
                }
            }
        } catch (SQLException e) {
            log.error("Can't get payment status",e);
            throw e;
        }
        return result;
    }
}
