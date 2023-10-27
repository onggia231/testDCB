package com.telsoft.cbs.camel.service.cbs;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.camel.service.ServiceAdapter;
import com.telsoft.cbs.camel.service.cache.CacheService;
import com.telsoft.cbs.domain.*;
import com.telsoft.database.Database;
import com.telsoft.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.DbUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CbsDbService extends ServiceAdapter implements CacheService {
    private String lastChange = null;
    private Map<String, Map> mapCache = new ConcurrentHashMap<>();

    public static String getLastConfigurationChange(Connection cn) throws Exception {
        String strSQL = "SELECT PAR_VALUE FROM AP_PARAM WHERE PAR_TYPE='SYSTEM' AND PAR_NAME='CONFIGURATION_LAST_CHANGED'";
        Vector vt = Database.executeQuery(cn, strSQL);
        return vt.size() > 0 ? StringUtil.nvl(((Vector) vt.get(0)).get(0), "") : "";
    }

    @Override
    public void init() {

    }

    @Override
    public void close() {
    }

    @Override
    protected void resetModifyFlag(Connection connection) {
    }

    @Override
    protected boolean hasModified(Connection connection) {
        try {
            String last = getLastConfigurationChange(connection);
            return !last.equals(lastChange);
        } catch (Exception e) {
            log.error("checkModified CbsDbService error", e);
            return false;
        }
    }

    @Override
    protected void doSync(Connection connection) {
        try {
            Map<String, Map> tempCache = new ConcurrentHashMap<>();
            syncStore(tempCache, connection);
            syncAccessList(tempCache, connection);


            this.mapCache = tempCache;
            lastChange = getLastConfigurationChange(connection);
        } catch (Exception e) {
            log.error("Sync data error", e);
        }
    }

    private void syncStore(Map<String, Map> tempCache, Connection connection) throws Exception {
        try {
            // STORE
            String sql =
                    "SELECT ID,STORE_CODE, NAME,STATUS, YEARLY_LIMIT, MONTHLY_LIMIT, WEEKLY_LIMIT, DAILY_LIMIT, TRANS_LIMIT " +
                            "FROM CB_STORE WHERE STATUS = 1";

            // STORE_ATTR
            String sqlAttr =
                    "SELECT STORE_ID, NAME,VALUE FROM CB_STORE_ATTR WHERE STORE_ID IN (SELECT STORE_ID FROM CB_STORE WHERE STATUS = 1)";

            Vector<Vector> vtData = Database.executeQuery(connection, sql);
            Vector<Vector> vtDataAttr = Database.executeQuery(connection, sqlAttr);
            Map<String, CBStore> mapStore = new HashMap<>();

            for (Vector vtRow : vtData) {
                String storeId = StringUtil.nvl(vtRow.get(0), "");
                Properties properties = new Properties();
                for (Vector vtRowAttr : vtDataAttr) {
                    String attrStoreId = StringUtil.nvl(vtRowAttr.get(0), "");
                    String attrName = StringUtil.nvl(vtRowAttr.get(1), "");
                    String attrValue = StringUtil.nvl(vtRowAttr.get(2), "");
                    if (attrStoreId.equals(storeId)) {
                        properties.setProperty(attrName, attrValue);
                    }
                }

                CBStore store = CBStore.builder()
                        .storeId(storeId)
                        .storeCode(StringUtil.nvl(vtRow.get(1), ""))
                        .name(StringUtil.nvl(vtRow.get(2), ""))
                        .status(StringUtil.nvl(vtRow.get(3), ""))
                        .yearlyLimits(StringUtil.evl(vtRow.get(4), "-1").equals("-1") ? null : Long.parseLong((String) vtRow.get(4)))
                        .monthlyLimits(StringUtil.evl(vtRow.get(5), "-1").equals("-1") ? null : Long.parseLong((String) vtRow.get(5)))
                        .weeklyLimits(StringUtil.evl(vtRow.get(6), "-1").equals("-1") ? null : Long.parseLong((String) vtRow.get(6)))
                        .dailyLimits(StringUtil.evl(vtRow.get(7), "-1").equals("-1") ? null : Long.parseLong((String) vtRow.get(7)))
                        .transactionLimits(StringUtil.evl(vtRow.get(8), "-1").equals("-1") ? null : Long.parseLong((String) vtRow.get(8)))
                        .attributes(properties)
                        .build();
                mapStore.put(store.getStoreCode(), store);
            }
            tempCache.put(CbsContansts.STORE, mapStore);
        } catch (Exception ex) {
            throw new Exception("Sync Store error", ex);
        }
    }

    private void syncAccessList(Map<String, Map> tempCache, Connection connection) throws Exception {
        PreparedStatement isdnStmt = null;
        PreparedStatement contentStmt = null;
        try {
            // ACCESS_LIST
            String sql =
                    "SELECT LIST_ID, LIST_NAME, ACCESS_TYPE, DESCRIPTION, STATUS, LIST_TYPE, CACHED FROM CB_LIST WHERE STATUS = 1";

            // CB_ITEM_ISDN
            String sqlItemIsdn =
                    "SELECT STORE_ID, ISDN FROM CB_ITEM_ISDN WHERE LIST_ID = ?";

            // CB_ITEM_CONTENT
            String sqlItemContent =
                    "SELECT I.STORE_ID, C.MATCHING_TYPE, C.KEYWORD, C.CASE_SENSITIVE " +
                            " FROM CB_ITEM_CONTENT I,CB_CONTENT_RECOGNIZE C WHERE I.LIST_ID = ? AND C.CONTENT_ID = I.CONTENT_ID ";

            Vector<Vector> vtList = Database.executeQuery(connection, sql);
            Map<String, AccessList> mapAccessList = new HashMap<>();
            isdnStmt = connection.prepareStatement(sqlItemIsdn);
            contentStmt = connection.prepareStatement(sqlItemContent);

            for (Vector vtRow : vtList) {
                Long list_id = Long.parseLong(StringUtil.nvl(vtRow.get(0), "-1"));
                String list_name = StringUtil.nvl(vtRow.get(1), "");
                LIST_ACCESS_TYPE access_type = LIST_ACCESS_TYPE.fromCode(Integer.parseInt(StringUtil.nvl(vtRow.get(2), "0")));
                String description = StringUtil.nvl(vtRow.get(3), "");
                String status = StringUtil.nvl(vtRow.get(4), "");
                LIST_TYPE list_type = LIST_TYPE.fromCode(Integer.parseInt(StringUtil.nvl(vtRow.get(5), "0")));
                boolean cached = StringUtil.nvl(vtRow.get(6), "1").equals("1");

                AccessList list = AccessList.builder()
                        .listId(list_id)
                        .name(list_name)
                        .access_type(access_type)
                        .description(description)
                        .enabled("1".equals(status))
                        .type(list_type)
                        .cached(cached)
                        .build();

                if (list.isCached()) {
                    List<Item> itemList = new ArrayList<>();
                    switch (list_type) {
                        case ISDN: {
                            isdnStmt.setLong(1, list_id);
                            ResultSet rs = isdnStmt.executeQuery();
                            while (rs.next()) {
                                Item item = Isdn.builder()
                                        .isdn(rs.getString(2))
                                        .build();
                                item.setStore_id(rs.getString(1));
                                itemList.add(item);
                            }
                            break;
                        }
                        case CONTENT: {
                            contentStmt.setLong(1, list_id);
                            ResultSet rs = contentStmt.executeQuery();
                            while (rs.next()) {
                                Item item = Content.builder()
                                        .matchingType(MATCHING_TYPE.valueOf(rs.getString(2)))
                                        .keyword(rs.getString(3))
                                        .caseSensitive(rs.getBoolean("CASE_SENSITIVE"))
                                        .build();
                                item.setStore_id(rs.getString(1));
                                itemList.add(item);
                            }
                            break;
                        }
                    }
                    list.setItems(itemList);
                }

                mapAccessList.put(list.getName(), list);
            }
            tempCache.put(CbsContansts.ACCESS_MAP, mapAccessList);
        } catch (Exception ex) {
            throw new Exception("Sync Store error", ex);
        } finally {
            DbUtils.closeQuietly(contentStmt);
            DbUtils.closeQuietly(isdnStmt);
        }
    }

    @Override
    public Map getMap(String name) {
        return mapCache.get(name);
    }
}
