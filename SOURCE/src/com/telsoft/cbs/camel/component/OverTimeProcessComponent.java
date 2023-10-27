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
 * Check timeout and process rollback
 * <p>
 */

@Component("cbs-overtime-process")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-overtime-process",
        title = "Check timeout and process",
        syntax = "cbs-overtime-process:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)

public class OverTimeProcessComponent extends ProcessorComponent{
    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
        CBStore store = (CBStore) messageContext.getProperty(CbsContansts.STORE);
        String refer_idempotent_id = (String) mapFullRequest.get(CbsContansts.REFERENCE_IDEMPOTENT_ID);

        if(mapFullRequest.get(CbsContansts.FLOW_STATUS) != null || mapFullRequest.get(CbsContansts.FLOW_STATUS) == CBCode.PROCESS_TIMEOUT) {
            //update status
            try (Connection connection = getManager().getConnection()) {
                String sql = "UPDATE CB_REQUEST SET RES_CONTENT = ?, STATUS = ?, RESPONSE_TIME = ?, RESULT_CODE = ?, amount_full_tax = ?, refer_idempotent_id = ?, cps_transaction_id = ? WHERE REQUEST_TIME = ? AND TRANSACTION_ID = ?";
                CBResponse response = (CBResponse) mapFullRequest.get(CbsContansts.TIMEOUT_RESPONSE);

                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    if(response != null) {
                        stmt.setString(1, StringUtils.substring(response.getContent(), 0, 3999));
                    }else{
                        stmt.setNull(1, Types.VARCHAR);
                    }
                    stmt.setString(2, REQUEST_STATUS.FAILED.getCode());
                    stmt.setTimestamp(3, new Timestamp(messageContext.getClientResponseDate().getTime()));
                    stmt.setLong(4, response.getCode().getCode());
                    stmt.setObject(5, mapFullRequest.get(CbsContansts.AMOUNT_FULL_TAX));
                    if(refer_idempotent_id != null && !refer_idempotent_id.isEmpty()){
                        stmt.setString(6,refer_idempotent_id);
                    }else {
                        stmt.setNull(6, Types.VARCHAR);
                    }
                    stmt.setString(7, StringUtil.nvl(mapFullRequest.get(CbsContansts.CPS_CONTENTID), ""));
                    stmt.setTimestamp(8, new Timestamp(messageContext.getClientRequestDate().getTime()));
                    stmt.setString(9, messageContext.getTransID());
                    stmt.executeUpdate();
//                    connection.commit();
                }
            } catch (Exception e) {
                log.error("Error when update request timeout to db: " + messageContext.getTransID(), e);
                CbsLog.error(messageContext, "OverTimeProcessComponent.UpdateTimeoutRequest", CBCode.INTERNAL_SERVER_ERROR, "", e.getMessage());
//                throw new CBException(CBCode.INTERNAL_SERVER_ERROR);
            }
            //Todo check if charged, need to rollback
            CBCommand command = (CBCommand) exchange.getProperty(CbsContansts.COMMAND);
            if(command == CBCommand.CHARGE && MessageContext.STATUS_SUCCESS.equals(messageContext.getStatus())){
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

                String sql = "UPDATE CB_REQUEST SET RES_CONTENT = ?, STATUS = ?, RESPONSE_TIME = ?, RESULT_CODE = ?, amount_full_tax = ?, refer_idempotent_id = ?, cps_transaction_id = ? WHERE REQUEST_TIME = ? AND TRANSACTION_ID = ?";
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
                    stmt.setString(7, StringUtil.nvl(mapFullRequest.get(CbsContansts.CPS_CONTENTID), ""));
                    stmt.setTimestamp(8, new Timestamp(messageContext.getClientRequestDate().getTime()));
                    stmt.setString(9, messageContext.getTransID());
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
