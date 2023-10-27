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
import vn.com.telsoft.util.ConnUtil;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * @author CHIENDX, TELSOFT
 */
public class AppModel extends AMDataPreprocessor implements Serializable {

    public void delete(List<AppGUI> listApp) throws Exception {

        try {
            open();

            String strSQL = "DELETE FROM am_app WHERE app_id=?";
            mConnection.setAutoCommit(false);

            //Delete app
            for (AppGUI appGUI : listApp) {
                logBeforeDelete("am_app", "app_id=" + appGUI.getAppId());
                mStmt = mConnection.prepareStatement(strSQL);
                mStmt.setLong(1, appGUI.getAppId());
                mStmt.execute();
            }

            //Commit
            mConnection.commit();

        } catch (Exception ex) {
            ConnUtil.rollback(mConnection);
            SystemLogger.getLogger().error(ex);
            throw ex;

        } finally {
            mConnection.setAutoCommit(true);
            close(mStmt);
            close(mConnection);
        }
    }

    public long add(AppGUI app) throws Exception {
        long idFromSequence;

        try {
            open();
            mConnection.setAutoCommit(false);

            //Update app
            idFromSequence = SQLUtil.getSequenceValue(mConnection, "SEQ_AM_APP_ID");
            app.setAppId(idFromSequence);

            String strSQL = "INSERT INTO am_app(app_id, name, code, status, ord) VALUES(?,?,?,?,?)";
            mStmt = mConnection.prepareStatement(strSQL);
            mStmt.setLong(1, idFromSequence);
            mStmt.setString(2, app.getName());
            mStmt.setString(3, app.getCode());
            mStmt.setString(4, app.getStatus());
            mStmt.setLong(5, app.getOrd());
            mStmt.execute();
            logAfterInsert("am_app", "app_id=" + idFromSequence);

            //Done
            mConnection.commit();

        } catch (Exception ex) {
            ConnUtil.rollback(mConnection);
            SystemLogger.getLogger().error(ex);
            throw ex;

        } finally {
            mConnection.setAutoCommit(true);
            close(mStmt);
            close();
        }

        return idFromSequence;
    }

    public void edit(AppGUI app) throws Exception {

        try {
            open();
            mConnection.setAutoCommit(false);

            //Update app
            List listChange = logBeforeUpdate("am_app", "app_id=" + app.getAppId());
            String strSQL = "UPDATE am_app SET name=?, code=?, status=?, ord=? WHERE app_id=?";
            mStmt = mConnection.prepareStatement(strSQL);
            mStmt.setString(1, app.getName());
            mStmt.setString(2, app.getCode());
            mStmt.setString(3, app.getStatus());
            mStmt.setLong(4, app.getOrd());
            mStmt.setLong(5, app.getAppId());
            mStmt.execute();
            logAfterUpdate(listChange);
            close(mStmt);

            //Commit
            mConnection.commit();

        } catch (Exception ex) {
            ConnUtil.rollback(mConnection);
            SystemLogger.getLogger().error(ex);
            throw ex;

        } finally {
            mConnection.setAutoCommit(true);
            close(mStmt);
            close();
        }
    }

    public List<AppGUI> getListApp() throws Exception {
        List<AppGUI> listReturn = new ArrayList<>();

        try {
            open();
            String strSQL = "SELECT a.app_id, a.code, a.name, a.status, a.ord FROM am_app a ORDER BY a.ord ASC";
            mStmt = mConnection.prepareStatement(strSQL);
            mRs = mStmt.executeQuery();

            while (mRs.next()) {
                AppGUI tmpAppGUI = new AppGUI();
                tmpAppGUI.setAppId(mRs.getLong(1));
                tmpAppGUI.setCode(mRs.getString(2));
                tmpAppGUI.setName(mRs.getString(3));
                tmpAppGUI.setStatus(mRs.getString(4));
                tmpAppGUI.setOrd(mRs.getLong(5));
                listReturn.add(tmpAppGUI);
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

    public List<AppGUI> getListAppObj(String strAppCode) throws Exception {

        if (Config.isWSEnabled()) {
            return ClientRequestProcessor.getListAppGUI(strAppCode);
        }

        List<AppGUI> listReturn = new ArrayList<>();

        PreparedStatement stmt = null;
        ResultSet rs = null;

        PreparedStatement stmt2 = null;
        ResultSet rs2 = null;

        try {
            open();

            String strIn = "";
            String[] arrAppCode = strAppCode.split(",");
            for (String strCode : arrAppCode) {
                if (!strAppCode.isEmpty()) {
                    strIn = strIn + "'" + strCode.trim() + "',";
                }
            }
            strIn = StringUtil.removeLastChar(strIn);

            String strSQL = "SELECT a.app_id, a.code, a.name, a.status "
                    + "FROM am_app a WHERE a.code IN(" + strIn + ")"
                    + "AND a.status=1 ORDER BY a.ord ASC";

            String strSQL2 = "  SELECT a.obj_id,\n" +
                    "         b.name,\n" +
                    "         b.status,\n" +
                    "         b.parent_id,\n" +
                    "         b.PATH,\n" +
                    "         b.obj_type,\n" +
                    "         b.ord,\n" +
                    "         b.description,\n" +
                    "         b.is_render,\n" +
                    "         b.icon\n" +
                    "    FROM am_app_obj a, am_object b\n" +
                    "   WHERE a.app_id = ? AND a.obj_id = b.object_id\n" +
                    "ORDER BY b.ord ASC";

            stmt = mConnection.prepareStatement(strSQL);
            stmt2 = mConnection.prepareStatement(strSQL2);
            rs = stmt.executeQuery();

            while (rs.next()) {
                AppGUI tmpAppGUI = new AppGUI();
                tmpAppGUI.setAppId(rs.getLong(1));
                tmpAppGUI.setCode(rs.getString(2));
                tmpAppGUI.setName(rs.getString(3));
                tmpAppGUI.setStatus(rs.getString(4));

                //Get object
                stmt2.setLong(1, tmpAppGUI.getAppId());
                rs2 = stmt2.executeQuery();
                while (rs2.next()) {
                    ModuleGUI tmpModuleGUI = new ModuleGUI();
                    tmpModuleGUI.setObjectId(rs2.getLong(1));
                    tmpModuleGUI.setName(rs2.getString(2));
                    tmpModuleGUI.setStatus(rs2.getLong(3));
                    tmpModuleGUI.setParentId(rs2.getLong(4));
                    tmpModuleGUI.setPath(rs2.getString(5));
                    tmpModuleGUI.setObjType(rs2.getString(6));
                    tmpModuleGUI.setOrder(rs2.getLong(7));
                    tmpModuleGUI.setDescription(rs2.getString(8));
                    tmpModuleGUI.setRender("1".equals(rs2.getString(9)));
                    tmpModuleGUI.setIcon(rs2.getString(10));
                    tmpAppGUI.getMlistModule().add(tmpModuleGUI);
                }

                listReturn.add(tmpAppGUI);
            }

        } catch (Exception ex) {
            SystemLogger.getLogger().error(ex);
            throw ex;

        } finally {
            close(rs);
            close(stmt);
            close(rs2);
            close(stmt2);
            close();
        }

        if (listReturn.isEmpty()) {
            SystemLogger.getLogger().error(strAppCode + " have no module!!!");
        }

        return listReturn;
    }

    public List<ModuleGUI> getListObj(long appId) throws Exception {
        List<ModuleGUI> listReturn = new ArrayList<>();

        try {
            open();

            String strSQL2 = "  SELECT a.obj_id,\n" +
                    "         b.name,\n" +
                    "         b.status,\n" +
                    "         b.parent_id,\n" +
                    "         b.PATH,\n" +
                    "         b.obj_type,\n" +
                    "         b.ord,\n" +
                    "         b.description,\n" +
                    "         b.is_render,\n" +
                    "         b.icon\n" +
                    "    FROM am_app_obj a, am_object b\n" +
                    "   WHERE a.app_id = ? AND a.obj_id = b.object_id\n" +
                    "ORDER BY b.ord ASC";

            mStmt = mConnection.prepareStatement(strSQL2);
            mStmt.setLong(1, appId);
            mRs = mStmt.executeQuery();

            while (mRs.next()) {
                ModuleGUI tmpModuleGUI = new ModuleGUI();
                tmpModuleGUI.setObjectId(mRs.getLong(1));
                tmpModuleGUI.setName(mRs.getString(2));
                tmpModuleGUI.setStatus(mRs.getLong(3));
                tmpModuleGUI.setParentId(mRs.getLong(4));
                tmpModuleGUI.setPath(mRs.getString(5));
                tmpModuleGUI.setObjType(mRs.getString(6));
                tmpModuleGUI.setOrder(mRs.getLong(7));
                tmpModuleGUI.setDescription(mRs.getString(8));
                tmpModuleGUI.setRender("1".equals(mRs.getString(9)));
                tmpModuleGUI.setIcon(mRs.getString(10));
                listReturn.add(tmpModuleGUI);
            }

        } finally {
            close(mConnection, mStmt, mRs);
        }

        return listReturn;
    }

    public List<AppGUI> getListAll() throws Exception {
        List<AppGUI> listReturn = new ArrayList<>();
        List<AccessRight> listAcessRight = new ArrayList<>();

        PreparedStatement stmt = null;
        ResultSet rs = null;

        PreparedStatement stmt2 = null;
        ResultSet rs2 = null;

        ResultSet rsRight = null;
        PreparedStatement stmtRight = null;

        ResultSet rsApp = null;
        PreparedStatement stmtApp = null;

        try {
            open();
            //Get list access right
            String StrSQL = "SELECT code, name FROM am_right ORDER BY code";
            stmt = mConnection.prepareStatement(StrSQL);
            rs = stmt.executeQuery();
            while (rs.next()) {
                AccessRight tmp = new AccessRight();
                tmp.setRightCode(rs.getString(1));
                tmp.setName(rs.getString(2));
                tmp.setAccess(0);
                tmp.setActive(false);
                listAcessRight.add(tmp);
            }
            close(rs);
            close(stmt);

            //Get apps
            String strSQL = "SELECT a.app_id, a.code, a.name, a.status FROM am_app a ORDER BY a.ord ASC";
            String strSQL2 = "" +
                    "SELECT a.obj_id, b.name, b.status, b.parent_id, " +
                    "       b.path, b.obj_type, b.ord, b.description, " +
                    "       b.is_render, b.icon " +
                    "FROM am_app_obj a, am_object b " +
                    "WHERE a.app_id = ? " +
                    "   AND a.obj_id = b.object_id " +
                    "ORDER BY b.ord ASC";
            String strSqlRight = "SELECT a.right_code, a.access_type FROM am_object_right a WHERE a.object_id=?";
            String strSqlApp = "SELECT a.app_id FROM am_app_obj a, am_app b WHERE a.obj_id = ? AND a.app_id = b.app_id";

            stmt = mConnection.prepareStatement(strSQL);
            stmt2 = mConnection.prepareStatement(strSQL2);
            stmtRight = mConnection.prepareStatement(strSqlRight);
            stmtApp = mConnection.prepareStatement(strSqlApp);
            rs = stmt.executeQuery();

            while (rs.next()) {
                AppGUI tmpAppGUI = new AppGUI();
                tmpAppGUI.setAppId(rs.getLong(1));
                tmpAppGUI.setCode(rs.getString(2));
                tmpAppGUI.setName(rs.getString(3));
                tmpAppGUI.setStatus(rs.getString(4));

                stmt2.setLong(1, tmpAppGUI.getAppId());
                rs2 = stmt2.executeQuery();
                while (rs2.next()) {
                    ModuleGUI tmpModuleGUI = new ModuleGUI();
                    tmpModuleGUI.setObjectId(rs2.getLong(1));
                    tmpModuleGUI.setName(rs2.getString(2));
                    tmpModuleGUI.setStatus(rs2.getLong(3));
                    tmpModuleGUI.setParentId(rs2.getLong(4));
                    tmpModuleGUI.setPath(rs2.getString(5));
                    tmpModuleGUI.setObjType(rs2.getString(6));
                    tmpModuleGUI.setOrder(rs2.getLong(7));
                    tmpModuleGUI.setDescription(rs2.getString(8));
                    tmpModuleGUI.setRender("1".equals(rs2.getString(9)));
                    tmpModuleGUI.setIcon(rs2.getString(10));

                    if (tmpModuleGUI.getParentId() == 0) {
                        tmpModuleGUI.setParentAppId(tmpAppGUI.getAppId() * -1);
                    } else {
                        tmpModuleGUI.setParentAppId(tmpModuleGUI.getParentId());
                    }

                    //Get object right
                    List<AccessRight> tmpListAcessRight = new ArrayList<>();
                    for (AccessRight accessRight : listAcessRight) {
                        tmpListAcessRight.add(new AccessRight(accessRight));
                    }
                    tmpModuleGUI.setListAccessRight(tmpListAcessRight);

                    stmtRight.setLong(1, tmpModuleGUI.getObjectId());
                    rsRight = stmtRight.executeQuery();
                    while (rsRight.next()) {
                        for (AccessRight accessRight : tmpListAcessRight) {
                            if (rsRight.getString(1).equals(accessRight.getRightCode())) {
                                accessRight.setActive(true);
                                accessRight.setAccess(rsRight.getInt(2));
                            }
                        }
                    }
                    close(rsRight);

                    //Get module app
                    stmtApp.setLong(1, tmpModuleGUI.getObjectId());
                    rsApp = stmtApp.executeQuery();
                    while (rsApp.next()) {
                        AppGUI tmpApp = new AppGUI();
                        tmpApp.setAppId(rsApp.getLong(1));
                        tmpModuleGUI.setApp(tmpApp);
                    }
                    close(rsApp);

                    //Add to tmpApp
                    tmpAppGUI.getMlistModule().add(tmpModuleGUI);
                }

                listReturn.add(tmpAppGUI);
            }

        } catch (Exception ex) {
            SystemLogger.getLogger().error(ex);
            throw ex;

        } finally {
            close(rsApp);
            close(stmtApp);
            close(rsRight);
            close(stmtRight);
            close(rs2);
            close(stmt2);
            close(rs);
            close(stmt);
            close();
        }

        return listReturn;
    }
}
