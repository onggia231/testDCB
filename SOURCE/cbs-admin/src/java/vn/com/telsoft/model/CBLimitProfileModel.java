package vn.com.telsoft.model;

import com.faplib.lib.SystemLogger;
import com.faplib.lib.TelsoftException;
import com.faplib.lib.admin.data.AMDataPreprocessor;
import com.faplib.lib.util.SQLUtil;
import vn.com.telsoft.entity.CBLimitProfile;
import vn.com.telsoft.util.ConnUtil;
import vn.com.telsoft.util.ForceSyncUtil;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CBLimitProfileModel extends AMDataPreprocessor implements Serializable {

    private static final long serialVersionUID = 1237062331884664676L;

    public List<CBLimitProfile> getData() throws Exception {
        List<CBLimitProfile> listReturn = new ArrayList<>();
        try {
            open();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT a.id, a.code, a.name, a.description, a.yearly_limit, ");
            sql.append(" a.monthly_limit, a.weekly_limit, a.daily_limit, a.trans_limit");
            sql.append(" FROM cb_limit_profile a ORDER BY a.code, a.name");
            mStmt = mConnection.prepareStatement(sql.toString());
            mRs = mStmt.executeQuery();
            while (mRs.next()) {
                CBLimitProfile ett = new CBLimitProfile();
                ett.setLimitProfileId(mRs.getInt("id"));
                ett.setLimitProfileCode(mRs.getString("code"));
                ett.setLimitProfileName(mRs.getString("name"));
                ett.setLimitProfileDesc(mRs.getString("description"));
                ett.setYearlyLimit(mRs.getLong("yearly_limit"));
                ett.setMonthlyLimit(mRs.getLong("monthly_limit"));
                ett.setWeeklyLimit(mRs.getLong("weekly_limit"));
                ett.setDailyLimit(mRs.getLong("daily_limit"));
                ett.setTransLimit(mRs.getLong("trans_limit"));
                listReturn.add(ett);
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

    public void insert(CBLimitProfile limitProfile) throws Exception {
        try {
            open();
            mConnection.setAutoCommit(false);

            String strSQL = "insert into CB_LIMIT_PROFILE(id,code,name,description,yearly_limit,monthly_limit,weekly_limit,daily_limit,trans_limit) values (?,?,?,?,?,?,?,?,?)";
            mStmt = mConnection.prepareStatement(strSQL);
            Long id = SQLUtil.getSequenceValue(mConnection, "SEQ_CB_LIMIT_PROFILE");
            mStmt.setInt(1, id.intValue());
            mStmt.setString(2, limitProfile.getLimitProfileCode());
            mStmt.setString(3, limitProfile.getLimitProfileName());
            mStmt.setString(4, limitProfile.getLimitProfileDesc());
            mStmt.setLong(5, limitProfile.getYearlyLimit());
            mStmt.setLong(6, limitProfile.getMonthlyLimit());
            mStmt.setLong(7, limitProfile.getWeeklyLimit());
            mStmt.setLong(8, limitProfile.getDailyLimit());
            mStmt.setLong(9, limitProfile.getTransLimit());
            mStmt.executeUpdate();
            limitProfile.setLimitProfileId(id.intValue());
            logAfterInsert("CB_LIMIT_PROFILE", "ID=" + limitProfile.getLimitProfileId());
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

    public void update(CBLimitProfile limitProfile) throws Exception {
        try {
            open();
            mConnection.setAutoCommit(false);

            //Update cb_sub_store
            List listChange = logBeforeUpdate("CB_LIMIT_PROFILE", "id=" + limitProfile.getLimitProfileId());
            String strSQL = "UPDATE CB_LIMIT_PROFILE\n" +
                    "SET code=?, name=?, description = ?, yearly_limit=?, monthly_limit=?, weekly_limit=?, daily_limit=?, trans_limit=?\n" +
                    "WHERE id=?";
            mStmt = mConnection.prepareStatement(strSQL);
            mStmt.setString(1, limitProfile.getLimitProfileCode());
            mStmt.setString(2, limitProfile.getLimitProfileName());
            mStmt.setString(3, limitProfile.getLimitProfileDesc());
            if (limitProfile.getYearlyLimit() != null) {
                mStmt.setLong(4, limitProfile.getYearlyLimit());
            } else {
                mStmt.setNull(4, java.sql.Types.NULL);
            }
            if (limitProfile.getMonthlyLimit() != null) {
                mStmt.setLong(5, limitProfile.getMonthlyLimit());
            } else {
                mStmt.setNull(5, java.sql.Types.NULL);
            }
            if (limitProfile.getWeeklyLimit() != null) {
                mStmt.setLong(6, limitProfile.getWeeklyLimit());
            } else {
                mStmt.setNull(6, java.sql.Types.NULL);
            }
            if (limitProfile.getDailyLimit() != null) {
                mStmt.setLong(7, limitProfile.getDailyLimit());
            } else {
                mStmt.setNull(7, java.sql.Types.NULL);
            }
            if (limitProfile.getTransLimit() != null) {
                mStmt.setLong(8, limitProfile.getTransLimit());
            } else {
                mStmt.setNull(8, java.sql.Types.NULL);
            }
            mStmt.setLong(9, limitProfile.getLimitProfileId());
            mStmt.executeUpdate();
            logAfterUpdate(listChange);
            close(mStmt);
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

    public void delete(CBLimitProfile limitProfile) throws Exception {
        try {
            open();
            mConnection.setAutoCommit(false);
            if (!checkValueDelete(limitProfile)) {
                throw new TelsoftException("ERROR_EXIST_LIMIT_PROFILE");
            }

            String strSQL = "DELETE FROM CB_LIMIT_PROFILE WHERE ID=?";
            logBeforeDelete("CB_LIMIT_PROFILE", "ID=" + limitProfile.getLimitProfileId());
            mStmt = mConnection.prepareStatement(strSQL);
            mStmt.setInt(1, limitProfile.getLimitProfileId());
            mStmt.executeUpdate();
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

    private boolean checkValueDelete(CBLimitProfile limitProfile) throws Exception {
        PreparedStatement stmtCheckStore = null;
        ResultSet rsCheckStore = null;
        String checkStore = "SELECT * from cb_store a where (a.prepaid_limit_profile = ? or a.postpaid_limit_profile = ?) and rownum = 1";

        PreparedStatement stmtCheckSubStore = null;
        ResultSet rsCheckSubStore = null;
        String checkSubStore = "SELECT * from cb_sub_store a where a.limit_profile_id = ? and rownum = 1";

        try {
            stmtCheckStore = mConnection.prepareStatement(checkStore);
            stmtCheckStore.setInt(1, limitProfile.getLimitProfileId());
            stmtCheckStore.setInt(2, limitProfile.getLimitProfileId());
            rsCheckStore = stmtCheckStore.executeQuery();
            while (rsCheckStore.next()) {
                return false;
            }
            stmtCheckSubStore = mConnection.prepareStatement(checkSubStore);
            stmtCheckSubStore.setInt(1, limitProfile.getLimitProfileId());
            rsCheckSubStore = stmtCheckSubStore.executeQuery();
            while (rsCheckSubStore.next()) {
                return false;
            }

        } catch (Exception ex) {
            SystemLogger.getLogger().error(ex);
            throw ex;
        } finally {
            close(rsCheckStore);
            close(stmtCheckStore);
            close(rsCheckSubStore);
            close(stmtCheckSubStore);
        }
        return true;
    }
}
