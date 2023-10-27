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
import vn.com.telsoft.entity.CBStore;
import vn.com.telsoft.entity.CBSubStore;
import vn.com.telsoft.entity.CBSubscriber;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author TUNGLM, TELSOFT
 */
public class SearchISDNModel extends AMDataPreprocessor implements Serializable {

    private static final long serialVersionUID = 8580860994557411610L;

    public CBSubscriber getSubscriberInfo(String strIsdn, Long subId) throws Exception {
        CBSubscriber tmpCBSubscriber = new CBSubscriber();
        List<CBStore> lstCBStore = new ArrayList<>();
        Map<Long, CBSubStore> mapSubStore = new HashMap<Long, CBSubStore>();

        PreparedStatement stmt = null;
        ResultSet rs = null;

        PreparedStatement stmt1 = null;
        ResultSet rs1 = null;

        try {
            open();

            String strSQL = "SELECT\n" +
                    "    a.id,\n" +
                    "    a.isdn,\n" +
                    "    a.reg_date,\n" +
                    "    a.sub_type,\n" +
                    "    a.status\n" +
                    "FROM\n" +
                    "    cb_subscriber a\n" +
                    "WHERE\n" +
                    "    a.isdn = ? and a.id = ? ";

            String strSQL1 = "SELECT\n" +
                    "    a.sub_id,\n" +
                    "    a.store_id,\n" +
                    "    a.yearly_limit,\n" +
                    "    a.monthly_limit,\n" +
                    "    a.weekly_limit,\n" +
                    "    a.daily_limit,\n" +
                    "    a.trans_limit,\n" +
                    "    a.reg_date,\n" +
                    "    a.total_year,\n" +
                    "    a.total_month,\n" +
                    "    a.total_week,\n" +
                    "    a.total_day,\n" +
                    "    a.start_year,\n" +
                    "    a.start_month,\n" +
                    "    a.start_week,\n" +
                    "    a.start_day,\n" +
                    "    a.reserved_charge,\n" +
                    "    a.limit_profile_id,\n" +
                    "    b.store_code,\n" +
                    "    b.name\n" +
                    "FROM\n" +
                    "    cb_sub_store a,\n" +
                    "    cb_store b\n" +
                    "WHERE\n" +
                    "        a.store_id = b.id\n" +
                    "        AND\n" +
                    "        a.sub_id = ?";

            stmt = mConnection.prepareStatement(strSQL);
            stmt1 = mConnection.prepareStatement(strSQL1);

            stmt.setString(1, strIsdn);
            stmt.setLong(2, subId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                tmpCBSubscriber.setId(rs.getLong(1));
                tmpCBSubscriber.setIsdn(rs.getString(2));
                tmpCBSubscriber.setRegDate(rs.getTimestamp(3));
                tmpCBSubscriber.setSubType(rs.getString(4));
                tmpCBSubscriber.setStatus(rs.getLong(5));

                //Get substore
                stmt1.setLong(1, tmpCBSubscriber.getId());
                rs1 = stmt1.executeQuery();
                while (rs1.next()) {
                    CBStore tmpCBStore = new CBStore();
                    CBSubStore tmpCBSubStore = new CBSubStore();
                    tmpCBSubStore.setSubId(rs1.getLong(1));
                    tmpCBSubStore.setStoreId(rs1.getLong(2));
                    tmpCBSubStore.setYearlyLimit(rs1.getObject(3) != null ? rs1.getLong(3) : null);
                    tmpCBSubStore.setMonthlyLimit(rs1.getObject(4) != null ? rs1.getLong(4) : null);
                    tmpCBSubStore.setWeeklyLimit(rs1.getObject(5) != null ? rs1.getLong(5) : null);
                    tmpCBSubStore.setDailyLimit(rs1.getObject(6) != null ? rs1.getLong(6) : null);
                    tmpCBSubStore.setTransLimit(rs1.getObject(7) != null ? rs1.getLong(7) : null);
                    tmpCBSubStore.setRegDate(rs1.getTimestamp(8));
                    tmpCBSubStore.setTotalYear(rs1.getObject(9) != null ? rs1.getLong(9) : null);
                    tmpCBSubStore.setTotalMonth(rs1.getObject(10) != null ? rs1.getLong(10) : null);
                    tmpCBSubStore.setTotalWeek(rs1.getObject(11) != null ? rs1.getLong(11) : null);
                    tmpCBSubStore.setTotalDay(rs1.getObject(12) != null ? rs1.getLong(12) : null);
                    tmpCBSubStore.setStartYear(rs1.getTimestamp(13));
                    tmpCBSubStore.setStartMonth(rs1.getTimestamp(14));
                    tmpCBSubStore.setStartWeek(rs1.getTimestamp(15));
                    tmpCBSubStore.setStartDay(rs1.getTimestamp(16));
                    tmpCBSubStore.setReservedCharge(rs1.getObject(17) != null ? rs1.getLong(17) : null);
                    tmpCBSubStore.setLimitProfileId(rs1.getObject(18) != null ? rs1.getLong(18) : null);
                    if (!mapSubStore.containsKey(tmpCBSubStore.getStoreId())) {
                        mapSubStore.put(tmpCBSubStore.getStoreId(), tmpCBSubStore);
                    }

                    tmpCBStore.setId(rs1.getLong(2));
                    tmpCBStore.setStoreCode(rs1.getString(19));
                    tmpCBStore.setName(rs1.getString(20));
                    lstCBStore.add(tmpCBStore);
                }
                tmpCBSubscriber.setMapSubStore(mapSubStore);
                tmpCBSubscriber.setLstStore(lstCBStore);
            }

        } catch (Exception ex) {
            SystemLogger.getLogger().error(ex);
            throw ex;
        } finally {
            close(rs);
            close(stmt);
            close(rs1);
            close(stmt1);
            close();
        }
        return tmpCBSubscriber;
    }

    public List<CBSubscriber> getListSubscriberInfo(String strIsdn) throws Exception {
        List<CBSubscriber> listReturn = new ArrayList<>();
        CBSubscriber tmpCBSubscriber = new CBSubscriber();
        List<CBStore> lstCBStore = new ArrayList<>();
        Map<Long, CBSubStore> mapSubStore = new HashMap<Long, CBSubStore>();

        PreparedStatement stmt = null;
        ResultSet rs = null;

        PreparedStatement stmt1 = null;
        ResultSet rs1 = null;

        try {
            open();
            String strSQL = "SELECT\n" +
                    "    a.id,\n" +
                    "    a.isdn,\n" +
                    "    a.reg_date,\n" +
                    "    a.sub_type,\n" +
                    "    a.status\n" +
                    "FROM\n" +
                    "    cb_subscriber a\n" +
                    "WHERE\n" +
                    "    a.isdn = ? order by a.status DESC, a.reg_date DESC";

            String strSQL1 = "SELECT\n" +
                    "    a.sub_id,\n" +
                    "    a.store_id,\n" +
                    "    a.yearly_limit,\n" +
                    "    a.monthly_limit,\n" +
                    "    a.weekly_limit,\n" +
                    "    a.daily_limit,\n" +
                    "    a.trans_limit,\n" +
                    "    a.reg_date,\n" +
                    "    a.total_year,\n" +
                    "    a.total_month,\n" +
                    "    a.total_week,\n" +
                    "    a.total_day,\n" +
                    "    a.start_year,\n" +
                    "    a.start_month,\n" +
                    "    a.start_week,\n" +
                    "    a.start_day,\n" +
                    "    a.reserved_charge,\n" +
                    "    a.limit_profile_id,\n" +
                    "    b.store_code,\n" +
                    "    b.name\n" +
                    "FROM\n" +
                    "    cb_sub_store a,\n" +
                    "    cb_store b\n" +
                    "WHERE\n" +
                    "        a.store_id = b.id\n" +
                    "        AND\n" +
                    "        a.sub_id = ?";

            stmt = mConnection.prepareStatement(strSQL);
            stmt1 = mConnection.prepareStatement(strSQL1);

            stmt.setString(1, strIsdn);
            rs = stmt.executeQuery();

            while (rs.next()) {
                tmpCBSubscriber = new CBSubscriber();
                tmpCBSubscriber.setId(rs.getLong(1));
                tmpCBSubscriber.setIsdn(rs.getString(2));
                tmpCBSubscriber.setRegDate(rs.getTimestamp(3));
                tmpCBSubscriber.setSubType(rs.getString(4));
                tmpCBSubscriber.setStatus(rs.getLong(5));

                //Get substore
                stmt1.setLong(1, tmpCBSubscriber.getId());
                rs1 = stmt1.executeQuery();
                while (rs1.next()) {
                    CBStore tmpCBStore = new CBStore();
                    CBSubStore tmpCBSubStore = new CBSubStore();
                    tmpCBSubStore.setSubId(rs1.getLong(1));
                    tmpCBSubStore.setStoreId(rs1.getLong(2));
                    tmpCBSubStore.setYearlyLimit(rs1.getObject(3) != null ? rs1.getLong(3) : null);
                    tmpCBSubStore.setMonthlyLimit(rs1.getObject(4) != null ? rs1.getLong(4) : null);
                    tmpCBSubStore.setWeeklyLimit(rs1.getObject(5) != null ? rs1.getLong(5) : null);
                    tmpCBSubStore.setDailyLimit(rs1.getObject(6) != null ? rs1.getLong(6) : null);
                    tmpCBSubStore.setTransLimit(rs1.getObject(7) != null ? rs1.getLong(7) : null);
                    tmpCBSubStore.setRegDate(rs1.getTimestamp(8));
                    tmpCBSubStore.setTotalYear(rs1.getObject(9) != null ? rs1.getLong(9) : null);
                    tmpCBSubStore.setTotalMonth(rs1.getObject(10) != null ? rs1.getLong(10) : null);
                    tmpCBSubStore.setTotalWeek(rs1.getObject(11) != null ? rs1.getLong(11) : null);
                    tmpCBSubStore.setTotalDay(rs1.getObject(12) != null ? rs1.getLong(12) : null);
                    tmpCBSubStore.setStartYear(rs1.getTimestamp(13));
                    tmpCBSubStore.setStartMonth(rs1.getTimestamp(14));
                    tmpCBSubStore.setStartWeek(rs1.getTimestamp(15));
                    tmpCBSubStore.setStartDay(rs1.getTimestamp(16));
                    tmpCBSubStore.setReservedCharge(rs1.getObject(17) != null ? rs1.getLong(17) : null);
                    tmpCBSubStore.setLimitProfileId(rs1.getObject(18) != null ? rs1.getLong(18) : null);
                    if (!mapSubStore.containsKey(tmpCBSubStore.getStoreId())) {
                        mapSubStore.put(tmpCBSubStore.getStoreId(), tmpCBSubStore);
                    }

                    tmpCBStore.setId(rs1.getLong(2));
                    tmpCBStore.setStoreCode(rs1.getString(19));
                    tmpCBStore.setName(rs1.getString(20));
                    lstCBStore.add(tmpCBStore);
                }
                tmpCBSubscriber.setMapSubStore(mapSubStore);
                tmpCBSubscriber.setLstStore(lstCBStore);
                listReturn.add(tmpCBSubscriber);
            }
        } catch (Exception ex) {
            SystemLogger.getLogger().error(ex);
            throw ex;
        } finally {
            close(rs);
            close(stmt);
            close(rs1);
            close(stmt1);
            close();
        }
        return listReturn;
    }
}
