package telsoft.gateway.core.component;

import com.telsoft.database.Database;
import telsoft.gateway.commons.DBSynchronizable;
import telsoft.gateway.core.GatewayAuthenticator;
import telsoft.gateway.core.GatewayManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Title: Core Gateway System</p>
 *
 * <p>Description: A part of TELSOFT Gateway System</p>
 *
 * <p>Copyright: Copyright (c) 2009</p>
 *
 * <p>Company: TELSOFT</p>
 *
 * @author Nguyen Cong Khanh
 * @version 1.0
 */
public class DBUserManager extends GWComponent implements UserManager, DBSynchronizable {
    private String mstrCachePasswordExpireDuration = "0";
    private Map<String, UserInformation> mpUser = new HashMap<String, UserInformation>();
    private Map<String, UserInformation> mpUserID = new HashMap<String, UserInformation>();

    public DBUserManager(GatewayManager gatewayManager) {
        super(gatewayManager);
    }

    /**
     * getUser
     *
     * @param user_name String
     * @return UserInformation
     */
    public UserInformation getUser(String user_name) {
        user_name = user_name.toLowerCase();
        return mpUser.get(user_name);
    }


    public UserInformation getUserById(String user_id) {
        return mpUserID.get(user_id);
    }

    /**
     * @param connection Connection
     * @param _int       int
     * @throws Exception
     */
    public void syncData(Connection connection, int _int) throws Exception {
        Map<String, UserInformation> tempUser = new HashMap<String, UserInformation>();
        Map<String, UserInformation> tempUserID = new HashMap<String, UserInformation>();

// query user
        String strUserSql =
                " SELECT U.USER_ID, U.USER_NAME,U.PASSWORD,U.PRIORITY,U.EXPIRE_STATUS,U.MODIFIED_PASSWORD, " +
                        "       U.RIGHT_TYPE, U.AUTH_METHOD, U.EXPIRE_TIME " +
                        " FROM AM_USER U, AM_GROUP_USER UG, AM_GROUP G " +
                        " WHERE U.USER_ID = UG.USER_ID(+) " +
                        " AND UG.GROUP_ID = G.GROUP_ID(+) " +
                        " AND NVL(G.STATUS,1)>0 " +
                        " AND NVL(U.STATUS,1)>0 ";
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(strUserSql);
            while (rs.next()) {
                UserInformation ui = new UserInformation();
                ui.domain = getManager().getConfig().getParameter("ldap.domain");
                ui.host = getManager().getConfig().getParameter("ldap.host");

                ui.user_id = rs.getString(1);
                ui.user_name = rs.getString(2).toLowerCase();
                ui.password = rs.getString(3);
                ui.priority = rs.getInt(4);
                ui.expire_status = rs.getInt(5);
                ui.password_modified = rs.getDate(6);
                ui.right_type = rs.getInt(7);
                ui.authMethod = rs.getString(8);
                if (ui.right_type < 0 || ui.right_type > 2) {
                    ui.right_type = 0;
                }
                java.sql.Timestamp expireTime = rs.getTimestamp(9);
                if (expireTime != null)
                    ui.expiredTime = expireTime.getTime();
                else
                    ui.expiredTime = -1L;
                tempUser.put(ui.user_name, ui);
                tempUserID.put(ui.user_id, ui);
            }
        } finally {
            Database.closeObject(rs);
            Database.closeObject(stmt);
        }

        GatewayAuthenticator authenticator = new GatewayAuthenticator();
        authenticator.setConnection(connection);

        for (UserInformation ui : tempUser.values()) {
            ui.ip_list = authenticator.queryIPData(ui.user_id);
            ui.access_time_list = authenticator.queryScheduleData(ui.user_id);
            ui.sessionCount = -1;
            ui.sessionPerGateway = -1;
            ui.module = authenticator.queryModuleData(ui.user_id);
        }
        mpUser = tempUser;
        mpUserID = tempUserID;

        mstrCachePasswordExpireDuration = authenticator.getPolicy("PASSWORD_EXPIRE_DURATION");
    }

    public String getPasswordExpireDuration() {
        return mstrCachePasswordExpireDuration;
    }
}
