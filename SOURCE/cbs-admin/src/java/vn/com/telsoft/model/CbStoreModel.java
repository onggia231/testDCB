/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.com.telsoft.model;

import com.faplib.lib.SystemLogger;
import com.faplib.lib.admin.data.AMDataPreprocessor;
import vn.com.telsoft.entity.CBStore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author trieunv
 */
public class CbStoreModel extends AMDataPreprocessor implements Serializable {
    private static final long serialVersionUID = -2844370274433555886L;

    public List<CBStore> getCBStoreAll(CBStore store) throws Exception {
        List<CBStore> listReturn = new ArrayList<>();
        List lstParam = new ArrayList<>();
        int i = 0;
        try {
            open();
            String strSQL = "SELECT id, store_Code, name, status, description, yearly_Limit,monthly_Limit,weekly_Limit,daily_Limit,trans_Limit " +
                    " FROM CB_STORE where 1=1 ";
            if (store != null) {
                if (store.getStatus() != null) {
                    strSQL += " and status = ? ";
                    lstParam.add(store.getStatus());
                }
            }
            strSQL += " order by store_Code";
            mStmt = mConnection.prepareStatement(strSQL);
            if (lstParam != null && lstParam.size() > 0) {
                for (Object obj : lstParam) {
                    mStmt.setObject(++i, obj);
                }
            }
            mRs = mStmt.executeQuery();

            while (mRs.next()) {
                CBStore dto = new CBStore();
                dto.setId(mRs.getLong(1));
                dto.setStoreCode(mRs.getString(2));
                dto.setName(mRs.getString(3));
                dto.setStatus(mRs.getLong(4));
                dto.setDescription(mRs.getString(5));
                dto.setYearlyLimit(mRs.getLong(6));
                dto.setMonthlyLimit(mRs.getLong(7));
                dto.setWeeklyLimit(mRs.getLong(8));
                dto.setDailyLimit(mRs.getLong(9));
                dto.setTransLimit(mRs.getLong(10));
                listReturn.add(dto);
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
