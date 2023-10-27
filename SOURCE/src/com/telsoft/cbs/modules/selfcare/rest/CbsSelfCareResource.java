package com.telsoft.cbs.modules.selfcare.rest;

import com.telsoft.cbs.module.cbsrest.server.service.RestException;
import com.telsoft.httpservice.server.RestResource;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;

@Path("/")
@Produces({"application/xml"})
@Consumes({"application/xml"})
public class CbsSelfCareResource implements RestResource {
    private static final Logger log = LoggerFactory.getLogger(com.telsoft.cbs.modules.rest.service.RestResource.class);


    @Getter
    @Setter
    @Context
    private CbsSelfCareStarter cbsSelfCareStarter;

    protected String generateRandomString(String isdn) throws RestException {
        String Capital_chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
//        String Small_chars = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
//        String symbols = "!@#$%^&*_=+-/.?<>)";


        String values = Capital_chars + //Small_chars +
                numbers;// + symbols;

        // Using random method
        Random rndm_method = new Random();

        char[] otp = new char[6];

        for (int i = 0; i < 6; i++) {
            // Use of charAt() method : to get character value
            // Use of nextInt() as it is scanning the value as int
            otp[i] = values.charAt(rndm_method.nextInt(values.length()));

        }
        return String.valueOf(otp);

    }

    private boolean checkIsdn(String isdn) {
        String sql = "select isdn from cb_subscriber where isdn = ? and status =1";
        try (Connection cnn = cbsSelfCareStarter.mmgrMain.getConnection();
             PreparedStatement pstmMtQueue = cnn.prepareStatement(sql)) {
            pstmMtQueue.setString(1, isdn);
            try (ResultSet rs = pstmMtQueue.executeQuery()) {
                if (rs.next())
                    return true;
            }
        } catch (Exception e) {
            cbsSelfCareStarter.logMonitor("Error check ISDN");
            cbsSelfCareStarter.getLog().error("Error check ISDN", e);
        }
        return false;
    }

    private boolean insMtQueue(String otp, String isdn) {
        String sql = " insert into cb_mt_queue(id, isdn, content, store_code,retries,\n" +
                "sent_time,channel_type,command_id)\n" +
                "values (seq_mt_queue.nextval,?,?,?,?,sysdate,?,?)";
        try (Connection cnn = cbsSelfCareStarter.mmgrMain.getConnection();
             PreparedStatement pstmMtQueue = cnn.prepareStatement(sql)) {
            pstmMtQueue.setString(1, isdn);
            pstmMtQueue.setString(2, cbsSelfCareStarter.getOtpTemplate().replace("{otp}", otp));
            pstmMtQueue.setString(3, cbsSelfCareStarter.getStoreCode());
            pstmMtQueue.setLong(4, 4);
            pstmMtQueue.setString(5, cbsSelfCareStarter.getChannelType());
            pstmMtQueue.setLong(6, cbsSelfCareStarter.getOtpCommandId());
            int i = pstmMtQueue.executeUpdate();
            if (i > 0) {
                return true;
            }
        } catch (Exception e) {
            cbsSelfCareStarter.logMonitor("Insert Mt_Queue error");
            cbsSelfCareStarter.getLog().error("Insert Mt_Queue error", e);
        }

        return false;
    }

    @Path("/getOTP")
    @POST
    public GetOTPResponse getOTP(GetOTPRequest request, @Context HttpServletRequest httpRequest) {

        try {
            String msisdn = request.getMsisdn();
            cbsSelfCareStarter.logMonitor("OPT request from " + msisdn);
            GetOTPResponse getOTPResponse = new GetOTPResponse();
            if (checkIsdn(msisdn)) {
                String otp = generateRandomString(msisdn);
                if (insMtQueue(otp, msisdn)) {
                    cbsSelfCareStarter.logMonitor("OTP for " + msisdn + " is : " + otp);
                    return fillParam(getOTPResponse, otp, 0, "ok");
                } else {
                    return fillParam(getOTPResponse, null, 3, "can't send message");
                }
            } else {
                return fillParam(getOTPResponse, null, 2, "invalid isdn");
            }
        } catch (Exception ex) {
            GetOTPResponse getOTPResponse = new GetOTPResponse();

            return fillParam(getOTPResponse, null, 1, "error gen otp");
        }

    }

    private GetOTPResponse fillParam(GetOTPResponse response, String otp, int code, String des) {
        response.setOtp(otp);
        response.setCode(code);
        response.setDescription(des);
        return response;
    }


}
