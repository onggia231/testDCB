package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.*;
import com.telsoft.cbs.utils.CbsLog;
import com.telsoft.cbs.utils.CbsUtils;
import com.telsoft.util.StringUtil;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.annotations.Component;
import org.apache.commons.dbutils.DbUtils;
import telsoft.gateway.core.log.MessageContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Check Duplication Component limit time
 * <p>
 */

@Component("cbs-check-duplicate-limit-time")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-check-duplicate-limit-time",
        title = "Check Content",
        syntax = "cbs-check-duplicate-limit-time:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)

public class R_CheckRequestIdempotentLimitTimeCmp extends ProcessorComponent {

    @UriParam(name = "idempotentTime", displayName = "IdempotentTime", description = "Duration for check idempotent in hours")
    int idempotentTime;

    @UriParam(name = "hintIndex", displayName = "HintIndex", description = "String hint index")
    String strHintIndex;

    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        int idempotentTime = Integer.parseInt(StringUtil.nvl((String) parameters.get("idempotentTime"),"0"));
        String strHintIndex = StringUtil.nvl(parameters.get("hintIndex"),"");

        CBCommand command = (CBCommand) exchange.getProperty(CbsContansts.COMMAND);
        CBStore store = (CBStore) messageContext.getProperty(CbsContansts.STORE);

        Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);

        if (command.isIdempotent()) {
            try (Connection connection = getManager().getConnection()) {
                String sql = "SELECT * from (" +
                        "SELECT "+ strHintIndex +" TRANSACTION_ID, REQ_CONTENT, RES_CONTENT, STATUS, AMOUNT, CLIENT_TRANSACTION_ID, COMMAND, RESULT_CODE, final_result_code, \n" +
                        "row_number() over (order by request_time desc) as rn \n" +
                        "FROM CB_REQUEST \n" +
                        "WHERE\n" +
                        "(request_time >= sysdate - " + idempotentTime + "/24 or (request_time >= ?  - 10/24/60 \n" +
                        "and request_time <= ? + " + idempotentTime + "/24))\n" +
                        "AND STORE_CODE = ? \n" +
                        "AND command = ? \n" +
                        "AND TRANSACTION_ID != ? \n" +
                        "and request_id = ? \n" +
                        "and refer_idempotent_id is null \n " +
                        ") where rn = 1";
                ResultSet rs = null;
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setTimestamp(1, new Timestamp(((Date)messageContext.getProperty(CbsContansts.PAYMENT_DATE)).getTime()));
                    stmt.setTimestamp(2, new Timestamp(((Date)messageContext.getProperty(CbsContansts.PAYMENT_DATE)).getTime()));
                    stmt.setString(3, store.getStoreCode());
                    stmt.setString(4, command.name());
                    stmt.setString(5, messageContext.getTransID());
                    stmt.setString(6, String.valueOf(messageContext.getProperty(CbsContansts.REQUEST_ID)));
                    rs = stmt.executeQuery();
                    if (rs.next()) {

                        String referTransId = rs.getString(1);
                        String req_content = rs.getString(2);
                        String res_content = rs.getString(3);
                        String status = rs.getString(4);
                        String amount = rs.getString(5);
                        String client_transaction_id = rs.getString(6);
                        String dbCommand = rs.getString(7);
                        String resultCode = rs.getString(8);
                        String finalResultCode = rs.getString(9);

                        if (String.valueOf(CBCode.INTERNAL_SERVER_ERROR.getCode()).equals(resultCode)
                                || String.valueOf(CBCode.IN_OTHER_PROGRESS.getCode()).equals(resultCode)
                                || String.valueOf(CBCode.CARRIER_MAINTENANCE.getCode()).equals(resultCode)
                                || String.valueOf(CBCode.INSUFFICIENT_FUNDS.getCode()).equals(resultCode)
                        ){
                            if(!String.valueOf(CBCode.OK.getCode()).equals(finalResultCode))
                                return;
                        }
                        CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest,CbsContansts.REFERENCE_IDEMPOTENT_ID,referTransId);//Payment_date

                        if (REQUEST_STATUS.IN_PROCESSING.getCode().equals(status)) {
                            throw new CBException(CBCode.IN_PROGRESS);
                        }

                        throw new CBException(CBCode.valueOfCode(resultCode));
                    }
                } finally {
                    DbUtils.closeQuietly(rs);
                }
            } catch (CBException cbe) {
                throw cbe;
            } catch (Exception e) {
                CbsLog.error(messageContext, "R_CheckRequestIdempotentLimitTimeCmp", CBCode.INTERNAL_SERVER_ERROR, "", e.getMessage());
                throw new CBException(CBCode.INTERNAL_SERVER_ERROR);
            }
        }
    }
}
