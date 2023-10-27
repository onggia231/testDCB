package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.CBCode;
import com.telsoft.cbs.domain.CBException;
import com.telsoft.cbs.domain.CBRequest;
import com.telsoft.cbs.domain.SubscriberStatus;
import com.telsoft.cbs.utils.CbsLog;
import com.telsoft.cbs.utils.CbsUtils;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.annotations.Component;
import telsoft.gateway.core.log.MessageContext;

import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Check Subscriber Status from vas gate sync
 * <p>
 */

@Component("cbs-check-subscriber-status")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-check-subscriber-status",
        title = "Check Subscriber Status from vas gate sync",
        syntax = "cbs-check-subscriber-status:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)


public class CheckSubscriberStatusComponent extends ProcessorComponent  {

    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {

        try (Connection con = getManager().getConnection()) {
            String isdn = messageContext.getIsdn();
            Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
            SubscriberStatus subscriberStatus = getSubscriberStatus(con,isdn,mapFullRequest,exchange,messageContext);

            CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest,CbsContansts.SUB_VAS_STATUS,subscriberStatus); //put to gw messageContext
            switch (subscriberStatus) {
                case BLOCKED_1_WAY:
                case BLOCKED_2_WAY:
//                    throw new CBException(CBCode.USER_BARRED);
                case DESTROYED:
                    throw new CBException(CBCode.USER_IN_INACTIVE_STATE);
                case NOT_EXISTS:
                    throw new CBException(CBCode.USER_UNKNOWN);
                default:
                    break;
            }
        }catch (CBException e) {
            throw e;
        } catch (Exception e) {
            CbsLog.error(messageContext, "CheckSubscriberStatusComponent", CBCode.INTERNAL_SERVER_ERROR, "", e.getMessage());
            throw new CBException(CBCode.INTERNAL_SERVER_ERROR, e);
        }
    }

    public SubscriberStatus getSubscriberStatus(Connection cn, String isdn, Map mapFullRequest,Exchange exchange,MessageContext messageContext) throws Exception {

        SubscriberStatus result = SubscriberStatus.NOT_EXISTS;
        String strSQL = "SELECT VASGATE_STATUS, ID FROM CB_SUBSCRIBER WHERE ISDN = ? AND  STATUS = 1 ";
        try (PreparedStatement pstmt = cn.prepareStatement(strSQL)) {
            pstmt.setString(1, isdn);
            try(ResultSet rs = pstmt.executeQuery()){
                if(rs.next()){
                    result = SubscriberStatus.valueOfCode(rs.getInt("VASGATE_STATUS"));
                    String subId = rs.getString("ID");
                    messageContext.setProperty(CbsContansts.SUB_ID, subId);
                    exchange.setProperty(CbsContansts.SUB_ID, subId);
                    CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest,CbsContansts.SUB_ID, subId);
                }
            }

        }

        return result;
    }

}
