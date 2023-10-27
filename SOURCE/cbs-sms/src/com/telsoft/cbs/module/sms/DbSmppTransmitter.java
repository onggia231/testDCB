package com.telsoft.cbs.module.sms;

import com.telsoft.database.Database;
import com.telsoft.util.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import telsoft.gateway.DBParameter;
import telsoft.gateway.commons.GWUtil;

import java.sql.*;
import java.util.Vector;

/**
 * Created by khanhnc on 1/25/16.
 */
public class DbSmppTransmitter extends SmsTransmitter {
    private static final Logger log = LoggerFactory.getLogger(DbSmppTransmitter.class);
    private DBParameter db = new DBParameter();


    public DbSmppTransmitter() {
        super();
    }

    public void start() throws Exception {
        db.initPool(this);
        super.start();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        db.closePool(this);
    }

    @Override
    protected void messageIncomming(SmsMessage message) {
        Connection cn = null;
        PreparedStatement stmt = null;
        try {
            cn = db.getDBConnection();
            String id = "";//Util.generateRequestId(message.getOriginator(), null);

            String sqlUpdate = "INSERT INTO mo_queue( " +
                    "                    USER_ID, " +
                    "                    SERVICE_ID, " +
                    "                    MOBILE_OPERATOR, " +
                    "                    COMMAND_CODE, " +
                    "                    INFO, " +
                    "                    REQUEST_ID, " +
                    "                    CHANNEL_TYPE) " +
                    "            VALUES (?,?,?,?,?,?,?);";
            stmt = cn.prepareStatement(sqlUpdate);
            stmt.setString(1, "84" + message.getOriginator());
            stmt.setString(2, "service_id");
            stmt.setString(3, "VMS");
            stmt.setString(4, "LIXI");
            stmt.setString(5, message.getContent());
            stmt.setString(6, id);
//            switch (type) {
//                case "SMS":
//                    stmt.setString(7, "0");
//                    break;
//                case "USSD":
//                    stmt.setString(7, "6");
//                    break;
//                default:
//                    stmt.setString(7, "0");
//            }

            if (stmt.executeUpdate() == 1) {
                logMonitor("process_id");
            } else {
                logMonitor("cannot insert process_id");
            }
            cn.commit();
        } catch (Exception e) {
            logMonitor(GWUtil.decodeException(e));
        } finally {
            Database.closeObject(stmt);
            Database.closeObject(cn);
        }
    }

    @Override
    protected void processOutgoingMessage() {
        Connection cn = null;
        Statement stmt = null;
        ResultSet rs = null;
        PreparedStatement stmtLog = null;
        try {
            cn = db.getDBConnection();
            String sql = "SELECT " +
                    "   ID, " +
                    "   USER_ID, " +
                    "   INFO, " +
                    "   RETRIES_NUM," +
                    "   SERVICE_ID," +
                    "   REQUEST_ID," +
                    "   SUBMIT_DATE," +
                    "   DONE_DATE," +
                    "   SESSION_ID," +
                    "   MT_TYPE " +
                    "FROM mt_queue WHERE RETRIES_NUM <= 3 and (SUBMIT_DATE is null or SUBMIT_DATE <= now());";
            stmt = cn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

            String sqlUpdate = "INSERT INTO mt( " +
                    "                    USER_ID, " +
                    "                    SERVICE_ID, " +
                    "                    MOBILE_OPERATOR, " +
                    "                    COMMAND_CODE, " +
                    "                    INFO, " +
                    "                    REQUEST_ID, " +
                    "                    CHANNEL_TYPE," +
                    "                    MESSAGE_TYPE," +
                    "                    PROCESS_RESULT," +
                    "                    SUBMIT_DATE," +
                    "                    DONE_DATE," +
                    "                    SESSION_ID," +
                    "                    MTTYPE " +
                    ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?);";
            stmtLog = cn.prepareStatement(sqlUpdate);

            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                SmsMessage smsMessage = new SmsMessage();
                smsMessage.setReceiver(rs.getString(2));
                smsMessage.setContent(rs.getString(3));
                smsMessage.setOriginator(rs.getString(5));
                int retries = rs.getInt(4);
                boolean success = retries < 3;

                if (retries >= 3 || sendMessage(smsMessage)) {
                    {
                        stmtLog.setString(1, rs.getString(2));
                        stmtLog.setString(2, rs.getString(5));
                        stmtLog.setString(3, "VMS");
                        stmtLog.setString(4, "LIXI");
                        stmtLog.setString(5, rs.getString(3));
                        stmtLog.setString(6, rs.getString(6));
                        stmtLog.setString(7, "0");
                        stmtLog.setInt(8, 0);
                        stmtLog.setInt(9, (success ? 0 : 1));
                        stmtLog.setTimestamp(10, retries > 0 ? rs.getTimestamp(7) : new Timestamp(System.currentTimeMillis()));
                        stmtLog.setTimestamp(11, new Timestamp(System.currentTimeMillis()));
                        stmtLog.setString(12, rs.getString(9));
                        stmtLog.setString(13, rs.getString(10));
                        stmtLog.executeUpdate();
                        cn.commit();
                    }
                    rs.deleteRow();
                } else {
                    rs.updateInt(4, rs.getInt(4) + 1);
                    if (retries == 0) {
                        rs.updateTimestamp(7, new Timestamp(System.currentTimeMillis()));
                    }
                    rs.updateRow();
                }
            }
        } catch (Exception ex) {
            logMonitor(GWUtil.decodeException(ex));
        } finally {
            Database.closeObject(rs);
            Database.closeObject(stmt);
            Database.closeObject(stmtLog);
            Database.closeObject(cn);
        }
    }

    @Override
    public Vector getParameterDefinition() {
        Vector vtReturn = new Vector();
        db.getParameterDefinition(this, vtReturn);
        vtReturn.addAll(super.getParameterDefinition());
        return vtReturn;
    }

    @Override
    public void fillParameter() throws AppException {
        super.fillParameter();
        db.fillParameter(this);
    }
}
