package com.telsoft.cbs.modules.selfcare.subsriberSync;

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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Vector;

import static com.telsoft.cbs.modules.selfcare.ulti.VasgateUlti.convertPhone;

/**
 * <p>Title: Core Gateway System</p>
 *
 * <p>Description: A part of TELSOFT Gateway System</p>
 *
 * <p>Copyright: Copyright (c) 2009</p>
 *
 * <p>Company: TELSOFT</p>
 *
 * @author Nguyen Viet Ha
 * @version 1.0
 */

@Slf4j
public class CbsVasgateBlackListSyncThread extends DBManageableThread {

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
    private String strListCode;

    @Getter
    @Setter
    private String strStoreCode;

    @Getter
    @Setter
    private Long storeId;

    @Getter
    @Setter
    private long listId;

    @Getter
    @Setter
    private String rebuildIndex;

    PreparedStatement pstmInsertList = null;
    int countInsert;

    @Override
    public Vector getParameterDefinition() {

        Vector vtReturn = new Vector();
        vtReturn.addAll(super.getParameterDefinition());
        vtReturn.addElement(createParameter("List code", "", ParameterType.PARAM_TEXTBOX_MAX, ""));
        vtReturn.addElement(createParameter("Store code", "", ParameterType.PARAM_TEXTBOX_MAX, ""));
        vtReturn.addElement(createParameter("File Directory", "", ParameterType.PARAM_TEXTBOX_MAX, ""));
        vtReturn.addElement(createParameter("Back Up File Path", "", ParameterType.PARAM_TEXTBOX_MAX, ""));
        vtReturn.addElement(createParameter("Error File Path", "", ParameterType.PARAM_TEXTBOX_MAX, ""));

        Vector vtValue = new Vector();
        vtValue.addElement("Y");
        vtValue.addElement("N");
//        vtValue.addElement("3");
        vtReturn.addElement(createParameter("Re-build index after sync", "", ParameterType.PARAM_COMBOBOX, vtValue, ""));
        vtReturn.addElement(createParameter("Wildcard", "", ParameterType.PARAM_TEXTBOX_MAX, ""));
        return vtReturn;
    }


    public void fillParameter() throws AppException {
        super.fillParameter();
        strListCode = loadString("List code");
        try {
            strStoreCode = loadString("Store code");
        } catch (Exception e) {
            strStoreCode = "";
        }
        strInputFilePath = loadString("File Directory");
        strBackupFilePath = loadString("Back Up File Path");
        strErrorFilePath = loadString("Error File Path");
        strWildcard = loadString("Wildcard");
        rebuildIndex = loadString("Re-build index after sync");
    }

    private File[] sortFileByDateModified(File[] listFile) {
        try {
            Arrays.sort(listFile, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
                }
            });
        } catch (Exception e) {
            logMonitor("[ERROR]Sort file error at " + e.getMessage());
            log.error("[ERROR]Sort file error at " + e.getMessage(), e);
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
                {
                    if (miThreadCommand == ThreadConstant.THREAD_STOP) {
                        break;
                    }
                    try {
                        log.info("Process file " + f.getName());
                        logMonitor("Process file " + f.getName());
                        importFile(f);
                        logMonitor("Sync Success");
                    } catch (Exception e) {
                        logMonitor("[ERROR]Error when processing file " + f.getName());
                        log.error("[ERROR]Error when processing file " + f.getName(), e);
                    }
                }
            }
//            }
        }
    }

    private void InsertTmp(String isdn) throws Exception {
        pstmInsertList.setString(1, isdn);
        pstmInsertList.addBatch();
//        countInsert++;
        if (++countInsert % 1000 == 0) {
            pstmInsertList.executeBatch();
            pstmInsertList.clearBatch();
        }
    }

    private void DeleteSub() throws SQLException {
        if (InsertLogDelete() > 0) {
            String sql = "delete from cb_item_isdn a where not exists (select 1 from tmp_black_list_2 b where b.isdn = a.isdn) and list_id=?";
            try (PreparedStatement pstm = mcnMain.prepareStatement(sql)) {
                pstm.setLong(1, listId);
                int rs = pstm.executeUpdate();
                logMonitor("Deleted " + rs + " subscribers");
            }
        }
    }

    private int InsertLogDelete() throws SQLException {
        String sql = "insert into cb_log_vasgate_blacklist(issue_time,isdn,action) \n" +
                "(select sysdate,isdn,? from cb_item_isdn a where not exists (select 1 from tmp_black_list_2 b where b.isdn = a.isdn) and list_id=?)";
        try (PreparedStatement pstm = mcnMain.prepareStatement(sql)) {
            pstm.setString(1, "D");
            pstm.setLong(2, listId);
            int rs = pstm.executeUpdate();
            logMonitor("Insert " + rs + " rows(delete action) into log table");
            return rs;
        }
    }

    private int InsertLogInsert() throws SQLException {
        String sql = "insert into cb_log_vasgate_blacklist(issue_time,isdn,action) \n" +
                "(select distinct sysdate,isdn,? from tmp_black_list_2 b " +
                "where not exists (select 1 from cb_item_isdn c where c.isdn = b.isdn and c.list_id=?))";
        try (PreparedStatement pstm = mcnMain.prepareStatement(sql)) {
            pstm.setString(1, "I");
            pstm.setLong(2, listId);
            int rs = pstm.executeUpdate();
            logMonitor("Insert " + rs + " rows(insert action) into log table");
            return rs;
        }
    }

    private void InsertSub() throws SQLException {
        if (InsertLogInsert() > 0) {
            String sql = "insert into cb_item_isdn(isdn,reason,issue_time,list_id,store_id) " +
                    "(select distinct isdn,'SYNC_VASGATE',sysdate,?,? from tmp_black_list_2 b " +
                    "where not exists (select 1 from cb_item_isdn c where c.isdn = b.isdn and c.list_id=?))";
            try (PreparedStatement pstm = mcnMain.prepareStatement(sql)) {
                pstm.setLong(1, listId);
                pstm.setObject(2, storeId);
                pstm.setLong(3, listId);
                int rs = pstm.executeUpdate();
                logMonitor("Inserted " + rs + " subscribers");
            }
        }
    }

    private void BuildIndex() throws SQLException {
        if (rebuildIndex.equalsIgnoreCase("Y")) {
            String sql = "ALTER INDEX CB_ITEM_ISDN_UK1 REBUILD ONLINE";
            try (PreparedStatement pstm = mcnMain.prepareStatement(sql)) {
                pstm.executeUpdate();
            }
        }
        try (PreparedStatement pstm = mcnMain.prepareStatement("call FORCE_SYNC()")) {
            pstm.execute();

        }
        ;
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

            pstmInsertList.executeBatch();
            pstmInsertList.clearBatch();
            logMonitor("Processed " + countInsert + " records in file " + f.getName());
            DeleteSub();
            InsertSub();
            BuildIndex();
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
            super.beforeSession();
            String sql = "";
            storeId = null;

            if (strStoreCode != "")
                try (PreparedStatement pstm = mcnMain.prepareStatement("select id from cb_store where store_code =?")) {
                    pstm.setString(1, strStoreCode);
                    try (ResultSet rs = pstm.executeQuery()) {
                        if (rs.next()) {
                            storeId = rs.getLong("id");
                        } else {
                            logMonitor("Can not find code " + strStoreCode + " in database");
                            miThreadCommand = ThreadConstant.THREAD_STOP;
                        }
                    }
                }

            try (PreparedStatement pstm = mcnMain.prepareStatement("select list_id from cb_list where list_name=?")) {
                pstm.setString(1, strListCode);
                try (ResultSet rs = pstm.executeQuery()) {
                    if (rs.next()) {
                        listId = rs.getLong("list_id");
                    } else {
                        logMonitor("Can not find code " + strListCode + " in database");
                        miThreadCommand = ThreadConstant.THREAD_STOP;
                    }
                }
            }

            countInsert = 0;
            sql = "insert into tmp_black_list_2(isdn) values (?)";

            pstmInsertList = mcnMain.prepareCall(sql);

        } catch (Exception ex) {
            log.error("[ERROR]Error open connection", ex);
            logMonitor("[ERROR]Error open connection");
            throw ex;
        }
    }

    private void truncateTmp() throws SQLException {
        String sql = "truncate table tmp_black_list_2";
        try (PreparedStatement pstm = mcnMain.prepareStatement(sql)) {
            pstm.execute();
        }
    }





    protected void afterSession() throws Exception {
        Database.closeObject(pstmInsertList);
        closeConnection();
    }


}
