package com.telsoft.cbs.modules.selfcare.subsriberSync;

import com.telsoft.cbs.modules.selfcare.entities.SubscriberEntitty;
import com.telsoft.database.Database;
import com.telsoft.thread.DBManageableThread;
import com.telsoft.thread.ParameterType;
import com.telsoft.thread.ThreadConstant;
import com.telsoft.util.AppException;
import com.telsoft.util.FileUtil;
import com.telsoft.util.WildcardFileFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.telsoft.cbs.modules.selfcare.ulti.VasgateUlti.convertPhone;

@Slf4j
public class AccountChangeImportThread extends DBManageableThread {

    @Getter
    @Setter
    private String strDateFormat;

    @Getter
    @Setter
    private String storeChangeAccountAttr;

    @Getter
    @Setter
    private String strBackupFilePath;

    @Getter
    @Setter
    private String strErrorFilePath;

    @Getter
    @Setter
    private String strWildcard;

    @Getter
    @Setter
    private String strInputFilePath;
    @Getter
    @Setter
    private String strFileType;

    @Getter
    @Setter
    private String strUrl;

    @Getter
    @Setter
    private String strXForward;

    PreparedStatement pstmInsertTmp = null;
    int countInsert;

    @Override
    public Vector getParameterDefinition() {

        Vector vtReturn = new Vector();
        vtReturn.addAll(super.getParameterDefinition());
        vtReturn.addElement(createParameter("Date format", "", ParameterType.PARAM_TEXTBOX_MAX, ""));
        vtReturn.addElement(createParameter("Send Notif Account Change Attribute", "", ParameterType.PARAM_TEXTBOX_MAX, ""));
        vtReturn.addElement(createParameter("File Directory", "", ParameterType.PARAM_TEXTBOX_MAX, ""));
        vtReturn.addElement(createParameter("Back Up File Path", "", ParameterType.PARAM_TEXTBOX_MAX, ""));
        vtReturn.addElement(createParameter("Error File Path", "", ParameterType.PARAM_TEXTBOX_MAX, ""));
        vtReturn.addElement(createParameter("Wildcard", "", ParameterType.PARAM_TEXTBOX_MAX, ""));
        return vtReturn;
    }


    public void fillParameter() throws AppException {
        super.fillParameter();
        // Fill parameter
        strDateFormat = loadString("Date format");
        storeChangeAccountAttr = loadString("Send Notif Account Change Attribute");
        strInputFilePath = loadString("File Directory");
        strBackupFilePath = loadString("Back Up File Path");
        strErrorFilePath = loadString("Error File Path");
        strWildcard = loadString("Wildcard");

    }

    private File[] sortFileByDateModified(File[] listFile) {
        try {
            Arrays.sort(listFile, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
                }
            });
        } catch (Exception e) {
            logMonitor("Sort file error at " + e.getMessage());
        }
        return listFile;
    }


    @Override
    protected void processSession() throws Exception {
        File folder = new File(strInputFilePath);
        File[] listFile = folder.listFiles(new WildcardFileFilter(strWildcard, false));
        if (listFile != null) {
            listFile = this.sortFileByDateModified(listFile);
            File[] arrFile = listFile;

            for (int i = 0; i < listFile.length; ++i) {
                truncateTmp();
                File f = arrFile[i];
                if (f.length() == 0) {
                    log.info("File " + f.getName() + " is empty. This file will be deleted");
                    logMonitor("File " + f.getName() + " is empty. This file will be deleted");
                    f.delete();
                } else {
                    if (miThreadCommand == ThreadConstant.THREAD_STOP) {
                        break;
                    }

                    try {
                        log.info("Process file " + f.getName());
                        logMonitor("Process file " + f.getName());
                        importFile(f);
                        logMonitor("Import Success");
                    } catch (Exception e) {
                        logMonitor("[ERROR]Error when processing file " + f.getName());
                        log.error("[ERROR]Error when processing file " + f.getName(), e);
                    }
                }
            }
        }
    }

    private void importFile(File f) throws Exception {
        BufferedReader br = null;
        boolean fileError = false;

        try {
            br = new BufferedReader(new FileReader(f.getAbsolutePath()));
            logMonitor("Start sync list");
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                if (!line.trim().equals("")) {
                    try {
                        InsertTmp(convertPhone(line));
                    } catch (Exception e) {
                        fileError = true;
                        logMonitor("Error at line " + line);
                        continue;
                    }
                }
            }

            pstmInsertTmp.executeBatch();
            pstmInsertTmp.clearBatch();
            logMonitor("Processed " + countInsert + " records in file " + f.getName());
            InsertSub();
//            BuildIndex();
        } catch (Exception e) {
            fileError = true;
            log.error("[ERROR]Import error at " + f.getName(), e);
            logMonitor("[ERROR]Import error at " + f.getName());
            throw e;
        } finally {
            br.close();
            if (fileError) {
                moveFile(f, strErrorFilePath);
            } else
                moveFile(f, strBackupFilePath);
        }

    }

    private void InsertTmp(String isdn) throws Exception {
        pstmInsertTmp.setString(1, isdn);
        pstmInsertTmp.addBatch();
//        countInsert++;
        if (++countInsert % 1000 == 0) {
            pstmInsertTmp.executeBatch();
            pstmInsertTmp.clearBatch();
        }
    }

    private void truncateTmp() throws SQLException {
        String sql = "truncate table tmp_account_change";
        try (PreparedStatement pstm = mcnMain.prepareStatement(sql)) {
            pstm.execute();
        }
    }

    private void importRaw(List<SubscriberEntitty> listRaw) throws Exception {
        String sql = "insert into cb_import_account_change(id,isdn,store_id) values (cb_import_account_change_seq.nextval,?,?)";
        String sql2 = "update cb_subscriber set status = 0 where isdn=? and status=1";
        String sql3 = "select css.store_id from cb_sub_store css join cb_subscriber cs on css.sub_id = cs.id " +
                "join cb_store_attr csa on css.store_id = csa.store_id and csa.name = ? and csa.value = ? " +
                "where cs.isdn = ? and cs.status=1";
        try {
            mcnMain.setAutoCommit(false);
//            try (PreparedStatement pstmt = mcnMain.prepareStatement(sql);
//                 PreparedStatement pstmt2 = mcnMain.prepareStatement(sql2)) {
            try (PreparedStatement pstmt = mcnMain.prepareStatement(sql)) {
                int countBatch = 0;
                for (SubscriberEntitty sub : listRaw) {
                    try (PreparedStatement pstm3 = mcnMain.prepareStatement(sql3)) {
                        pstm3.setString(1, storeChangeAccountAttr);
                        pstm3.setString(2, "ENABLE");
                        pstm3.setString(3, sub.getIsdn());
                        try (ResultSet rs = pstm3.executeQuery()) {
                            while (rs.next()) {
                                pstmt.setString(1, sub.getIsdn());
                                pstmt.setLong(2, rs.getLong("store_id"));
                                pstmt.addBatch();
                            }
                        }
                    }

//                    pstmt2.setString(1, sub.getIsdn());
//                    pstmt2.addBatch();
                    if (++countBatch == 1000) {
                        pstmt.executeBatch();
//                        pstmt2.executeBatch();
                        countBatch = 0;
                    }
                }
                pstmt.executeBatch();
//                pstmt2.executeBatch();
            }
        } catch (Exception e) {
            mcnMain.rollback();
            logMonitor("[ERROR]Error import raw at " + e.getMessage());
            log.error("[ERROR]Error import raw at " + e.getMessage(), e);
        } finally {
            mcnMain.commit();
            mcnMain.setAutoCommit(true);
        }
    }

    private List<SubscriberEntitty> readFile(File f) throws Exception {
        List<SubscriberEntitty> list = new ArrayList();
        BufferedReader br = null;
        boolean fileError = false;

        try {
            br = new BufferedReader(new FileReader(f.getAbsolutePath()));
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                if (!line.trim().equals("")) {
                    SubscriberEntitty sub = new SubscriberEntitty();
                    sub.setIsdn(line.trim());
                    list.add(sub);
                }
            }
        } catch (Exception e) {
            fileError = true;
            log.error("[ERROR]Reading error at " + f.getName(), e);
            logMonitor("[ERROR]Reading error at " + f.getName());
            throw e;
        } finally {
            br.close();
            if (fileError) {
                moveFile(f, strErrorFilePath);
            } else
                moveFile(f, strBackupFilePath);
        }

        return list;
    }

    private void moveFile(File f, String path) {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();
        String dateName = dateFormat.format(date);
        if (path.endsWith("/")) {
            path = removeLastChar(path);
        }
        try {
            String backUpSubFolder = path + "/" + dateName + "/";
            FileUtil.forceFolderExist(backUpSubFolder);
            if (FileUtil.renameFile(this.strInputFilePath + f.getName(), backUpSubFolder + f.getName()))
                f.delete();
            else {
                logMonitor("Can not back up file " + path + " : " + f.getAbsolutePath());
            }
        } catch (Exception e) {
            logMonitor("[ERROR]Error moving file to " + path + " : " + f.getAbsolutePath());
            log.error("[ERROR]Error delete file to " + path + " : " + f.getAbsolutePath(), e);
        }
    }

    public static String removeLastChar(String strInput) {
        if (strInput == null || strInput.isEmpty()) {
            return "";
        }
        return strInput.substring(0, strInput.length() - 1);
    }

    protected void beforeSession() throws Exception {
        try {
            openConnection();
            String sql = "insert into tmp_account_change(isdn) values (?)";

            pstmInsertTmp = mcnMain.prepareCall(sql);
            countInsert=0;
        } catch (Exception ex) {
            log.error("[ERROR]Error open connection", ex);
            logMonitor("[ERROR]Error open connection");
            throw ex;
        }
    }

    protected void afterSession() throws Exception {
        Database.closeObject(pstmInsertTmp);
        closeConnection();
    }

    private void InsertSub() throws SQLException {
        String sql = "insert into cb_import_account_change(id,isdn,store_id)\n" +
                "(select cb_import_account_change_seq.nextval, cs.isdn,css.store_id \n" +
                "from tmp_account_change tmp,cb_subscriber cs,cb_sub_store css, cb_store_attr csa \n" +
                "where cs.ISDN = tmp.ISDN and css.sub_id = cs.id and cs.status = 1 " +
                "and css.store_id = csa.store_id and csa.name = 'api_account_change_notif' and csa.value = 'ENABLE')";
        try (PreparedStatement pstm = mcnMain.prepareStatement(sql)) {
            int rs = pstm.executeUpdate();
            logMonitor("Inserted " + rs + " subscribers");
        }
    }


}