package com.telsoft.cbs.thread;


import com.telsoft.cbs.utils.MailAuthenticator;
import com.telsoft.cbs.utils.MailServicesUtil;
import com.telsoft.cbs.utils.XlsxConverter;
import com.telsoft.database.Database;
import com.telsoft.thread.DBManageableThread;
import com.telsoft.thread.GroupParameter;
import com.telsoft.thread.ParameterType;
import com.telsoft.thread.ThreadParameter;
import com.telsoft.thread.basic.SimpleAuthenticator;
import com.telsoft.util.AppException;
import com.telsoft.util.StringUtil;
import oracle.jdbc.OracleTypes;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.sql.*;
import java.util.*;
import javax.mail.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Viz on 16/03/2016.
 * Last Edited by Nampt
 */
public class AutoSendEmailReportProcedure3 extends DBManageableThread {


    private String mstrProtocol;
    private String mstrHost;
    private int miPort;
    private boolean mbStartTLS;
    private boolean mbSSLOnConnect;
    private String mstrUser;
    private String mstrPassword;
    private String mstrSender;
    private Vector mvtListMailConfig;
    private Vector mstrFileName;
    private Session mailsession;
    private Transport transport;
//    private Vector mvtListRecipient;
//    private Vector mvtProcedureConfig;
//    private String mstrMailSubject;
//    private String mstrMailBody;
//    private boolean mbZipFile = false;

//    private MailServicesUtil mMailServicesUtil;

    public Vector getParameterDefinition() {
        Vector vtReturn = new Vector();
        //jsmpp parameter
        Vector<ThreadParameter> vtMailReturn = new Vector<ThreadParameter>();


        vtMailReturn.add(createParameter("Protocol", "", ParameterType.PARAM_TEXTBOX_MAX, "50", "Protocol used to send mail"));
        vtMailReturn.addElement(createParameter("Host", "", ParameterType.PARAM_TEXTBOX_FILTER,
                ParameterType.FILTER_REGULAR, "IP address of smtp server"));
        vtMailReturn.addElement(createParameter("Port", "",
                ParameterType.PARAM_TEXTBOX_MASK, "99990", "SMTP server port, ussually is 25"));
        Vector vtTrueFalse = new Vector();
        vtTrueFalse.add("true");
        vtTrueFalse.add("false");
        vtMailReturn.addElement(createParameter("StartTLS", "", ParameterType.PARAM_COMBOBOX, vtTrueFalse, "Server start TLS"));
        vtMailReturn.addElement(createParameter("UsingSSLOnConnect", "", ParameterType.PARAM_COMBOBOX, vtTrueFalse, "Server using SSL on Connect"));

        vtMailReturn.addElement(createParameter("User", "",
                ParameterType.PARAM_TEXTBOX_MAX, "256", "User to login to smtp server"));
        vtMailReturn.addElement(createParameter("Password", "",
                ParameterType.PARAM_PASSWORD, "100", "User password"));
        vtMailReturn.addElement(createParameter("Sender", "",
                ParameterType.PARAM_TEXTBOX_MAX, "400", "Email address used as source address"));

        vtReturn.add(createParameter("MailSetting", "", ParameterType.PARAM_GROUP, vtMailReturn));

        Vector<ThreadParameter> vtListMailConfig = new Vector<ThreadParameter>();

        Vector<ThreadParameter> vtMailList = new Vector<ThreadParameter>();
        vtMailList.addElement(createParameter("Recipient", "", ParameterType.PARAM_TEXTBOX_MAX, "900", "List Recipient"));
        vtMailList.addElement(createParameter("EmailAddress", "", ParameterType.PARAM_TEXTBOX_MAX, "900", "Email Address"));
        vtListMailConfig.add(createParameter("ListRecipient", "", ParameterType.PARAM_TABLE, vtMailList));

        Vector<ThreadParameter> vtConfigFile = new Vector<>();
        Vector<ThreadParameter> vtConfigProcedure = new Vector<>();
        Vector<ThreadParameter> vtConfigProcedureParam = new Vector<>();
        Vector vtYesNo = new Vector<>();
        vtYesNo.add("Y");
        vtYesNo.add("N");

        vtConfigProcedureParam.addElement(createParameter("ParamName", "", ParameterType.PARAM_TEXTBOX_MAX, "900", "Parameter name"));
        vtConfigProcedureParam.addElement(createParameter("ParamValue", "", ParameterType.PARAM_TEXTBOX_MAX, "900", "Parameter Value"));


        vtConfigProcedure.addElement(createParameter("ProcedureName", "", ParameterType.PARAM_TEXTBOX_MAX, "900", "Procedure Name"));
        vtConfigProcedure.addElement(createParameter("ProcedureParameter", "", ParameterType.PARAM_TABLE, vtConfigProcedureParam));
        vtConfigProcedure.addElement(createParameter("HtmlMail", "", ParameterType.PARAM_COMBOBOX, vtYesNo, "Embed html content in mail"));
//        vtConfigProcedure.addElement(createParameter("SheetSerial","",ParameterType.PARAM_TEXTBOX_MASK,"255","Ordinal number of sheet"));
        vtConfigProcedure.addElement(createParameter("SheetName", "", ParameterType.PARAM_TEXTBOX_MAX, "900", "Sheet Name"));
        vtConfigProcedure.addElement(createParameter("Row", "", ParameterType.PARAM_TEXTBOX_MASK, "999990", "Row start data recording"));
        vtConfigProcedure.addElement(createParameter("Column", "", ParameterType.PARAM_TEXTBOX_MASK, "9999", "Column start data recording"));

        vtConfigFile.addElement(createParameter("ProcedureConfig", "", ParameterType.PARAM_TABLE, vtConfigProcedure));
        vtConfigFile.addElement(createParameter("FileName", "", ParameterType.PARAM_TEXTBOX_MAX, "99", "Exported file name"));
//        vtConfigFile.addElement(createParameter("ReportHeader", "", ParameterType.PARAM_TEXTBOX_MAX, "900", "Report Header "));
        vtConfigFile.addElement(createParameter("FileTemplate", "", ParameterType.PARAM_TEXTBOX_MAX, "900", "File Template"));

        vtListMailConfig.addElement(createParameter("FileConfig", "", ParameterType.PARAM_TABLE, vtConfigFile));
        vtListMailConfig.addElement(createParameter("MailSubject", "", ParameterType.PARAM_TEXTBOX_MAX, "256", "Subject email"));

        Vector<ThreadParameter> vtConfigMailParam = new Vector<>();
        vtConfigMailParam.addElement(createParameter("Code", "", ParameterType.PARAM_TEXTBOX_MAX, "99", "Mail Indicator Code"));
        vtConfigMailParam.addElement(createParameter("Value", "", ParameterType.PARAM_TEXTAREA_MAX, "900", "Mail Indicator Value"));
        vtListMailConfig.addElement(createParameter("MailIndicator", "", ParameterType.PARAM_TABLE, vtConfigMailParam));

        Vector vtActiveInActive = new Vector<>();
        vtActiveInActive.add("Active");
        vtActiveInActive.add("Inactive");
        vtListMailConfig.addElement(createParameter("MailStatus", "", ParameterType.PARAM_COMBOBOX, vtActiveInActive));
        vtListMailConfig.addElement(createParameter("ZipFileReport", "", ParameterType.PARAM_COMBOBOX, vtYesNo, "Zip file report before send"));

        vtListMailConfig.addElement(createParameter("MailBody", "", ParameterType.PARAM_TEXTAREA_MAX, "900", "Body email"));

        vtReturn.addElement(createParameter("ListMailConfig", "", ParameterType.PARAM_TABLE, vtListMailConfig));
        vtReturn.addAll(super.getParameterDefinition());
        return vtReturn;
    }

    @Override
    public void fillParameter() throws AppException {
        super.fillParameter();
        //jsmpp parameter
        GroupParameter gp = new GroupParameter(this, "MailSetting");

        mstrProtocol = gp.loadString("Protocol");
        mstrHost = gp.loadString("Host");
        miPort = gp.loadUnsignedInteger("Port");
        mbStartTLS = gp.loadBoolean("StartTLS");
        mbSSLOnConnect = gp.loadBoolean("UsingSSLOnConnect");
        mstrUser = StringUtil.nvl(gp.loadString("User"), "");
        mstrPassword = StringUtil.nvl(gp.loadString("Password"), "");
        mstrSender = gp.loadString("Sender");

//        mvtListRecipient = (Vector) getParameter("MailAddress");
//        mvtProcedureConfig = (Vector) getParameter("ProcedureConfig");
//        mstrMailSubject = loadString("MailSubject");
//        mstrMailBody = loadString("MailBody");
//        mbZipFile = loadString("ZipFileReport").equals("Y");

        mvtListMailConfig = (Vector) getParameter("ListMailConfig");
    }

    @Override
    protected void processSession() throws Exception {
        try {
            MailServicesUtil mMailServicesUtil = new MailServicesUtil(mstrHost, miPort, mstrUser, mstrPassword, mbStartTLS, mbSSLOnConnect, mstrSender);

            for (int iMailIndex = 0; iMailIndex < mvtListMailConfig.size(); iMailIndex++) {

                try {

                    //process mail
                    Vector vtMailConfig = (Vector) mvtListMailConfig.get(iMailIndex);

                    //Get ListRecipient
                    List<String> lsEmail = new ArrayList<String>();
                    if (vtMailConfig.get(0) instanceof Vector) {
                        Vector vtListRecipient = (Vector) vtMailConfig.get(0);
                        for (int iIndex = 0; iIndex < vtListRecipient.size(); iIndex++) {
                            Vector vtRow = (Vector) vtListRecipient.elementAt(iIndex);
                            String strRecipient = (String) vtRow.elementAt(1);
                            if (strRecipient != null) {
                                if (!strRecipient.equals("")) {
                                    lsEmail.add(strRecipient);
                                }
                            }
                        }
                    }

                    //Get File Config

                    Vector vtConfigFile = new Vector();

                    if (vtMailConfig.get(1) instanceof Vector) {
                        vtConfigFile = (Vector) vtMailConfig.get(1);
                    }


                    //Get Mail Subject
                    String strMailSubject = StringUtil.nvl(vtMailConfig.get(2), "").trim();
                    //get Mail Indicator
                    Vector vtMailIndicator = new Vector();
                    if (vtMailConfig.get(3) instanceof Vector) {
                        vtMailIndicator = (Vector) vtMailConfig.get(3);
                    }
                    //Get mail status
                    String strMailStatus = StringUtil.nvl(vtMailConfig.get(4), "").trim();
                    //Get zip file status
                    boolean bZipFile = StringUtil.nvl(vtMailConfig.get(5), "").equals("Y");
                    //Get Mail Body
                    String strMailBody = StringUtil.nvl(vtMailConfig.get(6), "").trim();
                    HashMap<String, String> hmMailIndicator = new HashMap<>();

                    if (vtMailIndicator != null && vtMailIndicator.size() > 0) {
                        try {
                            hmMailIndicator = getDataMailIndicator(vtMailIndicator);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (hmMailIndicator.size() > 0) {
                        for (HashMap.Entry<String, String> entry : hmMailIndicator.entrySet()) {
                            strMailSubject = strMailSubject.replace("${" + entry.getKey() + "}", entry.getValue());
                            strMailBody = strMailBody.replace("${" + entry.getKey() + "}", entry.getValue());
                        }
                    }

                    if (lsEmail.size() <= 0 || vtConfigFile.size() <= 0 || strMailSubject.equals("") || strMailBody.equals("")) {
                        logMonitor("Config wrong in mail name: " + strMailSubject);
                        continue;
                    }
                    if (strMailStatus.equalsIgnoreCase("Active")) {
                        processProcedureMail(mMailServicesUtil, lsEmail, vtConfigFile, strMailSubject, strMailBody, bZipFile);
                    }
                } catch (Exception ex) {
                    logMonitor("Error when process mail " + iMailIndex + " - " + ex.getMessage());
                    continue;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private HashMap<String, String> getDataMailIndicator(Vector vtMailIndicator) throws SQLException {
        HashMap<String, String> hmReturn = new HashMap<>();
        PreparedStatement stmtIndicator = null;
        ResultSet rsIndicator = null;
        for (int i = 0; i < vtMailIndicator.size(); i++) {
            String code = "";
            try {
                Vector vtIndicator = (Vector) vtMailIndicator.get(i);

                code = (String) vtIndicator.get(0);
                String valueSQL = (String) vtIndicator.get(1);

                stmtIndicator = this.getConnection().prepareCall(valueSQL.toString());
                rsIndicator = stmtIndicator.executeQuery();
                rsIndicator.next();
                hmReturn.put(code, (String) rsIndicator.getObject(1));
            } catch (Exception ex) {
                ex.printStackTrace();
                logMonitor("Error when get mail indicator : {" + code + "} -" + ex.getMessage());
                continue;
            } finally {
                Database.closeObject(rsIndicator);
                Database.closeObject(stmtIndicator);
            }
        }
        return hmReturn;
    }


    private void processProcedureMail(MailServicesUtil mMailServicesUtil, List<String> lsEmail, Vector vtConfigFile, String strMailSubject, String strMailBody, boolean bZipFile) throws Exception {
        List<String> listFile = new ArrayList<String>();
        List<String> listFileZiped = new ArrayList<String>();
        String htmlContent = "";

        //Get Procedure Config


        //create html report file dir
//        String relativePathHtml = "htmlReport/";
//
//        File parentHTML = new File(relativePathHtml);
//        if (!parentHTML.exists() && !parentHTML.mkdirs()) {
//            throw new IllegalStateException("Couldn't create dir: " + parentHTML);
//        }
//        String pathHTML = parentHTML.getAbsolutePath();
//        if (!pathHTML.endsWith("/") && !pathHTML.endsWith("\\")) {
//            pathHTML += "/";
//        }

        //create export dir
        String relativePath = "excelReport/";
        File parent = new File(relativePath);
        if (!parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Couldn't create dir: " + parent);
        }
        String path = parent.getAbsolutePath();
        if (!path.endsWith("/") && !path.endsWith("\\")) {
            path += "/";
        }

        String relativeTemplatePath = "templateReport/";
        File parentTemp = new File(relativeTemplatePath);
        if (!parentTemp.exists() && !parentTemp.mkdirs()) {
            throw new IllegalStateException("Couldn't create dir: " + parentTemp);
        }
        String pathTemp = parentTemp.getAbsolutePath();
        if (!pathTemp.endsWith("/") && !pathTemp.endsWith("\\")) {
            pathTemp += "/";
        }


        try {
            logMonitor("Starting process mail: " + strMailSubject);
            HashMap<String, String> hmSheetConvert = new HashMap<>();
            for (int i = 0; i < vtConfigFile.size(); i++) {
                Vector vtProcedureConfig = new Vector();
                if (vtConfigFile.get(i) instanceof Vector) {
                    vtProcedureConfig = (Vector) vtConfigFile.get(i);
                }
                String strFileName = ((Vector) vtProcedureConfig).get(1).toString().trim();
//                String strReportHeader = ((Vector) vtProcedureConfig).get(2).toString();
                String strTemplatePath = ((Vector) vtProcedureConfig).get(2).toString();

                if (strTemplatePath != null && !"".equals(strTemplatePath)) {
                    strTemplatePath = pathTemp + strTemplatePath;
                }

                //Get File Name
                logMonitor("Export excel attachment  " + strFileName);
                List listResult = exportReport((Vector) vtProcedureConfig, path, strFileName, hmSheetConvert, strTemplatePath);

                if(listResult != null && !listResult.isEmpty()) {
                    String strFileReport = (String) listResult.get(0);

                    htmlContent += XlsxConverter.convert(strFileReport, hmSheetConvert, (HashMap<String, Object>) listResult.get(2));
                    if (strFileReport != null && !"".equals(strFileReport)) {
                        listFile.add(strFileReport);
                    } else {
                        logMonitor("No file report!");
                    }
                }else{
                    logMonitor("No data!");
                }
            }

            if (listFile.size() > 0 && lsEmail.size() > 0) {
                logMonitor("Sending mail");
                if (bZipFile) {
                    DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                    Date date = new Date();
                    String dateName = dateFormat.format(date);
                    String strNameReplace = strMailSubject.trim().replaceAll("[-+.^:, ~!@#$%^&*()-+;'\"\\/`=]", "");
                    listFileZiped = mMailServicesUtil.zipFile(listFile, path + "REPORT_" + strNameReplace + "_" + dateName);
                    if (listFileZiped.size() > 0) {
                        mMailServicesUtil.sendMailHtml(lsEmail, strMailSubject, strMailBody, htmlContent, listFileZiped);
                    }

                } else {
                    mMailServicesUtil.sendMailHtml(lsEmail, strMailSubject, strMailBody, htmlContent, listFile);
                }
            }
            logMonitor("Done!");

        } catch (Exception ex) {
            ex.printStackTrace();
            logMonitor("Error when process mail " + strMailSubject + ": " + ex.toString());
        } finally {
            if (listFile.size() > 0) {
                mMailServicesUtil.deleteFile(listFile);
            }
            if (listFileZiped.size() > 0) {
                mMailServicesUtil.deleteFile(listFileZiped);
            }
        }
    }

    private SXSSFWorkbook createWookBook(String strFilePath) throws Exception {
        try {
            if (strFilePath != null && !"".equals(strFilePath)) {
                XSSFWorkbook wb = new XSSFWorkbook(strFilePath);
                return new SXSSFWorkbook(wb, -1, false, true);
            } else {
                return new SXSSFWorkbook(5000);
            }
        } catch (Exception ex) {
            logMonitor("Error when create file from template " + strFilePath + ": " + ex.toString());
        }
        return null;
    }

    private XSSFWorkbook createWookBookXSSF(String strFilePath) throws Exception {
        try {
            if (strFilePath != null && !"".equals(strFilePath)) {
                return new XSSFWorkbook(strFilePath);
            } else {
                return new XSSFWorkbook();
            }
        } catch (Exception ex) {
            logMonitor("Error when create file from template " + strFilePath + ": " + ex.toString());
        }
        return null;
    }

    private File cloneTemplateFile(String templateFilePath, String tempDir) throws Exception {
        if (templateFilePath != null && !"".equals(templateFilePath)) {
            try {
                File srcFile = new File(templateFilePath);
                if (!srcFile.exists() || !srcFile.isFile()) {
                    return null;
                }
                String dstFileName = srcFile.getName() + UUID.randomUUID().toString().replace("-", "") + ".tmp";
                if (!"".equals(tempDir) && !tempDir.endsWith("/")) {
                    tempDir += "/";
                }

                File dstFile = new File(tempDir + dstFileName);

                FileUtils.copyFile(srcFile, dstFile);
                return dstFile;
            } catch (IOException e) {
                e.printStackTrace();
                logMonitor("Error when cloneTemplateFile " + templateFilePath + ": " + e.getMessage());
                throw e;
            }
        }
        return null;
    }


    public List exportReport(Vector vtProcedureConfig, String strReportPath, String strFileName, HashMap<String, String> hmSheetConvert, String strTemplatePath) throws Exception {
        List listReturn = new ArrayList();
        if (!strReportPath.endsWith("/") && !strReportPath.endsWith("\\")) {
            strReportPath += "/";
        }

        String relativeTemplatePath = "tempFile/";
        File parentTemp = new File(relativeTemplatePath);
        if (!parentTemp.exists() && !parentTemp.mkdirs()) {
            throw new IllegalStateException("Couldn't create dir: " + parentTemp);
        }
        String pathTemp = parentTemp.getAbsolutePath();
        if (!pathTemp.endsWith("/") && !pathTemp.endsWith("\\")) {
            pathTemp += "/";
        }

        File fileTemp = null;
//        SXSSFWorkbook wb = null;
        XSSFWorkbook wb = null;
        if (strTemplatePath != null && !"".equals(strTemplatePath)) {
            fileTemp = cloneTemplateFile(strTemplatePath, pathTemp);
//            wb = createWookBook(fileTemp.getPath());
            wb = createWookBookXSSF(fileTemp.getPath());
        } else {
//            wb = createWookBook("");
            wb = createWookBookXSSF("");
        }

//        SXSSFWorkbook wb = createWookBook(strTemplatePath);

        wb.setForceFormulaRecalculation(true);
        Font headerFont = wb.createFont();
        headerFont.setFontHeightInPoints((short) 11);
        headerFont.setFontName("Calibri");
//        headerFont.setBold(true);
        headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        headerFont.setItalic(false);

        Font titleFont = wb.createFont();
        titleFont.setFontHeightInPoints((short) 12);
        titleFont.setFontName("Calibri");
        titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);

        CellStyle titleStyle = wb.createCellStyle();
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(CellStyle.ALIGN_LEFT);

        CellStyle headerStyle = wb.createCellStyle();
        ;
        headerStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        headerStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        headerStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        headerStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(HSSFColor.PALE_BLUE.index);
        headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        headerStyle.setWrapText(true);
        headerStyle.setAlignment(CellStyle.ALIGN_CENTER);

        CellStyle style = wb.createCellStyle();
        style.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        style.setBorderTop(XSSFCellStyle.BORDER_THIN);
        style.setBorderRight(XSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(XSSFCellStyle.BORDER_THIN);

        DataFormat format = wb.createDataFormat();

        CellStyle numberCellStyle = wb.createCellStyle();
        numberCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        numberCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        numberCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        numberCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        numberCellStyle.setDataFormat(format.getFormat("#,##0; -#,##0"));

        String filePath = "";
        String dateName = "";
        Row r = null;
        Cell cell = null;

        int sheetNum = 2;


        try {
            Vector vtProcedure = (Vector) vtProcedureConfig.get(0);
            HashMap<String, Object> hmSheetRowRecord = new HashMap<>();
            boolean hasData = false;
            for (int j = 0; j < vtProcedure.size(); j++) {
                String strProcedureName = "";
                CallableStatement stmt = null;
                ResultSet rsData = null;
                try {
                    Vector vtConfig = (Vector) vtProcedure.get(j);
                    strProcedureName = ((Vector) vtConfig).get(0).toString();
                    Vector vtListParam = new Vector();
                    if (((Vector) vtConfig).get(1) instanceof Vector) {
                        vtListParam = (Vector) ((Vector) vtConfig).get(1);
                    }
                    boolean bHtmlMail = StringUtil.nvl(((Vector) vtConfig).get(2), "").equals("Y");
                    String strSheetName = ((Vector) vtConfig).get(3).toString();

                    //row start record data (-1 because rownum++)
                    int rowStartRecord = Integer.parseInt(StringUtil.nvl(vtConfig.get(4), "0").toString()) - 1;
                    //column start record
                    int columnStartRecord = Integer.parseInt(StringUtil.nvl(vtConfig.get(5), "0").toString());
                    int rowNum = rowStartRecord;
                    List listData = getResultData(stmt,strProcedureName, vtListParam);
                    String headerReport = "";
                    if (listData == null || listData.isEmpty()) {
                        logMonitor("No data return from procedure: " + strProcedureName);
                        continue;
                    } else {
                        hasData = true;
                        rsData = (ResultSet) listData.get(0);
                        headerReport = (String) listData.get(1);
                        if (bHtmlMail == true) {
                            hmSheetConvert.put(strSheetName, headerReport);
                        }
                    }

//                SXSSFSheet sheet = null;
                    XSSFSheet sheet = null;
                    ResultSetMetaData metaData = rsData.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    if ("".equals(strTemplatePath)) {
                        hmSheetRowRecord.put(strSheetName, rowStartRecord);
//                    sheet = (SXSSFSheet) wb.createSheet(strSheetName);
                        sheet = (XSSFSheet) wb.createSheet(strSheetName);
                        sheet.setDefaultColumnWidth(14);

                        //create header for report no template
                        r = sheet.createRow(0);
                        cell = r.createCell(0);
                        cell.setCellValue(headerReport);
                        cell.setCellStyle(titleStyle);

                        r = sheet.createRow(2);
                        //Ghi column name
                        for (int i = 1; i < columnCount + 1; i++) {
                            cell = r.createCell(i - 1);
                            cell.setCellValue(metaData.getColumnName(i));
                            cell.setCellStyle(headerStyle);
                        }
                        //ghi dữ liệu từ dòng 3 nếu không có file template
                        rowNum = 2;
                        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columnCount - 1));
                    } else {
                        hmSheetRowRecord.put(strSheetName, rowStartRecord - 1);
                        try {
//                        sheet = (SXSSFSheet) wb.getSheet(strSheetName);
                            sheet = (XSSFSheet) wb.getSheet(strSheetName);
                            if (sheet == null) {
//                            sheet = (SXSSFSheet) wb.createSheet(strSheetName);
                                sheet = (XSSFSheet) wb.createSheet(strSheetName);
                            }
                        } catch (Exception ex) {
//                        sheet = (SXSSFSheet) wb.createSheet(strSheetName);
                            sheet = (XSSFSheet) wb.createSheet(strSheetName);
                        }
                    }
                    while (rsData.next()) {
                        rowNum++;
                        if (rowNum >= 1000000) {

                            r = sheet.createRow(0);
                            cell = r.createCell(0);
                            cell.setCellValue(headerReport);
                            cell.setCellStyle(titleStyle);

//                        sheet = (SXSSFSheet) wb.createSheet(strSheetName + sheetNum);
                            sheet = (XSSFSheet) wb.createSheet(strSheetName + sheetNum);
                            sheet.setDefaultColumnWidth(14);
                            rowNum = rowStartRecord;
                            sheetNum++;
                            r = sheet.createRow(rowStartRecord);
                            for (int i = 1; i < columnCount + 1; i++) {
                                cell = r.createCell(i - 1 + columnStartRecord);
                                cell.setCellValue(metaData.getColumnName(i));
                                cell.setCellStyle(headerStyle);
                            }
                            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columnCount - 1));

                        }
                        //test
                        r = sheet.createRow(0);
                        cell = r.createCell(0);
                        cell.setCellValue(headerReport);
                        cell.setCellStyle(titleStyle);

                        if (sheet.getRow(rowNum) != null) {
                            r = sheet.getRow(rowNum);
                        } else {
                            r = sheet.createRow(rowNum);
                        }
                        for (int i = 0; i < columnCount; i++) {
                            cell = r.createCell(i + columnStartRecord);
                            String strCellValue = StringUtil.nvl(rsData.getString(i + 1), "");
                            try {
                                Double dbCellValue = Double.valueOf(strCellValue);
                                cell.setCellValue(dbCellValue);
                                if ((dbCellValue == Math.floor(dbCellValue)) && !Double.isInfinite(dbCellValue)) {
                                    cell.setCellStyle(numberCellStyle);
                                } else {
                                    cell.setCellStyle(numberCellStyle);
                                }
                            } catch (NumberFormatException ex) {
                                cell.setCellValue(rsData.getString(i + 1));
                                cell.setCellStyle(style);
                            }

                        }

                    }

                    //auto size column
                    if (strTemplatePath == null || "".equals(strTemplatePath)) {
                        for (int i = columnStartRecord; i < columnCount + columnStartRecord; i++) {
                            try {
                                sheet.autoSizeColumn(i);
                            } catch (Exception ex) {
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    logMonitor("Error when run procedure " + strProcedureName + ": " + ex.getMessage());
                    continue;
                }finally {
                    Database.closeObject(rsData);
                    Database.closeObject(stmt);
                }
            }

            if(hasData) {
                DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                Date date = new Date();
                dateName = dateFormat.format(date);
                filePath = strReportPath + strFileName + "_" + dateName + ".xlsx";
                try (FileOutputStream output = new FileOutputStream(new File(filePath))) {
                    wb.write(output);
//                wb.close();
                    //when wb = sxssf enable dispose
                    //   wb.dispose();
                    logMonitor("Excel written successfully: " + filePath);
                } catch (Exception ex) {
                    logMonitor("Error when write file. " + ex.getMessage());
                    throw ex;
                } finally {
                    wb.close();
                }
                listReturn.add(filePath);
                listReturn.add(hmSheetConvert);
                listReturn.add(hmSheetRowRecord);
            }else{
                logMonitor("No data in report");
            }

        } catch (Exception ex) {
            throw ex;
        } finally {
            if (fileTemp != null) {
                fileTemp.delete();
            }

        }
        return listReturn;
    }


    private List getResultData(CallableStatement stmt, String strProcedureName, Vector vtListParam) throws Exception {
        List listReturn = new ArrayList();
        ResultSet rsHeader = null;
        ResultSet rsResult = null;
        String strHeader = "";
        StringBuilder strbSQL = new StringBuilder("BEGIN " + strProcedureName + "(?,?");
        for (int i = 0; i < vtListParam.size(); i++) {
            strbSQL.append(",?");
        }
        strbSQL.append("); END;");
        try {
            stmt = this.getConnection().prepareCall(strbSQL.toString());
            stmt.registerOutParameter(1, OracleTypes.CURSOR); //REF CURSOR out
            stmt.registerOutParameter(2, OracleTypes.CURSOR); //REF CURSOR out
            for (int i = 0; i < vtListParam.size(); i++) {
                Vector<String> vtParam = (Vector<String>) vtListParam.get(i);
                String strParamValue = vtParam.get(1);
                stmt.setString(3 + i, strParamValue);
            }
            stmt.execute();
            rsResult = (ResultSet) stmt.getObject(1);
            if (!rsResult.isBeforeFirst()) {
//                return null;
                logMonitor("No data return from procedure: " + strProcedureName);
            }
            rsHeader = (ResultSet) stmt.getObject(2);
            if(rsHeader.next()) {
                strHeader = rsHeader.getString("title");
            }
            listReturn.add(rsResult);
            listReturn.add(strHeader);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            Database.closeObject(rsHeader);
        }
        return listReturn;
    }

}
