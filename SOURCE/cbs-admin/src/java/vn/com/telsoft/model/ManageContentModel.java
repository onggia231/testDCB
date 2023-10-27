package vn.com.telsoft.model;

import com.faplib.lib.SystemLogger;
import com.faplib.lib.TelsoftException;
import com.faplib.lib.admin.data.AMDataPreprocessor;
import com.faplib.lib.util.SQLUtil;
import org.apache.commons.dbutils.DbUtils;
import org.primefaces.model.SortOrder;
import vn.com.telsoft.entity.Content;
import vn.com.telsoft.entity.ContentRecognize;
import vn.com.telsoft.util.ConnUtil;
import vn.com.telsoft.util.ForceSyncUtil;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class ManageContentModel extends AMDataPreprocessor implements Serializable {

    private static final long serialVersionUID = -2768926694990811125L;

    public List<Content> getAllContent() throws Exception {
        List<Content> listValue = new ArrayList<>();
        try {
            open();
            String strSQL = "SELECT ct.content_id, ct.content_description, ct.status FROM cb_content ct";
            mStmt = mConnection.prepareStatement(strSQL);
            mRs = mStmt.executeQuery();
            while (mRs.next()) {
                Content content = new Content();
                content.setContentId(mRs.getInt("CONTENT_ID"));
                content.setContentDescription(mRs.getString("CONTENT_DESCRIPTION"));
                content.setStatus(mRs.getString("STATUS"));
                listValue.add(content);
            }
        } catch (Exception ex) {
            SystemLogger.getLogger().error(ex);
            throw ex;
        } finally {
            close(mRs);
            close(mStmt);
            close();
        }
        return listValue;
    }


    public List<ContentRecognize> getRecognizeByContendId(Integer contentId) throws Exception {
        List<ContentRecognize> listValue = new ArrayList<>();
        try {
            open();
            String strSQL = " SELECT  a.content_recognize_id,a.content_id, a.matching_type, a.keyword, a.case_sensitive FROM cb_content_recognize a where 1 = 1 and a.content_id = ?";
            mStmt = mConnection.prepareStatement(strSQL);
            mStmt.setInt(1, contentId);
            mRs = mStmt.executeQuery();
            while (mRs.next()) {
                ContentRecognize ett = new ContentRecognize();
                ett.setContentRecognizeId(mRs.getInt("CONTENT_RECOGNIZE_ID"));
                ett.setContentId(mRs.getInt("CONTENT_ID"));
                ett.setMatchingType(mRs.getString("MATCHING_TYPE"));
                ett.setKeyWord(mRs.getString("KEYWORD"));
                ett.setCaseSensitive(mRs.getInt("CASE_SENSITIVE"));
                listValue.add(ett);
            }
        } catch (Exception ex) {
            SystemLogger.getLogger().error(ex);
            throw ex;
        } finally {
            close(mRs);
            close(mStmt);
            close();
        }
        return listValue;
    }

    public String getSQLLazy() throws Exception {
        String strSQL = "";
        strSQL = "SELECT ct.content_id, ct.content_description, ct.status FROM cb_content ct where 1 = 1 order by content_description";
        return strSQL;
    }

    public int deleteContent(Content content) throws Exception {
        PreparedStatement stmt = null;
        PreparedStatement stmtItem = null;
        int content_id = content.getContentId();
        try {
            open();
            mConnection.setAutoCommit(false);

            logBeforeDelete("cb_content", "CONTENT_ID=" + content.getContentId());
            String sqlDeleteContent = "DELETE cb_content where content_id = ?";
            mStmt = mConnection.prepareStatement(sqlDeleteContent);
            mStmt.setInt(1, content_id);
            mStmt.executeUpdate();

            logBeforeDelete("cb_content_recognize", "CONTENT_ID=" + content.getContentId());
            String sqlDeleleRecognize = "DELETE cb_content_recognize where content_id = ?";
            stmt = mConnection.prepareStatement(sqlDeleleRecognize);
            stmt.setInt(1, content_id);
            stmt.executeUpdate();

            logBeforeDelete("cb_item_content", "CONTENT_ID=" + content.getContentId());
            String sqlDeleteItem = "DELETE cb_item_content where content_id =?";
            stmtItem = mConnection.prepareStatement(sqlDeleteItem);
            stmtItem.setInt(1, content_id);
            stmtItem.executeUpdate();
            mConnection.commit();
            ForceSyncUtil.updateSystemApParam(mConnection);
            return content_id;
        } catch (Exception ex) {
            ConnUtil.rollback(mConnection);
            SystemLogger.getLogger().error(ex);
            throw ex;
        } finally {
            mConnection.setAutoCommit(true);
            close(mStmt);
            close(stmt);
            close(stmtItem);
            close();
        }
    }

    public List insert(Content content, List<ContentRecognize> listContentRecognize) throws Exception {
        PreparedStatement stmtContent = null;
        PreparedStatement stmtRecognize = null;
        List listInsert = new ArrayList();
        String sqlContent = "INSERT INTO cb_content(content_id, content_description, status) VALUES(?,?,?)";
        String sqlRecognize = "INSERT INTO cb_content_recognize(content_recognize_id, content_id, matching_type, keyword, case_sensitive) VALUES(?,?,?,?,?)";
        try {
            open();
            mConnection.setAutoCommit(false);
            Long content_id = SQLUtil.getSequenceValue(mConnection, "SEQ_CB_CONTENT");
            stmtContent = mConnection.prepareStatement(sqlContent);
            int i = 1;
            stmtContent.setInt(i++, content_id.intValue());
            stmtContent.setString(i++, content.getContentDescription());
            stmtContent.setString(i++, content.getStatus());
            stmtContent.executeUpdate();
            content.setContentId(content_id.intValue());
            logAfterInsert("cb_content", "content_id=" + content.getContentId());
            listInsert.add(content);

            for (ContentRecognize contentRecognize : listContentRecognize) {
                Long recognize_id = SQLUtil.getSequenceValue(mConnection, "SEQ_CB_CONTENT_RECOGNIZE");
                stmtRecognize = mConnection.prepareStatement(sqlRecognize);
                int j = 1;
                stmtRecognize.setInt(j++, recognize_id.intValue());
                stmtRecognize.setInt(j++, content_id.intValue());
                stmtRecognize.setString(j++, contentRecognize.getMatchingType());
                stmtRecognize.setString(j++, contentRecognize.getKeyWord());
                stmtRecognize.setInt(j++, contentRecognize.getCaseSensitive());
                stmtRecognize.executeUpdate();
                contentRecognize.setContentRecognizeId(recognize_id.intValue());
                logAfterInsert("cb_content_recognize", "content_recognize_id=" + contentRecognize.getContentRecognizeId());
            }
            listInsert.add(listContentRecognize);
            mConnection.commit();
            ForceSyncUtil.updateSystemApParam(mConnection);
        } catch (Exception ex) {
            ConnUtil.rollback(mConnection);
            SystemLogger.getLogger().error(ex);
            throw ex;
        } finally {
            mConnection.setAutoCommit(true);
            close(stmtContent);
            close(stmtRecognize);
            close();
        }
        return listInsert;
    }

    public Content update(Content content, List<ContentRecognize> lsInsert, List<ContentRecognize> lsUpdate, List<ContentRecognize> lsDelete) throws Exception {
        PreparedStatement stmtContent = null;
        String sqlContent = "update cb_content set content_description=?, status=? where content_id=?";

        PreparedStatement stmtInsertRecog = null;
        String sqlInsertRecog = "INSERT INTO cb_content_recognize(content_recognize_id, content_id, matching_type, keyword, case_sensitive) " +
                " VALUES(?,?,?,?,?)";

        PreparedStatement stmtUpdateRecog = null;
        String sqlUpdateRecog = "UPDATE cb_content_recognize set matching_type=?, keyword=?, case_sensitive=? " +
                " where content_recognize_id=?";

        PreparedStatement stmtDeleteRecog = null;

        try {
            open();
            mConnection.setAutoCommit(false);
            List listChange = logBeforeUpdate("cb_content", "content_id=" + content.getContentId());
            stmtContent = mConnection.prepareStatement(sqlContent);
            int i = 1;
            stmtContent.setString(i++, content.getContentDescription());
            stmtContent.setString(i++, content.getStatus());
            stmtContent.setInt(i++, content.getContentId());
            stmtContent.executeUpdate();

            if (lsUpdate != null && lsUpdate.size() > 0) {
                stmtUpdateRecog = mConnection.prepareStatement(sqlUpdateRecog);
                for (ContentRecognize contentRecognize : lsUpdate) {
                    List listChangeUpdate = logBeforeUpdate("cb_content_recognize", "content_recognize_id=" + contentRecognize.getContentRecognizeId());
                    int k = 1;
                    stmtUpdateRecog.setString(k++, contentRecognize.getMatchingType());
                    stmtUpdateRecog.setString(k++, contentRecognize.getKeyWord());
                    stmtUpdateRecog.setInt(k++, contentRecognize.getCaseSensitive());
                    stmtUpdateRecog.setInt(k++, contentRecognize.getContentRecognizeId());
                    stmtUpdateRecog.addBatch();
                    logAfterUpdate(listChangeUpdate);
                }
                stmtUpdateRecog.executeBatch();
                stmtUpdateRecog.clearBatch();
            }

            if (lsDelete != null && lsDelete.size() > 0) {
                String sqlDelete = "DELETE FROM CB_CONTENT_RECOGNIZE WHERE CONTENT_RECOGNIZE_ID = ? ";
                stmtDeleteRecog = mConnection.prepareStatement(sqlDelete);
                int iCount = 0;
                for (ContentRecognize recogDelete : lsDelete) {
                    if (recogDelete.getContentRecognizeId() != null) {
                        logBeforeDelete("CB_CONTENT_RECOGNIZE", "CONTENT_RECOGNIZE_ID=" + recogDelete.getContentRecognizeId());
                        stmtDeleteRecog.setInt(1, recogDelete.getContentRecognizeId());
                    } else {
                        stmtDeleteRecog.setNull(1, java.sql.Types.NULL);
                    }
                    stmtDeleteRecog.addBatch();
                    iCount++;
                    if (iCount % 500 == 0) {
                        stmtDeleteRecog.executeBatch();
                        stmtDeleteRecog.clearBatch();
                    }
                }
                stmtDeleteRecog.executeBatch();
                stmtDeleteRecog.clearBatch();
            }

            if (lsInsert != null && lsInsert.size() > 0) {
                if (!validateInput(lsInsert, content.getContentId())) {
                    throw new TelsoftException("ERROR_UNIQUE_CONTENT");
                }
                stmtInsertRecog = mConnection.prepareStatement(sqlInsertRecog);
                for (ContentRecognize recogInsert : lsInsert) {
                    Long recognize_id = SQLUtil.getSequenceValue(mConnection, "SEQ_CB_CONTENT_RECOGNIZE");
                    int j = 1;
                    stmtInsertRecog.setInt(j++, recognize_id.intValue());
                    stmtInsertRecog.setInt(j++, content.getContentId());
                    stmtInsertRecog.setString(j++, recogInsert.getMatchingType());
                    stmtInsertRecog.setString(j++, recogInsert.getKeyWord());
                    stmtInsertRecog.setInt(j++, recogInsert.getCaseSensitive());
                    stmtInsertRecog.addBatch();
                    logAfterInsert("cb_content_recognize", "content_recognize_id=" + recognize_id);
                }
                stmtInsertRecog.executeBatch();
                stmtInsertRecog.clearBatch();
            }

            logAfterUpdate(listChange);
            mConnection.commit();
            ForceSyncUtil.updateSystemApParam(mConnection);
        } catch (Exception ex) {
            ConnUtil.rollback(mConnection);
            SystemLogger.getLogger().error(ex);
            throw ex;
        } finally {
            mConnection.setAutoCommit(true);
            close(stmtContent);
            close(stmtDeleteRecog);
            close(stmtInsertRecog);
            close(stmtUpdateRecog);
            close();
        }
        return content;
    }

    private boolean validateInput(List<ContentRecognize> listValue, int content_id) throws Exception {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String strSQLCheck = "SELECT 1 FROM cb_content_recognize a where a.content_id =? and a.matching_type=? and a.keyword=? ";
            stmt = mConnection.prepareStatement(strSQLCheck);
            for (ContentRecognize recog : listValue) {
                stmt.setInt(1, content_id);
                stmt.setString(2, recog.getMatchingType());
                stmt.setString(3, recog.getKeyWord());
                rs = stmt.executeQuery();
                if (rs.next()) {
                    return false;
                }
            }
        } catch (Exception ex) {
            SystemLogger.getLogger().error(ex);
            throw ex;
        } finally {
            close(rs);
            close(stmt);
        }
        return true;
    }

    public boolean checkData(String keyword, String matchingType, Integer contentId) throws Exception {
        try {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            String sql = "select * from cb_content_recognize where keyword = ? and matching_type= ? and content_id = ?";
            open();
            stmt = mConnection.prepareStatement(sql);
            stmt.setString(1, keyword);
            stmt.setString(2, matchingType);
            if (contentId != null) {
                stmt.setInt(3, contentId);
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (Exception ex) {
            SystemLogger.getLogger().error(ex);
            throw ex;
        } finally {
            close();
        }
        return false;
    }
}
