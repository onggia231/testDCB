package com.telsoft.cbs.cdr;

import com.telsoft.cbs.domain.Folder;
import com.telsoft.cbs.domain.FolderStructure;
import com.telsoft.database.Database;
import com.telsoft.thread.ManageableThread;
import com.telsoft.thread.ParameterType;
import com.telsoft.thread.ThreadConstant;
import com.telsoft.util.*;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * <p>Title: TELSOFT Gateway Extensions</p>
 * <p/>
 * <p>Description: TELSOFT Gateway Extensions</p>
 * <p/>
 * <p>Company: TELSOFT</p>
 *
 * @author Truong Quang Minh
 * @version 1.0
 */
public class MergeCDR extends ManageableThread {

    private final Folder inputFolder = new Folder();
    private final Folder backupFolder = new Folder();
    private final Folder rejectFolder = new Folder();
    private final Folder outputFolder = new Folder();
    private final Folder tempFolder = new Folder();

    private String processDate;
    private String wildcard;
    private String dateFormat;
    private String outputFileFormat;

    private String mstrSeqFormat;
    private String mstrSeqName;
    protected Connection mcnMain = null;
    private PreparedStatement stmt_SELECT = null;
    private String strSQL = "SELECT $seq_name.nextval FROM DUAL";
    private int iMaxFileSize;
    FileOutputStream outputStream = null;
    File tempOutputFile = null;
    private boolean bFirst;

    public Vector getParameterDefinition() {
        Vector vtReturn = new Vector();
        vtReturn.add(createParameter("SeqName", "", ParameterType.PARAM_TEXTBOX_MAX, "256", ""));
        vtReturn.add(createParameter("SeqFormat", "", 2, "100", "", ""));
        vtReturn.add(createParameter("MaxFileSize", "", 1, "999999999", "Write max file size (in bytes)", "", "Storage"));

        vtReturn.add(createParameter("InputDirName", "", ParameterType.PARAM_TEXTBOX_MAX, "256", ""));
        vtReturn.add(createParameter("InputDirStructure", "", ParameterType.PARAM_COMBOBOX, FolderStructure.class, ""));
        vtReturn.add(createParameter("ProcessDate", "", ParameterType.PARAM_TEXTBOX_MAX, "256", ""));
        vtReturn.add(createParameter("Wildcard", "", ParameterType.PARAM_TEXTBOX_MAX, "256", ""));


        vtReturn.add(createParameter("OutputDirName", "", ParameterType.PARAM_TEXTBOX_MAX, "256", ""));
        vtReturn.add(createParameter("OutputDirStructure", "", ParameterType.PARAM_COMBOBOX, FolderStructure.class, ""));
        vtReturn.add(createParameter("OutputFileFormat", "", ParameterType.PARAM_TEXTBOX_MAX, "256", "Format output file name.\n" +
                "Format file name.\n" +
                " Can use $Time, $Sequence as parameter"));

        vtReturn.add(createParameter("BackupDirName", "", ParameterType.PARAM_TEXTBOX_MAX, "256", ""));
        vtReturn.add(createParameter("BackupDirStructure", "", ParameterType.PARAM_COMBOBOX, FolderStructure.class, ""));

        vtReturn.add(createParameter("RejectedDirName", "", ParameterType.PARAM_TEXTBOX_MAX, "256", ""));
        vtReturn.add(createParameter("RejectedDirStructure", "", ParameterType.PARAM_COMBOBOX, FolderStructure.class, ""));

        vtReturn.add(createParameter("TempDirName", "", ParameterType.PARAM_TEXTBOX_MAX, "256", ""));

        vtReturn.add(createParameter("DateFormat", "", ParameterType.PARAM_TEXTBOX_MAX, "256", ""));

        vtReturn.addAll(super.getParameterDefinition());
        return vtReturn;
    }

    public void fillParameter() throws AppException {
        this.mstrSeqName = loadString("SeqName");
        this.mstrSeqFormat = this.loadMandatory("SeqFormat");

        this.iMaxFileSize = this.loadUnsignedInteger("MaxFileSize");

        inputFolder.setName(loadDirectory("InputDirName", true, true));
        inputFolder.setStructure(FolderStructure.valueOf(loadString("InputDirStructure")));
        processDate = loadString("ProcessDate");
        wildcard = loadString("Wildcard");

        outputFolder.setName(loadDirectory("OutputDirName", true, true));
        outputFolder.setStructure(FolderStructure.valueOf(loadString("OutputDirStructure")));
        outputFileFormat = loadString("OutputFileFormat");

        backupFolder.setName(loadDirectory("BackupDirName", true, true));
        backupFolder.setStructure(FolderStructure.valueOf(loadString("BackupDirStructure")));


        rejectFolder.setName(loadDirectory("RejectedDirName", true, true));
        rejectFolder.setStructure(FolderStructure.valueOf(loadString("RejectedDirStructure")));


        tempFolder.setName(loadDirectory("TempDirName", true, true));

        dateFormat = loadString("DateFormat");
        super.fillParameter();
    }

    public void validateParameter() throws Exception {
        super.validateParameter();
    }

    protected List<File> listFile() {
        String mstrScanDir;

        if (inputFolder.getStructure() != null && inputFolder.getStructure() != FolderStructure.Directly) {
            mstrScanDir = inputFolder.getName() + this.processDate + "/";
        } else {
            mstrScanDir = inputFolder.getName();
        }

        File fl = new File(mstrScanDir);
        File[] strFileList = fl.listFiles(new WildcardFilter(this.wildcard));
        if (strFileList != null && strFileList.length > 0) {
            Arrays.sort(strFileList);
            return Arrays.asList(strFileList);
        }
        return new ArrayList<File>();
    }


    // //////////////////////////////////////////////////////
    public void beforeSession() throws Exception {
        super.beforeSession();
        openConnection();
        fillLogFile();
        try {
            strSQL = StringUtil.replaceAll(strSQL, "$seq_name", mstrSeqName);
            stmt_SELECT = mcnMain.prepareStatement(strSQL);
        } catch (Exception ex) {
            logMonitor(ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    // //////////////////////////////////////////////////////
    public void afterSession() throws Exception {
        try {
            Database.closeObject(stmt_SELECT);
        } catch (Exception ex) {
            logMonitor(ex.getLocalizedMessage());
            throw ex;
        } finally {
            super.afterSession();
            closeConnection();
        }
    }

    @Override
    protected void processSession() throws Exception {
        try {
            logMonitor("Start process CDR file");
            List<File> fileList = listFile();
            if (fileList != null && fileList.size() > 0) {
                logMonitor("Loading file list.....");
                bFirst = true;
                for (File file : fileList) {
                    if (this.miThreadCommand == ThreadConstant.THREAD_STOPPED) {
                        break;
                    }
                    if (file.isFile()) {
                        logMonitor("Starting to process file " + file.getName());
                        process(file);
                        logMonitor("File " + file.getName() + " is processed");
                    }
                }
            } else if (this.miThreadCommand != ThreadConstant.THREAD_STOPPED) {
                this.changeProcessDate();
            }
        } finally {
            closeFile();
            logMonitor("End process CDR");
        }
    }

    /////////////////////////////////////////////////////////////
    private void closeFile() throws Exception {
        if(outputStream != null) {
            outputStream.close();
            outputStream = null;
            logMonitor("File " + tempOutputFile.getName() + " has been exported");
            FileUtil.backup(tempFolder.getName(), outputFolder.getName(), tempOutputFile.getName(), tempOutputFile.getName(), outputFolder.getStructure().name());
        }
    }

    private void openFile() throws Exception {
        tempOutputFile = new File(setRoot() + setFileName());
        outputStream = new FileOutputStream(tempOutputFile);
    }

    ////////////////////////////////////////////////////////////
    private void writeData(File file) throws Exception {
        Scanner myReader = new Scanner(file);

        while(myReader.hasNext()) {
            String str = myReader.nextLine();
            if(!str.isEmpty()) {
                if(bFirst){
                    openFile();
                    bFirst = false;
                }
                str += "\n";
                long strSize = str.length();
                long fileSize = outputStream.getChannel().size();
                if (fileSize + strSize >= iMaxFileSize) {
                    closeFile();
                    openFile();
                }
                outputStream.write(str.getBytes());
            }
        }
        myReader.close();
        outputStream.flush();
    }

    private String setRoot() throws Exception {
        return tempFolder.getName();
    }


    protected void process(File file) throws Exception {
        File tempInputFile = new File(this.tempFolder.getName() + file.getName());
        try {
            FileUtil.copyFile(file.getAbsolutePath(), this.tempFolder.getName() + file.getName());

            if (this.miThreadCommand == ThreadConstant.THREAD_STOPPED) {
                throw new AppException("The processing is interrupted by administrator");
            }
            writeData(tempInputFile);
            FileUtil.deleteFile(tempInputFile.getAbsolutePath());
            FileUtil.backup(file.getParent()+"/", backupFolder.getName(), file.getName(), file.getName(), backupFolder.getStructure().name());
        } catch (Exception var14) {
            FileUtil.deleteFile(tempInputFile.getAbsolutePath());
            FileUtil.backup(file.getParent()+"/", this.rejectFolder.getName(), file.getName(), file.getName(), this.rejectFolder.getStructure().name());
        }
    }

    ////////////////////////////////////////////////////////////
    private String setFileName() throws Exception {
        ResultSet rs = null;
        try {
            rs = stmt_SELECT.executeQuery();
            long lId = 0;
            if (rs.next()) {
                lId = rs.getLong(1);
            }
            String fileName = outputFileFormat;
            fileName = StringUtil.replaceAll(fileName, "$Time", StringUtil.format(new Date(), this.dateFormat));
            fileName = StringUtil.replaceAll(fileName, "$Sequence", StringUtil.format(lId, mstrSeqFormat));
            return fileName;
        } catch (Exception ex2) {
            ex2.printStackTrace();
            logMonitor("Error occurs: " + ex2.getMessage());
            throw ex2;
        }
    }



    protected void changeProcessDate() throws Exception {
        if (inputFolder.getStructure() != null && inputFolder.getStructure() != FolderStructure.Directly) {
            Date dt = DateUtil.toDate(this.processDate, this.dateFormat);

            do {
                switch (inputFolder.getStructure()) {
                    case Daily:
                        dt = DateUtil.addDay(dt, 1);
                        break;
                    case Monthly:
                        dt = DateUtil.addMonth(dt, 1);
                        break;
                    case Yearly:
                        dt = DateUtil.addYear(dt, 1);
                        break;
                }

                String strNextProcessDate = StringUtil.format(dt, this.dateFormat);
                File fl = new File(this.inputFolder.getName() + strNextProcessDate + "/");
                if (fl.exists() && fl.isDirectory()) {
                    this.processDate = strNextProcessDate;
                    this.setParameter("ProcessDate", this.processDate);
                    this.storeConfig();
                    return;
                }
            } while (dt.getTime() < System.currentTimeMillis());
        }
    }
    //////////////////////////////////////////////////////////

    protected void openConnection() throws Exception {
        mcnMain = this.getManager().getConnection();
    }

    // //////////////////////////////////////////////////////
    protected void closeConnection() {
        Database.closeObject(mcnMain);
    }

}
