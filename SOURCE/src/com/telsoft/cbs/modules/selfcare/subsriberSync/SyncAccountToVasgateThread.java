package com.telsoft.cbs.modules.selfcare.subsriberSync;

import com.telsoft.cbs.modules.selfcare.entities.SubscriberEntitty;
import com.telsoft.thread.DBManageableThread;
import com.telsoft.thread.ParameterType;
import com.telsoft.util.AppException;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

@Slf4j
public class SyncAccountToVasgateThread extends DBManageableThread {
    private List<SubscriberEntitty> lstSub;
    private int backDate;
    private String strUrl;
//    private String xForward;

    @Override
    public Vector getParameterDefinition() {
        super.getParameterDefinition();
        Vector vtReturn = new Vector();
        vtReturn.addAll(super.getParameterDefinition());
        vtReturn.addElement(createParameter("Url", "", ParameterType.PARAM_TEXTBOX_MAX, ""));
//        vtReturn.addElement(createParameter("xForward", "", ParameterType.PARAM_TEXTBOX_MAX, ""));
        vtReturn.addElement(createParameter("Back date", "", ParameterType.PARAM_TEXTBOX_MAX, "99999", ""));
        return vtReturn;
    }

    @Override
    public void fillParameter() throws AppException {
        super.fillParameter();
        strUrl = loadString("Url");
        backDate = loadInteger("Back date");
//        xForward = loadString("xForward");
//        try {
//            xForward = loadString("xForward");
//        } catch (Exception e) {
//            xForward = null;
//        }
    }

    @Override
    protected void beforeSession() throws Exception {

        super.beforeSession();
    }

    @Override
    protected void processSession() throws Exception {
        String sql = "select isdn ,'subscribe' as action,REG_DATE \"Date\" \n" +
                "  from cb_subscriber where REG_DATE>trunc(sysdate-1-"+backDate+") and REG_DATE<trunc(sysdate-"+backDate+") and status =1\n" +
                "  union\n" +
                "  select isdn,'unsubscribe' as action,ISSUE_TIME as \"Date\" from CB_CHANGE_STATUS \n" +
                "  where ISSUE_TIME>trunc(sysdate-1-"+backDate+") and ISSUE_TIME<trunc(sysdate-"+backDate+") and NEW_STATUS =5";
        try (PreparedStatement pstm = mcnMain.prepareStatement(sql)) {
            try (ResultSet resultSet = pstm.executeQuery()) {
                lstSub = new ArrayList<>();
                while (resultSet.next()) {
                    SubscriberEntitty sub = new SubscriberEntitty();
                    sub.setIsdn(resultSet.getString("isdn"));
                    sub.setReason(resultSet.getString("action"));
                    sub.setProcessTime(resultSet.getDate("Date"));
                    lstSub.add(sub);
                }
            }
        } catch (Exception e) {
            logMonitor("Error query data from database: " + e.getMessage());
            return;
        }

        try {
            if (lstSub.size() > 0)
                SyncVasgateServiceVasgate(lstSub);
        } catch (Exception e) {
            logMonitor("Error sending request at " + e.getMessage());
            log.error("Error sending request at " + e.getMessage(), e);
        }
    }


    public void SyncVasgateServiceVasgate(List<SubscriberEntitty> listSub) throws Exception {
        soapserversimulator.VasgateServiceSoapBindingStub binding;
        try {
            URL url = new URL(strUrl);
            binding = (soapserversimulator.VasgateServiceSoapBindingStub)
                    new soapserversimulator.VasgateServiceServiceLocator().getVasgateService(url);

            binding.setTimeout(60000);
            for (SubscriberEntitty sub : listSub) {
                int value = -3;
                value = binding.vasgate(sub.getIsdn(), sub.getReason());
                logMonitor(sub.getIsdn() + " - " + sub.getReason() + " : " + (value == 1 ? "success" : "fail"));
            }
        } catch (Exception e) {
            logMonitor("Error sync vasgate: " + e.getMessage());
        }
    }
}
