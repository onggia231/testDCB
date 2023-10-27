/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.com.telsoft.model;

import com.faplib.lib.SystemLogger;
import com.faplib.lib.admin.data.AMDataPreprocessor;
import vn.com.telsoft.entity.CBLimitProfile;
import vn.com.telsoft.entity.CBStore;
import vn.com.telsoft.entity.CBSubStore;
import vn.com.telsoft.util.ConnUtil;
import vn.com.telsoft.util.ForceSyncUtil;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * @author TUNGLM, TELSOFT
 */
public class InfoISDNTransModel extends AMDataPreprocessor implements Serializable {
    private static final long serialVersionUID = 2377691616470481890L;

    public List<CBStore> getListAllStore() throws Exception {
        List<CBStore> listReturn = new ArrayList<>();

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            open();
            //Get list cb_store
            String StrSQL = "SELECT\n" +
                    "    a.id,\n" +
                    "    a.store_code,\n" +
                    "    a.name,\n" +
                    "    a.status,\n" +
                    "    a.description,\n" +
                    "    a.yearly_limit,\n" +
                    "    a.monthly_limit,\n" +
                    "    a.weekly_limit,\n" +
                    "    a.daily_limit,\n" +
                    "    a.trans_limit\n" +
                    "FROM\n" +
                    "    cb_store a";
            stmt = mConnection.prepareStatement(StrSQL);
            rs = stmt.executeQuery();
            while (rs.next()) {
                CBStore tmp = new CBStore();
                tmp.setId(rs.getLong(1));
                tmp.setStoreCode(rs.getString(2));
                tmp.setName(rs.getString(3));
                tmp.setStatus(rs.getLong(4));
                tmp.setDescription(rs.getString(5));
                tmp.setYearlyLimit(rs.getLong(6));
                tmp.setMonthlyLimit(rs.getLong(7));
                tmp.setWeeklyLimit(rs.getLong(8));
                tmp.setDailyLimit(rs.getLong(9));
                tmp.setTransLimit(rs.getLong(10));
                listReturn.add(tmp);
            }
        } catch (Exception ex) {
            SystemLogger.getLogger().error(ex);
            throw ex;

        } finally {
            close(rs);
            close(stmt);
            close();
        }

        return listReturn;
    }

    public void updateInfoISDNTrans(CBSubStore subStore) throws Exception {

        try {
            open();
            mConnection.setAutoCommit(false);

            //Update cb_sub_store
            List listChange = logBeforeUpdate("cb_sub_store", "sub_id=" + subStore.getSubId() + " AND store_id=" + subStore.getStoreId());
            String strSQL = "UPDATE cb_sub_store\n" +
                    "SET yearly_limit=?, monthly_limit=?, weekly_limit=?, daily_limit=?, trans_limit=?, limit_profile_id = ? \n" +
                    "WHERE sub_id=?\n" +
                    "   AND store_id=?";
            mStmt = mConnection.prepareStatement(strSQL);
//            if (subStore.getYearlyLimit() != null) {
//                mStmt.setLong(1, subStore.getYearlyLimit());
//            } else {
//                mStmt.setNull(1, java.sql.Types.NULL);
//            }
//            if (subStore.getMonthlyLimit() != null) {
//                mStmt.setLong(2, subStore.getMonthlyLimit());
//            } else {
//                mStmt.setNull(2, java.sql.Types.NULL);
//            }
//            if (subStore.getWeeklyLimit() != null) {
//                mStmt.setLong(3, subStore.getWeeklyLimit());
//            } else {
//                mStmt.setNull(3, java.sql.Types.NULL);
//            }
//            if (subStore.getDailyLimit() != null) {
//                mStmt.setLong(4, subStore.getDailyLimit());
//            } else {
//                mStmt.setNull(4, java.sql.Types.NULL);
//            }
//            if (subStore.getTransLimit() != null) {
//                mStmt.setLong(5, subStore.getTransLimit());
//            } else {
//                mStmt.setNull(5, java.sql.Types.NULL);
//            }

            mStmt.setObject(1, subStore.getYearlyLimit());
            mStmt.setObject(2, subStore.getMonthlyLimit());
            mStmt.setObject(3, subStore.getWeeklyLimit());
            mStmt.setObject(4, subStore.getDailyLimit());
            mStmt.setObject(5, subStore.getTransLimit());
            if (subStore.getLimitProfileId() != null) {
                mStmt.setLong(6, subStore.getLimitProfileId());
            } else {
                mStmt.setNull(6, java.sql.Types.NULL);
            }
            mStmt.setLong(7, subStore.getSubId());
            mStmt.setLong(8, subStore.getStoreId());
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

    public List<CBLimitProfile> getListLimitProfile() throws Exception {
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
}
