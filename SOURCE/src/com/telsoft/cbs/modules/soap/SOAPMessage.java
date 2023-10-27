package com.telsoft.cbs.modules.soap;

import com.telsoft.util.StringUtil;
import telsoft.gateway.core.cmp.gw_rtec.RTECMessage;
import telsoft.gateway.core.message.XMLMessage;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringBufferInputStream;

public class SOAPMessage extends XMLMessage {
    private String mstrDocType = null;

    public static final String getErrorDesc(int code) {
        switch(code) {
            case 0:
                return "OK";
            case 2:
                return "NOK_REJECT_DUE_TO_SERVICE";
            case 3:
                return "NOK_MALFORMED_REQUEST";
            case 4:
                return "NOK_MALFORMATTED_PARAMETER";
            case 5:
                return "NOK_INVALID_PARAMETER";
            case 6:
                return "NOK_UNEXPECTED_VALUE";
            case 7:
                return "NOK_TIME_OUT";
            case 8:
                return "NOK_MALFORMED_XML_PROLOG";
            case 9:
                return "NOK_XML_PARSE_ERROR";
            case 10:
                return "NOK_NOT_ENOUGH_CREDIT";
            case 50:
                return "OK_POSTPAID_SUBSCRIBER";
            case 51:
                return "NOK_POSTPAID_QUOTA";
            case 61:
                return "NOK_RATE_CODE";
            case 62:
                return "NOK_PREPAID_QUOTA";
            case 68:
                return "OK_FREE_QUOTA";
            case 69:
                return "OK_LAST_SLICE";
            case 201:
                return "NOK Account_Not found";
            case 234:
                return "NOK_No_credit";
            case 243:
                return "Nok_high_debit";
            case 244:
                return "Nok refund due to service";
            case 245:
                return "NOK Account_Not found";
            case 246:
                return "Nok refund due to service";
            case 248:
                return "Nok credit postpaid";
            case 252:
                return "Nok refund refused";
            case 253:
                return "Nok no more credit available";
            default:
                return code >= 202 && code <= 215 ? "NOK_Account_status" : "Nok technical problem";
        }
    }

    public static final String getErrorDescICC(int code) {
        switch(code) {
            case 0:
                return "OK";
            case 2:
                return "NOK_REJECT_DUE_TO_SERVICE";
            case 3:
                return "NOK_MALFORMED_REQUEST";
            case 4:
                return "NOK_MALFORMATTED_PARAMETER";
            case 5:
                return "NOK_INVALID_PARAMETER";
            case 6:
                return "NOK_UNEXPECTED_VALUE";
            case 7:
                return "NOK_TIME_OUT";
            case 8:
                return "NOK_MALFORMED_XML_PROLOG";
            case 9:
                return "NOK_XML_PARSE_ERROR";
            case 10:
                return "NOK_NOT_ENOUGH_CREDIT";
            case 12:
                return "NOK_BUCKET_UPDATE";
            case 61:
                return "NOK_SERVICE_IDENTIFIER";
            case 62:
                return "BUCKET_NOT_FOUND";
            case 63:
                return "BUCKET_INVALID";
            case 69:
                return "OK_LAST_SLICE";
            case 70:
                return "OK_DEFAULT_FREE_SLICE";
            case 201:
                return "NOK_ACCOUNT_NOT_FOUND";
            case 202:
                return "NOK_ACCOUNT_NOT_VALIDATED";
            case 203:
                return "NOK_ACCOUNT_NOT_ACTIVATED";
            case 205:
                return "NOK_ACCOUNT_IS_INACTIVE";
            case 207:
                return "NOK_ACCOUNT_IS_EXPIRED";
            case 208:
                return "NOK_ACCOUNT_BLOCKED";
            case 209:
                return "NOK_ACCOUNT_DOES_NOT_ALLOW_ACTION";
            case 216:
                return "NOK_MAX_CREDIT";
            case 232:
                return "NOK_NEG_PARAM";
            case 251:
                return "NOK_REFUND_OUTDATED";
            case 252:
                return "NOK_RELOAD_NOT_ALLOWED";
            case 253:
                return "NOK_NO_MORE_AVAILABLE_CREDIT";
            default:
                return code >= 202 && code <= 215 ? "NOK_Account_status" : "Nok technical problem";
        }
    }

    public SOAPMessage(String strRequest) throws Exception {
        StringBufferInputStream in = new StringBufferInputStream(strRequest);

        try {
            this.load(in);
        } finally {
            in.close();
        }

    }

    public SOAPMessage(InputStream in) throws Exception {
        this.load(in);
    }

    public SOAPMessage() {
    }

    public void store(OutputStream os, boolean bIncludeHeader) throws Exception {
        if (bIncludeHeader) {
            if (this.getDoctype() != null && !this.getDoctype().equals("")) {
                os.write(this.getDoctype().getBytes());
                os.write("\r\n".getBytes());
            }

            if (this.getEncoding().equals("")) {
                os.write("<?xml version='1.0'?>\r\n".getBytes());
            } else {
                os.write(("<?xml version='1.0' encoding='" + this.getEncoding() + "'?>\r\n").getBytes());
            }
        }

        super.store(os, false);
    }

    public String getDoctype() {
        return StringUtil.nvl(this.mstrDocType, "");
    }

    public void setDocType(String strDocType) {
        this.mstrDocType = strDocType;
    }

    public void load(InputStream is) throws Exception {
        StringBuffer buf = new StringBuffer();
        int i;
        if (is != null) {
            byte[] bytebuf = new byte[8192];

            while((i = is.read(bytebuf)) != -1) {
                buf.append(new String(bytebuf, 0, i));
            }
        }

        i = buf.indexOf("<!DOCTYPE");
        if (i >= 0) {
            int i2 = buf.indexOf(">", i);
            this.setDocType(buf.substring(i, i2 + 1));
            buf.delete(i, i2 + 1);
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(buf.toString().trim().getBytes());

        try {
            super.load(bis);
        } finally {
            bis.close();
        }

    }

    public static String getSoapCommand(SOAPMessage msgRequest) {
        String strOperationName = StringUtil.nvl(msgRequest.getValue("cp_request.command"), "");
        if (strOperationName == null || strOperationName.equals("")) {
            try {
                String strApplication = msgRequest.getValue("cp_request.application");
                if (strApplication.length() > 0) {
                    String strAction = msgRequest.getValue("cp_request.action");
                    if (strAction.length() > 0) {
                        strOperationName = "rtec_" + strApplication + "-" + strAction;
                    }
                }
            } catch (Exception var4) {
            }
        }

        return strOperationName;
    }

    public String getCommandName() {
        return getSoapCommand(this);
    }
}
