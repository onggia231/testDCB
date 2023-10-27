package com.telsoft.cbs.thread;

import com.telsoft.cbs.utils.MailServicesUtil;
import com.telsoft.database.Database;
import com.telsoft.thread.DBManageableThread;
import com.telsoft.thread.GroupParameter;
import com.telsoft.thread.ParameterType;
import com.telsoft.thread.ThreadParameter;
import com.telsoft.util.AppException;
import com.telsoft.util.StringUtil;
import oracle.jdbc.OracleTypes;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;


import java.io.File;
import java.io.FileOutputStream;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * Created by Viz on 16/03/2016.
 */
public class AutoSendEmailReportProcedure2 extends DBManageableThread {


    private String mstrHost;
    private int miPort;
    private boolean mbStartTLS;
    private boolean mbSSLOnConnect;
    private String mstrUser;
    private String mstrPassword;
    private String mstrSender;
    private Vector mvtListMailConfig;
    private Vector mstrFileName;
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

        Vector<ThreadParameter> vtConfigProcedure = new Vector<>();
        Vector<ThreadParameter> vtConfigProcedureParam = new Vector<>();
        vtConfigProcedureParam.addElement(createParameter("ParamName", "", ParameterType.PARAM_TEXTBOX_MAX, "900", "Parameter name"));
        vtConfigProcedureParam.addElement(createParameter("ParamValue", "", ParameterType.PARAM_TEXTBOX_MAX, "900", "Parameter Value"));
        vtConfigProcedure.addElement(createParameter("ReportName", "", ParameterType.PARAM_TEXTBOX_MAX, "900", "Report Name"));
        vtConfigProcedure.addElement(createParameter("ProcedureName", "", ParameterType.PARAM_TEXTBOX_MAX, "900", "Procedure Name"));
        vtConfigProcedure.addElement(createParameter("ProcedureParameter", "", ParameterType.PARAM_TABLE, vtConfigProcedureParam));
        vtConfigProcedure.addElement(createParameter("FileName", "", ParameterType.PARAM_TEXTBOX_MAX, "256", "Exported file name"));
        vtListMailConfig.addElement(createParameter("ProcedureConfig", "", ParameterType.PARAM_TABLE, vtConfigProcedure));
        vtListMailConfig.addElement(createParameter("MailSubject", "", ParameterType.PARAM_TEXTBOX_MAX, "256", "Subject email"));
        Vector vtYesNo = new Vector<>();
        vtYesNo.add("Y");
        vtYesNo.add("N");
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

                //Get Procedure Config
                Vector vtProcedureConfig = new Vector();
                if (vtMailConfig.get(1) instanceof Vector) {
                    vtProcedureConfig = (Vector) vtMailConfig.get(1);
                }

                //Get Mail Subject
                String strMailSubject = StringUtil.nvl(vtMailConfig.get(2), "").trim();
                //Get zip file status
                boolean bZipFile = StringUtil.nvl(vtMailConfig.get(3), "").equals("Y");
                //Get Mail Body
                String strMailBody = StringUtil.nvl(vtMailConfig.get(4), "").trim();


                if (lsEmail.size() <= 0 || vtProcedureConfig.size() <= 0 || strMailSubject.equals("") || strMailBody.equals("")) {
                    logMonitor("Config wrong in mail name: " + strMailSubject);
                    continue;
                }

                processProcedureMail(mMailServicesUtil, lsEmail, vtProcedureConfig, strMailSubject, strMailBody, bZipFile);

            } catch (Exception ex) {
                logMonitor("Error when process mail " + iMailIndex + " - " + ex.getMessage());
                continue;
            }
        }

/*
        List<String> lsEmail = new ArrayList<String>();
        for (int iIndex = 0; iIndex < mvtListRecipient.size(); iIndex++) {
            Vector vtRow = (Vector) mvtListRecipient.elementAt(iIndex);
            String strRecipient = (String) vtRow.elementAt(1);
            if (strRecipient != null) {
                if (!strRecipient.equals("")) {
                    lsEmail.add(strRecipient);
                }
            }
        }

        if (lsEmail.size() > 0 && mvtProcedureConfig.size() > 0) {
            mMailServicesUtil = new MailServicesUtil(mstrHost, miPort, mstrUser, mstrPassword, mbStartTLS, mbSSLOnConnect, mstrSender);

            List<String> listFile = new ArrayList<String>();
            List<String> listFileZiped = new ArrayList<String>();

            //create export dir
            String relativePath = "excelReport/";
            File parent = new File(relativePath);
            if (!parent.exists() && !parent.mkdirs()) {
                throw new IllegalStateException("Couldn't create dir: " + parent);
            }
            String path = parent.getAbsolutePath();
            if(!path.endsWith("/") && !path.endsWith("\\")){
                path += "/";
            }
            try {
                logMonitor("Start process mail: ");

                for(Object vtProcedure : mvtProcedureConfig) {
                    String strReportName = ((Vector)vtProcedure).get(0).toString();
                    String strProcedureName = ((Vector)vtProcedure).get(1).toString();
                    Vector vtListParam = new Vector();
                    if(((Vector)vtProcedure).get(2) instanceof Vector){
                        vtListParam = (Vector)((Vector)vtProcedure).get(2);
                    }
                    logMonitor("Export excel attachment procedure " + strProcedureName);
                    String strFileReport = exportReport(strProcedureName,vtListParam,path,strReportName);
                    if (!strFileReport.equals("")) {
                        listFile.add(strFileReport);
                    }
                }
                if (listFile.size() > 0 && lsEmail.size() > 0) {
                    logMonitor("Sending mail");
                    if(mbZipFile){
                        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                        Date date = new Date();
                        String dateName = dateFormat.format(date);
                        listFileZiped = mMailServicesUtil.zipFile(listFile,path + "REPORT_" + mstrMailSubject.trim().replace(" ","").replace("/","").replace("\\","") + dateName);
                        if(listFileZiped.size()>0){
                            mMailServicesUtil.sendMail(lsEmail, mstrMailSubject, mstrMailBody, listFileZiped);
                        }

                    }else {
                        mMailServicesUtil.sendMail(lsEmail, mstrMailSubject, mstrMailBody, listFile);
                    }
                }

            } catch (Exception ex) {
                logMonitor(ex.toString());
            } finally {
                if (listFile.size() > 0) {
                    mMailServicesUtil.deleteFile(listFile);
                }
                if (listFileZiped.size() > 0) {
                    mMailServicesUtil.deleteFile(listFileZiped);
                }
            }
        }
        */
    }

    private void processProcedureMail(MailServicesUtil mMailServicesUtil, List<String> lsEmail, Vector vtProcedureConfig, String strMailSubject, String strMailBody, boolean bZipFile) throws Exception {
        List<String> listFile = new ArrayList<String>();
        List<String> listFileZiped = new ArrayList<String>();

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
        try {
            logMonitor("Starting process mail: " + strMailSubject);

            for (Object vtProcedure : vtProcedureConfig) {
                String strFileName = ((Vector) vtProcedure).get(3).toString().trim();

                String strReportName = ((Vector) vtProcedure).get(0).toString();
                String strProcedureName = ((Vector) vtProcedure).get(1).toString();
                Vector vtListParam = new Vector();
                if (((Vector) vtProcedure).get(2) instanceof Vector) {
                    vtListParam = (Vector) ((Vector) vtProcedure).get(2);
                }
                //Get File Name
                logMonitor("Export excel attachment procedure " + strProcedureName);
                String strFileReport = exportReport(strProcedureName, vtListParam, path, strReportName, strFileName);
                if (strFileReport != null && !"".equals(strFileReport)) {
                    listFile.add(strFileReport);
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
                        mMailServicesUtil.sendMail(lsEmail, strMailSubject, strMailBody, listFileZiped);
                    }

                } else {
                    mMailServicesUtil.sendMail(lsEmail, strMailSubject, strMailBody, listFile);
                }
            }
            logMonitor("Done!");

        } catch (Exception ex) {
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

    public String exportReport(String strProcedureName, Vector vtlistParam, String strReportPath, String strReportName, String strFileName) throws Exception {
        String strReturn = "";

        CallableStatement stmt = null;
        ResultSet rsResult = null;
        ResultSet rsResultConfig = null;
        StringBuilder strbSQL = new StringBuilder("BEGIN " + strProcedureName + "(?,?");
        for (int i = 0; i < vtlistParam.size(); i++) {
            strbSQL.append(",?");
        }
        strbSQL.append("); END;");
        try {
            stmt = this.getConnection().prepareCall(strbSQL.toString());
            stmt.registerOutParameter(1, OracleTypes.CURSOR); //REF CURSOR out
            stmt.registerOutParameter(2, OracleTypes.CURSOR); //REF CURSOR out
            for (int i = 0; i < vtlistParam.size(); i++) {
                Vector<String> vtParam = (Vector<String>) vtlistParam.get(i);
                String strParamValue = vtParam.get(1);
                stmt.setString(3 + i, strParamValue);
            }
            stmt.execute();
            rsResult = (ResultSet) stmt.getObject(1);
            if(!rsResult.isBeforeFirst()){
                return "";
            }
            rsResultConfig = (ResultSet) stmt.getObject(2);

            rsResultConfig.next();
            if (strFileName.equals(""))
                strFileName = rsResultConfig.getString("filename");
            strReportName = rsResultConfig.getString("headername");
            String strSheetName = rsResultConfig.getString("sheetname");

            strReturn = exportExcel(rsResult, strReportPath, strFileName, strReportName, strSheetName);
        } catch (Exception ex) {
            throw ex;
        } finally {
            Database.closeObject(rsResult);
            Database.closeObject(stmt);
        }
        return strReturn;
    }

    public static String exportExcel(ResultSet rs, String path, String strFileName,
                                     String strHeader, String strSheetName) throws Exception {
        if (!path.endsWith("/") && !path.endsWith("\\")) {
            path += "/";
        }
        SXSSFWorkbook wb = new SXSSFWorkbook(5000);
        SXSSFSheet sheet = (SXSSFSheet) wb.createSheet(strSheetName);
        sheet.setDefaultColumnWidth(14);
        Row r = null;
        Cell cell = null;
        int rowNum = 2;
        int sheetNum = 2;
        Font headerFont = wb.createFont();
        headerFont.setFontHeightInPoints((short) 10);
        headerFont.setFontName("Arial");
//        headerFont.setBold(true);
        headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        headerFont.setItalic(false);

        Font titleFont = wb.createFont();
        titleFont.setFontHeightInPoints((short) 12);
        titleFont.setFontName("Arial");
        titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);

        CellStyle titleStyle = wb.createCellStyle();
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(CellStyle.ALIGN_CENTER);

        CellStyle headerStyle = wb.createCellStyle();
        ;
        headerStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        headerStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        headerStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        headerStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(HSSFColor.AQUA.index);
        headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        headerStyle.setWrapText(true);

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
        numberCellStyle.setDataFormat(format.getFormat("###,##0.##"));

        String filePath = "";
        String dateName = "";

        r = sheet.createRow(0);
        cell = r.createCell(0);
        cell.setCellValue(strHeader);
        cell.setCellStyle(titleStyle);

        try {

            r = sheet.createRow(2);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i < columnCount + 1; i++) {
                cell = r.createCell(i - 1);
                cell.setCellValue(metaData.getColumnName(i));
                cell.setCellStyle(headerStyle);
            }
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columnCount - 1));

            while (rs.next()) {
                rowNum++;
                if (rowNum >= 30000) {
                    sheet = (SXSSFSheet) wb.createSheet(strSheetName + sheetNum);
                    sheet.setDefaultColumnWidth(14);
                    rowNum = 3;
                    sheetNum++;
                    r = sheet.createRow(2);
                    for (int i = 1; i < columnCount + 1; i++) {
                        cell = r.createCell(i - 1);
                        cell.setCellValue(metaData.getColumnName(i));
                        cell.setCellStyle(headerStyle);
                    }
                    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columnCount - 1));

                }
                r = sheet.createRow(rowNum);
                for (int i = 0; i < columnCount; i++) {
                    cell = r.createCell(i);
                    String strCellValue = StringUtil.nvl(rs.getString(i + 1), "");
                    try {
                        Double dbCellValue = Double.valueOf(strCellValue);
                        cell.setCellValue(dbCellValue);
                        if ((dbCellValue == Math.floor(dbCellValue)) && !Double.isInfinite(dbCellValue)) {
                            cell.setCellStyle(style);
                        } else {
                            cell.setCellStyle(numberCellStyle);
                        }
                    } catch (NumberFormatException ex) {
                        cell.setCellValue(rs.getString(i + 1));
                        cell.setCellStyle(style);
                    }
                }

            }
//            for (int j = 0; j < wb.getNumberOfSheets(); j++) {
//                for (int i = 0; i < columnCount + 3; i++) {
//                    try {
//                        wb.getSheetAt(j).autoSizeColumn(i);
//                    } catch (Exception ex) {
//                    }
//                }
//            }

            DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            Date date = new Date();
            dateName = dateFormat.format(date);
            filePath = path + strFileName + "_" + dateName + ".xlsx";
            try (FileOutputStream output = new FileOutputStream(new File(filePath))) {
                wb.write(output);
            } catch (Exception ex) {
                throw ex;
            }

            System.out.println("Excel written successfully..");
        } catch (Exception ex) {
            throw new Exception("Error when exportExcel" + ex.getMessage());
        }
        return filePath;
    }
}
