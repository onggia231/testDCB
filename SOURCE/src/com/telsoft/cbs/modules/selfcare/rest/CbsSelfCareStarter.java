package com.telsoft.cbs.modules.selfcare.rest;

import com.telsoft.database.Database;
import com.telsoft.httpservice.server.RestStarter;
import com.telsoft.thread.ParameterType;
import com.telsoft.thread.ParameterUtil;
import com.telsoft.thread.ThreadConstant;
import com.telsoft.util.AppException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

/**
 * <p>Title: Core Gateway System</p>
 *
 * <p>Description: A part of TELSOFT Gateway System</p>
 *
 * <p>Copyright: Copyright (c) 2009</p>
 *
 * <p>Company: TELSOFT</p>
 *
 * @author Nguyen Cong Khanh
 * @version 1.0
 */

@Slf4j
public class CbsSelfCareStarter extends RestStarter {

    @Getter
    CbsSelfCareStarter cbsSelfCareStarter = this;

    @Getter
    @Setter
    private String otpTemplate;

    @Getter
    @Setter
    private Long otpCommandId;

    @Getter
    @Setter
    private String otpCode;

    @Getter
    @Setter
    private String channelType;

    @Getter
    @Setter
    private String storeCode;

    @Override
    public Vector getParameterDefinition() {

        Vector vtReturn = new Vector();
        vtReturn.addAll(super.getParameterDefinition());
        vtReturn.add(ParameterUtil.createParameter("OTP SMS Command Code", "", ParameterType.PARAM_TEXTBOX_MAX, "99999", ""));
        vtReturn.add(ParameterUtil.createParameter("Store code", "", ParameterType.PARAM_TEXTBOX_MAX, "99999", ""));
        vtReturn.add(ParameterUtil.createParameter("Channel type", "", ParameterType.PARAM_TEXTBOX_MAX, "99999", ""));
        return vtReturn;
    }

    public void fillParameter() throws AppException {
        super.fillParameter();
        // Fill parameter
        otpCode = loadString("OTP SMS Command Code");
        storeCode = loadString("Store code");
        channelType = loadString("Channel type");
    }


    @Override
    public ResourceConfig createResourceConfig() {

        ResourceConfig resourceConfig = super.createResourceConfig();
        resourceConfig.register(CbsSelfCareResource.class);
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(getCbsSelfCareStarter()).to(CbsSelfCareStarter.class);
            }
        });
        resourceConfig.register(CbsSelfCareResource.class);
        return resourceConfig;
    }

    @Override
    protected void beforeSession() throws Exception {
        super.beforeSession();
        getOtpContentFromCode();
        if(otpCommandId==null){
            logMonitor("Can not find "+otpCode+" in database");
            miThreadCommand = ThreadConstant.THREAD_STOPPED;
        }
    }

     void getOtpContentFromCode() {
        String sql = "select cmd_msg_content,cmd_id from cb_sms_command where status = ? and cmd_code=?";

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = cbsSelfCareStarter.mmgrMain.getConnection().prepareStatement(sql);
            pstmt.setInt(1,1);
            pstmt.setString(2,otpCode);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                setOtpTemplate(rs.getString("cmd_msg_content"));
                setOtpCommandId(rs.getLong("cmd_id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Database.closeObject(rs);
            Database.closeObject(pstmt);
        }
    }
}
