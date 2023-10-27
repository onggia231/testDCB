package vn.com.telsoft.model;

import com.faplib.lib.SystemConfig;
import com.faplib.lib.SystemLogger;
import com.faplib.lib.data.ConnectionFactory;
import org.apache.commons.dbutils.DbUtils;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import vn.com.telsoft.entity.Content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class LazyContentModel extends LazyDataModel<Content> {
    private static final long serialVersionUID = 290728377480146293L;

    String strSQL;
    String strLastSortField;
    private List<Content> datasource;

    public LazyContentModel(String strSQL) {
        this.strSQL = strSQL;
    }

    @Override
    public Content getRowData(String rowKey) {
        for (Content content : datasource) {
            if (rowKey != null && content.getContentId() != null && content.getContentId().toString().equals(rowKey)) {
                return content;
            }
        }
        return null;
    }

    @Override
    public Object getRowKey(Content content) {
        return content.getContentId();
    }

    @Override
    public List<Content> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        datasource = new ArrayList<>();

        if (!"".equals(strSQL)) {
            String strSQLQ = "SELECT * FROM ( " + strSQL + ") ot WHERE 1=1 " + buildFilterCondition(filters, "ot") + buildSortCondition(sortField, sortOrder);
            setRowCount(getRowCount(strSQLQ));
            Connection con = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                con = ConnectionFactory.getConnection(SystemConfig.getConfig("DefaultDB"));
                stmt = con.prepareStatement(buildPagingQuery(first, pageSize, strSQLQ));
                rs = stmt.executeQuery();
                while (rs.next()) {
                    Content ett = new Content();
                    ett.setContentId(rs.getInt("content_id"));
                    ett.setContentDescription(rs.getString("content_description"));
                    ett.setStatus(rs.getString("status"));
                    datasource.add(ett);
                }
            } catch (Exception ex) {
                SystemLogger.getLogger().error(ex);
            } finally {
                DbUtils.closeQuietly(rs);
                DbUtils.closeQuietly(stmt);
                DbUtils.closeQuietly(con);
            }
        }
        //rowCount
//        int dataSize = ldata.size();
//        this.setRowCount(dataSize);
        return datasource;
    }

    private String buildPagingQuery(int startRow, int pageSize, String strSQL) {
        return "SELECT * FROM (SELECT a.*, rownum rown FROM (" + strSQL + ")  a) b WHERE b.rown between " + (startRow + 1) + " AND " + (startRow + pageSize);
    }

    private String buildCountQuery(String strSQL) {
        return "SELECT count(1) FROM (" + strSQL + ") ";
    }

    private int getRowCount(String strSQL) {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = ConnectionFactory.getConnection(SystemConfig.getConfig("DefaultDB"));
            stmt = con.prepareStatement(buildCountQuery(strSQL));
            rs = stmt.executeQuery();
            while (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception ex) {
            SystemLogger.getLogger().error(ex);
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(con);
        }
        return 0;
    }

    private String buildFilterCondition(Map<String, Object> filters, String table) {
        String strReturnVal = "";
        if (filters.size() > 0) {
            Set<String> keySet = filters.keySet();
            for (String key : keySet) {
                String value = filters.get(key).toString();
                strReturnVal += " AND upper(" + table + "." + getMapColumn().get(key) + ") LIKE upper('%" + value + "%') ESCAPE '`' ";
            }
        }
        return strReturnVal;
    }

    private String buildSortCondition(String sortField, SortOrder sortOrder) {
        String strReturnVal = "";

        if (sortField != null) {
            String order = "";
            if (sortOrder.toString().equals("ASCENDING")) {
                order = "ASC";
            } else {
                order = "DESC";
            }
//            strReturnVal += " ORDER BY " + (Integer.parseInt(sortField) + 1) + order;
            strReturnVal += " ORDER BY ot." + getMapColumn().get(sortField).replace("\"", "") + " " + order;
        }
        strLastSortField = sortField;
        return strReturnVal;
    }

    private HashMap<String, String> getMapColumn() {
        HashMap<String, String> column = new HashMap<>();
        column.put("contentId", "content_id");
        column.put("contentDescription", "content_description");
        column.put("status", "status");
        return column;
    }

    public Content getFirst() {
        Content content = null;
        if (this.datasource != null && this.datasource.size() > 0) {
            content = this.datasource.get(0);
        } else {
            content = new Content();
        }
        return content;
    }

    public List<Content> getDatasource() {
        return datasource;
    }

    public void setDatasource(List<Content> datasource) {
        this.datasource = datasource;
    }
}
