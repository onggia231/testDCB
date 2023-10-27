package com.telsoft.cbs.module.sms;

import com.telsoft.cbs.module.sms.handler.CommandContext;
import com.telsoft.cbs.module.sms.handler.CommandProcessor;
import com.telsoft.database.Database;
import com.telsoft.thread.ParameterType;
import com.telsoft.util.AppException;
import telsoft.gateway.DBParameter;
import telsoft.gateway.commons.GWUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

/**
 * Read MO_QUEUE
 * Process message
 * Write MT_QUEUE
 */

public class DbSmsProcessor extends SmsProcessor {
    private CommandProcessor commandProcessor;
    private String definitionScript;
    private DBParameter db = new DBParameter();

    @Override
    public Vector getParameterDefinition() {
        Vector vtReturn = new Vector();
        db.getParameterDefinition(this, vtReturn);
        vtReturn.add(createParameter("Definition", "", ParameterType.PARAM_TEXTAREA_MAX, "99999"));
        vtReturn.addAll(super.getParameterDefinition());
        return vtReturn;
    }

    @Override
    public void fillParameter() throws AppException {
        super.fillParameter();
        db.fillParameter(this);
        definitionScript = loadString("Definition");
    }

    @Override
    protected void beforeSession() throws Exception {
        super.beforeSession();
        db.initPool(this);
        commandProcessor = new CommandProcessor();
        commandProcessor.configure(definitionScript);
        commandProcessor.start();
    }

    @Override
    protected void afterSession() throws Exception {
        super.afterSession();
        db.closePool(this);
        commandProcessor.stop();
        commandProcessor = null;
    }

    @Override
    protected void processMessages() {
        Connection cn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            cn = db.getDBConnection();
            String sql = "SELECT ID, USER_ID, CHANNEL_TYPE, INFO, RECEIVE_DATE,SERVICE_ID,REQUEST_ID,SESSION_ID FROM mo_queue;";
            stmt = cn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                SmsMessage message = new SmsMessage();
                processMessage(message);
                rs.deleteRow();
            }
        } catch (Exception ex) {
            logMonitor(GWUtil.decodeException(ex));
        } finally {
            Database.closeObject(rs);
            Database.closeObject(stmt);
            Database.closeObject(cn);
        }
    }

    @Override
    protected void processMessage(SmsMessage message) {
        CommandContext commandContext = new CommandContext(message);
        commandProcessor.process(commandContext);
    }
}
