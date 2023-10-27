package com.telsoft.cbs.cdr;

import com.telsoft.cbs.domain.CBResponse;
import com.telsoft.cbs.utils.IsdnUtils;
import com.telsoft.thread.ParameterType;
import com.telsoft.thread.ThreadParameter;
import com.telsoft.util.AppException;
import com.telsoft.util.FileUtil;
import com.telsoft.util.StringEscapeUtil;
import com.telsoft.util.StringUtil;
import org.jdiameter.api.Avp;
import org.jdiameter.api.AvpSet;
import org.jdiameter.api.validation.AvpRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import telsoft.gateway.core.log.MessageContext;
import telsoft.gateway.core.message.*;
import telsoft.gateway.ext.FileTransLogger2;
import telsoft.jdiameter.message.AvpDictionary;
import telsoft.jdiameter.message.DiameterMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * <p>Title: TELSOFT Gateway Extensions</p>
 * <p/>
 * <p>Description: TELSOFT Gateway Extensions</p>
 * <p/>
 * <p>Copyright: Copyright (c) 2010</p>
 * <p/>
 * <p>Company: TELSOFT</p>
 *
 * @author Pham Vu Tuan Anh
 * @version 1.0
 */

public class ExportCDR extends FileTransLogger2 {
    public static final String COMMAND = "COMMAND";
    public static final String CLIENT_TYPE = "CLIENT_TYPE";
    public static final String SERVER_TYPE = "SERVER_TYPE";
    public static final String REQTIME = "REQTIME";
    public static final String RESTIME = "RESTIME";
    public static final String STATUS = "STATUS";
    public static final String ERROR_DESC = "ERROR_DESC";
    public static final String USERNAME = "USERNAME";
    public static final String IPADDRESS = "IPADDRESS";
    public static final String PREV_CONTEXT = "PREV_";
    public static final String[] ALLOW_HEADER = new String[]{COMMAND, CLIENT_TYPE, SERVER_TYPE, REQTIME, RESTIME,
            STATUS, ERROR_DESC, USERNAME, IPADDRESS};
    private final Logger log = LoggerFactory.getLogger(ExportCDR.class);
    private String mstrHeader;
    private String[] marrayHeader;
    private EXPORT_TYPE mExportType;
    private String mstrEOFSymbol = ",";
    private String mstrEORSymbol = "\n";
    private byte[] mbtEORSymbol = mstrEORSymbol.getBytes();
    private Vector<ProtocolHeader> vtAdditionHeader = new Vector<ProtocolHeader>();

    ;

    public static void main(String[] args) {

    }

    ;

    /**
     * @param vtProtocolHeader Vector
     * @param serverProtocol   String
     * @param clientProtocol   String
     * @return ProtocolHeader
     */
    private static ProtocolHeader findProtocolHeader(List<ProtocolHeader> vtProtocolHeader, String serverProtocol,
                                                     String clientProtocol) {
        for (ProtocolHeader ph : vtProtocolHeader) {
            if (("*".equals(ph.serverProtocol) || serverProtocol.equals(ph.serverProtocol)) &&
                    ("*".equals(ph.clientProtocol) || clientProtocol.equals(ph.clientProtocol))) {
                return ph;
            }
        }
        return null;
    }

    /**
     * @param msg DiameterMessage
     * @return Map
     * @throws Exception
     */
    private static Map convertDIAMETERtoMap(DiameterMessage msg) throws Exception {
        AvpSet node = msg.getDiameterMessage().getAvps();
        Map prop = new LinkedHashMap();
        if (node.size() <= 0) {
            throw new Exception("Empty message");
        }

        Avp[] avps = node.asArray();
        for (Avp avp : avps) {
            getAvpRecord(avp.getCode() + ".", avp, prop);
        }

        return prop;
    }

    private static Map convertCBResponseToMap(CBResponse msg) throws Exception {
        Map prop = new LinkedHashMap();
        prop.put(CBResponse.CODE,msg.getCode());
        prop.put(CBResponse.CODE+".code",msg.getCode().getCode());
        prop.putAll(msg.getValues());
        return prop;
    }

    /**
     * @param msg XMLMessage
     * @return Map
     * @throws Exception
     */
    public static Map convertXMLtoMap(XMLMessage msg) throws Exception {
        XMLNode node = msg.getRootNode();
        Map prop = new LinkedHashMap();
        if (node.mndFirstChild == null) {
            throw new Exception("Empty message");
        }

        node = node.mndFirstChild;
        do {
            getRecord(node.mstrName + ".", node, prop);
            node = node.mndNext;
        } while (node != null);
        return prop;
    }

    /**
     * @param nd XMLNode
     * @return String
     */
    private static String getAttribute(XMLNode nd) {
        StringBuilder sb = new StringBuilder();
        Properties mapAttr = nd.mprt;
        if (mapAttr != null) {
            for (Map.Entry entry : mapAttr.entrySet()) {
                sb.append("#").append(entry.getKey()).append(":").append(entry.getValue());
            }
        }
        return sb.toString();
    }

    /**
     * @param parent  String
     * @param nd      XMLNode
     * @param mRecord Map
     * @throws Exception
     */
    private static void getRecord(String parent, XMLNode nd, Map<String, String> mRecord) throws Exception {
        if (nd.mndFirstChild == null) {
            if (parent.endsWith(".")) {
                parent = parent.substring(0, parent.length() - 1);
            }
            String str = getAttribute(nd);
            if (str.length() > 0) {
                mRecord.put(((nd.getNodeType() == XMLNode.CDATA_NODE ? "!" : "") + parent + str).toLowerCase(),
                        nd.mstrValue);
            } else {
                mRecord.put(((nd.getNodeType() == XMLNode.CDATA_NODE ? "!" : "") + parent).toLowerCase(), nd.mstrValue);
            }
        } else {
            XMLNode child = nd.mndFirstChild;
            while (child != null) {
                getRecord(parent + child.mstrName + ".", child, mRecord);
                child = child.mndNext;
            }
        }
    }

    /**
     * @param parent  String
     * @param avp     Avp
     * @param mRecord Map
     * @throws Exception
     */
    private static void getAvpRecord(String parent, Avp avp, Map<String, String> mRecord) throws Exception {
        int iCode = avp.getCode();
        long lVendorId = avp.getVendorId();
        AvpRepresentation present = AvpDictionary.INSTANCE.getAvp(iCode, lVendorId);

        if (present == null || !present.isGrouped()) {
            if (parent.endsWith(".")) {
                parent = parent.substring(0, parent.length() - 1);
            }
            String str = telsoft.jdiameter.message.MessageUtil.getAvpValue(avp);
            mRecord.put(parent, str);
        } else {
            Avp[] avps = avp.getGrouped().asArray();
            for (Avp a : avps) {
                getAvpRecord(parent + a.getCode() + ".", a, mRecord);
            }
        }
    }

    /**
     * @return Vector
     */
    public Vector getParameterDefinition() {
        Vector<ThreadParameter> vtReturn = new Vector<ThreadParameter>();
        vtReturn.add(createParameter("Header", "", ParameterType.PARAM_TEXTAREA_MAX, "1024", ""));

        Vector<String> vtStatus = new Vector<String>();
        vtStatus.add("Y");
        vtStatus.add("N");

        Vector<ThreadParameter> vtHeaderParam = new Vector<ThreadParameter>();
        vtHeaderParam.add(createParameter("FIELD_NAME", "", ParameterType.PARAM_TEXTBOX_MAX, "128", "", "0"));
        vtHeaderParam.add(createParameter("COMMAND_PARAM_NAME", "", ParameterType.PARAM_TEXTBOX_MAX, "128", "", "1"));
        vtHeaderParam.add(createParameter("MESSAGE_TYPE", "", ParameterType.PARAM_COMBOBOX, MSG_TYPE.class, "", "2"));
        vtHeaderParam.add(createParameter("MESSAGE_TYPE_SECONDARY", "", ParameterType.PARAM_COMBOBOX, MSG_TYPE.class,
                "", "3"));
        vtHeaderParam.add(createParameter("DEFAULT", "", ParameterType.PARAM_TEXTBOX_MAX, "128", "", "4"));
        vtHeaderParam.add(createParameter("ENABLED", "", ParameterType.PARAM_COMBOBOX, vtStatus, "", "5"));
        vtHeaderParam.add(createParameter("FORMAT", "", ParameterType.PARAM_TEXTBOX_MAX, "128","Format for date value", "6"));

        Vector<ThreadParameter> vtAdditionHeader = new Vector<ThreadParameter>();
        vtAdditionHeader.add(createParameter("ServerProtocol", "", ParameterType.PARAM_TEXTBOX_MAX, "100", "", "0",
                "Logging"));
        vtAdditionHeader.add(createParameter("ClientProtocol", "", ParameterType.PARAM_TEXTBOX_MAX, "100", "", "1"));
        vtAdditionHeader.add(createParameter("Header", "", ParameterType.PARAM_TABLE, vtHeaderParam, "", "2"));

        vtReturn.add(createParameter("AdditionHeader", "", ParameterType.PARAM_TABLE, vtAdditionHeader));

        vtReturn.add(createParameter("ExportType", "", ParameterType.PARAM_COMBOBOX, EXPORT_TYPE.class, ""));
        vtReturn.add(createParameter("Seperator", "", ParameterType.PARAM_TEXTBOX_MAX, "10"));
        vtReturn.add(createParameter("EORSymbol", "", ParameterType.PARAM_TEXTBOX_MAX, "10"));
        vtReturn.addAll(super.getParameterDefinition());
        return vtReturn;
    }

    /**
     * @throws AppException
     */
    public void fillParameter() throws AppException {
        mstrHeader = StringUtil.nvl(getParameter("Header"), "").trim();

        Vector<ProtocolHeader> tempAdditionHeader = new Vector<ProtocolHeader>();
        Object obj = getParameter("AdditionHeader");
        if (obj != null && obj instanceof Vector) {
            Vector vtObj = (Vector) obj;
            for (int i = 0; i < vtObj.size(); i++) {
                Vector vtRow = (Vector) vtObj.get(i);
                String serverProtocol = loadString("AdditionHeader.ServerProtocol", StringUtil.nvl(vtRow.get(0), ""));
                String clientProtocol = loadString("AdditionHeader.ClientProtocol", StringUtil.nvl(vtRow.get(1), ""));

                Vector header = (Vector) vtRow.get(2);
                if (header == null || !(header instanceof Vector) || (((Vector) header).size() == 0)) {
                    throw new AppException("Field HEADER is empty or invalided", "AdditionHeader.Header");
                }

                Vector vtHeader = new Vector();
                for (int j = 0; j < header.size(); j++) {
                    Vector vtElement = (Vector) header.elementAt(j);
                    String strStatus = StringUtil.nvl(vtElement.elementAt(5), "Y");
                    if (strStatus.equalsIgnoreCase("Y")) {
                        vtHeader.add(vtElement);
                    }
                }
                ProtocolHeader proHeader = new ProtocolHeader();
                proHeader.serverProtocol = serverProtocol.toUpperCase();
                proHeader.clientProtocol = clientProtocol.toUpperCase();
                proHeader.header = vtHeader;
                proHeader.arrayHeader = null;
                tempAdditionHeader.add(proHeader);
            }
        }
        Collections.sort(tempAdditionHeader, new java.util.Comparator<ProtocolHeader>() {
            public int compare(ProtocolHeader o1, ProtocolHeader o2) {
                String s1 = o1.serverProtocol;
                String s2 = o2.serverProtocol;
                String c1 = o1.clientProtocol;
                String c2 = o2.clientProtocol;

                int i1 = 2;
                int i2 = 2;

                if ("*".equals(s1)) {
                    i1--;
                }

                if ("*".equals(c1)) {
                    i1--;
                }

                if ("*".equals(s2)) {
                    i2--;
                }

                if ("*".equals(c2)) {
                    i2--;
                }

                if (i1 != i2) {
                    return i1 - i2;
                } else {
                    int i = s1.compareTo(s2);
                    if (i != 0) {
                        return i;
                    }
                    return c1.compareTo(c2);
                }
            }
        });
        vtAdditionHeader = tempAdditionHeader;

        String strExpType = loadString("ExportType");
        mExportType = EXPORT_TYPE.valueOf(strExpType);
        mstrEOFSymbol = StringEscapeUtil.unescapeJava(loadString("Seperator"));
        mstrEORSymbol = StringEscapeUtil.unescapeJava(loadString("EORSymbol"));
        mbtEORSymbol = mstrEORSymbol.getBytes();
        super.fillParameter();
    }

    /**
     * @throws Exception
     */
    public void validateParameter() throws Exception {
        super.validateParameter();
        mstrHeader = mstrHeader.toUpperCase().trim();
        if (!mstrHeader.equals("")) {
            String arrayHeader[] = mstrHeader.split(",");
            for (String item : arrayHeader) {
                //String itemValidated = loadString("Header.Item", item);
                String itemValidated = item;
                if (!item.equals("")) {
                    boolean bFound = false;
                    for (String item2 : ALLOW_HEADER) {
                        if (itemValidated.equalsIgnoreCase(item2)) {
                            bFound = true;
                            break;
                        }
                    }
                    if (!bFound) {
                        throw new AppException("Field '" + itemValidated + "' is not supported");
                    }
                }
            }
            marrayHeader = arrayHeader;
        } else {
            marrayHeader = new String[0];
        }

        ProtocolHeader[] headers = new ProtocolHeader[vtAdditionHeader.size()];
        vtAdditionHeader.toArray(headers);
        for (int i = 0; i < headers.length; i++) {
            ProtocolHeader proHeader = headers[i];
            HeaderItem aHeader[] = new HeaderItem[proHeader.header.size()];
            Set flags = new HashSet();
            log.debug("header size= {}", aHeader.length);
            for (int j = 0; j < proHeader.header.size(); j++) {
                Vector vtRow = (Vector) proHeader.header.get(j);
                aHeader[j] = new HeaderItem();
                aHeader[j].FIELD_NAME = loadString("ProtocolHeader.FIELD_NAME", StringUtil.nvl(vtRow.get(0), ""));
                aHeader[j].FIELD_NAME = aHeader[j].FIELD_NAME.toLowerCase();
                aHeader[j].COMMAND_PARAM_NAME = loadString("ProtocolHeader.COMMAND_PARAM_NAME",
                        StringUtil.nvl(vtRow.get(1), ""));
                aHeader[j].COMMAND_PARAM_NAME = aHeader[j].COMMAND_PARAM_NAME.toLowerCase();
                String strMsgType = loadString("ProtocolHeader.MESSAGE_TYPE", StringUtil.nvl(vtRow.get(2), ""));
                aHeader[j].msgType = MSG_TYPE.valueOf(strMsgType);
                if (aHeader[j].msgType == MSG_TYPE.NONE) {
                    throw new AppException("ProtocolHeader.MESSAGE_TYPE is not empty");
                }
                String strMsgTypeSecond = StringUtil.nvl(vtRow.get(3), "");
                aHeader[j].msgTypeSecondary = MSG_TYPE.valueOf(strMsgTypeSecond);
                aHeader[j]._default = StringUtil.nvl(vtRow.get(4), "");
                if(vtRow.size()>=7) {
                    aHeader[j].format = StringUtil.nvl(vtRow.get(6), "");
                }
                flags.add(aHeader[j].msgType);
                flags.add(aHeader[j].msgTypeSecondary);
            }
            proHeader.arrayHeader = aHeader;
            proHeader.flags = flags;
        }
    }

    /**
     * @param record MessageContext
     * @param aof    ArrayOfFile
     * @return boolean
     * @throws Exception
     */
    protected boolean logRecord(MessageContext record, OutputStream os) throws Exception {
        log.debug("Begin log record");
        // Don't log emty request
        if (record.getRequest() == null) {
            return false;
        }

        // TODO : Filter messages (log messages by a special condition)

//        String strRequest = record.getRequest().getContent();
//        if (strRequest == null || strRequest.trim().length() == 0) {
//            return false;
//        }

        Map<String, Object> mpAllData = new HashMap<String, Object>() {
            public Object put(String key, Object value) {
                if (value == null) {
                    value = "";
                }
                return super.put(key.toLowerCase(), value);
            }

            public Object get(Object key) {
                return super.get(key.toString().toLowerCase());
            }
        };

        List<GatewayMessage> serverResponseMessage = record.getServerResponses();
        List<GatewayMessage> serverRequestMessage = record.getServerRequests();

        String serverType = StringUtil.nvl(record.getServerProtocol(), "").toUpperCase();
        String clientType = StringUtil.nvl(record.getClientProtocol(), "").toUpperCase();

        mpAllData.put(STATUS, record.getStatus() == MessageContext.STATUS_SUCCESS ? 1 : 0);
        mpAllData.put(REQTIME, formatDate(record.getClientRequestDate()));
        mpAllData.put(RESTIME, formatDate(record.getClientResponseDate()));
        mpAllData.put(COMMAND, record.getCommand());
        mpAllData.put(SERVER_TYPE, serverType);
        mpAllData.put(CLIENT_TYPE, clientType);
        mpAllData.put(ERROR_DESC, record.getErrorDescription());
        mpAllData.put(USERNAME, record.getProperty(MessageContext.USERNAME));
        mpAllData.put(IPADDRESS, record.getProperty(MessageContext.IPADDRESS));
        log.debug("applyInformation");

        boolean bApply = applyInformation(serverType, clientType, mpAllData, record.getRequest(),
                record.getClientResponse(),
                serverRequestMessage,
                serverResponseMessage, record);
        if (bApply) {
            log.debug("write record");
            boolean b = writeRecord(mpAllData, os, serverType, clientType);
            log.debug("end write record");
            return b;
        } else {
            return bApply;
        }
    }

    /**
     * @param mpAllData  Map
     * @param os         OutputStream
     * @param serverType String
     * @param clientType String
     * @return boolean
     * @throws Exception
     */
    public boolean writeRecord(Map mpAllData, OutputStream os, String serverType, String clientType) throws Exception {
        if (mExportType == EXPORT_TYPE.XML) {
            //Add node record type for each protocol
            Properties prp = new Properties();
            prp.setProperty("type", serverType);

            XMLNode ndRecord = new XMLNode("record", "", prp);
            XMLMessage msg = new XMLMessage();
            msg.addChild(ndRecord);
            //Add cac the con thuoc header chung
            for (int i = 0; i < marrayHeader.length; i++) {
                String strNodeName = marrayHeader[i];
                String strValue = StringUtil.nvl(mpAllData.get(strNodeName), "");
                XMLNode ndElement = new XMLNode(strNodeName.toLowerCase(), strValue, null);
                ndRecord.addChild(ndElement);
            }
            //Add cac the con Addition

            ProtocolHeader ph = findProtocolHeader(vtAdditionHeader, serverType, clientType);

            if (ph != null) {
                for (int i = 0; i < ph.arrayHeader.length; i++) {
                    HeaderItem hi = ph.arrayHeader[i];
                    //todo format date value
                    String strValue = "";
                    if(mpAllData.get(hi.msgType.toString() + "." + hi.FIELD_NAME) != null &&
                            mpAllData.get(hi.msgType.toString() + "." + hi.FIELD_NAME) instanceof Date &&
                            hi.format != null && !hi.format.isEmpty()){
                        try{
                            strValue = new SimpleDateFormat(hi.format).format(mpAllData.get(hi.msgType.toString() + "." + hi.FIELD_NAME));
                        }catch (Exception ignore){
                            ;
                        }
                    }
                    if("".equals(strValue)) {
                        strValue = StringUtil.nvl(mpAllData.get(hi.msgType.toString() + "." + hi.FIELD_NAME),
                                hi._default);
                    }
                    if (strValue.equals("")) {
                        strValue = StringUtil.nvl(mpAllData.get(hi.msgTypeSecondary.toString() + "." + hi.FIELD_NAME),
                                hi._default);
                    }
                    XMLNode ndElement = new XMLNode(hi.FIELD_NAME, strValue, null);
                    ndRecord.addChild(ndElement);
                }
            }
            //Store
            synchronized (os) {
                msg.store(os, false);
            }
            return true;
        } else {
            StringBuilder buf = new StringBuilder();

            //Add cac the con thuoc header chung
            for (int i = 0; i < marrayHeader.length; i++) {
                String strNodeName = marrayHeader[i];
                String strValue = StringUtil.nvl(mpAllData.get(strNodeName), "");
                buf.append(strValue).append(mstrEOFSymbol);
            }
            //Add cac the con Addition

            ProtocolHeader ph = findProtocolHeader(vtAdditionHeader, serverType, clientType);

            if (ph != null) {
                for (int i = 0; i < ph.arrayHeader.length; i++) {
                    HeaderItem hi = ph.arrayHeader[i];

                    String strValue = "";
                    if(mpAllData.get(hi.msgType.toString() + "." + hi.FIELD_NAME) != null &&
                            mpAllData.get(hi.msgType.toString() + "." + hi.FIELD_NAME) instanceof Date &&
                            hi.format != null && !hi.format.isEmpty()){
                        try{
                            strValue = new SimpleDateFormat(hi.format).format(mpAllData.get(hi.msgType.toString() + "." + hi.FIELD_NAME));
                        }catch (Exception ignore){
                            ;
                        }
                    }
                    if("".equals(strValue)) {
                        strValue = StringUtil.nvl(mpAllData.get(hi.msgType.toString() + "." + hi.FIELD_NAME),
                                hi._default);
                    }

                    if (strValue.equals("")) {
                        strValue = StringUtil.nvl(mpAllData.get(hi.msgTypeSecondary.toString() + "." + hi.FIELD_NAME),
                                hi._default);
                    }

                    strValue = StringUtil.replaceAll(strValue, "\r", "");
                    strValue = StringUtil.replaceAll(strValue, "\n", "");

                    buf.append(strValue).append(mstrEOFSymbol);
                }
            }
            if (buf.length() > 0) {
                buf.delete(buf.length() - mstrEOFSymbol.length(), buf.length());
            }

            synchronized (os) {
                os.write(buf.toString().getBytes());
                os.write(mbtEORSymbol);
            }
            return true;
        }
    }

    /**
     * @param fl File
     * @throws Exception
     */
    protected void writeBeginFile(File fl) throws Exception {
        if (mExportType == EXPORT_TYPE.XML) {
            FileOutputStream os = null;
            try {
                os = new FileOutputStream(fl, true);
                os.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n".getBytes());
                os.write("<cdr>\r\n".getBytes());
            } finally {
                FileUtil.safeClose(os);
            }
        }
    }

    /**
     * @param fl File
     * @throws Exception
     */
    protected void writeEndFile(File fl) throws Exception {
        if (mExportType == EXPORT_TYPE.XML) {
            FileOutputStream os = null;
            try {
                os = new FileOutputStream(fl, true);
                os.write("</cdr>".getBytes());
            } finally {
                FileUtil.safeClose(os);
            }
        }
    }

    /**
     * @param mpAllData            Map
     * @param clientRequest        GatewayMessage
     * @param clientResponse       GatewayMessage
     * @param serverRequestMessage PlainMessage
     * @param serverMessage        PlainMessage
     * @throws Exception
     */
    public String[] getForceStringList(Object obj) {
        if (obj == null) {
            return null;
        }

        if (obj instanceof String[]) {
            return (String[]) obj;
        }

        if (obj instanceof String) {
            return StringUtil.toStringArray((String) obj, "&");
        }
        return null;
    }

    private boolean applyInformation(String serverProtocol, String clientProtocol, Map mpAllData,
                                     GatewayMessage clientRequest,
                                     GatewayMessage clientResponse, List<GatewayMessage> serverRequest,
                                     List<GatewayMessage> serverResponse, MessageContext record) throws Exception {
        ProtocolHeader ph = findProtocolHeader(vtAdditionHeader, serverProtocol, clientProtocol);
        if (ph == null) {
            debugMonitor("Addition header for SP : " + serverProtocol + ",CP:" + clientProtocol + " not found");
            return false;
        }

        MSG_TYPE[] msgTypes = MSG_TYPE.values();
        Map[] mpMessage = new Map[msgTypes.length];
        for (int i = 0; i < mpMessage.length; i++) {
            mpMessage[msgTypes[i].ordinal()] = new HashMap();
        }

        if (ph.flags.contains(MSG_TYPE.FIXED)) {
            mpMessage[MSG_TYPE.FIXED.ordinal()].put("status",
                    record.getStatus() == MessageContext.STATUS_SUCCESS ? 1 : 0);
            mpMessage[MSG_TYPE.FIXED.ordinal()].put("reqtime", formatDate(record.getClientRequestDate()));
            mpMessage[MSG_TYPE.FIXED.ordinal()].put("restime", formatDate(record.getClientResponseDate()));
            mpMessage[MSG_TYPE.FIXED.ordinal()].put("server_type", serverProtocol);
            mpMessage[MSG_TYPE.FIXED.ordinal()].put("client_type", clientProtocol);
            mpMessage[MSG_TYPE.FIXED.ordinal()].put("error_desc", record.getErrorDescription());
            mpMessage[MSG_TYPE.FIXED.ordinal()].put("msisdn",
                    IsdnUtils.correctISDN(StringUtil.nvl(record.getProperty(MessageContext.MSISDN), "")));
            mpMessage[MSG_TYPE.FIXED.ordinal()].put("user_name", record.getProperty(MessageContext.USERNAME));
            mpMessage[MSG_TYPE.FIXED.ordinal()].put("ipaddress", record.getProperty(MessageContext.IPADDRESS));
            mpMessage[MSG_TYPE.FIXED.ordinal()].put("command", record.getCommand());
        }
        if (ph.flags.contains(MSG_TYPE.CTX_ATTR)) {
            analysisContextAttribute(mpMessage[MSG_TYPE.CTX_ATTR.ordinal()], record);
        }
        if (ph.flags.contains(MSG_TYPE.CTX_PROP)) {
            analysisContextProperties(mpMessage[MSG_TYPE.CTX_PROP.ordinal()], record);
        }
        if (ph.flags.contains(MSG_TYPE.CLIENT_REQUEST)) {
            analysisMessage(mpMessage[MSG_TYPE.CLIENT_REQUEST.ordinal()], clientRequest);
        }
        if (ph.flags.contains(MSG_TYPE.CLIENT_RESPONSE)) {
            analysisMessage(mpMessage[MSG_TYPE.CLIENT_RESPONSE.ordinal()], clientResponse);
        }

        if (ph.flags.contains(MSG_TYPE.SERVER_REQUEST)) {
            for (GatewayMessage gm : serverRequest) {
                analysisMessage(mpMessage[MSG_TYPE.SERVER_REQUEST.ordinal()], gm);
            }
        }
        if (ph.flags.contains(MSG_TYPE.SERVER_RESPONSE)) {

            for (GatewayMessage gm : serverResponse) {
                analysisMessage(mpMessage[MSG_TYPE.SERVER_RESPONSE.ordinal()], gm);
            }
        }
        if (log.isTraceEnabled()) {
            log.debug("Gathered all informations");
            for (int i = 0; i < mpMessage.length; i++) {
                log.trace("{}:{}", msgTypes[i].toString(), mpMessage[msgTypes[i].ordinal()]);
            }
        }
        for (int i = 0; i < ph.arrayHeader.length; i++) {
            HeaderItem hi = ph.arrayHeader[i];

            String strValue = null;
            Object oValue = mpMessage[hi.msgType.ordinal()].get(hi.COMMAND_PARAM_NAME);
            if(oValue != null && oValue instanceof Date &&
                    hi.format != null && !hi.format.isEmpty()){
                try{
                    strValue = new SimpleDateFormat(hi.format).format(oValue);
                }catch (Exception ignore){
                    ;
                }
            }
            if(strValue != null){
                mpAllData.put(hi.msgType.toString() + "." + hi.FIELD_NAME, strValue);
            }else {
                mpAllData.put(hi.msgType.toString() + "." + hi.FIELD_NAME,
                        StringUtil.nvl(oValue, StringUtil.nvl(hi._default, "")));
            }
            if (hi.msgTypeSecondary != MSG_TYPE.NONE) {
                String strValue2 = null;
                Object oValue2 = mpMessage[hi.msgTypeSecondary.ordinal()].get(hi.COMMAND_PARAM_NAME);
                if(oValue2 != null && oValue2 instanceof Date &&
                        hi.format != null && !hi.format.isEmpty()){
                    try{
                        strValue2 = new SimpleDateFormat(hi.format).format(oValue2);
                    }catch (Exception ignore){
                        ;
                    }
                }
                if(strValue2 != null){
                    mpAllData.put(hi.msgTypeSecondary.toString() + "." + hi.FIELD_NAME, strValue2);
                }else {
                    mpAllData.put(hi.msgTypeSecondary.toString() + "." + hi.FIELD_NAME,
                            StringUtil.nvl(oValue2, StringUtil.nvl(hi._default, "")));
                }

                mpAllData.put(hi.msgTypeSecondary.toString() + "." + hi.FIELD_NAME,
                        StringUtil.nvl(mpMessage[hi.msgTypeSecondary.ordinal()].get(hi.COMMAND_PARAM_NAME),
                                StringUtil.nvl(hi._default, "")));
            }
        }
        return true;

    }

    public String convertArrayToString(String[] arrStr, String separator) {
        StringBuilder sb = new StringBuilder();
        for (String str : arrStr) {
            sb.append(str + separator);
        }
        String strReturn = sb.toString();
        if (strReturn.endsWith(separator)) {
            return strReturn.substring(0, strReturn.length() - 1);
        }
        return strReturn;
    }

    /*
     *
     * @param map Map
     * @param message GatewayMessage
     */
    private void analysisContextAttribute(Map map, MessageContext message) throws Exception {
        if (message == null) {
            return;
        }
        putAll(message.getAttributes(), map);
    }

    /*
     *
     * @param map Map
     * @param message GatewayMessage
     */
    private void analysisContextProperties(Map map, MessageContext message) throws Exception {
        if (message == null) {
            return;
        }
        putAll(message.getProperties(), map);
    }

    private void putAll(Map mapSource, Map mapDest) {
        int numKeysToBeAdded = mapSource.size();
        if (numKeysToBeAdded == 0) {
            return;
        }

        for (Iterator<Map.Entry<Object, Object>> i = mapSource.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = i.next();
            mapDest.put(e.getKey().toString().toLowerCase(), e.getValue());
            Object value = e.getValue();
            if (value == null) {
                continue;
            }

            if (value instanceof Collection) {
                if (value instanceof Vector) {
                    Vector vt = (Vector) value;
                    int ii = 0;
                    for (Object item : (Collection) value) {
                        mapDest.put((e.getKey() + "." + ii).toString().toLowerCase(), item);
                        ii++;
                    }

                } else {
                    int ii = 0;
                    for (Object item : (Collection) value) {
                        mapDest.put((e.getKey() + "." + ii).toString().toLowerCase(), item);
                        ii++;
                    }
                }
            } else if (value instanceof Map) {
                for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) value).entrySet()) {
                    mapDest.put((e.getKey() + "." + entry.getKey()).toString().toLowerCase(), entry.getValue());
                }
            } else if (value instanceof MessageContext) {
                MessageContext ctx = (MessageContext) value;
                Map mp = new HashMap();
                try {
                    analysisContextProperties(mp, ctx);
                } catch (Exception ex) {
                }
                for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) mp).entrySet()) {
                    mapDest.put((e.getKey() + "." + entry.getKey()).toString().toLowerCase(), entry.getValue());
                }

            } else {
                /*Class clazz = value.getClass();
                if (clazz.isArray()) {
                    int length = Array.getLength(value);
                    for (int ii = 0; ii < length; ii++) {
                        mapDest.put((e.getKey() + "." + ii).toString().toLowerCase(), Array.get(value, ii));
                    }
                } else {
                    Field[] fields = clazz.getDeclaredFields();
                    try {
                        for (Field f : fields) {
                            Object obj = f.get(value);
                            if (obj instanceof java.util.Date) {
                                mapDest.put((e.getKey() + "." + f.getName()).toLowerCase(), formatDate((Date) obj));
                            } else {
                                mapDest.put((e.getKey() + "." + f.getName()).toLowerCase(), obj);
                            }
                        }
                    } catch (Exception ex) {
                    }
                }*/
            }
        }
    }

    /*
     *
     * @param map Map
     * @param message GatewayMessage
     */
    private void analysisMessage(Map map, GatewayMessage message) throws Exception {
        if (message == null) {
            return;
        }

        if (message instanceof telsoft.gateway.core.cmp.gw_rtec.RTECMessage) {
            telsoft.gateway.core.cmp.gw_rtec.RTECMessage realMsg = (telsoft.gateway.core.cmp.gw_rtec.RTECMessage) message;
            putAll(convertXMLtoMap(realMsg), map);

            if (realMsg.getChild("cp_reply") != null) {
                String strResult = StringUtil.nvl(map.get("result"), "255");
                map.put("result_desc", telsoft.gateway.core.cmp.gw_rtec.RTECMessage.getErrorDesc(Integer.parseInt(strResult)));
            }
        } else if (message instanceof XMLMessage) {
            XMLMessage realMsg = (XMLMessage) message;
            putAll(convertXMLtoMap(realMsg), map);
        } else if (message instanceof DiameterMessage) {
            DiameterMessage realMsg = (DiameterMessage) message;
            putAll(convertDIAMETERtoMap(realMsg), map);
        } else if (message instanceof PlainMessage) {
            PlainMessage realMsg = (PlainMessage) message;
            Map map2 = new HashMap();
            MessageUtil.analysePPSMessage(realMsg.getContent(), map2, true);
            putAll(map2, map);
        } else if (message instanceof CBResponse) {
            CBResponse realMsg = (CBResponse) message;
            map.putAll(convertCBResponseToMap(realMsg));
        }else {
            Map map2 = new HashMap();
            MessageUtil.analysePPSMessage(message.getContent(), map2, true);
            putAll(map2, map);
        }
    }

    public static enum EXPORT_TYPE {
        XML, CSV;
    }

    public static enum MSG_TYPE {
        NONE, CLIENT_REQUEST, CLIENT_RESPONSE, SERVER_REQUEST, SERVER_RESPONSE, CTX_ATTR, CTX_PROP, FIXED, PREV_CONTEXT;
    }

    class HeaderItem {
        String FIELD_NAME;
        String COMMAND_PARAM_NAME;
        MSG_TYPE msgType;
        MSG_TYPE msgTypeSecondary;
        String _default;
        String format;
    }

    class ProtocolHeader {
        String serverProtocol;
        String clientProtocol;
        Vector header;
        HeaderItem[] arrayHeader;
        Set<MSG_TYPE> flags;

        public String getHeaders() {
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < arrayHeader.length; i++) {
                buf.append(arrayHeader[i].FIELD_NAME).append(",");
            }
            if (buf.length() > 0) {
                buf.deleteCharAt(buf.length() - 1);
            }
            return buf.toString();
        }
    }

}
