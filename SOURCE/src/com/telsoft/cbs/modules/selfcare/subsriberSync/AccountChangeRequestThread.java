package com.telsoft.cbs.modules.selfcare.subsriberSync;

import com.telsoft.cbs.module.cbsrest.domain.CBCommand;
import com.telsoft.cbs.module.cbsrest.domain.RestRequest;
import com.telsoft.cbs.module.cbsrest.domain.RestResponse;
import com.telsoft.cbs.modules.selfcare.entities.SubscriberEntitty;
import com.telsoft.thread.DBManageableThread;
import com.telsoft.thread.ParameterType;
import com.telsoft.util.AppException;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

@Slf4j
public class AccountChangeRequestThread extends DBManageableThread {
    private List<SubscriberEntitty> lstSub;
    private int processAmount;
    private String attribute;
    private String xForward;
    private String url;

    @Override
    public Vector getParameterDefinition() {
        super.getParameterDefinition();
        Vector vtReturn = new Vector();
        vtReturn.addAll(super.getParameterDefinition());
        vtReturn.addElement(createParameter("Store attribute", "", ParameterType.PARAM_TEXTBOX_MAX, ""));
        vtReturn.addElement(createParameter("xForward", "", ParameterType.PARAM_TEXTBOX_MAX, ""));
        vtReturn.addElement(createParameter("Process amount", "", ParameterType.PARAM_TEXTBOX_MAX, "99999", ""));
        return vtReturn;
    }

    @Override
    public void fillParameter() throws AppException {
        super.fillParameter();
        processAmount = loadUnsignedInteger("Process amount");
        attribute = loadString("Store attribute");
        xForward = loadString("xForward");
        try {
            xForward = loadString("xForward");
        } catch (Exception e) {
            xForward = null;
        }
    }

    @Override
    protected void beforeSession() throws Exception {
        super.beforeSession();
    }

    @Override
    protected void processSession() throws Exception {
//        while (miThreadCommand != ThreadConstant.THREAD_STOP) {
//            logMonitor("Start processing");
        String sql = "select a.id,a.isdn,a.retry,nvl(a.retried,0) retried,a.store_id,b.value url\n" +
                "from cb_import_account_change a join cb_store_attr b on a.store_id = b.store_id\n" +
                "where rownum<=? and sysdate>a.next_retry and b.name=?";
        try (PreparedStatement pstm = mcnMain.prepareStatement(sql)) {
            pstm.setInt(1, processAmount);
            pstm.setString(2, attribute);
            try (ResultSet resultSet = pstm.executeQuery()) {
                lstSub = new ArrayList<>();
                while (resultSet.next()) {
                    SubscriberEntitty sub = new SubscriberEntitty();
                    sub.setIsdn(resultSet.getString("isdn"));
                    sub.setId(resultSet.getLong("id"));
                    sub.setRetry(resultSet.getInt("retry"));
                    sub.setRetried(resultSet.getInt("retried"));
                    sub.setStoreId(resultSet.getInt("store_id"));
                    sub.setUrl(resultSet.getString("url"));
                    lstSub.add(sub);
                }
            }
        }

        try {
            for (SubscriberEntitty sub : lstSub) {
                if (sendRequestChangeAccount(sub.getId() + "", sub.getIsdn(), sub.getUrl(), xForward)) {
                    logMonitor("Send request to fortumo with isdn " + sub.getIsdn() + " ok");
                    InsertAccountChangeHis(sub, 1);
                } else {
                    int retried = sub.getRetried() + 1;
                    sub.setRetried(retried);
                    logMonitor("Send request to fortumo with isdn " + sub.getIsdn() + " fail " + retried + " times");
                    if (retried == sub.getRetry()) {
                        InsertAccountChangeHis(sub, 0);
                    } else {
                        UpdateRetryAccountChange(sub);
                    }
                }
            }
        } catch (Exception e) {
            logMonitor("Error processing at " + e.getMessage());
            log.error("Error processing at " + e.getMessage(), e);
        }
//            logMonitor("Process finished");
//        }

    }

    private void InsertAccountChangeHis(SubscriberEntitty sub, int success) throws SQLException {
        String sql = "insert into cb_import_account_change_his(id,isdn,retried,insert_time,success,store_id) values (?,?,?,sysdate,?,?)";
        try (PreparedStatement pstm = mcnMain.prepareStatement(sql)) {
            pstm.setLong(1, sub.getId());
            pstm.setString(2, sub.getIsdn());
            pstm.setInt(3, sub.getRetried());
            pstm.setInt(4, success);
            pstm.setInt(5, sub.getStoreId());
            if (pstm.executeUpdate() > 0) {
                sql = "delete from cb_import_account_change where id=?";
                try (PreparedStatement pstm2 = mcnMain.prepareStatement(sql)) {
                    pstm2.setLong(1, sub.getId());
                    pstm2.executeUpdate();
                }
            }
        }
    }


    private void UpdateRetryAccountChange(SubscriberEntitty sub) throws SQLException {
        String sql = "update cb_import_account_change set retried = ?, next_retry = sysdate+1/60/24 where id=?";
        try (PreparedStatement pstm = mcnMain.prepareStatement(sql)) {
            pstm.setInt(1, sub.getRetried());
            pstm.setLong(2, sub.getId());
            pstm.executeUpdate();
        }
    }

    private boolean sendRequestChangeAccount(String transactionId, String msisdn, String url, String xForward) {
        RestRequest request = new RestRequest();

        request.setName(CBCommand.ACCOUNT_CHANGE);
        request.setTransaction_id(transactionId);
        request.setIsdn(msisdn);

        Client client = ClientBuilder.newClient();
        try {
            Invocation.Builder invocationBuilder;
//            WebTarget target = client.target("http://10.11.10.143:8005/fortumoclient/fortumoclient");
            WebTarget target = client.target(url);
            invocationBuilder = target.request(MediaType.APPLICATION_XML);
            if (xForward != null) {
                invocationBuilder.header("X-Forward-For", xForward);
            }

            Response response;
            if ("POST".equalsIgnoreCase("POST") && request != null) {
                response = invocationBuilder.method("POST", Entity.entity(request, MediaType.APPLICATION_XML));
            } else {
                response = invocationBuilder.method("POST");
            }

            if (response.getStatus() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
            }

            RestResponse restResponse = response.readEntity(RestResponse.class);
            if (restResponse.getCode() == 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            logMonitor("Error sending request to fortumo at " + e.getMessage());
            log.error("Error sending request to fortumo at " + e.getMessage(), e);
            return false;
        }
    }
}
