package vn.com.telsoft.connection;

import com.faplib.lib.SystemLogger;
import com.faplib.lib.admin.data.DataLogger;
import org.apache.commons.dbutils.DbUtils;

import java.sql.*;

public class DataPreprocessor {
    //main db connection
    public Connection mConnection = null;
    //secondary db connection
    public Connection mSCConnection = null;

    public PreparedStatement mStmt = null;
    public ResultSet mRs = null;
    private DataLogger log = null;

    public String mstrLogID;

    private String mstrUserId;
    private String mstrObjectId;
    private String mstrClientIp;
    private String mstrPassword;

    public Connection getConnection() throws Exception {
        return this.mConnection;
    }

    public ConnectionFactory.GroupType groupType = ConnectionFactory.GroupType.ADMIN;

    public void open() throws Exception {
        groupType = ConnectionFactory.GroupType.ADMIN;
        if (this.mConnection == null || this.mConnection.isClosed()) {
            while (true) {
                try {
                    this.mConnection = ConnectionFactory.getConnection(this.groupType);

                    break;
                } catch (Exception ex) {
                    if (!ex.toString().contains("Closed Connection")) {
                        throw ex;
                    }
                }
            }
        }
    }

    public void close() {
        DbUtils.closeQuietly(this.mConnection);
        DbUtils.closeQuietly(this.mSCConnection);
    }

    public void close(ResultSet rs, PreparedStatement stmt) throws SQLException {
        close(rs);
        close(stmt);
    }

    public void close(PreparedStatement stmt, Connection connection) throws SQLException {
        close(stmt);
        close(connection);
    }

    public void close(Connection connection) throws SQLException {
        DbUtils.closeQuietly(connection);
    }

    public void close(PreparedStatement stmt) throws SQLException {
        DbUtils.closeQuietly(stmt);
    }

    public void close(Statement stmt) throws SQLException {
        DbUtils.closeQuietly(stmt);
    }

    public void close(ResultSet rs) throws SQLException {
        DbUtils.closeQuietly(rs);
    }

    public void close(Connection connection, PreparedStatement stmt, ResultSet rs) throws SQLException {
        close(rs);
        close(stmt);
        close(connection);
    }

    private void initAMConnection() {
        try {
            if (this.mConnection == null || this.mConnection.isClosed()) {
                this.mConnection = ConnectionFactory.getConnection(ConnectionFactory.GroupType.ADMIN);
            }
        } catch (Exception ex) {
            SystemLogger.getLogger().error(ex);
        }
    }

    private void initSCConnection() {
        try {
            if (mSCConnection == null || mSCConnection.isClosed()) {
                mSCConnection = ConnectionFactory.getConnection(ConnectionFactory.GroupType.SECONDARY);
            }

        } catch (Exception ex) {
            SystemLogger.getLogger().error(ex);
        }
    }

    private DataLogger getLog() throws Exception {
        if (this.groupType == ConnectionFactory.GroupType.ADMIN) {
            initAMConnection();
            if (this.log == null) {
                this.log = new DataLogger(this.mConnection);
            } else if (this.log.getConnection().isClosed()) {
                this.log.setConnection(this.mConnection);
            }
        } else {
            initSCConnection();
            if (this.log == null) {
                this.log = new DataLogger(this.mSCConnection);
            } else if (this.log.getConnection().isClosed()) {
                this.log.setConnection(this.mSCConnection);
            }
        }
        return this.log;
    }

    public void setLog(DataLogger log) {
        this.log = log;
    }

    public void setLogHeader(String objectId, String userId, String password, String clientIp) {
        this.mstrObjectId = objectId;
        this.mstrUserId = userId;
        this.mstrPassword = password;
        this.mstrClientIp = clientIp;
    }
}
