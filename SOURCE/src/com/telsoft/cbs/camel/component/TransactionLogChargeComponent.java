package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.*;
import com.telsoft.cbs.utils.CbsLog;
import com.telsoft.util.StringUtil;
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
 * The Transaction Log Component write transaction log of charge into local database
 * <p>
 */

@Component("cbs-transaction-charge-log")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-transaction-charge-log",
        title = "Log transaction charge into database",
        syntax = "cbs-transaction-charge-log:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)


public class TransactionLogChargeComponent extends ProcessorComponent{

    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        CBCommand command = (CBCommand) exchange.getProperty(CbsContansts.COMMAND);
        CBStore store = (CBStore) messageContext.getProperty(CbsContansts.STORE);
        Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);


        if(mapFullRequest.get(CbsContansts.REFERENCE_IDEMPOTENT_ID) == null) {
            if (command == CBCommand.CHARGE) {
                Object reserved = mapFullRequest.get(CbsContansts.RESERVED_CHARGE_SUCCESS);
                Long amountFullTax = (Long) mapFullRequest.get(CbsContansts.AMOUNT_FULL_TAX);

                try (Connection connection = getManager().getConnection()) {
                    INSERT_CB_REQUEST_CHARGE:
                    {
                        String sql = "INSERT INTO cb_request_charge (REQUEST_TIME,RESPONSE_TIME,ISDN,STATUS,COMMAND,AMOUNT,\n" +
                                "REQ_CONTENT,RES_CONTENT,ADDRESS,STORE_CODE,TRANSACTION_ID,CLIENT_TRANSACTION_ID,\n" +
                                "STORE_TRANSACTION_ID,REQUEST_ID,RESULT_CODE,CONTENT_ID,AMOUNT_FULL_TAX,CONTENT_DESCRIPTION,\n" +
                                "CHANNEL_TYPE,REFER_IDEMPOTENT_ID, final_result_code, sub_id, store_id, payment_status, refund_info,cps_transaction_id) \n" +
                                "SELECT a.request_time, a.response_time, a.isdn, a.status, a.command,\n" +
                                "       a.amount, a.req_content, a.res_content, a.address, a.store_code,\n" +
                                "       a.transaction_id, a.client_transaction_id,\n" +
                                "       a.store_transaction_id, a.request_id, a.result_code,\n" +
                                "       a.content_id, a.amount_full_tax, a.content_description,\n" +
                                "       a.channel_type, a.refer_idempotent_id, a.final_result_code,?,?,?,?,?\n" +
                                "  FROM cb_request a WHERE a.REQUEST_TIME = ? AND a.TRANSACTION_ID = ?";
                        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                            stmt.setString(1, (String) exchange.getProperty(CbsContansts.SUB_ID));
                            stmt.setString(2, (String) store.getStoreId());
                            stmt.setInt(3, 0);
                            stmt.setString(4, StringUtil.nvl(mapFullRequest.get(CbsContansts.CPS_R_REFUND_INFORMATION), ""));
                            stmt.setString(5, StringUtil.nvl(mapFullRequest.get(CbsContansts.CPS_CONTENTID), ""));
                            stmt.setTimestamp(6, new Timestamp(messageContext.getClientRequestDate().getTime()));
                            stmt.setString(7, messageContext.getTransID());
                            stmt.executeUpdate();
//                            connection.commit();
                        }
                    }


                } catch (Exception e) {
                    log.error("Store charge request failed for transaction_id: " + messageContext.getTransID(), e);
                    CbsLog.error(messageContext, "TransactionLogChargeComponent.INSERT_CB_REQUEST_CHARGE", CBCode.UNKNOWN, "", e.getMessage());
                }

            }
            if (command == CBCommand.REFUND || command == CBCommand.REVERSE) {
//                Object reserved = mapFullRequest.get(CbsContansts.RESERVED_CHARGE_SUCCESS);
//                Long amountFullTax = (Long) mapFullRequest.get(CbsContansts.AMOUNT_FULL_TAX);

                try (Connection connection = getManager().getConnection()) {
                    INSERT_CB_REQUEST_REFUND:
                    {
                        String sql = "INSERT INTO cb_request_refund (REQUEST_TIME,RESPONSE_TIME,ISDN,STATUS,COMMAND,AMOUNT,\n" +
                                "REQ_CONTENT,RES_CONTENT,ADDRESS,STORE_CODE,TRANSACTION_ID,CLIENT_TRANSACTION_ID,\n" +
                                "STORE_TRANSACTION_ID,REQUEST_ID,RESULT_CODE,CONTENT_ID,AMOUNT_FULL_TAX,CONTENT_DESCRIPTION,\n" +
                                "CHANNEL_TYPE,REFER_IDEMPOTENT_ID,REFER_TRANSACTION_ID, final_result_code, " +
                                " sub_id, store_id, payment_status, refund_info,cps_transaction_id,refund_reason) \n" +
                                "SELECT a.request_time, a.response_time, a.isdn, a.status, a.command,\n" +
                                "       a.amount, a.req_content, a.res_content, a.address, a.store_code,\n" +
                                "       a.transaction_id, a.client_transaction_id,\n" +
                                "       a.store_transaction_id, a.request_id, a.result_code,\n" +
                                "       a.content_id, a.amount_full_tax, a.content_description,\n" +
                                "       a.channel_type, a.refer_idempotent_id, a.REFER_TRANSACTION_ID, a.final_result_code,?,?,?,?,?,?\n" +
                                "  FROM cb_request a WHERE a.REQUEST_TIME = ? AND a.TRANSACTION_ID = ?";
                        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                            stmt.setString(1, (String) exchange.getProperty(CbsContansts.SUB_ID));
                            stmt.setString(2, (String) store.getStoreId());
                            stmt.setInt(3, 0);
                            stmt.setString(4, StringUtil.nvl(mapFullRequest.get(CbsContansts.CPS_R_REFUND_INFORMATION), ""));
                            stmt.setString(5, StringUtil.nvl(mapFullRequest.get(CbsContansts.CPS_CONTENTID), ""));
                            stmt.setString(6, StringUtil.nvl(mapFullRequest.get(CbsContansts.REFUND_REASON), ""));
                            stmt.setTimestamp(7, new Timestamp(messageContext.getClientRequestDate().getTime()));
                            stmt.setString(8, messageContext.getTransID());
                            stmt.executeUpdate();
//                            connection.commit();
                        }
                    }


                } catch (Exception e) {
                    log.error("Store charge request failed for transaction_id: " + messageContext.getTransID(), e);
                    CbsLog.error(messageContext, "TransactionLogChargeComponent.INSERT_CB_REQUEST_REFUND", CBCode.UNKNOWN, "", e.getMessage());
                }

            }
        }
    }


}
