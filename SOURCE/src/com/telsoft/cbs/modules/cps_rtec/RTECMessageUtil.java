package com.telsoft.cbs.modules.cps_rtec;

import telsoft.gateway.core.cmp.gw_rtec.RTECMessage;
import telsoft.gateway.core.message.XMLNode;

import java.util.Properties;


/**
 * <p>Title: Core Gateway System</p>
 * <p/>
 * <p>Description: A part of TELSOFT Gateway System</p>
 * <p/>
 * <p>Copyright: Copyright (c) 2009</p>
 * <p/>
 * <p>Company: TELSOFT</p>
 *
 * @author HuyenDT
 * @version 1.0
 */
public class RTECMessageUtil {
    public RTECMessageUtil() {
    }

//    <cp_request>
//    <command>login</command>
//    <login>user</login>
//    <password>12345678@</password>
//    </cp_request>

    public static RTECMessage createLoginRequest(String username, String password) throws Exception {
        RTECMessage msg = new RTECMessage();
        msg.setDocType("<!DOCTYPE cp_request SYSTEM \"cp_req_websvr.dtd\">");
        XMLNode ndRoot = msg.getChild("cp_request", true);
        msg.setValue("cp_request.command", "login");
        msg.setValue("cp_request.login", username);
        msg.setValue("cp_request.password", password);
        return msg;
    }

    public static RTECMessage createDisplayRequest(String cp_id, String cp_transaction_id,
                                                   String op_transaction_id, String transaction_description,
                                                   String session_id, String msisdn) throws Exception {
        RTECMessage msg = new RTECMessage();
        msg.setDocType("<!DOCTYPE cp_request SYSTEM \"cp_req_websvr.dtd\">");
        XMLNode ndRoot = msg.getChild("cp_request", true);
        msg.setValue("cp_request.cp_id", cp_id);
        msg.setValue("cp_request.session", session_id);
        msg.setValue("cp_request.cp_transaction_id", cp_transaction_id);
        msg.setValue("cp_request.op_transaction_id", op_transaction_id);
        msg.setValue("cp_request.transaction_description", transaction_description);
        msg.setValue("cp_request.application", "1");
        msg.setValue("cp_request.action", "90");

        msg.setValue("cp_request.user_id", msisdn);
        Properties prtUser = new Properties();
        prtUser.put("type", "MSISDN");
        msg.getChild("cp_request.user_id").mprt = prtUser;
        return msg;
    }

    public static RTECMessage createPpmDisplayRequest(String cp_id, String cp_transaction_id,
                                                      String op_transaction_id, String transaction_description,
                                                      String session_id, String msisdn) throws Exception {
        RTECMessage msg = new RTECMessage();
        msg.setDocType("<!DOCTYPE cp_request SYSTEM \"cp_req_websvr.dtd\">");
        XMLNode ndRoot = msg.getChild("cp_request", true);
        msg.setValue("cp_request.cp_id", cp_id);
        msg.setValue("cp_request.session", session_id);
        msg.setValue("cp_request.cp_transaction_id", cp_transaction_id);
        msg.setValue("cp_request.op_transaction_id", op_transaction_id);
        msg.setValue("cp_request.transaction_description", transaction_description);
        msg.setValue("cp_request.application", "7");
        msg.setValue("cp_request.action", "0");

        msg.setValue("cp_request.user_id", msisdn);
        Properties prtUser = new Properties();
        prtUser.put("type", "MSISDN");
        msg.getChild("cp_request.user_id").mprt = prtUser;
        return msg;
    }

    public static RTECMessage createLogoutRequest(String sessionId) {
        RTECMessage msg = new RTECMessage();
        msg.setDocType("<!DOCTYPE cp_request SYSTEM \"cp_req_websvr.dtd\">");
        XMLNode ndRoot = msg.getChild("cp_request", true);
        msg.setValue("cp_request.command", "logout");
        msg.setValue("cp_request.session", sessionId);
        return msg;
    }
}
