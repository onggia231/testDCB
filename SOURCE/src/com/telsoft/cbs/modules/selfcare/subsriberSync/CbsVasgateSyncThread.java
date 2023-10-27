package com.telsoft.cbs.modules.selfcare.subsriberSync;

import com.telsoft.cbs.module.cbsrest.domain.CBCommand;
import com.telsoft.cbs.module.cbsrest.domain.RestRequest;
import com.telsoft.cbs.module.cbsrest.domain.RestResponse;
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

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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
 * @author Nguyen Cong Khanh
 * @version 1.0
 */

@Slf4j
public class CbsVasgateSyncThread extends DBManageableThread {

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

    PreparedStatement pstmVasgate = null;
    PreparedStatement pstmVasgate2 = null;
    PreparedStatement pstmVasgate3 = null;
    PreparedStatement pstmInsertLog = null;
    PreparedStatement pstmGetSubscriberInfo = null;
    PreparedStatement pstmGetPostpaidLimit = null;
    PreparedStatement pstmGetPrepaidLimit = null;
    PreparedStatement pstmResetSubStore = null;
    PreparedStatement pstmAccountChange = null;

    @Override
    public Vector getParameterDefinition() {

        Vector vtReturn = new Vector();
        vtReturn.addAll(super.getParameterDefinition());
        Vector vtValue = new Vector();
        vtValue.addElement("Prepaid");
        vtValue.addElement("Postpaid");
//        vtValue.addElement("3");
        vtReturn.addElement(createParameter("File type", "", ParameterType.PARAM_COMBOBOX, vtValue, ""));
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
        strFileType = loadString("File type");
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
//                            importRawFTP(f, stmtInsert);
                        List<SubscriberEntitty> listRaw = this.readFile(f);
                        if (listRaw != null) {
                            if (mcnMain.isClosed() || mcnMain == null) {
                                openConnection();
                            }

                            importRaw(listRaw, f);
                        }
                    } catch (Exception e) {
                        logMonitor("[ERROR]Error when processing file " + f.getName());
                        log.error("[ERROR]Error when processing file " + f.getName(), e);
                    }
                }
            }
//            }
        }
    }


    private void importRaw(List<SubscriberEntitty> listRaw, File f) throws Exception {
        try {
            int countSuc = 0;
            HashMap<String, Integer> hmLastVasgate = new HashMap<>();
            mcnMain.setAutoCommit(false);
            int countBatch = 0;
            for (SubscriberEntitty sub : listRaw) {
                countBatch++;
                SubscriberEntitty oldSub = getSubscriberInfo(sub.getIsdn());
                if (oldSub == null) {
                    if (isDebug()) {
                        logMonitor("ISDN " + sub.getIsdn() + " does not exist");
                    }
                    log.info("ISDN " + sub.getIsdn() + " does not exist");
                } else {
                    if (hmLastVasgate.get(oldSub.getIsdn()) == null) {
                        hmLastVasgate.put(oldSub.getIsdn(), oldSub.getVasgateStatus());
                    }
                    if (strFileType.equalsIgnoreCase("Postpaid")) {
                        int vasgateStt = sub.getVasgateStatus();
                        if (vasgateStt < 5) {
                            pstmVasgate.setInt(1, vasgateStt);
                            pstmVasgate.setString(2, sub.getIsdn());
                            pstmVasgate.addBatch();

                        } else if (vasgateStt == 5) {
                            updateCHTD(sub,oldSub);

                        } else if (vasgateStt < 8) {
                            pstmVasgate3.setInt(1, vasgateStt);
                            if (vasgateStt == 6) {
                                pstmVasgate3.setString(2, "PREPAID");
                                pstmGetPrepaidLimit.setLong(1, oldSub.getId());
                                ResultSet rsSubStore = null;
                                HashMap<Long, Long> hmSubStore = new HashMap<>();
                                try {
                                    rsSubStore = pstmGetPrepaidLimit.executeQuery();
                                    while (rsSubStore.next()) {
                                        hmSubStore.put(rsSubStore.getLong("store_id"), rsSubStore.getLong("LIMIT_PROFILE_ID"));
                                    }
                                    for (Map.Entry<Long, Long> map : hmSubStore.entrySet()) {
                                        pstmResetSubStore.setLong(1, map.getKey());
                                        pstmResetSubStore.setLong(2, map.getValue());
                                        pstmResetSubStore.setLong(3, oldSub.getId());
                                        pstmResetSubStore.addBatch();
                                    }
                                } catch (Exception e) {
                                    logMonitor("[ERROR]Error get limit_profile_id of " + sub.getIsdn());
                                    log.error("[ERROR]Error get limit_profile_id of " + sub.getIsdn(), e);
                                } finally {
                                    Database.closeObject(rsSubStore);
                                }
                            } else {
                                pstmVasgate3.setString(2, "POSTPAID");
                                pstmGetPostpaidLimit.setLong(1, oldSub.getId());
                                ResultSet rsSubStore = null;
                                HashMap<Long, Long> hmSubStore = new HashMap<>();
                                try {
                                    rsSubStore = pstmGetPostpaidLimit.executeQuery();
                                    while (rsSubStore.next()) {
                                        hmSubStore.put(rsSubStore.getLong("store_id"), rsSubStore.getLong("LIMIT_PROFILE_ID"));
                                    }
                                    for (Map.Entry<Long, Long> map : hmSubStore.entrySet()) {
                                        pstmResetSubStore.setLong(1, map.getValue());
                                        pstmResetSubStore.setLong(2, map.getKey());
                                        pstmResetSubStore.setLong(3, oldSub.getId());
                                        pstmResetSubStore.addBatch();
                                    }
                                } catch (Exception e) {
                                    logMonitor("[ERROR]Error get limit_profile_id of " + sub.getIsdn());
                                    log.error("[ERROR]Error get limit_profile_id of " + sub.getIsdn(), e);
                                } finally {
                                    Database.closeObject(rsSubStore);
                                }
                            }
                            pstmVasgate3.setString(3, sub.getIsdn());
                            pstmVasgate3.addBatch();
                        }

                    } else if (strFileType.equalsIgnoreCase("Prepaid")) {
                        updateCHTD(sub,oldSub);
                    }

                    pstmInsertLog.setString(1, sub.getIsdn());
                    pstmInsertLog.setInt(2, hmLastVasgate.get(sub.getIsdn()));
                    pstmInsertLog.setInt(3, sub.getVasgateStatus());
                    pstmInsertLog.addBatch();
                    hmLastVasgate.put(sub.getIsdn(), sub.getVasgateStatus());
                    countSuc++;

                }
                if (countBatch % 1000 == 0) {
                    executeBatchPstm();
                    clearBatchPstm();
                }
            }
            executeBatchPstm();
            clearBatchPstm();
            logMonitor("Process success " + countSuc + " records");
            moveFile(f, strBackupFilePath);
        } catch (Exception e) {
            logMonitor("Error sync vasgate status, rollbacking");
            try {
                moveFile(f, strErrorFilePath);
            } catch (Exception ex) {

            }
            mcnMain.rollback();
            e.printStackTrace();
        } finally {
            mcnMain.commit();
            mcnMain.setAutoCommit(true);

        }
    }

    private void updateCHTD(SubscriberEntitty sub,SubscriberEntitty oldSub) throws SQLException {
        pstmVasgate2.setString(1, sub.getIsdn());
        pstmVasgate2.addBatch();
        String sql3 = "select css.store_id from cb_sub_store css join cb_subscriber cs on css.sub_id = cs.id " +
                "join cb_store_attr csa on css.store_id = csa.store_id and csa.name = ? and csa.value = ? " +
                "where cs.id = ? ";
        try (PreparedStatement pstm3 = mcnMain.prepareStatement(sql3)) {
            pstm3.setString(1, storeChangeAccountAttr);
            pstm3.setString(2, "ENABLE");
            pstm3.setLong(3, oldSub.getId());
            try (ResultSet rs = pstm3.executeQuery()) {
                while (rs.next()) {
                    pstmAccountChange.setString(1, sub.getIsdn());
                    pstmAccountChange.setLong(2, rs.getLong("store_id"));
                    pstmAccountChange.addBatch();
                }
            }
        }
    }


    private void executeBatchPstm() throws SQLException {
        pstmVasgate.executeBatch();
        pstmVasgate2.executeBatch();
        pstmVasgate3.executeBatch();
        pstmInsertLog.executeBatch();
        pstmResetSubStore.executeBatch();
        pstmAccountChange.executeBatch();
    }


    private void clearBatchPstm() throws SQLException {
        pstmVasgate.clearBatch();
        pstmVasgate2.clearBatch();
        pstmVasgate3.clearBatch();
        pstmInsertLog.clearBatch();
        pstmResetSubStore.clearBatch();
        pstmAccountChange.clearBatch();
    }

    private SubscriberEntitty getSubscriberInfo(String isdn) {
        SubscriberEntitty sub = null;
        ResultSet rs = null;
        try {
            pstmGetSubscriberInfo.setString(1, isdn);
            rs = pstmGetSubscriberInfo.executeQuery();
            if (rs.next()) {
                sub = new SubscriberEntitty();
                sub.setId(rs.getLong("id"));
                sub.setIsdn(rs.getString("isdn"));
                sub.setStatus(rs.getInt("status"));
                sub.setVasgateStatus(rs.getInt("vasgate_status"));
            }
        } catch (Exception e) {
            logMonitor("[ERROR]Error getSubscriberInfo " + isdn + " : " + e);
            log.error("[ERROR]Error getSubscriberInfo " + isdn + " : " + e, e);
        } finally {
            return sub;
        }
    }

    private List<SubscriberEntitty> readFile(File f) throws Exception {
        List<SubscriberEntitty> list = new ArrayList();
        BufferedReader br = null;
        boolean fileError = false;

        try {
            br = new BufferedReader(new FileReader(f.getAbsolutePath()));
//            fileWriter = new FileWriter(fileName);
//            bw = new BufferedWriter(fileWriter);
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                if (!line.trim().equals("")) {
                    if (strFileType.equalsIgnoreCase("Prepaid")) {
                        if (line.startsWith("MSISDN"))
                            continue;
                    }
                    String[] arrRaw = line.split(",");
                    for (String raw :
                            arrRaw) {
                        if (raw == null || raw.isEmpty()) {
                            logMonitor("[ERROR]Error data at line: " + line);
                            fileError = true;
                            continue;
                        }
                    }
                    int index = 0;
                    SubscriberEntitty sub = new SubscriberEntitty();
                    sub.setIsdn(convertPhone(arrRaw[0]));
                    try {
                        sub.setVasgateStatus(Integer.valueOf(arrRaw[1]));
                    } catch (Exception e) {
                        fileError = true;
                        logMonitor("[ERROR]Error parse vasgate status at: " + line);
                        continue;
                    }
                    if (strFileType.equalsIgnoreCase("Postpaid")) {
                        Date date = null;
                        try {
                            date = formatDate(arrRaw[2]);
                            sub.setProcessTime(date);
                        } catch (Exception e) {
                            logMonitor("[ERROR]Error parse date at line: " + line);
                            fileError = true;
                            continue;
                        }

                    }
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
            }
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
            if (FileUtil.renameFile(this.strInputFilePath + f.getName(), backUpSubFolder + f.getName())) {
                f.delete();
                logMonitor("Moved file to " + path + " : " + f.getAbsolutePath());
            } else {
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
            sql = "update cb_subscriber set vasgate_status=? where isdn=? and status=1 ";
            pstmVasgate = mcnMain.prepareStatement(sql);
            sql = "update cb_subscriber set vasgate_status=5, status=0 where isdn=?  and status=1";
            pstmVasgate2 = mcnMain.prepareStatement(sql);
            sql = "update cb_subscriber set vasgate_status=?, sub_type=? where isdn=?  and status=1";
            pstmVasgate3 = mcnMain.prepareStatement(sql);
            sql = "select id,isdn,status,vasgate_status from cb_subscriber where isdn=? and status=1";
            pstmGetSubscriberInfo = mcnMain.prepareStatement(sql);
            sql = "insert into cb_change_status(issue_id,issue_time,isdn,old_status,new_status) values (cb_change_status_seq.nextval,sysdate,?,?,?)";
            pstmInsertLog = mcnMain.prepareStatement(sql);
            sql = "select store_id,(select c.POSTPAID_LIMIT_PROFILE from cb_store c where ss.STORE_ID = c.id) LIMIT_PROFILE_ID  \n" +
                    "from cb_sub_store ss join cb_subscriber s on s.id = ss.sub_id\n" +
                    "where s.id=?";
            pstmGetPostpaidLimit = mcnMain.prepareStatement(sql);
            sql = "select store_id,(select c.PREPAID_LIMIT_PROFILE from cb_store c where ss.STORE_ID = c.id) LIMIT_PROFILE_ID  \n" +
                    "from cb_sub_store ss join cb_subscriber s on s.id = ss.sub_id\n" +
                    "where s.id=?";
            pstmGetPrepaidLimit = mcnMain.prepareStatement(sql);
            sql = "update cb_sub_store set yearly_limit = null,\n" +
                    "monthly_limit = null, weekly_limit = null,\n" +
                    "daily_limit = null, limit_profile_id = ?\n" +
                    "where store_id=? and sub_id=?";
            pstmResetSubStore = mcnMain.prepareStatement(sql);
            sql = "insert into cb_import_account_change(id,isdn,store_id) values (cb_import_account_change_seq.nextval,?,?)";
            pstmAccountChange = mcnMain.prepareStatement(sql);
        } catch (Exception ex) {
            log.error("[ERROR]Error open connection", ex);
            logMonitor("[ERROR]Error open connection");
            throw ex;
        }
    }

    protected void afterSession() throws Exception {
        Database.closeObject(pstmVasgate);
        Database.closeObject(pstmVasgate2);
        Database.closeObject(pstmVasgate3);
        Database.closeObject(pstmInsertLog);
        Database.closeObject(pstmGetSubscriberInfo);
        Database.closeObject(pstmGetPostpaidLimit);
        Database.closeObject(pstmGetPrepaidLimit);
        Database.closeObject(pstmResetSubStore);
        closeConnection();
    }

    private Date formatDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
        try {
            return sdf.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }


    private boolean sendRequestChangeAccount(String transactionId, String msisdn, String url, String xForward) {
        RestRequest request = new RestRequest();

        request.setName(CBCommand.ACCOUNT_CHANGE);
        request.setTransaction_id(transactionId);
        request.setIsdn(msisdn);

        Client client = ClientBuilder.newClient();
        try {
            Invocation.Builder invocationBuilder;
//            WebTarget target = client.target("http://10.11.10.143:8005/fortumoclient/fortumoclient");
            WebTarget target = client.target(url);
            invocationBuilder = target.request(MediaType.APPLICATION_XML);
            if (xForward != null) {
                invocationBuilder.header("X-Forward-For", xForward);
            }

            Response response;
            if ("POST".equalsIgnoreCase("POST") && request != null) {
                response = invocationBuilder.method("POST", Entity.entity(request, MediaType.APPLICATION_XML));
            } else {
                response = invocationBuilder.method("POST");
            }

            if (response.getStatus() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
            }

            RestResponse restResponse = response.readEntity(RestResponse.class);
            if (restResponse.getCode() == 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
