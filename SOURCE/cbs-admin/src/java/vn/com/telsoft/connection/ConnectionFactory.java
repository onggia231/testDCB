package vn.com.telsoft.connection;

import com.faplib.lib.SystemConfig;
import com.faplib.util.StringUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.Serializable;
import java.sql.Connection;

public class ConnectionFactory implements Serializable {

    //AMData
    private static HikariDataSource amDataSource;
    private static HikariConfig amConfig;

    //SCData
    private static HikariDataSource scDataSource;
    private static HikariConfig scConfig;

    public enum GroupType {
        ADMIN, SECONDARY
    }

    private static void getAMConnectConfig() throws Exception {
        //Main connection
        String url = SystemConfig.getConfig("AMDBUrl");
        String userName = SystemConfig.getConfig("AMDBUser");
        String password = StringUtil.telsoftDecoder(SystemConfig.getConfig("AMDBPass"));
        int maxPoolSize = Integer.parseInt(StringUtil.evl(SystemConfig.getConfig("AMDBMaximumPoolSize"), "5"));
        int minimumIdle = Integer.parseInt(StringUtil.evl(SystemConfig.getConfig("AMDBMinimumIdle"), "1"));
        long maxLifetime = Long.parseLong(StringUtil.evl(SystemConfig.getConfig("AMDBMaxLifetime"), "3600000"));
        long idleTimeout = Long.parseLong(StringUtil.evl(SystemConfig.getConfig("AMDBIdleTimeout"), "3600000"));
        long connectionTimeout = Long.parseLong(StringUtil.evl(SystemConfig.getConfig("AMDBConnectionTimeout"), "15000"));
        String driver = SystemConfig.getConfig("AMDBDriver");

        //Create configs
        amConfig = new HikariConfig();
        createConfig(url, userName, password, maxPoolSize, minimumIdle, maxLifetime, idleTimeout, connectionTimeout, driver, amConfig);
    }

    private static void getSCConnectConfig() throws Exception {
        String url = SystemConfig.getConfig("SCDBUrl");
        String userName = SystemConfig.getConfig("SCDBUser");
        String password = StringUtil.telsoftDecoder(SystemConfig.getConfig("SCDBPass"));
        int maxPoolSize = Integer.parseInt(StringUtil.evl(SystemConfig.getConfig("SCDBMaximumPoolSize"), "5"));
        int minimumIdle = Integer.parseInt(StringUtil.evl(SystemConfig.getConfig("SCDBMinimumIdle"), "1"));
        long maxLifetime = Long.parseLong(StringUtil.evl(SystemConfig.getConfig("SCDBMaxLifetime"), "3600000"));
        long idleTimeout = Long.parseLong(StringUtil.evl(SystemConfig.getConfig("SCDBIdleTimeout"), "3600000"));
        long connectionTimeout = Long.parseLong(StringUtil.evl(SystemConfig.getConfig("SCDBConnectionTimeout"), "15000"));
        String driver = SystemConfig.getConfig("SCDBDriver");

        //Create configs
        scConfig = new HikariConfig();
        createConfig(url, userName, password, maxPoolSize, minimumIdle, maxLifetime, idleTimeout, connectionTimeout, driver, scConfig);
    }

    private static void createConfig(String url, String userName, String password, int maxPoolSize, int minimumIdle, long maxLifetime, long idleTimeout, long connectionTimeout, String driver, HikariConfig config) {
        config.setDriverClassName(driver);
        config.setJdbcUrl(url);
        config.setUsername(userName);
        config.setPassword(password);
        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(minimumIdle);
        config.setMaxLifetime(maxLifetime);
        config.setIdleTimeout(idleTimeout);
        config.setConnectionTimeout(connectionTimeout);
    }

    public static Connection getConnection(GroupType groupType) throws Exception {
        switch (groupType) {
            case ADMIN:
                getAMConnectConfig();
                if (amDataSource == null) {
                    amDataSource = new HikariDataSource(amConfig);
                }
                return amDataSource.getConnection();
            case SECONDARY:
                getSCConnectConfig();
                if (scDataSource == null) {
                    scDataSource = new HikariDataSource(scConfig);
                }
                return scDataSource.getConnection();
        }
        return null;
    }
}
