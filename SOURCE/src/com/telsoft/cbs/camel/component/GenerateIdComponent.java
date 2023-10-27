package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.CBCommand;
import com.telsoft.cbs.domain.CBException;
import com.telsoft.cbs.domain.CBRequest;
import com.telsoft.cbs.utils.CbsUtils;
import com.telsoft.cbs.utils.CustomUUID;
import com.telsoft.util.StringUtil;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.annotations.Component;
import telsoft.gateway.core.log.MessageContext;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Generate Transaction Id Component
 * <p>
 */

@Component("cbs-generate-id")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-generate-id",
        title = "Generate transaction id",
        syntax = "cbs-generate-id:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)

public class GenerateIdComponent extends ProcessorComponent {
    /*@Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
        String msisdn = StringUtil.nvl(request.get(CbsContansts.MSISDN),"");
//        if (msisdn == null || msisdn.length() == 0)
//            msisdn = ;

        String transaction_id = messageContext.getRequestID();
        CBCommand command = (CBCommand) exchange.getProperty(CbsContansts.COMMAND);
        Calendar dt = Calendar.getInstance();
        dt.setTimeInMillis(System.currentTimeMillis());
        CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest,CbsContansts.STORE_START_TIME,dt.getTime());//request time

        long year = dt.get(Calendar.YEAR) & 0b1_1111_1111_1111;
        long month = (dt.get(Calendar.MONTH) + 1) & 0b1111;
        long date = dt.get(Calendar.DATE) & 0b1_1111;
        //todo tăng size code len 8 bit.
        long cmdCode = command.getShortCode() & 0b1111_1111;

        long datePart = cmdCode << 22 | year << 9 | month << 5 | date;
//        String prefix = datePart + "F" + request.get(CbsContansts.MSISDN) + "F";
        String prefix = datePart + "F" + msisdn + "F" + dt.getTimeInMillis() + "F";
        String cbsTransactionID = prefix + transaction_id;
        messageContext.setTransID(cbsTransactionID);
        exchange.setProperty(CbsContansts.TRANSACTION_ID, cbsTransactionID);
        CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest,CbsContansts.TRANSACTION_ID,cbsTransactionID);//CBS transaction ID
    }*/

    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
        String msisdn = StringUtil.nvl(request.get(CbsContansts.MSISDN),"");
//        if (msisdn == null || msisdn.length() == 0)
//            msisdn = ;

        String transaction_id = messageContext.getRequestID();
        CBCommand command = (CBCommand) exchange.getProperty(CbsContansts.COMMAND);

        String uuid = CustomUUID.genCustomCodecUUIDVer1();
        String prefix = uuid + "F" + command.getShortCode() + "F" + msisdn + "F";
        String cbsTransactionID = prefix + transaction_id;

        Date requestTime = CustomUUID.getDateFromCustomCodecUUID(uuid);
        CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest,CbsContansts.STORE_START_TIME,requestTime);//request time
        messageContext.setTransID(cbsTransactionID);
        exchange.setProperty(CbsContansts.TRANSACTION_ID, cbsTransactionID);
        CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest,CbsContansts.TRANSACTION_ID,cbsTransactionID);//CBS transaction ID
    }
}

