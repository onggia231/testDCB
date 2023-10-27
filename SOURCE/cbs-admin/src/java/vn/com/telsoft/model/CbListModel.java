/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.com.telsoft.model;

import com.faplib.lib.SystemLogger;
import com.faplib.lib.admin.data.AMDataPreprocessor;
import com.faplib.lib.admin.gui.entity.AccessRight;
import com.faplib.lib.admin.gui.entity.AppGUI;
import com.faplib.lib.admin.gui.entity.ModuleGUI;
import com.faplib.lib.config.Config;
import com.faplib.lib.util.SQLUtil;
import com.faplib.util.StringUtil;
import com.faplib.ws.client.ClientRequestProcessor;
import vn.com.telsoft.entity.CBItemContent;
import vn.com.telsoft.entity.CBList;
import vn.com.telsoft.util.ConnUtil;
import vn.com.telsoft.util.ForceSyncUtil;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * @author HoangNH
 */
public class CbListModel extends AMDataPreprocessor implements Serializable {

    private static final long serialVersionUID = -5092433566421471893L;

    public void delete(List<CBList> list) throws Exception {
        try {
            open();
            mConnection.setAutoCommit(false);
            String strSQL = "DELETE FROM CB_LIST WHERE LIST_ID=?";

            //Delete app
            for (CBList cbList : list) {
                logBeforeDelete("CB_LIST", "LIST_ID=" + cbList.getListId());
                mStmt = mConnection.prepareStatement(strSQL);
                mStmt.setLong(1, cbList.getListId());
                mStmt.execute();
            }
            //Commit
            mConnection.commit();
            ForceSyncUtil.updateSystemApParam(mConnection);
        } catch (Exception ex) {
            ConnUtil.rollback(mConnection);
            SystemLogger.getLogger().error(ex);
            throw ex;
        } finally {
            mConnection.setAutoCommit(true);
            close(mStmt);
            close();
        }
    }

    public long add(CBList list) throws Exception {
        long idFromSequence;

        try {
            open();
            mConnection.setAutoCommit(false);

            //Update app
            idFromSequence = SQLUtil.getSequenceValue(mConnection, "SEQ_CB_LIST");
            list.setListId(idFromSequence);

            String strSQL = "INSERT INTO CB_LIST(LIST_ID, LIST_NAME, ACCESS_TYPE, DESCRIPTION, STATUS, LIST_TYPE, CACHED) VALUES(?,?,?,?,?,?,?)";
            mStmt = mConnection.prepareStatement(strSQL);
            mStmt.setLong(1, idFromSequence);
            mStmt.setString(2, list.getListName().trim());
            mStmt.setInt(3, list.getAccessType());
            mStmt.setString(4, list.getDescription().trim());
            mStmt.setInt(5, list.getStatus());
            mStmt.setInt(6, list.getListType());
            mStmt.setInt(7, list.getCached());
            mStmt.execute();
            logAfterInsert("CB_LIST", "LIST_ID=" + idFromSequence);
            //Done
            mConnection.commit();
            ForceSyncUtil.updateSystemApParam(mConnection);
        } catch (Exception ex) {
            ConnUtil.rollback(mConnection);
            SystemLogger.getLogger().error(ex);
            throw ex;

        } finally {
            mConnection.setAutoCommit(true);
            close(mStmt);
            close();
        }

        return idFromSequence;
    }

    public void edit(CBList list) throws Exception {

        try {
            open();
            mConnection.setAutoCommit(false);

            //Update app
            List listChange = logBeforeUpdate("CB_LIST", "LIST_ID=" + list.getListId());
            String strSQL = "UPDATE CB_LIST SET LIST_NAME=?, ACCESS_TYPE=?, DESCRIPTION=?, STATUS=?, LIST_TYPE=?, CACHED=? WHERE LIST_ID=?";
            mStmt = mConnection.prepareStatement(strSQL);
            mStmt.setString(1, list.getListName().trim());
            mStmt.setInt(2, list.getAccessType());
            mStmt.setString(3, list.getDescription().trim());
            mStmt.setInt(4, list.getStatus());
            mStmt.setInt(5, list.getListType());
            mStmt.setInt(6, list.getCached());
            mStmt.setLong(7, list.getListId());
            mStmt.execute();
            logAfterUpdate(listChange);
            close(mStmt);

            //Commit
            mConnection.commit();
            ForceSyncUtil.updateSystemApParam(mConnection);
        } catch (Exception ex) {
            ConnUtil.rollback(mConnection);
            SystemLogger.getLogger().error(ex);
            throw ex;
        } finally {
            mConnection.setAutoCommit(true);
            close(mStmt);
            close();
        }
    }

    public List<CBList> getListAll() throws Exception {
        List<CBList> listReturn = new ArrayList<>();

        try {
            open();
            String strSQL = "SELECT LIST_ID, LIST_NAME, ACCESS_TYPE, DESCRIPTION, STATUS, LIST_TYPE, CACHED FROM CB_LIST ORDER BY LIST_NAME";
            mStmt = mConnection.prepareStatement(strSQL);
            mRs = mStmt.executeQuery();

            while (mRs.next()) {
                CBList tmpCbList = new CBList();
                tmpCbList.setListId(mRs.getLong(1));
                tmpCbList.setListName(mRs.getString(2));
                tmpCbList.setAccessType(mRs.getInt(3));
                tmpCbList.setDescription(mRs.getString(4));
                tmpCbList.setStatus(mRs.getInt(5));
                tmpCbList.setListType(mRs.getInt(6));
                tmpCbList.setCached(mRs.getInt(7));
                listReturn.add(tmpCbList);
            }

        } catch (Exception ex) {
            SystemLogger.getLogger().error(ex);
            throw ex;

        } finally {
            close(mRs);
            close(mStmt);
            close();
        }

        return listReturn;
    }

    public List<CBItemContent> getListItemContent(Long listId) throws Exception {
        List<CBItemContent> listReturn = new ArrayList<>();

        try {
            open();
            String strSQL = "SELECT CONTENT_ID, REASON, ISSUE_TIME, LIST_ID, STORE_ID FROM CB_ITEM_CONTENT where 1 = 1 and list_id = ?";
            mStmt = mConnection.prepareStatement(strSQL);
            mStmt.setLong(1, listId);
            mRs = mStmt.executeQuery();

            while (mRs.next()) {
                CBItemContent tmpCbContent = new CBItemContent();
                tmpCbContent.setContentId(mRs.getInt(1));
                tmpCbContent.setReason(mRs.getString(2));
                tmpCbContent.setIssueTime(mRs.getTimestamp(3));
                tmpCbContent.setListId(mRs.getLong(4));
                tmpCbContent.setStoreId(mRs.getLong(5));
                listReturn.add(tmpCbContent);
            }
        } catch (Exception ex) {
            SystemLogger.getLogger().error(ex);
            throw ex;

        } finally {
            close(mRs);
            close(mStmt);
            close();
        }

        return listReturn;
    }

    public List<CBList> getCbList(CBList cbList) throws Exception {
        List<CBList> listReturn = new ArrayList<>();
        List<Object> parameter = new ArrayList<>();
        try {
            open();
            String strSQL = " SELECT LIST_ID, LIST_NAME, ACCESS_TYPE, DESCRIPTION, STATUS, LIST_TYPE, CACHED FROM CB_LIST where 1=1 ";

            if (cbList != null) {
                if (cbList.getListId() != null) {
                    strSQL += " and LIST_ID=? ";
                    parameter.add(cbList.getListId());
                }
                if (cbList.getListName() != null && !cbList.getListName().equals("")) {
                    strSQL += " and lower(LIST_NAME) like ? ";
                    parameter.add("%" + cbList.getListName().toLowerCase() + "%");
                }
                if (cbList.getAccessType() != null) {
                    strSQL += " and ACCESS_TYPE=? ";
                    parameter.add(cbList.getAccessType());
                }
                if (cbList.getDescription() != null && !cbList.getDescription().equals("")) {
                    strSQL += " and lower(DESCRIPTION) like ? ";
                    parameter.add("%" + cbList.getDescription().toLowerCase() + "%");
                }
                if (cbList.getStatus() != null) {
                    strSQL += " and STATUS=? ";
                    parameter.add(cbList.getStatus());
                }
                if (cbList.getListType() != null) {
                    strSQL += " and LIST_TYPE=? ";
                    parameter.add(cbList.getListType());
                }
                if (cbList.getCached() != null) {
                    strSQL += " and CACHED=? ";
                    parameter.add(cbList.getCached());
                }
            }
            strSQL += " order by LIST_NAME ";
            mStmt = mConnection.prepareStatement(strSQL);
            int i = 0;
            for (Object obj : parameter) {
                mStmt.setObject(++i, obj);
            }
            mRs = mStmt.executeQuery();

            while (mRs.next()) {
                CBList tmpCbList = new CBList();
                tmpCbList.setListId(mRs.getLong(1));
                tmpCbList.setListName(mRs.getString(2));
                tmpCbList.setAccessType(mRs.getInt(3));
                tmpCbList.setDescription(mRs.getString(4));
                tmpCbList.setStatus(mRs.getInt(5));
                tmpCbList.setListType(mRs.getInt(6));
                tmpCbList.setCached(mRs.getInt(7));
                listReturn.add(tmpCbList);
            }

        } catch (Exception ex) {
            SystemLogger.getLogger().error(ex);
            throw ex;

        } finally {
            close(mRs);
            close(mStmt);
            close();
        }
        return listReturn;
    }

    public void deleteCBItemISDN(Long listID) throws Exception {
        try {
            open();
            mConnection.setAutoCommit(false);
            String strSQL = "DELETE FROM CB_ITEM_ISDN WHERE LIST_ID=?";

            logBeforeDelete("CB_ITEM_ISDN", "LIST_ID=" + listID);
            mStmt = mConnection.prepareStatement(strSQL);
            mStmt.setLong(1, listID);
            mStmt.execute();

            mConnection.commit();
            ForceSyncUtil.updateSystemApParam(mConnection);
        } catch (Exception ex) {
            ConnUtil.rollback(mConnection);
            SystemLogger.getLogger().error(ex);
            throw ex;
        } finally {
            mConnection.setAutoCommit(true);
            close(mStmt);
            close(mConnection);
        }
    }

    public void deleteCBItemContent(Long listId) throws Exception {

        try {
            open();
            mConnection.setAutoCommit(false);
            String strSQL = "DELETE FROM CB_ITEM_CONTENT WHERE list_Id = ? ";
            logBeforeDelete("CB_ITEM_CONTENT", "list_Id=" + listId);
            mStmt = mConnection.prepareStatement(strSQL);
            mStmt.setLong(1, listId);
            mStmt.execute();

            //Commit
            mConnection.commit();
            ForceSyncUtil.updateSystemApParam(mConnection);
        } catch (Exception ex) {
            ConnUtil.rollback(mConnection);
            SystemLogger.getLogger().error(ex);
            throw ex;
        } finally {
            mConnection.setAutoCommit(true);
            close(mStmt);
            close(mConnection);
        }
    }
}
