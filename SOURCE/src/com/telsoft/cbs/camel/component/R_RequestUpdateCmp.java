package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.*;
import com.telsoft.cbs.utils.CbsLog;
import com.telsoft.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.annotations.Component;
import org.apache.commons.lang3.StringUtils;
import telsoft.gateway.core.log.MessageContext;

import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Transaction Log Component write transaction log into local database
 * <p>
 */

@Component("cbs-request-update")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-request-update",
        title = "Update status, response of a request",
        syntax = "cbs-request-update:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)

@Slf4j
public class R_RequestUpdateCmp extends ProcessorComponent {

    /*@Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {

        Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
        String refer_idempotent_id = (String) mapFullRequest.get(CbsContansts.REFERENCE_IDEMPOTENT_ID);
        // Update CB_REQUEST
        try (Connection connection = getManager().getConnection()) {

            String sql = "UPDATE CB_REQUEST SET RES_CONTENT = ?, STATUS = ?, RESPONSE_TIME = ?, RESULT_CODE = ?, amount_full_tax = ?, refer_idempotent_id = ? WHERE REQUEST_TIME = ? AND TRANSACTION_ID = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {

                stmt.setString(1, StringUtils.substring(messageContext.getClientResponse().getContent(), 0, 3999));
                stmt.setString(2, messageFContext.getStatus().endsWith(MessageContext.STATUS_SUCCESS) ? REQUEST_STATUS.SUCCESS.getCode() : REQUEST_STATUS.FAILED.getCode());
                stmt.setTimestamp(3, new Timestamp(messageContext.getClientResponseDate().getTime()));

                CBResponse response = (CBResponse) messageContext.getClientResponse();
                stmt.setLong(4, response.getCode().getCode());
                stmt.setObject(5, mapFullRequest.get(CbsContansts.AMOUNT_FULL_TAX));
                if(refer_idempotent_id != null && !refer_idempotent_id.isEmpty()){
                    stmt.setString(6,refer_idempotent_id);
                }else {
                    stmt.setNull(6, Types.VARCHAR);
                }
                stmt.setTimestamp(7, new Timestamp(messageContext.getClientRequestDate().getTime()));
                stmt.setString(8, messageContext.getTransID());
                stmt.executeUpdate();
//                connection.commit();

            }
        } catch (Exception e) {
            CbsLog.error(messageContext, "R_RequestUpdateCmp", CBCode.INTERNAL_SERVER_ERROR, "", e.getMessage());
            throw new CBException(CBCode.INTERNAL_SERVER_ERROR,e);
        }

    }*/

    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
        CBStore store = (CBStore) messageContext.getProperty(CbsContansts.STORE);
        String refer_idempotent_id = (String) mapFullRequest.get(CbsContansts.REFERENCE_IDEMPOTENT_ID);

        if(mapFullRequest.get(CbsContansts.FLOW_STATUS) != null || mapFullRequest.get(CbsContansts.FLOW_STATUS) == CBCode.PROCESS_TIMEOUT) {
            //update status
            try (Connection connection = getManager().getConnection()) {
                String sql = "UPDATE CB_REQUEST SET RES_CONTENT = ?, STATUS = ?, RESPONSE_TIME = ?, RESULT_CODE = ?, amount_full_tax = ?, refer_idempotent_id = ?, " +
                        " final_result_code = ?, cps_transaction_id = ? " +
                        " WHERE REQUEST_TIME = ? AND TRANSACTION_ID = ?";
                CBResponse response = (CBResponse) mapFullRequest.get(CbsContansts.TIMEOUT_RESPONSE);

                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    if(response != null) {
                        stmt.setString(1, StringUtils.substring(response.getContent(), 0, 3999));
                    }else{
                        stmt.setNull(1, Types.VARCHAR);
                    }
//                    stmt.setString(2, REQUEST_STATUS.FAILED.getCode());
                    stmt.setString(2, REQUEST_STATUS.TIMEOUT.getCode());
                    stmt.setTimestamp(3, new Timestamp(messageContext.getClientResponseDate().getTime()));
                    stmt.setLong(4, response.getCode().getCode());
                    stmt.setObject(5, mapFullRequest.get(CbsContansts.AMOUNT_FULL_TAX));
                    if(refer_idempotent_id != null && !refer_idempotent_id.isEmpty()){
                        stmt.setString(6,refer_idempotent_id);
                    }else {
                        stmt.setNull(6, Types.VARCHAR);
                    }

                    CBResponse flow_response = (CBResponse) messageContext.getClientResponse();
                    if(flow_response != null && flow_response.getCode() != null) //Final result code
                        stmt.setLong(7, flow_response.getCode().getCode());
                    else {
                        stmt.setNull(7,Types.INTEGER);
                    }
                    stmt.setString(8, StringUtil.nvl(mapFullRequest.get(CbsContansts.CPS_CONTENTID), ""));
                    stmt.setTimestamp(9, new Timestamp(messageContext.getClientRequestDate().getTime()));
                    stmt.setString(10, messageContext.getTransID());
                    stmt.executeUpdate();
//                    connection.commit();
                }
                //Set status timeout
                messageContext.setStatus(REQUEST_STATUS.TIMEOUT.getCode());
            } catch (Exception e) {
                log.error("Error when update request timeout to db: " + messageContext.getTransID(), e);
                CbsLog.error(messageContext, "OverTimeProcessComponent.UpdateTimeoutRequest", CBCode.INTERNAL_SERVER_ERROR, "", e.getMessage());
//                throw new CBException(CBCode.INTERNAL_SERVER_ERROR);
            }
            //Todo check if charged, need to rollback
            CBCommand command = (CBCommand) exchange.getProperty(CbsContansts.COMMAND);
            CBResponse flow_response = (CBResponse) messageContext.getClientResponse();
//            if(command == CBCommand.CHARGE && MessageContext.STATUS_SUCCESS.equals(messageContext.getStatus())){
            if(command == CBCommand.CHARGE && flow_response != null && CBCode.OK == flow_response.getCode()){
                Object reserved = mapFullRequest.get(CbsContansts.RESERVED_CHARGE_SUCCESS);
                if(reserved != null && reserved.equals(true)){
                    try (Connection connection = getManager().getConnection()) {
                        CheckTransactionLimitationComponent.rollbackReserved(connection, (Long) mapFullRequest.get(CbsContansts.AMOUNT_FULL_TAX),(String)mapFullRequest.get(CbsContansts.SUB_ID),store);
                        mapFullRequest.remove(CbsContansts.RESERVED_CHARGE_SUCCESS);
                    } catch (Exception e) {
                        log.error("Clear reserved failed ", e);
                        CbsLog.error(messageContext, "OverTimeProcessComponent.ClearReserved", CBCode.INTERNAL_SERVER_ERROR, "", e.getMessage());
                    }
                }
            }
        } else { // Update normal same RequestUpdate
            try (Connection connection = getManager().getConnection()) {

                String sql = "UPDATE CB_REQUEST SET RES_CONTENT = ?, STATUS = ?, RESPONSE_TIME = ?, RESULT_CODE = ?, amount_full_tax = ?, refer_idempotent_id = ?, " +
                        " final_result_code = ?, cps_transaction_id = ? " +
                        " WHERE REQUEST_TIME = ? AND TRANSACTION_ID = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {

                    stmt.setString(1, StringUtils.substring(messageContext.getClientResponse().getContent(), 0, 3999));
                    stmt.setString(2, REQUEST_STATUS.convertMessageContextStatus(messageContext.getStatus()).getCode());
                    stmt.setTimestamp(3, new Timestamp(messageContext.getClientResponseDate().getTime()));

                    CBResponse response = (CBResponse) messageContext.getClientResponse();
                    stmt.setLong(4, response.getCode().getCode());
                    stmt.setObject(5, mapFullRequest.get(CbsContansts.AMOUNT_FULL_TAX));
                    if(refer_idempotent_id != null && !refer_idempotent_id.isEmpty()){
                        stmt.setString(6,refer_idempotent_id);
                    }else {
                        stmt.setNull(6, Types.VARCHAR);
                    }

                    stmt.setLong(7, response.getCode().getCode());
                    stmt.setString(8, StringUtil.nvl(mapFullRequest.get(CbsContansts.CPS_CONTENTID), ""));
                    stmt.setTimestamp(9, new Timestamp(messageContext.getClientRequestDate().getTime()));
                    stmt.setObject(10, messageContext.getTransID());
                    stmt.executeUpdate();
//                connection.commit();

                }
            } catch (Exception e) {
                CbsLog.error(messageContext, "R_RequestUpdateCmp", CBCode.INTERNAL_SERVER_ERROR, "", e.getMessage());
                throw new CBException(CBCode.INTERNAL_SERVER_ERROR,e);
            }
        }
    }
}
