package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.*;
import com.telsoft.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import telsoft.gateway.core.log.MessageContext;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Write CDR for CPS
 * <p>
 */

@Slf4j
@Component("cbs-write-cdr-for-cps")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-write-cdr-for-cps",
        title = "Build response message",
        syntax = "cbs-write-cdr-for-cps:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)

public class WriteCDRForCPSComponent extends ProcessorComponent  {
    @UriParam(name = "status", displayName = "Status", description = "Status in CDR")
    CDR_CPS_STATUS status;

    @UriParam(name = "position", displayName = "Position in list call", description = "Position in Diameter call list")
    int position;

    @UriParam(name = "channelType", displayName = "ChannelType", description = "SYS, API, SMS, WAP, WEB, APP")
    String channelType;

    @UriParam(name = "cpsCategory", displayName = "CpsCategory", description = "Cps Category")
    CbsContansts.CPS_CATEGORY cpsCategory;

    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        Logger loggerCDR = LoggerFactory.getLogger("CDR_CPS");

        CDR_CPS_STATUS status = CDR_CPS_STATUS.valueOf((String) parameters.get("status"));
        int position = Integer.parseInt(StringUtil.nvl((String) parameters.get("position"),"0"));
        String channelType = (String) parameters.get("channelType");
        CbsContansts.CPS_CATEGORY cpsCategory = CbsContansts.CPS_CATEGORY.valueOf((String) parameters.get("cpsCategory"));

        CBStore store = (CBStore) messageContext.getProperty(CbsContansts.STORE);
        Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
//        #CDR charge cho TTTC
//#Datetime
//        ${date:exchangeProperty.CBMsgContext.getProperty("map_full_request")[list_cps_diameter_cdr][0][cps_request_time]:yyyyMMddHHmmss}:
        String dateTime = "";
        Date requestTime = null;
        try {
            requestTime = (Date) ((Map) ((List) mapFullRequest.get(CbsContansts.LIST_CPS_DIAMETER_CDR)).get(position)).get(CbsContansts.CPS_REQUEST_TIME);
            dateTime = new SimpleDateFormat("yyyyMMddHHmmss").format(requestTime);
        } catch (Exception e) {
//            log.warn("Can't find RequestTime when write CDR for CPS",e);
            requestTime = messageContext.getClientRequestDate();
            dateTime = new SimpleDateFormat("yyyyMMddHHmmss").format(messageContext.getClientRequestDate());
        }
//#A_Number
//        ${exchangeProperty.msisdn}:
        String aNumber = (String) mapFullRequest.get(CbsContansts.MSISDN);
//#B_Number
//        ${exchangeProperty.CBMsgContext.getProperty("map_full_request")[cps_b_isdn]}:
        String bNumber = store.getAttributes().getProperty(CbsContansts.CPS_PARAM_SHORT_CODE); //fixme Tai lieu ghi bNumber thi ghi shortcode
//#EventID
//        ${exchangeProperty.CBMsgContext.getProperty("map_full_request")[list_cps_diameter_cdr][0][cps_categoryid]}:
        String eventID = cpsCategory.getCategoryId();
//        try {
//            eventID = (String) ((Map) ((List) mapFullRequest.get(CbsContansts.LIST_CPS_DIAMETER_CDR)).get(position)).get(CbsContansts.CPS_CATEGORYID);
//        } catch (Exception e) {
//            log.warn("Can't find eventID when write CDR for CPS",e);
//            return;
//        }
//#SP/CPID
//        ${exchangeProperty.CBMsgContext.getProperty("store").getAttributes().getProperty("sp")}${exchangeProperty.CBMsgContext.getProperty("store").getAttributes().getProperty("cp")}:
        String sp = store.getAttributes().getProperty("sp");
        String cp = store.getAttributes().getProperty("cp");
//#ContentID
//        ${exchangeProperty.CBMsgContext.getProperty("map_full_request")[list_cps_diameter_cdr][0][cps_contentid]}:
        String contentID = "";
        try {
            contentID = (String)  mapFullRequest.get(CbsContansts.CPS_CONTENTID);
        } catch (Exception e) {
            log.warn("Can't find strContentID when write CDR for CPS",e);
            return;
        }
//#Status: 0: Khong thanh cong, 1: Thanh cong, 2: Can hoan cuoc tu dong 0-60/5-60 voi tb tra sau/tra truoc, 3: Can hoan cuoc tu dong >60 ngay, 4: Can hoan cuoc tu dong trong vong 5 ngay voi tra truoc bi loi
//        4:
//#Cost
//        ${exchangeProperty.CBMsgContext.getProperty("map_full_request")[amount_full_tax]}:
        String cost = String.valueOf(mapFullRequest.get(CbsContansts.AMOUNT_FULL_TAX));
//#ChannelType SYS, API, SMS, WAP, WEB, APP
//        SYS:
        //if not config, get from request
        if(channelType == null || channelType.isEmpty()){
            CHANNEL_TYPE ct = (CHANNEL_TYPE) mapFullRequest.get(CbsContansts.CHANNEL_TYPE);
            if(ct != null)
                channelType = ct.name();
        }
//#Information
//        ${exchangeProperty.CBMsgContext.getProperty("map_full_request")[cps_extra_info]}:
        String information = (String) mapFullRequest.get(CbsContansts.CPS_EXTRA_INFO);
//#Cost Tk1
//        ${exchangeProperty.CBMsgContext.getProperty("map_full_request")[amount_full_tax]}:
        String costTK1 = "0";
//#Km2
//        ${exchangeProperty.CBMsgContext.getProperty("map_full_request")[amount_full_tax]}:
        String km2 = "0";
//#Km3
//        ${exchangeProperty.CBMsgContext.getProperty("map_full_request")[amount_full_tax]}:
        String km3 = "0";

        loggerCDR.info(
                dateTime + ":" +
                        aNumber + ":" +
                        bNumber + ":" +
                        eventID + ":" +
                        sp + cp + ":" +
                        contentID + ":" +
                        status.getCode() + ":" +
                        cost + ":" +
                        channelType + ":" +
                        information + ":" +
                        costTK1 + ":" +
                        km2 + ":" +
                        km3
        );
        //todo: write cdr to log xml?

        //add to list to insert into db
        List<Map> listCDR = (List<Map>) mapFullRequest.get(CbsContansts.CPS_CDR_LIST);
        if (listCDR == null) {
            listCDR = new ArrayList<>();
            mapFullRequest.put(CbsContansts.CPS_CDR_LIST, listCDR);
        }
        Map mapCPSCDR = new HashMap();
        mapCPSCDR.put(CbsContansts.CPS_CDR_CPS_CALL_TIME, requestTime);
        mapCPSCDR.put(CbsContansts.CPS_CDR_DATETIME, dateTime);
        mapCPSCDR.put(CbsContansts.CPS_CDR_A_NUMBER, aNumber);
        mapCPSCDR.put(CbsContansts.CPS_CDR_B_NUMBER, bNumber);
        mapCPSCDR.put(CbsContansts.CPS_CDR_EVENTID, eventID);
        mapCPSCDR.put(CbsContansts.CPS_CDR_SPID, sp);
        mapCPSCDR.put(CbsContansts.CPS_CDR_CPID, cp);
        mapCPSCDR.put(CbsContansts.CPS_CDR_CONTENTID, contentID);
        mapCPSCDR.put(CbsContansts.CPS_CDR_STATUS, status.getCode());
        mapCPSCDR.put(CbsContansts.CPS_CDR_COST, cost);
        mapCPSCDR.put(CbsContansts.CPS_CDR_CHANNEL_TYPE, channelType);
        mapCPSCDR.put(CbsContansts.CPS_CDR_INFORMATION, information);
        mapCPSCDR.put(CbsContansts.CPS_CDR_COST_TK1, costTK1);
        mapCPSCDR.put(CbsContansts.CPS_CDR_KM2, km2);
        mapCPSCDR.put(CbsContansts.CPS_CDR_KM3, km3);
        listCDR.add(mapCPSCDR);
    }
}
