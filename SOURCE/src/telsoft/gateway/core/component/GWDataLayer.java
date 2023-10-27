package telsoft.gateway.core.component;

import com.telsoft.database.Database;
import com.telsoft.thread.ThreadInfo;
import com.telsoft.thread.ThreadLister;
import com.telsoft.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import telsoft.gateway.commons.ServerInfo;
import telsoft.gateway.commons.ServerTypeInfo;
import telsoft.gateway.core.GatewayAuthenticator;
import telsoft.gateway.core.GatewayManager;
import telsoft.gateway.core.gw.CommandRecord;
import telsoft.gateway.core.gw.Gateway;
import telsoft.gateway.core.gw.UserRight;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p>Title: Core Gateway System</p>
 * <p/>
 * <p>Description: A part of TELSOFT Gateway System</p>
 * <p/>
 * <p>Copyright: Copyright (c) 2009</p>
 * <p/>
 * <p>Company: TELSOFT</p>
 *
 * @author Nguyen Cong Khanh
 * @version 1.0
 */
public class GWDataLayer extends GWComponent {
    private static final Logger log = LoggerFactory.getLogger(GWDataLayer.class);
    private Connection mCn;
    private GatewayAuthenticator authenticator = new GatewayAuthenticator();
    private String group;

    /**
     * @param gateway GatewayManager
     */
    public GWDataLayer(GatewayManager gateway, String group) {
        super(gateway);
        this.group = group;
    }

    /**
     * @param cn         Connection
     * @param strGroupID String
     * @return Vector
     * @throws Exception
     */

    private static Vector queryChildGroup(Connection cn, String strGroupID) throws Exception {
        Vector vtGroup = new Vector();
        String strSQL = "SELECT GROUP_ID FROM AM_GROUP WHERE PARENT_ID=" + strGroupID;
        Vector vt = Database.executeQuery(cn, strSQL);
        for (int iIndex = 0; iIndex < vt.size(); iIndex++) {
            Vector vtRow = (Vector) vt.elementAt(iIndex);
            strGroupID = StringUtil.nvl(vtRow.elementAt(0), "");
            vtGroup.add(strGroupID);
            vtGroup.addAll(queryChildGroup(cn, strGroupID));
        }
        return vtGroup;
    }

    /**
     * @throws Exception
     */
    public void init() throws Exception {
        if (mCn == null) {
            mCn = getManager().getConnection(group);
            log.debug("init Connection: {}/{}", mCn.hashCode(), mCn.getWarnings());
        }
    }

    /**
     * @return GatewayAuthenticator
     */
    public GatewayAuthenticator getAuthenticator() {
        if (authenticator.getConnection() == null && mCn != null) {
            authenticator.setConnection(mCn);
        }
        return authenticator;
    }

    /**
     *
     */
    public void free() {
        if (mCn != null) {
            try {
                log.debug("free Connection: {}/{}", mCn.hashCode(), mCn.getWarnings());
            } catch (SQLException ex) {
                log.debug("free Connection: {}/{}", mCn.hashCode(), "Failed to get warning");
            }
        }
        Database.closeObject(mCn);
        mCn = null;
        authenticator = null;
    }

    /**
     * @param strUserID String
     * @throws Exception
     */
    public void syncUserPriority(String strUserID) throws Exception {
        // Get priority
        String strSQL = "SELECT PRIORITY FROM AM_USER WHERE USER_ID=?";
        PreparedStatement stmt = null;
        try {
            stmt = mCn.prepareStatement(strSQL);
            stmt.setString(1, strUserID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                getManager().setUserPriority(strUserID, rs.getInt(1));
            }
            rs.close();
        } finally {
            Database.closeObject(stmt);
        }
    }

    /**
     * @return Vector
     * @throws Exception
     */
    public Vector<ServerTypeInfo> loadServerTypeInfo() throws Exception {
        return new Vector<ServerTypeInfo>();
    }

    /**
     * @return Vector
     * @throws Exception
     */
    public Vector<ServerInfo> loadServerInfo() throws Exception {
        return new Vector();
    }

    /**
     * @return Connection
     */
    public Connection getConnection() {
        return mCn;
    }

    /**
     * @param cn Connection
     */
    public void setConnection(Connection cn) {
        free();
        mCn = cn;
    }

    /**
     * @throws Exception
     */
    public void updateUnclosedSession() throws Exception {
    }

    /**
     * @param lister ThreadLister
     * @return Vector
     * @throws Exception
     */
    public List<ThreadInfo> getGatewayDispatcherThreads(ThreadLister lister) throws Exception {
        return null;
    }

    /**
     * @param strGatewayId String
     * @return String
     * @throws Exception
     */
    public String getGatewayClass(String strGatewayId) throws Exception {
        return "-1";
    }

    /**
     * @param strDispatcherId String
     * @return String
     * @throws Exception
     */
    public String getDispatcherClass(String strDispatcherId) throws Exception {
        return "-1";
    }

    /**
     * @param strGatewayId String
     * @return Map
     * @throws Exception
     */
    public Map<String, CommandRecord> getCommandRight(String strGatewayId) throws Exception {
        return new HashMap<>();
    }

    /**
     * @param strGatewayId String
     * @return Vector
     * @throws Exception
     */
    public Vector<String> getUserGranted(String strGatewayId) throws Exception {
        return new Vector<String>();
    }

    /**
     * @param strUserID String
     * @param threadID  String
     * @return UserRight
     * @throws Exception
     */
    public UserRight queryUserRight(String strUserID, String threadID) throws Exception {
        UserRight userright = new UserRight();

        // Get group list
        Vector vtGroup = getAuthenticator().queryGroupList(strUserID, 1);
        userright.mvtGrantedGroup = new CopyOnWriteArrayList();
        for (int iIndex = 0; iIndex < vtGroup.size(); iIndex++) {
            Vector vtRow = (Vector) vtGroup.elementAt(iIndex);
            userright.mvtGrantedGroup.add(vtRow.elementAt(0));
        }

        userright.mvtGrantedCommand = new CopyOnWriteArrayList();
        userright.mvtGrantedServer = new CopyOnWriteArrayList();
        userright.mvtGrantedKey = new HashMap();

        userright.miMillisecPerCommand = -1;
        userright.miTps = -1;
        userright.miWarningTps = -1;
        userright.miOverloadAction = Gateway.OVERLOAD_ACTION.NONE;
        userright.mpExtPolicy = new HashMap<String, String>();
        userright.mvtGrantedDspType = new ArrayList();

        return userright;
    }

    /**
     * @param strDispatcherId String
     * @return Map
     * @throws Exception
     */
    public Map<String, String> getServerDispatcher(String strDispatcherId) throws Exception {
        return new LinkedHashMap<String, String>();
    }
}
