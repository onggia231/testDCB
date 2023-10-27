package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.*;
import com.telsoft.cbs.utils.CbsLog;
import com.telsoft.util.StringUtil;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.annotations.Component;
import org.apache.commons.lang3.StringUtils;
import telsoft.gateway.core.log.MessageContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Transaction Log Component write transaction log into local database with ref payment
 * <p>
 */

@Component("cbs-request-update-with-ref-payment")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-request-update-with-ref-payment",
        title = "Update status, response of a request with ref payment",
        syntax = "cbs-request-update-with-ref-payment:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)


public class R_RequestUpdateWithReferPaymentCmp extends ProcessorComponent {

    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
        String refer_idempotent_id = (String) mapFullRequest.get(CbsContansts.REFERENCE_IDEMPOTENT_ID);
        // Update CB_REQUEST
        if (mapFullRequest.get(CbsContansts.FLOW_STATUS) != null || mapFullRequest.get(CbsContansts.FLOW_STATUS) == CBCode.PROCESS_TIMEOUT) {

            try (Connection connection = getManager().getConnection()) {
                CBResponse response = (CBResponse) mapFullRequest.get(CbsContansts.TIMEOUT_RESPONSE);
                String sql = "UPDATE CB_REQUEST SET RES_CONTENT = ?, STATUS = ?, RESPONSE_TIME = ?, RESULT_CODE = ?, amount_full_tax = ?, refer_idempotent_id = ?," +
                        " isdn = ?, amount = ?, content_description = ?, REFER_TRANSACTION_ID = ?, final_result_code = ?, cps_transaction_id = ? " +
                        "WHERE REQUEST_TIME = ? AND TRANSACTION_ID = ? ";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    if(response != null) {
                        stmt.setString(1, StringUtils.substring(response.getContent(), 0, 3999));
                    }else{
                        stmt.setNull(1, Types.VARCHAR);
                    }
                    //fixme Dang code do
                    stmt.setString(2, REQUEST_STATUS.TIMEOUT.getCode());
//                    stmt.setString(2, messageContext.getStatus().endsWith(MessageContext.STATUS_SUCCESS) ? REQUEST_STATUS.SUCCESS.getCode() : REQUEST_STATUS.FAILED.getCode());
                    stmt.setTimestamp(3, new Timestamp(messageContext.getClientResponseDate().getTime()));

                    CBResponse flow_response = (CBResponse) messageContext.getClientResponse();
                    stmt.setLong(4, response.getCode().getCode());
                    stmt.setObject(5, mapFullRequest.get(CbsContansts.AMOUNT_FULL_TAX));
                    if (refer_idempotent_id != null && !refer_idempotent_id.isEmpty()) {
                        stmt.setString(6, refer_idempotent_id);
                    } else {
                        stmt.setNull(6, Types.VARCHAR);
                    }
                    stmt.setObject(7, messageContext.getIsdn());
                    stmt.setObject(8, (String) mapFullRequest.get(CbsContansts.AMOUNT));
                    stmt.setObject(9, (String) mapFullRequest.get(CbsContansts.CONTENT_DESCRIPTION));
                    stmt.setObject(10, (String) mapFullRequest.get(CbsContansts.PAYMENT_TRANSACTION_ID));

                    if(flow_response != null && flow_response.getCode() != null) //Final result code
                        stmt.setLong(11, flow_response.getCode().getCode());
                    else {
                        stmt.setNull(11,Types.INTEGER);
                    }
                    stmt.setString(12, StringUtil.nvl(mapFullRequest.get(CbsContansts.CPS_CONTENTID), ""));
                    stmt.setTimestamp(13, new Timestamp(messageContext.getClientRequestDate().getTime()));
                    stmt.setObject(14, messageContext.getTransID());
                    stmt.executeUpdate();
//                connection.commit();

                }
                //Set status timeout
                messageContext.setStatus(REQUEST_STATUS.TIMEOUT.getCode());
            } catch (Exception e) {
                log.error("Error when update request to db", e);
                CbsLog.error(messageContext, "R_CheckRequestIdempotentLimitTimeCmp", CBCode.INTERNAL_SERVER_ERROR, "", e.getMessage());
                throw new CBException(CBCode.INTERNAL_SERVER_ERROR, e);
            }

        } else {
            try (Connection connection = getManager().getConnection()) {

                String sql = "UPDATE CB_REQUEST SET RES_CONTENT = ?, STATUS = ?, RESPONSE_TIME = ?, RESULT_CODE = ?, amount_full_tax = ?, refer_idempotent_id = ?," +
                        " isdn = ?, amount = ?, content_description = ?, REFER_TRANSACTION_ID = ?, final_result_code = ?, cps_transaction_id = ? " +
                        "WHERE REQUEST_TIME = ? AND TRANSACTION_ID = ? ";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {

                    stmt.setString(1, StringUtils.substring(messageContext.getClientResponse().getContent(), 0, 3999));
                    stmt.setString(2, REQUEST_STATUS.convertMessageContextStatus(messageContext.getStatus()).getCode());
                    stmt.setTimestamp(3, new Timestamp(messageContext.getClientResponseDate().getTime()));

                    CBResponse response = (CBResponse) messageContext.getClientResponse();
                    stmt.setLong(4, response.getCode().getCode());
                    stmt.setObject(5, mapFullRequest.get(CbsContansts.AMOUNT_FULL_TAX));
                    if (refer_idempotent_id != null && !refer_idempotent_id.isEmpty()) {
                        stmt.setString(6, refer_idempotent_id);
                    } else {
                        stmt.setNull(6, Types.VARCHAR);
                    }
                    stmt.setObject(7, messageContext.getIsdn());
                    stmt.setObject(8, (String) mapFullRequest.get(CbsContansts.AMOUNT));
                    stmt.setObject(9, (String) mapFullRequest.get(CbsContansts.CONTENT_DESCRIPTION));
                    stmt.setObject(10, (String) mapFullRequest.get(CbsContansts.PAYMENT_TRANSACTION_ID));

                    stmt.setLong(11, response.getCode().getCode()); //Final result code
                    stmt.setString(12, StringUtil.nvl(mapFullRequest.get(CbsContansts.CPS_CONTENTID), ""));
                    stmt.setTimestamp(13, new Timestamp(messageContext.getClientRequestDate().getTime()));
                    stmt.setObject(14, messageContext.getTransID());
                    stmt.executeUpdate();
//                connection.commit();

                }
            } catch (Exception e) {
                log.error("Error when update request to db", e);
                CbsLog.error(messageContext, "R_CheckRequestIdempotentLimitTimeCmp", CBCode.INTERNAL_SERVER_ERROR, "", e.getMessage());
                throw new CBException(CBCode.INTERNAL_SERVER_ERROR);
            }
        }
    }

}
