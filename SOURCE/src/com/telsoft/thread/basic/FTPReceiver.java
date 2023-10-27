//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.telsoft.thread.basic;

import com.telsoft.database.Database;
import com.telsoft.thread.file.FileEntry;
import com.telsoft.util.AppException;
import com.telsoft.util.DateUtil;
import com.telsoft.util.FileUtil;
import com.telsoft.util.StringUtil;
import com.telsoft.util.WildcardFilter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.parser.EnterpriseUnixFTPEntryParser;
import org.apache.commons.net.ftp.parser.NTFTPEntryParser;
import org.apache.commons.net.ftp.parser.OS2FTPEntryParser;
import org.apache.commons.net.ftp.parser.OS400FTPEntryParser;
import org.apache.commons.net.ftp.parser.UnixFTPEntryParser;
import org.apache.commons.net.ftp.parser.VMSFTPEntryParser;
import org.apache.commons.net.ftp.parser.VMSVersioningFTPEntryParser;

public class FTPReceiver extends FTPThread {
    protected String mstrTempDir;
    protected String mstrListingMode;
    private String mstrDirectBackupDir;

    public FTPReceiver() {
    }

    public void fillParameter() throws AppException {
        super.fillParameter();
        this.mstrTempDir = this.loadDirectory("TempDir", true, false);
        this.mstrListingMode = StringUtil.nvl(this.getParameter("ListingMode"), "");
    }

    public Vector getParameterDefinition() {
        Vector vtReturn = super.getParameterDefinition();
        vtReturn.insertElementAt(createParameter("TempDir", "", 2, "256", ""), 16);
        Vector vtValue = new Vector();
        vtValue.addElement("Auto detect");
        vtValue.addElement("Unix");
        vtValue.addElement("EnterpriseUnix");
        vtValue.addElement("NT");
        vtValue.addElement("OS2");
        vtValue.addElement("OS400");
        vtValue.addElement("VMS");
        vtValue.addElement("VMSVersioning");
        vtReturn.insertElementAt(createParameter("ListingMode", "", 4, vtValue, ""), 7);
        removeParameterDefinition(vtReturn, "FtpFileFormat");
        return vtReturn;
    }

    public void validateParameter() throws Exception {
        super.validateParameter();
        if (this.mstrRemoteStyle != null && this.mstrRemoteStyle.length() > 0 && !this.mstrRemoteStyle.equals("Directly")) {
            if (this.mstrProcessDate != null && this.mstrProcessDate.length() != 0) {
                if (this.mstrDateFormat != null && this.mstrDateFormat.length() != 0) {
                    if (!DateUtil.isDate(this.mstrProcessDate, this.mstrDateFormat)) {
                        throw new AppException("ProcessDate does not match DateFormat", "FTPReceiver.validateParameter", "ProcessDate");
                    } else if (this.mstrRemoteStyle.equals("Daily") && this.mstrDateFormat.indexOf("dd") < 0) {
                        throw new AppException("DateFormat must contain 'dd' when FTPStyle='" + this.mstrRemoteStyle + "'", "FTPReceiver.validateParameter", "DateFormat");
                    } else if (this.mstrRemoteStyle.equals("Monthly") && this.mstrDateFormat.indexOf("MM") < 0) {
                        throw new AppException("DateFormat must contain 'MM' when FTPStyle='" + this.mstrRemoteStyle + "'", "FTPReceiver.validateParameter", "DateFormat");
                    } else if (this.mstrRemoteStyle.equals("Yearly") && this.mstrDateFormat.indexOf("yyyy") < 0) {
                        throw new AppException("DateFormat must contain 'yyyy' when FTPStyle='" + this.mstrRemoteStyle + "'", "FTPReceiver.validateParameter", "DateFormat");
                    }
                } else {
                    throw new AppException("DateFormat cannot be null when FTPStyle='" + this.mstrRemoteStyle + "'", "FTPReceiver.validateParameter", "DateFormat");
                }
            } else {
                throw new AppException("ProcessDate cannot be null when FTPStyle='" + this.mstrRemoteStyle + "'", "FTPReceiver.validateParameter", "ProcessDate");
            }
        }
    }

    public void process(int iFileIndex) throws Exception {
        FileEntry ffl = (FileEntry)this.mvtFileList.elementAt(iFileIndex);
        String strValidateResult = this.validateFile(ffl);
        boolean bResult = strValidateResult == null || strValidateResult.length() == 0;
        if (!bResult) {
            this.logMonitor(strValidateResult);
        } else {
            this.getFile(ffl);
        }

    }

    public void changeProcessDate() throws Exception {
        if (this.mstrRemoteStyle != null && this.mstrRemoteStyle.length() > 0 && !this.mstrRemoteStyle.equals("Directly")) {
            Date dt = DateUtil.toDate(this.mstrProcessDate, this.mstrDateFormat);

            while(true) {
                if (this.mstrRemoteStyle.equals("Daily")) {
                    dt = DateUtil.addDay(dt, 1);
                } else if (this.mstrRemoteStyle.equals("Monthly")) {
                    dt = DateUtil.addMonth(dt, 1);
                } else if (this.mstrRemoteStyle.equals("Yearly")) {
                    dt = DateUtil.addYear(dt, 1);
                }

                String strNextProcessDate = StringUtil.format(dt, this.mstrDateFormat);

                try {
                    this.mftpMain.changeWorkingDirectory(this.mstrRemoteDir + strNextProcessDate);
                    this.onChangeProcessDate(strNextProcessDate);
                    return;
                } catch (IOException var4) {
                    if (dt.getTime() >= System.currentTimeMillis()) {
                        break;
                    }
                }
            }
        }

    }

    public void onChangeProcessDate(String strProcessDate) throws Exception {
        this.mbNewDirectory = true;
        this.setParameter("NewDirectory", "Y");
        this.mstrProcessDate = strProcessDate;
        this.setParameter("ProcessDate", this.mstrProcessDate);
        this.storeConfig();
    }

    public FileEntry createListItem(FileEntry ffl) {
        return ffl.isFile() && WildcardFilter.match(this.mstrWildcard, ffl.filename) ? ffl : null;
    }

    protected void listFile() throws Exception {
        this.mbListing = true;

        try {
            this.beforeListFile();
            this.mvtFileList = new Vector();
            FileEntry[] fflFileList;
            if (this.mstrListingMode == null || this.mstrListingMode.length() == 0 || this.mstrListingMode.equals("Auto detect")) {
                this.logMonitor("Detecting listing mode");
                fflFileList = null;
                this.mstrListingMode = "";
                if (this.mstrListingMode.length() == 0) {
                    fflFileList = this.mftpMain.listFiles(new OS400FTPEntryParser());
                    if (fflFileList.length > 0) {
                        this.mstrListingMode = "OS400";
                    }
                }

                if (this.mstrListingMode.length() == 0) {
                    fflFileList = this.mftpMain.listFiles(new OS2FTPEntryParser());
                    if (fflFileList.length > 0) {
                        this.mstrListingMode = "OS2";
                    }
                }

                if (this.mstrListingMode.length() == 0) {
                    fflFileList = this.mftpMain.listFiles(new VMSVersioningFTPEntryParser());
                    if (fflFileList.length > 0) {
                        this.mstrListingMode = "VMSVersioning";
                    }
                }

                if (this.mstrListingMode.length() == 0) {
                    fflFileList = this.mftpMain.listFiles(new VMSFTPEntryParser());
                    if (fflFileList.length > 0) {
                        this.mstrListingMode = "VMS";
                    }
                }

                if (this.mstrListingMode.length() == 0) {
                    fflFileList = this.mftpMain.listFiles(new EnterpriseUnixFTPEntryParser());
                    if (fflFileList.length > 0) {
                        this.mstrListingMode = "EnterpriseUnix";
                    }
                }

                if (this.mstrListingMode.length() == 0) {
                    fflFileList = this.mftpMain.listFiles(new UnixFTPEntryParser());
                    if (fflFileList.length > 0) {
                        this.mstrListingMode = "Unix";
                    }
                }

                if (this.mstrListingMode.length() == 0) {
                    fflFileList = this.mftpMain.listFiles(new NTFTPEntryParser());
                    if (fflFileList.length > 0) {
                        this.mstrListingMode = "NT";
                    }
                }

                if (this.mstrListingMode.length() == 0) {
                    this.logMonitor("Could not detect the listing mode of this server");
                    this.mstrListingMode = "Auto detect";
                } else {
                    this.logMonitor("Detected " + this.mstrListingMode + " listing mode");
                    this.setParameter("ListingMode", this.mstrListingMode);
                    this.storeConfig();
                }
            }

            if (this.mstrListingMode.length() > 0 && !this.mstrListingMode.equals("Auto detect")) {
                fflFileList = null;
                Object parser;
                if (this.mstrListingMode.equals("EnterpriseUnix")) {
                    parser = new EnterpriseUnixFTPEntryParser();
                } else if (this.mstrListingMode.equals("NT")) {
                    parser = new NTFTPEntryParser();
                } else if (this.mstrListingMode.equals("OS2")) {
                    parser = new OS2FTPEntryParser();
                } else if (this.mstrListingMode.equals("OS400")) {
                    parser = new OS400FTPEntryParser();
                } else if (this.mstrListingMode.equals("VMS")) {
                    parser = new VMSFTPEntryParser();
                } else if (this.mstrListingMode.equals("VMSVersioning")) {
                    parser = new VMSVersioningFTPEntryParser();
                } else {
                    parser = new UnixFTPEntryParser();
                }

                if (this.mstrRemoteStyle != null && this.mstrRemoteStyle.length() > 0 && !this.mstrRemoteStyle.equals("Directly")) {
                    this.mstrScanDir = this.mstrRemoteDir + this.mstrProcessDate + "/";
                } else {
                    this.mstrScanDir = this.mstrRemoteDir;
                }

                this.mstrStorageDir = this.mstrLocalDir;
                if (this.mstrLocalStyle.equals("Daily")) {
                    this.mstrStorageDir = this.mstrStorageDir + StringUtil.format(new Date(), "yyyyMMdd") + "/";
                } else if (this.mstrLocalStyle.equals("Monthly")) {
                    this.mstrStorageDir = this.mstrStorageDir + StringUtil.format(new Date(), "yyyyMM") + "/";
                } else if (this.mstrLocalStyle.equals("Yearly")) {
                    this.mstrStorageDir = this.mstrStorageDir + StringUtil.format(new Date(), "yyyy") + "/";
                }

                FileUtil.forceFolderExist(this.mstrStorageDir);
                if (this.mstrBackupDir.length() > 0 && !this.mstrBackupStyle.equals("Delete file")) {
                    this.mstrDirectBackupDir = this.mstrBackupDir;
                    if (this.mstrBackupStyle.equals("Daily")) {
                        this.mstrDirectBackupDir = this.mstrDirectBackupDir + StringUtil.format(new Date(), "yyyyMMdd") + "/";
                    } else if (this.mstrBackupStyle.equals("Monthly")) {
                        this.mstrDirectBackupDir = this.mstrDirectBackupDir + StringUtil.format(new Date(), "yyyyMM") + "/";
                    } else if (this.mstrBackupStyle.equals("Yearly")) {
                        this.mstrDirectBackupDir = this.mstrDirectBackupDir + StringUtil.format(new Date(), "yyyy") + "/";
                    }

                    this.mftpMain.forceChangeWorkingDirectory(this.mstrDirectBackupDir);
                }

                this.mprtDirectoryList = new Hashtable();
                this.mvtFileList = new Vector();
                this.listFile("", (FTPFileEntryParser)parser);
            }

            this.afterListFile();
        } finally {
            this.mbListing = false;
        }

    }

    protected void listFile(String strAdditionPath, FTPFileEntryParser parser) throws Exception {
        this.mftpMain.changeWorkingDirectory(this.mstrScanDir + strAdditionPath);
        FileEntry[] fflFileList = this.mftpMain.listFiles(parser);
        if (fflFileList != null) {
            for(int iFileIndex = 0; iFileIndex < fflFileList.length; ++iFileIndex) {
                if (fflFileList[iFileIndex].isDirectory()) {
                    if (this.mbRecursive) {
                        String strAdditionChildPath = strAdditionPath + fflFileList[iFileIndex].filename + "/";
                        FileUtil.forceFolderExist(this.mstrStorageDir + strAdditionChildPath);
                        if (this.mstrBackupDir.length() > 0 && !this.mstrBackupStyle.equals("Delete file")) {
                            this.mftpMain.forceChangeWorkingDirectory(this.mstrDirectBackupDir + strAdditionChildPath);
                        }

                        this.listFile(strAdditionChildPath, parser);
                    }
                } else {
                    FileEntry ffl = this.createListItem(fflFileList[iFileIndex]);
                    if (ffl != null) {
                        this.mvtFileList.addElement(ffl);
                        this.mprtDirectoryList.put(ffl, strAdditionPath);
                    }
                }
            }
        }

    }

    protected void afterListFile() throws Exception {
        Collections.sort(this.mvtFileList, new Comparator() {
            public int compare(Object obj1, Object obj2) {
                return ((FileEntry)obj1).filename.compareTo(((FileEntry)obj2).filename);
            }
        });
    }

    protected void beforeListFile() throws Exception {
    }

    protected String validateFile(FileEntry ffl) throws Exception {
        if (this.mstrSQLValidateCommand != null && this.mstrSQLValidateCommand.length() > 0) {
            Connection cn = null;

            String var7;
            try {
                if (this.mcnMain != null) {
                    cn = this.mcnMain;
                } else {
                    cn = this.mmgrMain.getConnection();
                }

                String strSQL = this.mstrSQLValidateCommand;
                strSQL = StringUtil.replaceAll(strSQL, "$ThreadID", this.getThreadID());
                strSQL = StringUtil.replaceAll(strSQL, "$FileName", ffl.filename);
                Statement stmt = cn.createStatement();
                ResultSet rs = stmt.executeQuery(strSQL);
                String strValidationResult = null;
                if (rs.next()) {
                    strValidationResult = rs.getString(1);
                }

                rs.close();
                stmt.close();
                var7 = strValidationResult;
            } finally {
                if (cn != null && cn != this.mcnMain) {
                    Database.closeObject(cn);
                }

            }

            return var7;
        } else {
            return "";
        }
    }

    protected void beforeGetFile(FileEntry ffl) throws Exception {
        this.logMonitor("Start getting file " + ffl.filename + " from ftp server");
    }

    protected void afterGetFile(FileEntry ffl) throws Exception {
        this.logMonitor("Getting file " + ffl.filename + " completed");
    }

    protected void errGetFile(FileEntry ffl, String strErrorDescription) {
    }

    protected void getFile(FileEntry ffl) throws Exception {
        try {
            this.beforeGetFile(ffl);
            String strAdditionPath = StringUtil.nvl(this.mprtDirectoryList.get(ffl), "");
            String strRemoteFilePath = this.mstrScanDir + strAdditionPath + ffl.filename;
            FileOutputStream os = null;

            try {
                os = new FileOutputStream(this.mstrTempDir + ffl.getName());
                this.mftpMain.retrieveFile(strRemoteFilePath, os);
            } finally {
                FileUtil.safeClose(os);
            }

            File fl = new File(this.mstrTempDir + ffl.getName());
            if (!fl.exists()) {
                throw new Exception("Download file failed, file does not exist");
            } else if (fl.length() != ffl.getSize()) {
                throw new Exception("Getted file size does not equals to ftp file size");
            } else {
                String strGettedFilePath;
                if (this.mstrBackupStyle.equals("Delete file")) {
                    this.mftpMain.deleteFile(this.mstrScanDir + strAdditionPath + ffl.getName());
                } else if (this.mstrBackupDir.length() > 0) {
                    strGettedFilePath = this.mstrDirectBackupDir + strAdditionPath + this.formatBackupFileName(ffl.getName());
                    try {
                        this.mftpMain.deleteFile(strGettedFilePath);
                    }catch (Exception e){

                    }
                    this.mftpMain.rename(this.mstrScanDir + strAdditionPath + ffl.getName(), strGettedFilePath);
                }

                strGettedFilePath = FileUtil.backup(this.mstrTempDir, this.mstrLocalDir, ffl.getName(), this.formatLocalFileName(ffl.getName()), this.mstrLocalStyle, strAdditionPath);

                try {
                    this.afterGetFile(ffl);
                } catch (Exception var11) {
                    FileUtil.deleteFile(strGettedFilePath);
                    throw var11;
                }
            }
        } catch (Exception var13) {
            this.errGetFile(ffl, var13.getMessage());
            throw var13;
        }
    }

    public String formatBackupFileName(String strFileName) throws Exception {
        return FileUtil.formatFileName(strFileName, this.mstrBackupFileFormat);
    }

    public String formatLocalFileName(String strFileName) throws Exception {
        return FileUtil.formatFileName(strFileName, this.mstrLocalFileFormat);
    }
}
