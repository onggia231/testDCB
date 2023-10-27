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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Get Refund information Component
 * <p>
 */

@Slf4j
@Component("cbs-get-refund-info")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-get-refund-info",
        title = "Get subscriber information",
        syntax = "cbs-get-refund-info:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)

public class GetRefundInfoComponent extends ProcessorComponent {
    @UriParam(name = "scanTime", displayName = "ScanTime", description = "Scan time in hours from refund time")
    int scanTime;


    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        int scanTime = Integer.parseInt(StringUtil.nvl((String) parameters.get("scanTime"), "0"));


        CBCommand command = (CBCommand) exchange.getProperty(CbsContansts.COMMAND);
        CBStore store = (CBStore) messageContext.getProperty(CbsContansts.STORE);
        Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
        String storeTransactionID = (String) mapFullRequest.get(CbsContansts.STORE_TRANSACTION_ID);

        String sql = "SELECT * from (" +
                "SELECT a.request_time, a.response_time, a.isdn, a.status, a.command,\n" +
                "       a.amount, a.req_content, a.res_content, a.address, a.store_code,\n" +
                "       a.transaction_id, a.client_transaction_id,\n" +
                "       a.store_transaction_id, a.request_id, a.result_code,\n" +
                "       a.content_id, a.amount_full_tax, a.content_description,\n" +
                "       a.channel_type, a.refer_idempotent_id, a.sub_id, a.store_id,\n" +
                "       a.payment_status, a.refund_info, \n" +
                "       row_number() over (order by request_time desc) as rn " +
                "  FROM cb_request_refund a \n" +
                "  where a.request_time >= TO_TIMESTAMP(?, 'YYYYMMDDHH24miss') \n" +
                "    and a.request_time <= TO_TIMESTAMP(?, 'YYYYMMDDHH24miss')  + " + scanTime + "/24\n" +
                "    and a.store_transaction_id = ? \n" +
                "    and a.store_code = ? " +
                ") where rn = 1";

        try (Connection connection = getManager().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            String strPaymentDate = new SimpleDateFormat(CbsContansts.PURCHASE_TIME_FORMAT).format((Date) messageContext.getProperty(CbsContansts.PAYMENT_DATE));
            stmt.setString(1, strPaymentDate);
            stmt.setString(2, strPaymentDate);
            stmt.setString(3, storeTransactionID);
            stmt.setString(4, store.getStoreCode());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp payment_request_time = rs.getTimestamp("request_time");
                    String msisdn = rs.getString("isdn");
                    String status = rs.getString("status");
                    String payment_command = rs.getString("command");
                    String paymentAmount = StringUtil.nvl(rs.getString("amount"),"");
                    String paymentTransId = rs.getString("transaction_id");
                    String result_code = rs.getString("result_code");
                    Long amount_full_tax = rs.getLong("amount_full_tax");
                    String content_description = rs.getString("content_description");
                    String sub_id = rs.getString("sub_id");
                    String payment_status = rs.getString("payment_status");
                    String refund_info = rs.getString("refund_info");

                    //msisdn
                    exchange.setProperty(CbsContansts.MSISDN, msisdn);
                    messageContext.setIsdn(msisdn);
                    CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.MSISDN, msisdn);

                    //amount
                    if(mapFullRequest.get(CbsContansts.AMOUNT) == null) {
                        exchange.setProperty(CbsContansts.AMOUNT, paymentAmount);
                        messageContext.setAmount(Long.parseLong(paymentAmount));
                        CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.AMOUNT, paymentAmount);

                        exchange.setProperty(CbsContansts.AMOUNT_FULL_TAX, amount_full_tax);
                        CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.AMOUNT_FULL_TAX, amount_full_tax);
                    }

                    exchange.setProperty(CbsContansts.PAYMENT_AMOUNT, paymentAmount);
                    messageContext.setProperty(CbsContansts.PAYMENT_AMOUNT,Long.parseLong(paymentAmount));
                    CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.PAYMENT_AMOUNT, paymentAmount);

                    exchange.setProperty(CbsContansts.PAYMENT_AMOUNT_FULL_TAX, amount_full_tax);
                    messageContext.setProperty(CbsContansts.PAYMENT_AMOUNT_FULL_TAX,amount_full_tax);
                    CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.PAYMENT_AMOUNT, amount_full_tax);

                    //paymentTransId
                    exchange.setProperty(CbsContansts.PAYMENT_TRANSACTION_ID, paymentTransId);
                    CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.PAYMENT_TRANSACTION_ID, paymentTransId);

                    //refund_info
                    exchange.setProperty(CbsContansts.CPS_R_REFUND_INFORMATION, refund_info);
                    CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.CPS_R_REFUND_INFORMATION, refund_info);

                    //sub_id
                    messageContext.setProperty(CbsContansts.SUB_ID, sub_id);
                    exchange.setProperty(CbsContansts.SUB_ID, sub_id);
                    CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.SUB_ID, sub_id);

                    //result_code
                    messageContext.setProperty(CbsContansts.PAYMENT_RESULT_CODE, result_code);
                    exchange.setProperty(CbsContansts.PAYMENT_RESULT_CODE, result_code);
                    CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.PAYMENT_RESULT_CODE, result_code);

                    //status
                    messageContext.setProperty(CbsContansts.PAYMENT_REQUEST_STATUS, status);
                    exchange.setProperty(CbsContansts.PAYMENT_REQUEST_STATUS, status);
                    CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.PAYMENT_REQUEST_STATUS, status);

                    //payment_status
                    messageContext.setProperty(CbsContansts.PAYMENT_STATUS, payment_status);
                    exchange.setProperty(CbsContansts.PAYMENT_STATUS, payment_status);
                    CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.PAYMENT_STATUS, payment_status);

                    //payment_command
                    messageContext.setProperty(CbsContansts.PAYMENT_COMMAND, payment_command);
                    exchange.setProperty(CbsContansts.PAYMENT_COMMAND, payment_command);
                    CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.PAYMENT_COMMAND, payment_command);

                    //content_description
                    messageContext.setProperty(CbsContansts.CONTENT_DESCRIPTION, content_description);
                    exchange.setProperty(CbsContansts.CONTENT_DESCRIPTION, content_description);
                    CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.CONTENT_DESCRIPTION, content_description);


                    messageContext.setProperty(CbsContansts.PAYMENT_REQUEST_TIME, payment_request_time);
                    exchange.setProperty(CbsContansts.PAYMENT_REQUEST_TIME, payment_request_time);
                    CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.PAYMENT_REQUEST_TIME, payment_request_time);

                    //check charge/reverse window expire
                    Timestamp currentTime = new Timestamp(messageContext.getClientRequestDate().getTime());
                    Long deltaHours = CbsUtils.compareTwoTimeStampsByHours(currentTime, payment_request_time);

                    messageContext.setProperty(CbsContansts.PAYMENT_DELTA_HOURS, deltaHours);
                    exchange.setProperty(CbsContansts.PAYMENT_DELTA_HOURS, deltaHours);
                    CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.PAYMENT_DELTA_HOURS, deltaHours);


                } else {
                    throw new CBException(CBCode.TRANSACTION_NOT_FOUND);
                }
            }
        } catch (CBException cbe) {
            throw cbe;
        } catch (Exception e) {
            CbsLog.error(messageContext, "GetRefundInfoComponent", CBCode.INTERNAL_SERVER_ERROR, "", e.getMessage());
            throw new CBException(CBCode.INTERNAL_SERVER_ERROR,e);
        }
    }
}
