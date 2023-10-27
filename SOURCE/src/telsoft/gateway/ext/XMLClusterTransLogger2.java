package telsoft.gateway.ext;

import com.telsoft.cbs.camel.CamelDispatcherSplitThread;
import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.REQUEST_STATUS;
import com.telsoft.thread.ParameterType;
import com.telsoft.thread.ParameterUtil;
import com.telsoft.thread.ThreadParameter;
import com.telsoft.util.AppException;
import com.telsoft.util.FileUtil;
import com.telsoft.util.StringUtil;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import telsoft.gateway.core.log.MessageContext;
import telsoft.gateway.core.log.ServerCommand;
import telsoft.gateway.core.log.data.LogDataHolder;
import telsoft.gateway.core.log.data.TranslatorLogger;
import telsoft.gateway.core.message.XMLMessage;
import telsoft.gateway.core.message.XMLNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>Title: TELSOFT Gateway Extensions</p>
 * <p>
 * <p>Description: TELSOFT Gateway Extensions</p>
 * <p>
 * <p>Copyright: Copyright (c) 2010</p>
 * <p>
 * <p>Company: TELSOFT</p>
 *
 * @author Nguyen Cong Khanh
 * @version 1.0
 */
public class XMLClusterTransLogger2 extends FileTransLogger2 {
    /**
     * @param record MessageContext
     * @param os OutputStream
     * @return boolean
     * @throws Exception
     */

    // don't LoggerFactory
    private final Logger log = LoggerFactory.getLogger(XMLClusterTransLogger2.class);
    private List<HeaderItem> additionData = new ArrayList<HeaderItem>();
    private boolean bWriteCamelHistory = false;

    /**
     * @return Vector
     */
    public Vector getParameterDefinition() {
        Vector vtReturn = new Vector();

        Vector<ThreadParameter> vtHeaderParam = new Vector<ThreadParameter>();
        vtHeaderParam.add(createParameter("FieldName", "", ParameterType.PARAM_TEXTBOX_MAX, "128", "", "0"));
        vtHeaderParam.add(createParameter("XmlTagName", "", ParameterType.PARAM_TEXTBOX_MAX, "128", "", "1"));
        vtHeaderParam.add(createParameter("FieldType", "", ParameterType.PARAM_COMBOBOX, FieldType.class, "", "2"));
        vtHeaderParam.add(createParameter("Default", "", ParameterType.PARAM_TEXTBOX_MAX, "128", "", "3"));
        vtHeaderParam.add(createParameter("Enabled", "", ParameterType.PARAM_COMBOBOX, boolean.class, "", "4"));

        vtReturn.add(createParameter("AdditionData", "", ParameterType.PARAM_TABLE, vtHeaderParam));

        vtReturn.add(createParameter("WriteCamelHistory", "", ParameterType.PARAM_COMBOBOX, boolean.class, "", "4"));
        vtReturn.addAll(super.getParameterDefinition());
        return vtReturn;
    }

    /**
     * @throws AppException
     */
    public void fillParameter() throws AppException {
        List<HeaderItem> tempAdditionData = new ArrayList<HeaderItem>();
        Object obj = getParameter("AdditionData");
        if (obj != null && obj instanceof Vector) {
            Vector<Vector<String>> vtObj = (Vector) obj;
            for (Vector<String> header : vtObj) {
                HeaderItem item = new HeaderItem();
                item.fieldName = header.get(0);
                item.xmlTagName = header.get(1);
                item.fieldType = FieldType.valueOf(header.get(2));
                item._default = header.get(3);
                boolean enabled = ParameterUtil.loadBoolean("Enabled", StringUtil.nvl(header.get(4), "N"));
                if (enabled)
                    tempAdditionData.add(item);
            }
        }
        additionData = tempAdditionData;
        bWriteCamelHistory = loadBoolean("WriteCamelHistory");
        super.fillParameter();
    }

    public final SimpleDateFormat DATE_FORMAT() {
        return new SimpleDateFormat("yyyyMMddHHmmssSSS");
    }

    protected boolean logRecord(MessageContext record, OutputStream os) throws Exception {
        try {
            // Don't log emty request
            if (record.getRequest() == null) {
                return false;
            }

            String strRequest = record.getRequest().getContent();
            if (strRequest == null || strRequest.trim().length() == 0) {
                return false;
            }

            // BEGIN Original code
            TranslatorLogger log = getLogger(record.getServerProtocol(), record.getClientProtocol());
            if (log == null) {
                return false;
            }

            LogDataHolder logDataHolder = log.getLogData(this, record);
            if (logDataHolder == null) {
                return false;
            }

            // Message key
            XMLNode ndRoot = new XMLNode("log", "", null);
            XMLMessage msg = new XMLMessage();
            msg.addChild(ndRoot);
            ndRoot.addChild("command", StringEscape.escapeJava(record.getCommand()));
            ndRoot.addChild("cmd_id", record.getCommandID());
            ndRoot.addChild("server_type", record.getServerProtocol());
            ndRoot.addChild("client_type", record.getClientProtocol());
            ndRoot.addChild("sid", record.getSessionID());
            ndRoot.addChild("gwid", record.getGatewayID());
            ndRoot.addChild("reqid", record.getRequestID());
            ndRoot.addChild("isdn", StringEscape.escapeJava(record.getIsdn()));
            // Request
            ndRoot.addChild("reqtime", formatDate(record.getClientRequestDate()));
            ndRoot.addChild("reqcont", StringEscape.escapeJava(strRequest));
            // Response
            ndRoot.addChild("restime", formatDate1(record.getClientResponseDate()));
            String strResponseContent = null;
            if (record.getClientResponse() != null) {
                strResponseContent = record.getClientResponse().getContent();
                ndRoot.addChild("rescont", StringEscape.escapeJava(strResponseContent));
            }

            ndRoot.addChild("status", REQUEST_STATUS.convertMessageContextStatus(record.getStatus()).getCode());
            ndRoot.addChild("transaction_id", StringUtil.nvl(record.getTransID(), ""));
            ndRoot.addChild("store_transaction_id", record.getPropertyNvl(CbsContansts.STORE_TRANSACTION_ID, ""));
            ndRoot.addChild("client_request_id", record.getPropertyNvl(CbsContansts.REQUEST_ID, ""));
            ndRoot.addChild("user", StringUtil.nvl(record.getProperty(MessageContext.USERNAME), ""));
            ndRoot.addChild("address", StringUtil.nvl(record.getProperty(MessageContext.IPADDRESS), ""));

            XMLNode ndServerCommands = new XMLNode("server_cmds", "", null);
            ndRoot.addChild(ndServerCommands);
            List<ServerCommand> vtServerCommands = record.getServerCommand();
            for (int i = 0; i < vtServerCommands.size(); i++) {
                XMLNode ndServerCommand = new XMLNode("server_cmd", "", null);
                ndServerCommands.addChild(ndServerCommand);
                ServerCommand obj = vtServerCommands.get(i);
                ndServerCommand.addChild("dspid", StringUtil.nvl(obj.strDspId, ""));
                ndServerCommand.addChild("srvid", StringUtil.nvl(obj.strSrvId, ""));
                ndServerCommand.addChild("server_type", StringUtil.nvl(obj.strServerType, ""));
                ndServerCommand.addChild("request", StringUtil.nvl(obj.msgServerRequest, ""));
                ndServerCommand.addChild("request_time", formatDate1(obj.dtServerRequestTime));
                ndServerCommand.addChild("response", StringUtil.nvl(obj.msgServerResponse, ""));
                ndServerCommand.addChild("response_time", formatDate1(obj.dtServerResponseTime));
                ndServerCommand.addChild("dps_status", StringUtil.nvl(obj.getAttribute(CbsContansts.DPS_STATUS), ""));
                ndServerCommand.addChild("dps_message", StringUtil.nvl(obj.getAttribute(CbsContansts.DPS_MESSAGE), ""));
                if (obj.getAttributes() != null && obj.getAttributes().size() > 0) {
                    for (Map.Entry<String, Object> e : ((Map<String, Object>) obj.getAttributes()).entrySet()) {
                        ndServerCommand.addChild(e.getKey(), StringUtil.nvl(e.getValue(), ""));
                    }
                }
            }

            ndRoot.addChild("reason", record.getPropertyNvl("reason", ""));

            if (additionData != null && additionData.size() > 0) {
                for (HeaderItem item : additionData) {
                    switch (item.fieldType) {
                        case FIXED:
                            ndRoot.addChild(item.xmlTagName, item._default);
                            break;
                        case CTX_ATTR:
                            ndRoot.addChild(item.xmlTagName, StringEscape.escapeJava(StringUtil.nvl(record.getAttribute(item.fieldName), item._default)));
                            break;
                        case CTX_PROP:
                            ndRoot.addChild(item.xmlTagName, StringEscape.escapeJava(record.getPropertyNvl(item.fieldName, item._default)));
                            break;
                    }

                }
            }

            //Add print exceptions
            XMLNode ndExceptions = new XMLNode("exceptions", "", null);
            ndRoot.addChild(ndExceptions);
            List<String> exceptions = (List<String>) record.getProperty(CbsContansts.LIST_EXCEPTIONS);
            if(exceptions != null && !exceptions.isEmpty()){
                for (String ex:exceptions) {
                    if(ex != null && !ex.isEmpty()){
                        ndExceptions.addChild("exception", ex);
                    }
                }
            }

            //Add write camel history
            if(bWriteCamelHistory){
                //Add print exceptions
                StringBuilder camelHis = (StringBuilder) record.getProperty(CbsContansts.CB_CAMEL_HISTORY);
                if(camelHis != null) {
                    XMLNode ndCamelHistory = new XMLNode("camel_history", StringEscape.escapeJava(camelHis.toString()), null);
                    ndRoot.addChild(ndCamelHistory);
                }
            }

            synchronized (os) {
                msg.store(os, false);
            }
            return true;
        } catch (Exception e) {
            log.error("LogRecord error", e);
            return false;
        }
    }

    public String formatDate1(Date dt) {
        if (dt == null) {
            return "";
        } else {
            return DATE_FORMAT().format(dt);
        }
    }

    /**
     * @param fl File
     * @throws Exception
     */
    protected void writeBeginFile(File fl) throws Exception {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(fl);
            os.write("<records>\r\n".getBytes());
        } catch (Exception e) {
            log.error("WriteBeginFile error: " + e.getLocalizedMessage());
        } finally {
            FileUtil.safeClose(os);
        }
    }

    /**
     * @param fl File
     * @throws Exception
     */
    protected void writeEndFile(File fl) throws Exception {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(fl, true);
            XMLNode ndRoot = new XMLNode("audit", "", null);
            XMLMessage msg = new XMLMessage();
            msg.addChild(ndRoot);
            msg.setEncoding("");
            ndRoot.addChild("record_count", String.valueOf(miSuccessCount));
            ndRoot.addChild("release_time", StringUtil.format(new java.util.Date(), "yyyyMMddHHmmssSSS"));
            synchronized (os) {
                msg.store(os, false);
            }
            os.write("</records>".getBytes());
        } catch (Exception e) {
            log.error("writeEndFile error: " + e.getLocalizedMessage());
        } finally {
            FileUtil.safeClose(os);
        }
    }

    public enum FieldType {
        FIXED, CTX_ATTR, CTX_PROP;
    }

    class HeaderItem {
        String fieldName;
        String xmlTagName;
        FieldType fieldType;
        String _default;
    }
}
