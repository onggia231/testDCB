package vn.com.telsoft.model;

import com.faplib.lib.admin.data.AMDataPreprocessor;
import com.faplib.lib.util.SQLUtil;
import vn.com.telsoft.entity.*;
import vn.com.telsoft.util.ConnUtil;
import vn.com.telsoft.util.ForceSyncUtil;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppStoreDataModel extends AMDataPreprocessor implements Serializable {
    private static final long serialVersionUID = -950786022802422626L;

    public List<AppStoreData> getAllStore() throws Exception {
        List<AppStoreData> listRT = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String strSQL = "select a.id, a.store_code, a.name, a.status, a.description, a.yearly_limit, a.monthly_limit," +
                " a.weekly_limit, a.daily_limit, a.trans_limit, a.prepaid_limit_profile, a.postpaid_limit_profile from cb_store a order by store_code";
        try {
            open();
            stmt = mConnection.prepareStatement(strSQL);
            rs = stmt.executeQuery();
            while (rs.next()) {
                AppStoreData ent = new AppStoreData();
                CBStore ent1 = new CBStore();
                ent1.setId(rs.getLong(1));
                ent1.setStoreCode(rs.getString(2));
                ent1.setName(rs.getString(3));
                ent1.setStatus(rs.getLong(4));
                ent1.setDescription(rs.getString(5));
                Object yearlyLimit = rs.getObject(6);
                if (yearlyLimit == null) {
                    ent1.setYearlyLimit(null);
                } else {
                    ent1.setYearlyLimit(Long.parseLong(yearlyLimit.toString()));
                }
                Object monthlyLimit = rs.getObject(7);
                if (monthlyLimit == null) {
                    ent1.setMonthlyLimit(null);
                } else {
                    ent1.setMonthlyLimit(Long.parseLong(monthlyLimit.toString()));
                }
                Object weeklyLimit = rs.getObject(8);
                if (weeklyLimit == null) {
                    ent1.setWeeklyLimit(null);
                } else {
                    ent1.setWeeklyLimit(Long.parseLong(weeklyLimit.toString()));
                }
                Object dailyLimit = rs.getObject(9);
                if (dailyLimit == null) {
                    ent1.setDailyLimit(null);
                } else {
                    ent1.setDailyLimit(Long.parseLong(dailyLimit.toString()));
                }
                Object transLimit = rs.getObject(10);
                if (transLimit == null) {
                    ent1.setTransLimit(null);
                } else {
                    ent1.setTransLimit(Long.parseLong(transLimit.toString()));
                }
                ent1.setPrepaidLimitProfile(rs.getLong(11));
                ent1.setPostpaidLimitProfile(rs.getLong(12));
                ent.setCbStore(ent1);
                listRT.add(ent);
            }
        } catch (Exception ex) {
            Logger.getLogger(AppStoreDataModel.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } finally {
            close(rs);
            close(stmt);
            close();
        }
        return listRT;
    }

    public List<AppStoreData> getAllStoreAttrById(long storeId) throws Exception {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<AppStoreData> listRT = new ArrayList<>();
        String strSQL = "select a.attr_id, a.name, a.value from cb_store_attr a where a.store_id = ? order by name";
        try {
            open();
            stmt = mConnection.prepareStatement(strSQL);
            stmt.setLong(1, storeId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                AppStoreData ent = new AppStoreData();
                CBStoreAttr ent1 = new CBStoreAttr();
                ent1.setStoreID(storeId);
                ent1.setAttrID(rs.getLong(1));
                ent1.setName(rs.getString(2));
                ent1.setValue((rs.getString(3)));
                ent.setCbStoreAttr(ent1);
                listRT.add(ent);
            }
        } catch (Exception ex) {
            Logger.getLogger(AppStoreDataModel.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } finally {
            close(rs);
            close(stmt);
            close();
        }
        return listRT;
    }

    public AppStoreData doInsertCopy(AppStoreData entity, List<AppStoreData> lsAttr) throws Exception {
        PreparedStatement stmtStore = null;
        PreparedStatement stmtAttr = null;
        String strSqlStore = "insert into cb_store(id, store_code, name, description,status, yearly_limit, monthly_limit, weekly_limit, " +
                " daily_limit, trans_limit, prepaid_limit_profile, postpaid_limit_profile)" +
                "values(?,?,?,?,?,?,?,?,?,?,?,?)";
        String strSqlAttr = "insert into cb_store_attr(attr_id, store_id, name, value) values(?,?,?,?)";
        try {
            open();
            mConnection.setAutoCommit(false);
            stmtStore = mConnection.prepareStatement(strSqlStore);
            int i = 1;
            long id = SQLUtil.getSequenceValue(mConnection, "seq_cb_store");
            stmtStore.setLong(i++, id);
            stmtStore.setString(i++, entity.getCbStore().getStoreCode());
            stmtStore.setString(i++, entity.getCbStore().getName());
            stmtStore.setString(i++, entity.getCbStore().getDescription());
            stmtStore.setLong(i++, entity.getCbStore().getStatus());
            if (entity.getCbStore().getYearlyLimit() == null)
                stmtStore.setNull(i++, java.sql.Types.NULL);
            else
                stmtStore.setLong(i++, entity.getCbStore().getYearlyLimit());
            if (entity.getCbStore().getMonthlyLimit() == null)
                stmtStore.setNull(i++, java.sql.Types.NULL);
            else
                stmtStore.setLong(i++, entity.getCbStore().getMonthlyLimit());
            if (entity.getCbStore().getWeeklyLimit() == null)
                stmtStore.setNull(i++, java.sql.Types.NULL);
            else
                stmtStore.setLong(i++, entity.getCbStore().getWeeklyLimit());
            if (entity.getCbStore().getDailyLimit() == null)
                stmtStore.setNull(i++, java.sql.Types.NULL);
            else
                stmtStore.setLong(i++, entity.getCbStore().getDailyLimit());
            if (entity.getCbStore().getTransLimit() == null)
                stmtStore.setNull(i++, java.sql.Types.NULL);
            else
                stmtStore.setLong(i++, entity.getCbStore().getTransLimit());
            stmtStore.setLong(i++, entity.getCbStore().getPrepaidLimitProfile());
            stmtStore.setLong(i, entity.getCbStore().getPostpaidLimitProfile());
            stmtStore.executeUpdate();
            entity.getCbStore().setId(id);
            logAfterInsert("cb_store", "id=" + id);

            for (AppStoreData values : lsAttr) {
                long attrID = SQLUtil.getSequenceValue(mConnection, "seq_cb_store_attr");
                stmtAttr = mConnection.prepareStatement(strSqlAttr);
                int j = 1;
                stmtAttr.setLong(j++, attrID);
                stmtAttr.setLong(j++, id);
                stmtAttr.setString(j++, values.getCbStoreAttr().getName());
                stmtAttr.setString(j, values.getCbStoreAttr().getValue());
                stmtAttr.executeUpdate();
                logAfterInsert("cb_store_attr", "attr_id=" + attrID);
            }
            ForceSyncUtil.updateSystemApParam(mConnection);
            mConnection.commit();
        } catch (Exception ex) {
            ConnUtil.rollback(mConnection);
            Logger.getLogger(AppStoreDataModel.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } finally {
            mConnection.setAutoCommit(true);
            close(stmtStore);
            close(stmtAttr);
            close();
        }
        return entity;
    }

    public AppStoreData doUpdate(AppStoreData entity, List<AppStoreData> lsInsert, List<AppStoreData> lsUpdate, List<AppStoreData> lsDelete) throws Exception {
        PreparedStatement stmt = null;
        String strSqlStore = "update cb_store set store_code = ?, name = ?, description = ?, status = ? " +
                ", yearly_limit = ?, monthly_limit = ?, weekly_limit = ?, daily_limit = ?,trans_limit = ?" +
                ", prepaid_limit_profile = ?, postpaid_limit_profile = ? " +
                " where id = ?";
        String strSqlInsertAttr = "insert into cb_store_attr(attr_id, store_id, name, value) values(?,?,?,?)";
        String strSqlUpdateAttr = "update cb_store_attr set name = ? , value = ? where attr_id = ?";
        String strSqlDeleteAttr = "delete cb_store_attr where attr_id = ?";
        try {
            open();
            mConnection.setAutoCommit(false);
            List listChange = logBeforeUpdate("cb_store", "id=" + entity.getCbStore().getId());

            stmt = mConnection.prepareStatement(strSqlStore);
            int i = 1;
            stmt.setString(i++, entity.getCbStore().getStoreCode());
            stmt.setString(i++, entity.getCbStore().getName());
            stmt.setString(i++, entity.getCbStore().getDescription());
            stmt.setLong(i++, entity.getCbStore().getStatus());
            if (entity.getCbStore().getYearlyLimit() == null)
                stmt.setNull(i++, java.sql.Types.NULL);
            else
                stmt.setLong(i++, entity.getCbStore().getYearlyLimit());
            if (entity.getCbStore().getMonthlyLimit() == null)
                stmt.setNull(i++, java.sql.Types.NULL);
            else
                stmt.setLong(i++, entity.getCbStore().getMonthlyLimit());
            if (entity.getCbStore().getWeeklyLimit() == null)
                stmt.setNull(i++, java.sql.Types.NULL);
            else
                stmt.setLong(i++, entity.getCbStore().getWeeklyLimit());
            if (entity.getCbStore().getDailyLimit() == null)
                stmt.setNull(i++, java.sql.Types.NULL);
            else
                stmt.setLong(i++, entity.getCbStore().getDailyLimit());
            if (entity.getCbStore().getTransLimit() == null)
                stmt.setNull(i++, java.sql.Types.NULL);
            else
                stmt.setLong(i++, entity.getCbStore().getTransLimit());
            stmt.setLong(i++, entity.getCbStore().getPrepaidLimitProfile());
            stmt.setLong(i++, entity.getCbStore().getPostpaidLimitProfile());
            stmt.setLong(i, entity.getCbStore().getId());
            stmt.executeUpdate();
            logAfterUpdate(listChange);

            if (!lsUpdate.isEmpty()) {
                stmt = mConnection.prepareStatement(strSqlUpdateAttr);
                for (AppStoreData values : lsUpdate) {
                    List listChange1 = logBeforeUpdate("cb_store_attr", "attr_id=" + values.getCbStoreAttr().getAttrID());

                    int j = 1;
                    stmt.setString(j++, values.getCbStoreAttr().getName());
                    stmt.setString(j++, values.getCbStoreAttr().getValue());
                    stmt.setLong(j, values.getCbStoreAttr().getAttrID());
                    stmt.executeUpdate();
                    logAfterUpdate(listChange1);
                }
            }

            if (!lsDelete.isEmpty()) {
                stmt = mConnection.prepareStatement(strSqlDeleteAttr);
                for (AppStoreData values : lsDelete) {
                    logBeforeDelete("cb_store_attr", "attr_id=" + values.getCbStoreAttr().getAttrID());
                    stmt.setLong(1, values.getCbStoreAttr().getAttrID());
                    stmt.executeUpdate();
                }
            }
            if (!lsInsert.isEmpty()) {
                stmt = mConnection.prepareStatement(strSqlInsertAttr);
                for (AppStoreData values : lsInsert) {
                    long attrID = SQLUtil.getSequenceValue(mConnection, "seq_cb_store_attr");
                    int q = 1;
                    stmt.setLong(q++, attrID);
                    stmt.setLong(q++, entity.getCbStore().getId());
                    stmt.setString(q++, values.getCbStoreAttr().getName());
                    stmt.setString(q, values.getCbStoreAttr().getValue());
                    stmt.executeUpdate();
                    logAfterInsert("cb_store_attr", "attr_id=" + attrID);
                }
            }
            ForceSyncUtil.updateSystemApParam(mConnection);
            mConnection.commit();
        } catch (Exception ex) {
            ConnUtil.rollback(mConnection);
            Logger.getLogger(AppStoreDataModel.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } finally {
            mConnection.setAutoCommit(true);
            close(stmt);
            close();
        }
        return entity;
    }

    public boolean checkData(String strName, Long storeId) throws Exception {
        try {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            String sql = "select * from cb_store_attr where name = ? and store_id = ?";
            open();
            stmt = mConnection.prepareStatement(sql);
            stmt.setString(1, strName);
            stmt.setLong(2, storeId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            }

        } catch (Exception ex) {
            throw ex;
        } finally {
            close();
        }
        return false;
    }

    public List<CBLimitProfile> getListCbLimitProfile() throws Exception {
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
            ex.printStackTrace();
            throw ex;
        } finally {
            close(mRs);
            close(mStmt);
            close();
        }
        return listReturn;
    }

    public List<String> getListApparam(String query) throws Exception {
        List<String> listReturn = new ArrayList<>();
        try {
            open();
            StringBuilder sql = new StringBuilder();
            if (query == null || query.length() <= 0) {
                sql.append("Select DISTINCT a.par_type  ");
                sql.append(" FROM ap_param a where a.par_group = ?");
            } else {
                sql.append("Select DISTINCT a.par_type  ");
                sql.append(" FROM ap_param a where lower(a.par_type) like ? and a.par_group = ?");
            }

            mStmt = mConnection.prepareStatement(sql.toString());
            if (query == null || query.length() <= 0) {
                mStmt.setString(1, "STORE_PARAM_COMBO");
                mRs = mStmt.executeQuery();
            } else {
                mStmt.setString(1, "%" + query.trim() + "%");
                mStmt.setString(2, "STORE_PARAM_COMBO");
                mRs = mStmt.executeQuery();
            }
            while (mRs.next()) {
                listReturn.add(mRs.getString("par_type"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            close(mRs);
            close(mStmt);
            close();
        }
        return listReturn;
    }

    public List<String> getListApparam1(String query, String parType) throws Exception {
        List<String> listReturn = new ArrayList<>();
        try {
            open();
            StringBuilder sql = new StringBuilder();
            if (null != parType && null == query) {
                sql.append("Select a.par_name from ap_param a  ");
                sql.append(" where a.par_group = ? and a.par_type like ? ");
            } else if (null == parType && null == query) {
                return listReturn;
            } else if (null == parType && null != query) {
                return listReturn;
            } else if (null != parType && null != query) {
                sql.append("Select a.par_name from ap_param a ");
                sql.append("where a.par_group = ? and a.par_type like ? and a.par_name like ?");
            }

            mStmt = mConnection.prepareStatement(sql.toString());
            if (null != parType && null == query) {
                mStmt.setString(1, "STORE_PARAM_COMBO");
                mStmt.setString(2, "%" + parType.trim() + "%");
                mRs = mStmt.executeQuery();
            } else if (null != parType && null != query) {
                mStmt.setString(1, "STORE_PARAM_COMBO");
                mStmt.setString(2, "%" + parType.trim() + "%");
                mStmt.setString(3, "%" + query.trim() + "%");
                mRs = mStmt.executeQuery();
            }
            while (mRs.next()) {
                listReturn.add(mRs.getString("par_name"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            close(mRs);
            close(mStmt);
            close();
        }
        return listReturn;
    }

}
