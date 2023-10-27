package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.annotations.Component;
import org.apache.commons.lang3.StringUtils;
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

@Component("cbs-request-insert")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-request-insert",
        title = "Insert a request into database",
        syntax = "cbs-request-insert:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)

@Slf4j
public class R_RequestInsertCmp extends ProcessorComponent {
    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        CBCommand command = (CBCommand) exchange.getProperty(CbsContansts.COMMAND);
        CBStore store = (CBStore) messageContext.getProperty(CbsContansts.STORE);
        Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
        // Insert into CB_REQUEST
        try (Connection connection = getManager().getConnection()) {
            String sql = "INSERT INTO CB_REQUEST(REQUEST_TIME, ISDN, STATUS, COMMAND, AMOUNT, REQ_CONTENT, ADDRESS, " +
                    "STORE_CODE, TRANSACTION_ID, CLIENT_TRANSACTION_ID, STORE_TRANSACTION_ID, REQUEST_ID, CONTENT_DESCRIPTION,channel_type) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {

                stmt.setTimestamp(1, new Timestamp(messageContext.getClientRequestDate().getTime()));
                stmt.setString(2, messageContext.getIsdn());
                stmt.setString(3, REQUEST_STATUS.IN_PROCESSING.getCode());
                stmt.setString(4, command.name());
                stmt.setLong(5, Long.parseLong(messageContext.getPropertyNvl(MessageContext.AMOUNT, "0")));
                stmt.setString(6, StringUtils.substring(messageContext.getRequest().getContent(), 0, 3999));
                stmt.setString(7, String.valueOf(messageContext.getProperty(MessageContext.IPADDRESS)));
                stmt.setString(8, store.getStoreCode());
                stmt.setString(9, messageContext.getTransID());
                stmt.setString(10, messageContext.getOrigin());
                stmt.setString(11, messageContext.getPropertyNvl(CbsContansts.STORE_TRANSACTION_ID, ""));
                stmt.setString(12, messageContext.getPropertyNvl(CbsContansts.REQUEST_ID, ""));
                stmt.setString(13, messageContext.getPropertyNvl(CbsContansts.CONTENT_DESCRIPTION, ""));
                stmt.setObject(14, messageContext.getPropertyNvl(CbsContansts.CHANNEL_TYPE, ""));
                stmt.executeUpdate();
//                connection.commit();
            }
        } catch (Exception e) {
            throw new CBException(CBCode.INTERNAL_SERVER_ERROR);
        }
    }
}
