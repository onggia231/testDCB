package com.faplib.lib.admin.gui;

import com.faplib.lib.ClientMessage;
import com.faplib.lib.Session;
import com.faplib.lib.SystemLogger;
import com.faplib.lib.admin.gui.data.BirtReportModel;
import com.faplib.lib.admin.gui.entity.BirtInputParam;
import com.faplib.lib.admin.gui.entity.BirtReport;
import com.faplib.lib.admin.gui.entity.ReportDataset;
import com.faplib.lib.admin.gui.entity.ReportParam;
import com.faplib.lib.config.Config;
import com.faplib.lib.util.ResourceBundleUtil;
import com.faplib.util.ComponentUtil;
import com.faplib.util.DateUtil;
import com.faplib.util.FileUtil;
import com.faplib.util.StringUtil;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.birt.core.script.CoreJavaScriptInitializer;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.omnifaces.util.Faces;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import vn.com.telsoft.lib.TelSoftBIRTEngine;
import vn.com.telsoft.util.RegexUtil;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Project:  TSLIB-V8
 * Author:   ChienDX
 * Created:  09/07/2019 10:51
 **/
@Named
@ViewScoped
public class BirtReportController implements Serializable {
    private static String PARTERN_DATASET_COLUMN = "(dataSetRow|row)\\[\"(.*?)\"\\]";
    private static String PARTERN_REPORT_CODE = "\\/report\\/([0-9a-zA-Z_]+)";
    public static Map<String, BirtInputParam> MAP_CACHED_PARAM;
    private LinkedHashMap<String, ReportParam> mmapParam;
    private LinkedHashMap<String, ReportDataset> mmapDataset;
    private BirtReportModel mmodel;
    private TelSoftBIRTEngine mbirt;
    private Context mBirtContex;
    private Scriptable mScriptableScope;
    private String mstrReportCode;
    private String mstrError;
    private BirtReport mreport;
    private String mstrExportHtmlUrl;
    private String mstrRptOutputPath;
    private String mstrRptDesignPath;
    private String mstrReportDocumentName;

    private ReportPager mreportPage;

     public BirtReportController() {
        try {
            if (MAP_CACHED_PARAM == null) {
                MAP_CACHED_PARAM = new HashMap<>();
            }

            initBirtEngine();
            mmodel = new BirtReportModel();
            mmapParam = new LinkedHashMap<>();
            mmapDataset = new LinkedHashMap<>();
            mstrError = null;

            //Get report code
            mstrReportCode = RegexUtil.getTextBetween(Config.getCurrentModule(), PARTERN_REPORT_CODE).toUpperCase();

            //Get template location
            Properties prop = new Properties();
            prop.load(TelSoftBIRTEngine.class.getClassLoader().getResourceAsStream("birt-config.properties"));
            mstrRptDesignPath = FileUtil.getRealPath(prop.getProperty("rptdesign"));
            mstrRptOutputPath = FileUtil.getRealPath(prop.getProperty("output"));

            //Get report detail
            mreport = mmodel.getReportDetail(mstrReportCode, mstrRptDesignPath);

            if(mreport.getReportId() == 0) {
                mstrError = ResourceBundleUtil.getAMObjectAsString("PP_MNGREPORT", "report_not_found");
                return;
            }

            //Load cache
            loadParamCache();

        } catch (Exception e) {
            mstrError = e.getMessage();
            SystemLogger.getLogger().error(e, e);
        }
    }
    //////////////////////////////////////////////////////////////////////////////////

    private synchronized void loadParamCache() throws Exception {
        if (MAP_CACHED_PARAM.get(mstrReportCode) == null) {
            parseTemplate();

            BirtInputParam obj = new BirtInputParam();
            obj.setMapDataset((LinkedHashMap<String, ReportDataset>) SerializationUtils.clone(mmapDataset));
            obj.setMapParam((LinkedHashMap<String, ReportParam>) SerializationUtils.clone(mmapParam));
            MAP_CACHED_PARAM.put(mstrReportCode, obj);

        } else {
            mmapParam = (LinkedHashMap<String, ReportParam>) SerializationUtils.clone(MAP_CACHED_PARAM.get(mstrReportCode).getMapParam());
            mmapDataset = (LinkedHashMap<String, ReportDataset>) SerializationUtils.clone(MAP_CACHED_PARAM.get(mstrReportCode).getMapDataset());
            loadParamData();
        }
    }

    public boolean isIsExportCSV() {
        return mmapParam.get("export_txt") != null && mmapParam.get("export_txt").getHelpText() != null;
    }

    public void nextPage() {
        mreportPage.nextPage();
        exportHtml();
    }
    //////////////////////////////////////////////////////////////////////////////////

    public void prevPage() {
        mreportPage.prevPage();
        exportHtml();
    }
    //////////////////////////////////////////////////////////////////////////////////

    public void goPage() {
        exportHtml();
    }
    //////////////////////////////////////////////////////////////////////////////////

    private void callProcedure() throws Exception {
        String procedure = mreport.getProcedure();
        if (procedure != null) {
            for (Map.Entry<String, ReportParam> entry : mmapParam.entrySet()) {
                procedure = procedure.replace("{" + entry.getKey() + "}", getStringParamValue(entry.getValue()));
            }

            mmodel.callProcedure(procedure);
        }
    }
    //////////////////////////////////////////////////////////////////////////////////

    private String getStringParamValue(ReportParam param) {
        if (param.getValue() instanceof Date) {
            return DateUtil.getDateStr((Date) param.getValue(), param.getDatePattern());

        }
        if (param.getValue() instanceof String[]) {
            return StringUtils.join((String[]) param.getValue(), ",");

        } else {
            return String.valueOf(param.getValue());
        }
    }
    //////////////////////////////////////////////////////////////////////////////////

    public void renderDocument() {
        try {
            //Call procedure before report
            callProcedure();

            HashMap args = new HashMap();
            for (Map.Entry<String, ReportParam> entry : mmapParam.entrySet()) {
                ReportParam value = entry.getValue();
                args.put(value.getName(), value.getValue());
            }

            mstrReportDocumentName = mbirt.runReport(mstrReportCode, args);
            mreportPage = new ReportPager(mbirt.getPageCount(mstrReportDocumentName));

            if (mreportPage.getTotalPage() > 0) {
                exportHtml();
            }

        } catch (Exception e) {
            ClientMessage.logErr(e.getMessage().replace("Invalid javascript expression: java.lang.Exception", ""));
            SystemLogger.getLogger().error(e, e);
        }
    }
    ////////////////////////////////////////////////////////////////////////

    public void exportHtml() {
        try {
            //Create report
            FileOutputStream out = null;
            File exportFile;
            mstrExportHtmlUrl = null;

            try {
                SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                String strFileNameOut = mstrReportCode + "_" + df.format(new Date()) + ".html";
                String strFilePathOut = mstrRptOutputPath + File.separator + strFileNameOut;
                String strFileNameEnc = StringUtil.encryptMD5(strFileNameOut);

                exportFile = new File(strFilePathOut);
                exportFile.createNewFile();
                out = new FileOutputStream(exportFile, false);

                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_16LE));
                bw.write("\uFEFF");

                mbirt.renderReportInHtml(mstrReportDocumentName, mreportPage.getPageRange(), out);
                mstrExportHtmlUrl = Faces.getRequestBaseURL() + "faces/viewrpt.xhtml?code=" + strFileNameEnc;
                Session.setSessionValue(strFileNameEnc, strFilePathOut);

            } finally {
                if (out != null) {
                    out.close();
                }
            }

        } catch (Exception e) {
            ClientMessage.logErr(e.getMessage());
            SystemLogger.getLogger().error(e, e);
        }
    }
    ////////////////////////////////////////////////////////////////////////

    public DefaultStreamedContent report(String typeReport) {
        try {
            //Call procedure before report
            callProcedure();

            //Create report
            FileOutputStream out = null;
            File exportFile;

            try {
                SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                String strFileNameOut = null;
                if (typeReport.equals("xls") || typeReport.equals("xlsx")) {
                    strFileNameOut = mstrReportCode + "_" + df.format(new Date()) + ".xlsx";

                } else if (typeReport.equals("pdf")) {
                    strFileNameOut = mstrReportCode + "_" + df.format(new Date()) + ".pdf";

                } else if (typeReport.equals("txt")) {
                    strFileNameOut = mstrReportCode + "_" + df.format(new Date()) + ".txt";

                } else {
                    strFileNameOut = mstrReportCode + "_" + df.format(new Date()) + ".doc";
                }
                String strFilePathOut = mstrRptOutputPath + File.separator + strFileNameOut;

                exportFile = new File(strFilePathOut);
                exportFile.createNewFile();
                out = new FileOutputStream(exportFile, false);

                HashMap args = new HashMap();
                for (Map.Entry<String, ReportParam> entry : mmapParam.entrySet()) {
                    ReportParam value = entry.getValue();
                    args.put(value.getName(), value.getValue());
                }

                TelSoftBIRTEngine birtEngine = TelSoftBIRTEngine.getInstance();
                if (typeReport.equals("xls") || typeReport.equals("xlsx")) {
                    birtEngine.viewReportInExcel(mstrReportCode, args, out, true);

                } else if (typeReport.equals("pdf")) {
                    birtEngine.viewReportInPdf(mstrReportCode, args, out);

                } else if (typeReport.equals("txt")) {
                    ReportParam param = mmapParam.get("export_txt");
                    birtEngine.viewReportInTxt(mstrReportCode, args, out, param.getHelpText());

                } else {
                    birtEngine.viewReportInWord(mstrReportCode, args, out);
                }

                return FileUtil.downloadFile(new File(strFilePathOut));

            } finally {
                if (out != null) {
                    out.close();
                }
            }

        } catch (Exception e) {
            mstrError = e.getMessage().replace("Invalid javascript expression: java.lang.Exception", "");
            SystemLogger.getLogger().error(e, e);
        }

        return null;
    }
    /////////////////////////////////////////////////////////////////////////////////////////////

    public void onSelectValue(ReportParam param) {
        try {
            if (param.getCascadingNodeName() != null) {
                Map<String, String> mapParamValue = new HashMap<>();
                mapParamValue.put(param.getName(), param.getStringValue());

                for (Map.Entry<String, ReportParam> entry : mmapParam.entrySet()) {
                    ReportParam value = entry.getValue();

                    if (value.getName().equals(param.getChildNodeName())) {
                        value.getDataset().setListData(mmodel.getListData(value, mapParamValue));
                        value.setStringValue(null);

                        String updateId = ComponentUtil.findComponentIdByStyleClass("child-" + param.getChildNodeName());
                        PrimeFaces.current().ajax().update(updateId);

                        onSelectValue(value);
                        break;
                    }
                }
            }

        } catch (Exception e) {
            mstrError = e.getMessage();
            SystemLogger.getLogger().error(e, e);
        }
    }
    //////////////////////////////////////////////////////////////////////////////////

    public void onSelectValueMulti(ReportParam param) {

    }
    //////////////////////////////////////////////////////////////////////////////////

    private void initBirtEngine() {
        mbirt = TelSoftBIRTEngine.getInstance();
        mBirtContex = Context.enter();
        mScriptableScope = mBirtContex.initStandardObjects();
        new CoreJavaScriptInitializer().initialize(mBirtContex, mScriptableScope);
    }
    //////////////////////////////////////////////////////////////////////////////////

    private Object executeBirtScript(String script) {
        try {
            Object result = mBirtContex.evaluateString(mScriptableScope, script, "inline", 0, null);
            if (result != null && result.toString().contains("NativeDate")) {
                return Context.jsToJava(result, Date.class);
            }

            return result;

        } catch (Exception e) {
            mstrError = "Error on execute script: " + script + " [" + e.getMessage() + "]";
            SystemLogger.getLogger().error(e, e);
        }

        return null;
    }
    //////////////////////////////////////////////////////////////////////////////////

    private void loadParamData() throws Exception {
        for (Map.Entry<String, ReportParam> entry : mmapParam.entrySet()) {
            ReportParam tmpParam = entry.getValue();

            if (tmpParam.getDataset() != null) {
                tmpParam.getDataset().setListData(mmodel.getListData(tmpParam, null));
            }
        }
    }
    //////////////////////////////////////////////////////////////////////////////////

    private void parseTemplate() throws Exception {
        File fXmlFile = new File(FileUtil.getRealPath(mbirt.getRptDesignPath() + mstrReportCode + ".rptdesign"));
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        doc.getDocumentElement().normalize();

        //Get dataset
        NodeList nListDataset = doc.getElementsByTagName("oda-data-set");
        for (int i = 0; i < nListDataset.getLength(); i++) {
            Node nNode = nListDataset.item(i);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                ReportDataset tmpDataset = new ReportDataset();

                Element eElement = (Element) nNode;
                tmpDataset.setName(eElement.getAttribute("name"));

                NodeList xmlProperty = eElement.getElementsByTagName("xml-property");
                for (int j = 0; j < xmlProperty.getLength(); j++) {
                    Node pNode = xmlProperty.item(j);
                    Element pElement = (Element) pNode;
                    if (pElement != null && "queryText".equals(pElement.getAttribute("name"))) {
                        tmpDataset.setQueryText(pElement.getTextContent());
                    }
                }

                tmpDataset.setListParam(new ArrayList<>());
                xmlProperty = eElement.getElementsByTagName("list-property");
                for (int j = 0; j < xmlProperty.getLength(); j++) {
                    Node pNode = xmlProperty.item(j);
                    Element pElement = (Element) pNode;
                    if (pElement != null && "parameters".equals(pElement.getAttribute("name"))) {
                        NodeList structure = pElement.getElementsByTagName("structure");
                        for (int k = 0; k < structure.getLength(); k++) {
                            NodeList p = ((Element) structure.item(k)).getElementsByTagName("property");
                            for (int l = 0; l < p.getLength(); l++) {
                                Node pn = p.item(l);
                                Element pe = (Element) pn;
                                if (pe != null && "paramName".equals(pe.getAttribute("name"))) {
                                    tmpDataset.getListParam().add(pe.getTextContent());
                                }
                            }
                        }
                    }
                }

                mmapDataset.put(tmpDataset.getName(), tmpDataset);
            }
        }

        //Get paramater
        NodeList nListParam = doc.getElementsByTagName("scalar-parameter");
        String cascadingNodeName = "";
        ReportParam prevParam = null;

        for (int i = 0; i < nListParam.getLength(); i++) {
            Node nNode = nListParam.item(i);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                ReportParam tmpParam = new ReportParam();

                try {
                    Element eElement = (Element) nNode;
                    tmpParam.setName(eElement.getAttribute("name"));

                    //Get property
                    NodeList xmlProperty = eElement.getElementsByTagName("text-property");
                    for (int j = 0; j < xmlProperty.getLength(); j++) {
                        Node pNode = xmlProperty.item(j);
                        Element pElement = (Element) pNode;
                        if (pElement != null && "promptText".equals(pElement.getAttribute("name"))) {
                            tmpParam.setLabel(pElement.getTextContent());
                        }
                        if (pElement != null && "helpText".equals(pElement.getAttribute("name"))) {
                            tmpParam.setHelpText(pElement.getTextContent());
                        }
                    }

                    xmlProperty = eElement.getElementsByTagName("property");
                    boolean isFoundRequired = false;

                    for (int j = 0; j < xmlProperty.getLength(); j++) {
                        Node pNode = xmlProperty.item(j);
                        Element pElement = (Element) pNode;

                        if (pElement != null && "isRequired".equals(pElement.getAttribute("name"))) {
                            tmpParam.setRequired("true".equals(pElement.getTextContent()));
                            isFoundRequired = true;
                        }

                        if (pElement != null && "hidden".equals(pElement.getAttribute("name"))) {
                            tmpParam.setHidden("true".equals(pElement.getTextContent()));
                        }

                        if (pElement != null && "dataSetName".equals(pElement.getAttribute("name"))) {
                            tmpParam.setDataset(mmapDataset.get(pElement.getTextContent()));
                        }

                        if (pElement != null && "dataType".equals(pElement.getAttribute("name"))) {
                            tmpParam.setDataType(pElement.getTextContent());
                        }

                        if (pElement != null && "controlType".equals(pElement.getAttribute("name"))) {
                            tmpParam.setControlType(pElement.getTextContent());
                        }

                        if (pElement != null && "paramType".equals(pElement.getAttribute("name"))) {
                            tmpParam.setParamType(pElement.getTextContent());
                        }

                        if (pElement != null && "pattern".equals(pElement.getAttribute("name"))) {
                            tmpParam.setDatePattern(pElement.getTextContent());
                        }
                    }
                    if(!isFoundRequired) {
                        tmpParam.setRequired(true);
                    }

                    xmlProperty = eElement.getElementsByTagName("expression");
                    for (int j = 0; j < xmlProperty.getLength(); j++) {
                        Node pNode = xmlProperty.item(j);
                        Element pElement = (Element) pNode;
                        if (pElement != null && "valueExpr".equals(pElement.getAttribute("name"))) {
                            tmpParam.getDataset().setValueColumn(RegexUtil.getTextBetween(pElement.getTextContent(), PARTERN_DATASET_COLUMN, 2));
                        }
                        if (pElement != null && "labelExpr".equals(pElement.getAttribute("name"))) {
                            tmpParam.getDataset().setLabelColumn(RegexUtil.getTextBetween(pElement.getTextContent(), PARTERN_DATASET_COLUMN, 2));
                        }
                    }

                    xmlProperty = eElement.getElementsByTagName("simple-property-list");
                    for (int j = 0; j < xmlProperty.getLength(); j++) {
                        Node pNode = xmlProperty.item(j);
                        Element pElement = (Element) pNode;
                        if (pElement != null && "defaultValue".equals(pElement.getAttribute("name"))) {
                            NodeList nValue = pElement.getElementsByTagName("value");

                            if (nValue.getLength() != 0) {
                                Element eValue = (Element) nValue.item(0);

                                if ("constant".equals(eValue.getAttribute("type"))) {
                                    tmpParam.setStringValue(eValue.getTextContent());
                                    if (tmpParam.isInputNumber()) {
                                        tmpParam.setIntValue(Integer.parseInt(eValue.getTextContent()));
                                    }
                                } else if ("javascript".equals(eValue.getAttribute("type"))) {
                                    Object value = executeBirtScript(eValue.getTextContent());
                                    if (value instanceof Date) {
                                        tmpParam.setDateValue((Date) value);

                                    } else {
                                        tmpParam.setStringValue(String.valueOf(value));
                                    }
                                }
                            }
                        }
                    }

                    Node cascadingNode = getCascadingParameterGroup(nNode);
                    if (cascadingNode != null) {
                        tmpParam.setCascadingNodeName(((Element) cascadingNode).getAttribute("name"));
                        if (prevParam != null && tmpParam.getCascadingNodeName().equals(prevParam.getCascadingNodeName())) {
                            prevParam.setChildNodeName(tmpParam.getName());
                        }
                    }

                    if (tmpParam.getDataset() != null) {
                        tmpParam.getDataset().setListData(mmodel.getListData(tmpParam, null));
                    }

                    prevParam = tmpParam;
                    mmapParam.put(tmpParam.getName(), tmpParam);

                } catch (Exception e) {
                    mstrError = "Error on load param: " + tmpParam.getName() + " [" + e.getMessage() + "]";
                    SystemLogger.getLogger().error(e, e);
                }
            }
        }
    }
    //////////////////////////////////////////////////////////////////////////////////

    private Node getCascadingParameterGroup(Node node) {
        if (node != null && node.getParentNode() != null && node.getParentNode().getParentNode() != null) {
            String nodeName = node.getParentNode().getParentNode().getNodeName();
            if ("cascading-parameter-group".equals(nodeName)) {
                return node.getParentNode().getParentNode();
            }
        }

        return null;
    }
    //////////////////////////////////////////////////////////////////////////////////

    public int getColumnCss() {
        if (mmapParam.get("column_count") != null) {
            String columncount = StringUtil.nvl(mmapParam.get("column_count").getLabel(), "4");
            return 12 / Integer.parseInt(columncount);

        } else {
            return 3;
        }
    }
    //////////////////////////////////////////////////////////////////////////////////

    public LinkedHashMap<String, ReportParam> getMmapParam() {
        return mmapParam;
    }

    public List<ReportParam> getListParam() {
        return mmapParam.values().stream().collect(Collectors.toList());
    }

    public String getMstrError() {
        return mstrError;
    }

    public void setMstrError(String mstrError) {
        this.mstrError = mstrError;
    }

    public String getMstrExportHtmlUrl() {
        return mstrExportHtmlUrl;
    }

    public void setMstrExportHtmlUrl(String mstrExportHtmlUrl) {
        this.mstrExportHtmlUrl = mstrExportHtmlUrl;
    }

    public ReportPager getMreportPage() {
        return mreportPage;
    }

    public void setMreportPage(ReportPager mreportPage) {
        this.mreportPage = mreportPage;
    }

    public class ReportPager {
        private long totalPage;
        private long currentPage = 1;

        public ReportPager(long totalPage) {
            this.totalPage = totalPage;
        }

        public boolean isAllowNextPage() {
            return currentPage < totalPage && currentPage > 0;
        }

        public boolean isAllowPrevPage() {
            return currentPage > 1 && currentPage <= totalPage;
        }

        public void nextPage() {
            currentPage++;
        }

        public void prevPage() {
            currentPage--;
        }

        public long getTotalPage() {
            return totalPage;
        }

        public void setTotalPage(long totalPage) {
            this.totalPage = totalPage;
        }

        public long getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(long currentPage) {
            this.currentPage = currentPage;
        }

        public String getPageRange() {
            return this.currentPage + "," + this.currentPage;
        }
    }
}
