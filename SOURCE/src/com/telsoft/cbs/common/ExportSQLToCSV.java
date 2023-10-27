package com.telsoft.cbs.common;

import com.opencsv.CSVWriter;
import com.telsoft.cbs.utils.DbUtils;
import com.telsoft.cbs.utils.ExportCSVFile;
import com.telsoft.database.Database;
import com.telsoft.thread.DBManageableThread;
import com.telsoft.thread.ParameterType;
import com.telsoft.thread.ParameterUtil;
import com.telsoft.util.AppException;

import com.telsoft.util.FileUtil;
import com.telsoft.util.SmartZip;
import com.telsoft.util.StringUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;

/**
 * Created by VinhDHQ on 2016-11-01.
 */
public class ExportSQLToCSV extends DBManageableThread {
    private String mstrSQLCommandExp, mstrSqlCmdUpdate, mstrExpDir,
            mstrTempDir, mstrFileFormat, mstrDateFormat, mstrFileName, mstrCurrentDate,
            mstrLogFileType, mstrPartitionFormat;
    private char mstrSeparator = CSVWriter.DEFAULT_SEPARATOR,
            mcQuote = CSVWriter.DEFAULT_QUOTE_CHARACTER;
    private boolean mbForceQuote = false;

    private ArrayList arrRowId;
    int intMaxSeq = 0;
    int intMinSeq = 0;
    int intExpertSeq = 0;
    private String mstrSeqFormat;
    private boolean mbZipFile = false;
    private boolean mbIncludeHeaderColumns = false;

    public void fillParameter() throws AppException
    {
        mstrSQLCommandExp = loadString("SQLCommandExp");
        if (mstrSQLCommandExp.toUpperCase().indexOf("$PARTITION") > 0)
        {
            mstrPartitionFormat = loadString("PartitionFormat");
            String[] arrTemp = mstrPartitionFormat.split(";");
//            String strTemp;
//            try
//            {
////                if (mcnMain == null)
////                {
////                    mcnMain = mmgrMain.getConnection();
////                }
////				strTemp = StringUtil.replaceAll(arrTemp[0].toUpperCase(), "$DATE", "") + INUtil.getSysdate(mcnMain, arrTemp[1]);
//            }
//            catch (Exception e)
//            {
//                e.printStackTrace();
//                throw new AppException(e.getMessage());
//            }
//			mstrSQLCommandExp = StringUtil.replaceAll(mstrSQLCommandExp.toUpperCase(), "$PARTITION", strTemp);
        }
        mstrSqlCmdUpdate = StringUtil.nvl(getParameter("SQLCommandUpd"), "");
        mstrFileFormat = loadString("FileFormat");
        if (mstrFileFormat.indexOf("$Date") >= 0)
        {
            mstrDateFormat = loadString("DateFormat");
        }
        if (mstrFileFormat.indexOf("$Seq") >= 0)
        {
            intMinSeq = loadInteger("min-seq");
            intMaxSeq = loadInteger("max-seq");
            intExpertSeq = loadInteger("expert-seq");
            mstrSeqFormat = loadString("SeqFormat");
        }
        mstrExpDir = loadDirectory("ExportDir", true, true);
        mstrTempDir = loadDirectory("TempDir", true, true);

        String strSeparator = StringUtil.nvl(getParameter("Separator"), "");
        if (strSeparator.length()>0)
        {
            mstrSeparator = strSeparator.charAt(0);
        }
        String strQuote = StringUtil.nvl(getParameter("QuoteCharacter"), "");
        if (strQuote.length()>0)
        {
            mcQuote = strQuote.charAt(0);
        }
        mbForceQuote = loadBoolean("ForceQuote");

        mbZipFile = StringUtil.nvl(getParameter("ZipFile"), "N").equals("Y") ? true : false;
        mbIncludeHeaderColumns = StringUtil.nvl(getParameter("IncludeHeaderColumns"), "N").equals("Y") ? true : false;
//		mstrLogFileType = StringUtil.nvl(getParameter("LogFileType"), "").trim();
        super.fillParameter();
    }

    public Vector getParameterDefinition()
    {
        Vector vtReturn = new Vector();
        vtReturn.add(createParameter("SQLCommandExp", "", ParameterType.PARAM_TEXTAREA_MAX, "5000000",
                "Câu lệnh kết xuất dữ liệu, nếu khai báo SQLCommandUpd thì phải có rowid ở truờng đầu tiên. Sử dụng tham số $PARTITION để dùng partition"));
        vtReturn.add(createParameter("PartitionFormat", "", ParameterType.PARAM_TEXTAREA_MAX, "100",
                "Nếu sử dụng tham số $PARTITION ở khai báo SQLCommandExp thì phải khai báo giá trị định dạng parition. Ví dụ:DATA$Date;yyyymm"));
        vtReturn.add(createParameter("SQLCommandUpd", "", ParameterType.PARAM_TEXTAREA_MAX, "10000",
                "Cau lenh cap nhat ban ghi da ket xuat, tranh ket xuat du lieu trung nhieu lan"));
        vtReturn.add(createParameter("IncludeHeaderColumns", "", ParameterType.PARAM_COMBOBOX, new String[] { "Y", "N" }, "Them Header Column vao dong dau tien"));
        vtReturn.add(createParameter("FileFormat", "", ParameterType.PARAM_TEXTBOX_MAX, "100",
                "Dung $Date,$Seq de lam tham so"));
        vtReturn.add(createParameter("min-seq", "", ParameterType.PARAM_TEXTBOX_MASK, "9",
                "Gia tri Sequence nho nhat dung cho ten file"));
        vtReturn.add(createParameter("max-seq", "", ParameterType.PARAM_TEXTBOX_MASK, "99999",
                "Gia tri Sequence lon nhat dung cho ten file"));
        vtReturn.add(createParameter("expert-seq", "", ParameterType.PARAM_TEXTBOX_MASK, "99999",
                "Gia tri Sequence bat dau dung cho ten file"));
        vtReturn.add(createParameter("SeqFormat", "", ParameterType.PARAM_TEXTBOX_MAX, "100", "Dinh dang dung cho $Seq", ""));
        vtReturn.add(createParameter("DateFormat", "", ParameterType.PARAM_TEXTBOX_MAX, "100",
                "Dinh dang ngay dung cho ten file, thuong dung yyyyMMdd"));
        vtReturn.add(createParameter("ExportDir", "", ParameterType.PARAM_TEXTBOX_MAX, "100",
                "Thu muc luu file du lieu sau khi ket xuat"));
        vtReturn.add(createParameter("TempDir", "", ParameterType.PARAM_TEXTBOX_MAX, "100",
                "Thu muc luu file tam du lieu sau khi ket xuat"));
        vtReturn.add(createParameter("Separator", "", ParameterType.PARAM_TEXTBOX_MAX, "1",
                "Ngan cach giua cac truong trong file ket xuat. Mac dinh: ,"));
        vtReturn.add(createParameter("QuoteCharacter", "", ParameterType.PARAM_TEXTBOX_MAX, "1",
                "Ky tu bao cac truong gia tri. Mac dinh: \""));
        vtReturn.add(ParameterUtil.createParameter("ForceQuote", "", ParameterType.PARAM_COMBOBOX, Boolean.class, "Bat buoc bo sung ky tu quote cho cac truong du lieu"));
        vtReturn.add(createParameter("ZipFile", "", ParameterType.PARAM_COMBOBOX, new String[] { "Y", "N" }, "Nen zip sau khi ket xuat file"));

//		vtReturn.add(createParameter("LogFileType", "", ParameterType.PARAM_COMBOBOX, INUtil.mvtFileType,
//		        "Ghi log qua trinh xu ly du lieu"));
        vtReturn.addAll(super.getParameterDefinition());
        return vtReturn;
    }

    public void beforeSession()
    {
        try
        {
            super.beforeSession();
            logMonitor("Export data process started");
            arrRowId = null;
            mcnMain.setAutoCommit(false);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void afterSession() throws Exception
    {
        super.afterSession();
        logMonitor("Export data finished.");
    }

    public void processSession() throws Exception
    {
        try
        {
            processFile();
            mcnMain.commit();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logMonitor("Error : " + e.getMessage());
//            mcnMain.rollback();
            DbUtils.rollbackTransaction(mcnMain);
        }
        finally
        {
//            mcnMain.setAutoCommit(true);
            DbUtils.setAutocommitConnection(mcnMain,true);
        }
    }

    public void updateData() throws SQLException
    {
        PreparedStatement stmt = null;
        String[] arrTemp;
        long lgBatchSize = 10000;
        try
        {
            stmt = mcnMain.prepareStatement(mstrSqlCmdUpdate);
            for (int i = 0; i < arrRowId.size(); i++)
            {
                arrTemp = StringUtil.nvl(arrRowId.get(i), "").split(",");
                for (int j = 0; j < arrTemp.length; j++)
                {
                    stmt.setString(1, arrTemp[j]);
                    stmt.addBatch();
                    if (i > 0 && i % lgBatchSize == 0)
                    {
                        stmt.executeBatch();
                        stmt.clearBatch();
                    }
                }
            }
            stmt.executeBatch();
        }
        finally
        {
            Database.closeObject(stmt);
        }
    }

    public void processFile() throws Exception
    {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ExportCSVFile writeFile = new ExportCSVFile();
        String strFileBase, strFileZip;

        try
        {
//			String strStaDatetime = StringUtil.format(new java.util.Date(), INUtil.JAVA_FRM_DATETIME);
            pstmt = mcnMain.prepareStatement(mstrSQLCommandExp);
            rs = pstmt.executeQuery();
            if (rs != null && rs.isBeforeFirst())
            {
                strFileBase = mstrFileFormat;
                if (strFileBase.indexOf("$Date") >= 0)
                {
                    Calendar currentDate = Calendar.getInstance();
                    SimpleDateFormat formatter = new SimpleDateFormat(mstrDateFormat);
                    mstrCurrentDate = formatter.format(currentDate.getTime());
                    strFileBase = StringUtil.replaceAll(strFileBase, "$Date", mstrCurrentDate);
                }
                if (strFileBase.indexOf("$Seq") >= 0)
                {
                    strFileBase = StringUtil.replaceAll(strFileBase, "$Seq", StringUtil.format(intExpertSeq, mstrSeqFormat));
                }
                strFileZip = strFileBase + ".zip";
                mstrFileName = strFileBase;

                String strFileTemptxt = mstrTempDir + mstrFileName + ".tmp";
                writeFile.newFile(strFileTemptxt);
                writeFile.setMstrSeparator(mstrSeparator);
                writeFile.setMcQuote(mcQuote);
                writeFile.setMbForceQuote(mbForceQuote);
                if (mstrSqlCmdUpdate.equals("")) {
                    writeFile.writeResultSet(rs, mbIncludeHeaderColumns);
                }
                else{
                    arrRowId = writeFile.writeResultSetAndUpdateBack(rs,mbIncludeHeaderColumns);
                }
                writeFile.closeFile();
                Database.closeObject(rs);
                Database.closeObject(pstmt);

                String strExpFile = mstrExpDir + ((mbZipFile) ? strFileZip : mstrFileName);

                if (writeFile.getMiSuccessRecords() > 0)
                {
                    logMonitor("Export result:");
                    logMonitor("    Exported Records   : " + writeFile.getMiSuccessRecords());
                    logMonitor("    Errored Records    : " + writeFile.getMiErrorRecords());
                    logMonitor("Write data to file completed");
                    if (!mstrSqlCmdUpdate.equals(""))
                    {
                        updateData();
                    }

                    logMonitor("Data exported to file: " + strExpFile);
                    if (mstrFileFormat.indexOf("$Seq") >= 0)
                    {

                        if (intExpertSeq == intMaxSeq)
                        {
                            intExpertSeq = intMinSeq;
                        }
                        setParameter("expert-seq", Integer.toString(intExpertSeq + 1));
                        storeConfig();
                    }
                    if (mbZipFile)
                    {
                        SmartZip.Zip(strFileTemptxt, strFileZip, false);
                        FileUtil.deleteFile(strFileTemptxt);
                        FileUtil.renameFile(strFileZip, strExpFile, true);
                    }
                    else
                    {
                        FileUtil.renameFile(strFileTemptxt, strExpFile, true);
                    }
                }
                else
                {
                    logMonitor("No data found");
                }
            }
            else
            {
                logMonitor("No data found");
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            logMonitor("Could not export data: " + ex.getMessage());
//            mcnMain.rollback();
            DbUtils.rollbackTransaction(mcnMain);
        }
        finally
        {
            Database.closeObject(rs);
            Database.closeObject(pstmt);
            writeFile.closeFile();
        }
    }
}
