package vn.com.telsoft.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.faplib.applet.util.AppException;
import com.faplib.applet.util.StringUtil;
import com.faplib.lib.SystemLogger;
import com.faplib.lib.admin.data.AMDataPreprocessor;
import vn.com.telsoft.entity.ApParam;
import vn.com.telsoft.util.ConnUtil;

public class ApParamModel extends AMDataPreprocessor implements Serializable {
    private static final long serialVersionUID = 1706072146397722800L;

    public List<ApParam> getApParam() throws Exception {
        List<ApParam> ls = new ArrayList<>();
        try {
            open();
            String strSQL = "select PAR_GROUP,PAR_TYPE,PAR_NAME,PAR_VALUE,DESCRIPTION from AP_PARAM";
            mStmt = mConnection.prepareStatement(strSQL);
            mRs = mStmt.executeQuery();
            if (mRs != null) {
                while (mRs.next()) {
                    ApParam entity = new ApParam();
                    entity.setParGroup(mRs.getString("PAR_GROUP"));
                    entity.setParName(mRs.getString("PAR_NAME"));
                    entity.setParType(mRs.getString("PAR_TYPE"));
                    entity.setParValue(mRs.getString("PAR_VALUE"));
                    entity.setDescription(mRs.getString("DESCRIPTION"));
                    ls.add(entity);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            SystemLogger.getLogger().error(ex);
            throw new Exception(ex);
        } finally {
            close(mStmt);
            close(mRs);
            close();
        }
        return ls;
    }

    public void add(ApParam entity) throws Exception {
        try {
            String strSQL = "INSERT INTO AP_PARAM(PAR_GROUP,PAR_TYPE,PAR_NAME,PAR_VALUE,DESCRIPTION)" + " VALUES (?,?,?,?,?) ";
            open();
            mStmt = mConnection.prepareStatement(strSQL);
            mStmt.setString(1, StringUtil.nvl(entity.getParGroup(), "").trim());
            mStmt.setString(2, StringUtil.nvl(entity.getParType(), "").trim());
            mStmt.setString(3, StringUtil.nvl(entity.getParName(), "").trim());
            mStmt.setString(4, StringUtil.nvl(entity.getParValue(), "").trim());
            mStmt.setString(5, StringUtil.nvl(entity.getDescription(), "").trim());
            mStmt.execute();


            logAfterInsert("AP_PARAM", "PAR_NAME='" + entity.getParType()+"'");
        } catch (Exception ex) {
            ConnUtil.rollback(mConnection);
            SystemLogger.getLogger().error(ex);
            throw new AppException(ex.getMessage());
        } finally {
            mConnection.setAutoCommit(true);
            close(mRs);
            close(mStmt);
            close();
        }

    }

    public void update(ApParam entity) throws Exception {
        String strSQL = "";
        try {

            strSQL = "update AP_PARAM set PAR_TYPE=?,PAR_VALUE=?,DESCRIPTION=? " + "where PAR_NAME = ? ";
            open();
            List listChange = logBeforeUpdate("AP_PARAM", "PAR_NAME= '" + entity.getParName() + "'");
            mConnection.setAutoCommit(false);
            mStmt = mConnection.prepareStatement(strSQL);
            mStmt.setString(1, StringUtil.nvl(entity.getParType(), "").trim());

            mStmt.setString(2, StringUtil.nvl(entity.getParValue(), "").trim());
            mStmt.setString(3, StringUtil.nvl(entity.getDescription(), "").trim());
            mStmt.setString(4, StringUtil.nvl(entity.getParName(), "").trim());
            mStmt.execute();
            logAfterUpdate(listChange);
            mConnection.commit();
        } catch (Exception ex) {
            ConnUtil.rollback(mConnection);
            SystemLogger.getLogger().error(ex);
            throw new AppException(ex.getMessage());
        } finally {
            mConnection.setAutoCommit(true);
            close(mRs);
            close(mStmt);
            close();
        }
    }

    public void delete(String strID) throws Exception {
        try {
            open();
            mConnection.setAutoCommit(false);
            logBeforeDelete("AP_PARAM", "PAR_NAME='" + strID+"'");

            String strSQL = "DELETE AP_PARAM WHERE PAR_NAME=? ";
            mStmt = mConnection.prepareCall(strSQL);
            mStmt.setString(1, strID);
            mStmt.execute();
            mConnection.commit();
        } catch (Exception ex) {
            ConnUtil.rollback(mConnection);
            SystemLogger.getLogger().error(ex);
            throw new AppException(ex.getMessage());
        } finally {
            mConnection.setAutoCommit(true);
            close(mRs);
            close(mStmt);
            close();
        }
    }

    public ApParam getApParamByName(String parName) throws Exception {

        try {
            open();
            String strSQL = "select PAR_TYPE,PAR_NAME,PAR_VALUE,DESCRIPTION from AP_PARAM where PAR_NAME = ?";
            mStmt = mConnection.prepareStatement(strSQL);
            mStmt.setString(1, parName.trim());
            mRs = mStmt.executeQuery();
            if (mRs != null) {
                while (mRs.next()) {
                    ApParam entity = new ApParam();
                    entity.setParName(mRs.getString("PAR_NAME"));
                    entity.setParType(mRs.getString("PAR_TYPE"));
                    entity.setParValue(mRs.getString("PAR_VALUE"));
                    entity.setDescription(mRs.getString("DESCRIPTION"));
                    return entity;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            SystemLogger.getLogger().error(ex);
            throw new Exception(ex);
        } finally {
            close(mStmt);
            close(mRs);
            close();
        }
        return null;
    }

    public List<ApParam> getApParamByType(String parType) throws Exception {
        List<ApParam> lst = new ArrayList<>();

        try {
            open();
            String strSQL = "select PAR_TYPE,PAR_NAME,PAR_VALUE,DESCRIPTION from AP_PARAM where PAR_TYPE = ?";
            mStmt = mConnection.prepareStatement(strSQL);
            mStmt.setString(1, parType.trim());
            mRs = mStmt.executeQuery();
            if (mRs != null) {
                while (mRs.next()) {
                    ApParam entity = new ApParam();
                    entity.setParName(mRs.getString("PAR_NAME"));
                    entity.setParType(mRs.getString("PAR_TYPE"));
                    entity.setParValue(mRs.getString("PAR_VALUE"));
                    entity.setDescription(mRs.getString("DESCRIPTION"));
                    lst.add(entity);

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            SystemLogger.getLogger().error(ex);
            throw new Exception(ex);
        } finally {
            close(mStmt);
            close(mRs);
            close();
        }
        return lst;
    }


}
