package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.CBCode;
import com.telsoft.cbs.domain.CBException;
import com.telsoft.cbs.domain.CBRequest;
import com.telsoft.cbs.domain.CHANNEL_TYPE;
import com.telsoft.cbs.utils.CbsLog;
import com.telsoft.database.Database;
import com.telsoft.util.StringUtil;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.annotations.Component;
import org.apache.commons.lang3.StringUtils;
import telsoft.gateway.core.log.MessageContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Send 5M sms to subscriber
 * <p>
 */

@Component("cbs-send-sms")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-send-sms",
        title = "Send 5M sms to subscriber",
        syntax = "cbs-send-sms:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)

public class SendSMSComponent extends ProcessorComponent {

    @UriParam(name = "channelType", displayName = "ChannelType", description = "SYS, API, SMS, WAP, WEB, APP")
    CHANNEL_TYPE channelType;

    @UriParam(name = "apParamSMSType", displayName = "SmsCommandCode", description = "code in cb_sms_command")
    String apParamSMSType;

    @UriParam(name = "dateFormat", displayName = "DateFormat", description = "Format for date field")
    String dateFormat;

    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        CHANNEL_TYPE channelType = CHANNEL_TYPE.valueOf((String)parameters.get("channelType"));
        String cmdCode = (String) parameters.get("apParamSMSType");
        String dateFormat = (String) parameters.get("dateFormat");

        Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
        String templateSms = "";
        Long cmdId =0L;
        ResultSet resultSet = null;
        String storeCode = (String) messageContext.getProperty(CbsContansts.STORE_CODE);
        String isdn = (String) exchange.getProperty(CbsContansts.MSISDN);
        if (isdn != null) {
            try (Connection connection = getManager().getConnection()) {
                String sql = "select cmd_id,cmd_msg_content from cb_sms_command where status = 1 and cmd_code=?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, cmdCode);
                    resultSet = stmt.executeQuery();
                    if (resultSet.next()) {
                        templateSms = StringUtil.nvl(resultSet.getString("cmd_msg_content"),"");
                        cmdId = resultSet.getLong("cmd_id");
                    }
                } finally {
                    Database.closeObject(resultSet);
                }
                if (templateSms.equalsIgnoreCase("")) {
                    log.info("Can not get sms template from ap_param");
                } else {
                    String smsContent = fillDataIntoContent(templateSms,mapFullRequest,dateFormat);
                    sql = " insert into cb_mt_queue(id, isdn, content, store_code,retries,sent_time,channel_type,command_id)\n" +
                            "values (seq_mt_queue.nextval,?,?,?,?,sysdate,?,?)";
                    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                        stmt.setString(1, isdn);
                        stmt.setString(2, smsContent);
                        stmt.setString(3, storeCode);
                        stmt.setLong(4, 4);
                        stmt.setString(5, channelType.name());
                        stmt.setLong(6, cmdId);
                        int i = stmt.executeUpdate();
                        if (i > 0) {
                            log.info("Inserted mt into queue (isdn: " + isdn + ")");
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Insert SMS failed", e);
                CbsLog.error(messageContext, "SendSMSComponent", CBCode.INTERNAL_SERVER_ERROR, "Insert SMS failed", e.getMessage());
            }
        }
    }

    public static String fillDataIntoContent(String templateSms, Map mapFullRequest,String dateFormat) {
        String result = "";
        if(templateSms != null) {
            result = templateSms;
        }
        String[] paramList = StringUtils.substringsBetween(result, "{","}");
        if(paramList != null && paramList.length > 0){
            for (String paramCode:paramList) {
                String value = getDataFromMapRecursive(paramCode,mapFullRequest,dateFormat);
                if(value != null){
                    result = result.replaceAll("(?i)" + Pattern.quote("{" + paramCode + "}"),value);
                }
            }
        }
        return result;
    }

    public static String getDataFromMapRecursive(String paramCode, Map mapValue, String dateFormat) {
        String result = "";
        if(paramCode != null && !paramCode.equals("")){
            if(paramCode.indexOf(".") >=0){
                String paramCodePre = paramCode.substring(0, paramCode.indexOf("."));
                String paramCodePost = paramCode.substring(paramCode.indexOf("."));
                Object value = mapValue.get(paramCodePre);
                if(value != null && value instanceof Map){
                    result = getDataFromMapRecursive(paramCodePost,(Map)value,dateFormat);
                }
            }else{
                Object value = mapValue.get(paramCode);
                if(value != null){
                    if(value instanceof Date){
                        try{
                            result = new SimpleDateFormat(dateFormat).format(value);
                        }catch (Exception e){
                            result = value.toString();
//                            logger.warn("Can't parse date format in getDataFromMapRecursive", e);
                        }
                    }else result = value.toString();
                }
            }

        }
        return result;
    }
}
