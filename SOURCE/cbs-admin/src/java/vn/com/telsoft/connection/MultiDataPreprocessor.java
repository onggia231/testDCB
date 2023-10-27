package vn.com.telsoft.connection;

import java.sql.Connection;

public class MultiDataPreprocessor extends DataPreprocessor {
    public MultiDataPreprocessor() {
        this.groupType = ConnectionFactory.GroupType.ADMIN;
    }

    public Connection getConnection() throws Exception {
        return this.mConnection;
    }

    public Connection getSCConnection() throws Exception {
        return this.mSCConnection;
    }

    public void open() throws Exception {
        groupType = ConnectionFactory.GroupType.ADMIN;
        if (this.mConnection == null || this.mConnection.isClosed()) {
            while (true) {
                try {
                    this.mConnection = ConnectionFactory.getConnection(ConnectionFactory.GroupType.ADMIN);
                    break;
                } catch (Exception var2) {
                    if (!var2.toString().contains("Closed Connection")) {
                        throw var2;
                    }
                }
            }
        }
    }

    public void openSCConnection() throws Exception {
        groupType = ConnectionFactory.GroupType.SECONDARY;
        if (this.mSCConnection == null || this.mSCConnection.isClosed()) {
            while (true) {
                try {
                    this.mSCConnection = ConnectionFactory.getConnection(ConnectionFactory.GroupType.SECONDARY);
                    break;
                } catch (Exception var2) {
                    if (!var2.toString().contains("Closed Connection")) {
                        throw var2;
                    }
                }
            }
        }
    }
}
