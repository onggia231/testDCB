package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.*;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.annotations.Component;
import org.apache.commons.dbutils.DbUtils;
import telsoft.gateway.core.log.MessageContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

/**
 * The Check Duplication Component
 * <p>
 */

@Component("cbs-check-duplicate")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-check-duplicate",
        title = "Check Content",
        syntax = "cbs-check-duplicate:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)

public class R_CheckRequestIdempotentCmp extends ProcessorComponent {

    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        CBCommand command = (CBCommand) exchange.getProperty(CbsContansts.COMMAND);
        CBStore store = (CBStore) messageContext.getProperty(CbsContansts.STORE);

        if (command.isIdempotent()) {
            // Update CB_REQUEST
            try (Connection connection = getManager().getConnection()) {
                String sql = "SELECT TRANSACTION_ID, REQ_CONTENT, RES_CONTENT, STATUS, AMOUNT, CLIENT_TRANSACTION_ID, COMMAND, RESULT_CODE FROM CB_REQUEST WHERE REQUEST_ID = ? AND STORE_CODE = ? AND TRANSACTION_ID != ?";
                ResultSet rs = null;
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {

                    stmt.setString(1, String.valueOf(messageContext.getProperty(CbsContansts.REQUEST_ID)));
                    stmt.setString(2, store.getStoreCode());
                    stmt.setString(3, messageContext.getTransID());
                    rs = stmt.executeQuery();
                    if (rs.next()) {

                        String req_content = rs.getString(2);
                        String res_content = rs.getString(3);
                        String status = rs.getString(4);
                        String amount = rs.getString(5);
                        String client_transaction_id = rs.getString(6);
                        String dbCommand = rs.getString(7);
                        String resultCode = rs.getString(8);

                        if (REQUEST_STATUS.IN_PROCESSING.getCode().equals(status)) {
                            throw new CBException(CBCode.IN_PROGRESS);
                        }

/*                        if (messageContext.getAmount() != Long.parseLong(amount) ||
                                !messageContext.getOrigin().equals(client_transaction_id) ||
                                !command.name().equals(dbCommand)) {
                            throw new CBException(CBCode.PARAMETER_ERROR);
                        }*/

                        throw new CBException(CBCode.valueOfCode(resultCode));
                    }
                } finally {
                    DbUtils.closeQuietly(rs);
                }
            } catch (CBException cbe) {
                throw cbe;
            } catch (Exception e) {
                throw new CBException(CBCode.INTERNAL_SERVER_ERROR);
            }
        }
    }
}
