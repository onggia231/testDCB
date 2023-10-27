package com.telsoft.cbs.thread;

import com.telsoft.cbs.module.cbsrest.client.CBSClient;
import com.telsoft.cbs.module.cbsrest.domain.Attribute;
import com.telsoft.cbs.module.cbsrest.domain.CBCommand;
import com.telsoft.cbs.module.cbsrest.domain.RestRequest;
import com.telsoft.cbs.module.cbsrest.domain.RestResponse;
import com.telsoft.cbs.modules.selfcare.entities.RequestCharge;
import com.telsoft.cbs.utils.CustomUUID;
import com.telsoft.database.Database;
import com.telsoft.thread.DBManageableThread;
import com.telsoft.thread.ParameterType;
import com.telsoft.thread.ThreadConstant;
import com.telsoft.util.AppException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;

public class ReverseChargeThread extends DBManageableThread {

    private String threadName;
    private int processAmount;
    private int startHour;
    private int endHour;
    private String source;
    private String channelType;
    private String storeCode;
    private int delayTime;
    private String cbsUrl;
    CBSClient cbsClient;

    private Date startDate;
    private int tpsCount;
    private int tps;
    private String comment;

    private Long storeId;

    @Override
    public Vector getParameterDefinition() {

        Vector vtReturn = new Vector();
        vtReturn.addAll(super.getParameterDefinition());
        vtReturn.add(createParameter("ThreadName", "", ParameterType.PARAM_TEXTBOX_MAX, "99999", ""));
        vtReturn.add(createParameter("Process amount", "", ParameterType.PARAM_TEXTBOX_MASK, "9999", ""));
        vtReturn.add(createParameter("Start hour", "", ParameterType.PARAM_TEXTBOX_MASK, "9999", ""));
        vtReturn.add(createParameter("End hour", "", ParameterType.PARAM_TEXTBOX_MASK, "9999", ""));
        vtReturn.add(createParameter("Interval(ms)", "", ParameterType.PARAM_TEXTBOX_MASK, "9999", ""));
        vtReturn.add(createParameter("Source", "", ParameterType.PARAM_TEXTBOX_MAX, ""));
        vtReturn.add(createParameter("Channel type", "", ParameterType.PARAM_TEXTBOX_MAX, ""));
        vtReturn.add(createParameter("Store code", "", ParameterType.PARAM_TEXTBOX_MAX, ""));
        vtReturn.add(createParameter("CBS Url", "", ParameterType.PARAM_TEXTBOX_MAX, "99999", ""));
        vtReturn.add(createParameter("Tps", "", ParameterType.PARAM_TEXTBOX_MASK, "9999", ""));
        vtReturn.add(createParameter("CommentToRefundReason", "", ParameterType.PARAM_TEXTBOX_MAX, "255", ""));
        return vtReturn;
    }

    public void fillParameter() throws AppException {
        super.fillParameter();
        threadName = loadString("ThreadName");
        startHour = loadInteger("Start hour");
        endHour = loadInteger("End hour");
        processAmount = loadInteger("Process amount");
        channelType = loadString("Channel type");
        storeCode = loadString("Store code");
        source = loadString("Source");
        delayTime = loadUnsignedInteger("Interval(ms)");
        tps = loadUnsignedInteger("Tps");
        cbsUrl = loadString("CBS Url");
        comment = loadString("CommentToRefundReason");

    }

    @Override
    protected void beforeSession() throws Exception {
        super.beforeSession();
        ResultSet resultSet = null;
        String sql = "select id from cb_store where store_code = ?";
        try (PreparedStatement stmt = mcnMain.prepareStatement(sql)) {
            stmt.setString(1, storeCode);
            resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                storeId = resultSet.getLong("id");
            }
        } finally {
            Database.closeObject(resultSet);
        }

        if (storeId == 0L) {
            logMonitor("Can not find code " + storeCode + " in database");
            miThreadCommand = ThreadConstant.THREAD_STOP;
        } else {
            cbsClient = new CBSClient(this.source, this.storeCode, this.cbsUrl);
            cbsClient.start();
        }
    }

    @Override
    protected void processSession() throws Exception {
        String sql = "select isdn,store_transaction_id,request_id,request_time, CLIENT_TRANSACTION_ID from cb_request_charge where status = 1 " +
                "and result_code = 0 and payment_status =0 and rownum<=? and request_time>=(sysdate-?/24) and request_time<=(sysdate-?/24) and store_id=? order by request_time";
        List<RequestCharge> lstRequestCharge;
        startDate = new Date();
//        logMonitor("Start process at "+processTime.getTime());
        tpsCount = 0;
//        while (miThreadCommand != ThreadConstant.THREAD_STOP) {
        lstRequestCharge = new ArrayList<>();
        try (PreparedStatement pstm = mcnMain.prepareStatement(sql)) {
            pstm.setInt(1, processAmount);
            pstm.setInt(2, startHour);
            pstm.setInt(3, endHour);
            pstm.setLong(4, storeId);
            logDebug("storeId=" + storeId + ",processAmount=" + processAmount + ",startHour" + startHour + ",endHour=" + endHour + "miThreadCommand=" + miThreadCommand);
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    if (!isRunning())
                        break;
                    RequestCharge rc = new RequestCharge();
                    rc.setIsdn(rs.getString("isdn"));
                    rc.setStoreTransactionId(rs.getString("store_transaction_id"));
                    rc.setRequestId(rs.getString("request_id"));
                    rc.setRequestTime(rs.getDate("request_time"));
                    rc.setClientTransactionId(rs.getString("CLIENT_TRANSACTION_ID"));
                    lstRequestCharge.add(rc);
                }
            }
        }
        logDebug("Number of request selected: " + lstRequestCharge.size());
        for (RequestCharge rc : lstRequestCharge) {
            if (!isRunning())
                break;
            String purchaseTime = formatPurchaseTime(rc.getRequestTime());
            logMonitor("Sending reverse request with isdn " + rc.getIsdn()
                    + " - store transaction id " + rc.getStoreTransactionId()
                    + " - request id " + rc.getRequestId()
                    + " - client transaction id " + rc.getClientTransactionId()
                    + " - purchase time " + purchaseTime);
/*            String uuid = CustomUUID.genCustomCodecUUIDVer1();
            String prefix = uuid + "F" + CBCommand.REVERSE.getShortCode() + "F" + rc.getIsdn() + "F";
            String clientTransactionID = prefix + "0";*/
            RestResponse restResponse = sendReverseRequest(rc.getStoreTransactionId(), rc.getClientTransactionId(), rc.getRequestId(), purchaseTime);
            if (restResponse.getCode() == 0)
                logMonitor(Calendar.getInstance().getTime() + ": OK!");
            else logMonitor(restResponse.getDescription());
            tpsCount++;
//            if (tpsCount < tps) {
//
//            } else {
//                tpsCount = 0;
//
//                while (Calendar.getInstance().before(processTime)) {
//                    //logMonitor(Calendar.getInstance().toString());
//                    Thread.sleep(10);
//                }
//                processTime = Calendar.getInstance();
//                processTime.add(Calendar.SECOND, 1);
//            }

            if (tpsCount >= tps) {
                tpsCount = 0;
                Date currentDate = new Date();
                long timeToSleep = 1000 - (currentDate.getTime() - startDate.getTime());
                if (timeToSleep > 0) {
                    Thread.sleep(timeToSleep);
                    startDate = new Date();
                } else {
                    startDate = currentDate;
                }
            }
        }
    }


    public RestResponse sendReverseRequest(String storeTransactionId, String transactionId, String clientRequestId, String purchaseTime) throws Exception {

        RestRequest request = new RestRequest();

        request.setName(CBCommand.REVERSE);
        request.setStore_code(storeCode);
        request.setTransaction_id(transactionId);
        request.setSource(this.source);
        request.getParameters().add(new Attribute("store_transaction_id", storeTransactionId));
        request.getParameters().add(new Attribute("client_request_id", clientRequestId));
//        request.getParameters().add(new Attribute("purchase_time", "20210126000000"));
        request.getParameters().add(new Attribute("purchase_time", purchaseTime));
        request.getParameters().add(new Attribute("channel_type", channelType));
        request.getParameters().add(new Attribute("refund_reason", comment));
        return cbsClient.execute(null, request);
    }


    private String formatPurchaseTime(Date purchaseTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            return sdf.format(purchaseTime);

        } catch (Exception ex) {
            logMonitor("Error format purchase time");
        }
        return null;
    }

    private void logDebug(String s) {
        if (isDebug()) {
            logMonitor("DEBUG: " + s);
        }
    }
}
