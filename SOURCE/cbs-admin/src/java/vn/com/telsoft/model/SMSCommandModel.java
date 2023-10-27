/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.com.telsoft.model;

import com.faplib.lib.SystemLogger;
import com.faplib.lib.admin.data.AMDataPreprocessor;
import com.faplib.lib.util.SQLUtil;
import vn.com.telsoft.entity.SMSCommand;
import vn.com.telsoft.util.ConnUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author
 */
public class SMSCommandModel extends AMDataPreprocessor implements Serializable {
    private static final long serialVersionUID = -6700953055597768781L;

    public List<SMSCommand> getAll() throws Exception {
        List<SMSCommand> returnValue = new ArrayList<SMSCommand>();

        String strSQL = "SELECT CMD_ID,CMD_CODE,CMD_TYPE,CMD_MSG_CONTENT,CMD_PARAM_COUNT,DESCRIPTION,CMD_REGEX,STATUS" +
                " FROM CB_SMS_COMMAND " +
                "ORDER BY cmd_code";
        try {
            open();
            mStmt = mConnection.prepareStatement(strSQL);
            mRs = mStmt.executeQuery();
            while (mRs.next()) {
                SMSCommand tmp = new SMSCommand();
                tmp.setCmdId(mRs.getLong(1));
                tmp.setCmdCode(mRs.getString(2));
                tmp.setCmdType(mRs.getString(3));
                tmp.setCmdMsgContent(mRs.getString(4));
                tmp.setCmdParamCount(mRs.getLong(5));
                tmp.setDescription(mRs.getString(6));
                tmp.setCmdRegex(mRs.getString(7));
                tmp.setStatus(mRs.getString(8));
                returnValue.add(tmp);
            }
        } catch (Exception ex) {
            SystemLogger.getLogger().error(ex);
            throw ex;
        } finally {
            close(mRs);
            close(mStmt);
            close();
        }
        return returnValue;
    }

    public long add(SMSCommand ett) throws Exception {
        long idFromSequence;
        try {
            this.open();
            mConnection.setAutoCommit(false);
            idFromSequence = SQLUtil.getSequenceValue(this.mConnection, "CB_SMS_COMMAND_SEQ");
            ett.setCmdId(idFromSequence);
            String strSQL = "INSERT INTO CB_SMS_COMMAND (CMD_ID, CMD_CODE, CMD_TYPE, CMD_MSG_CONTENT, CMD_PARAM_COUNT,CMD_REGEX, DESCRIPTION,STATUS) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            mStmt = mConnection.prepareStatement(strSQL);
            mStmt.setLong(1, idFromSequence);
            mStmt.setString(2, ett.getCmdCode().trim());
            mStmt.setString(3, ett.getCmdType());
            mStmt.setString(4, ett.getCmdMsgContent().trim());
            mStmt.setLong(5, ett.getCmdParamCount());
            mStmt.setString(6, ett.getCmdRegex().trim());
            mStmt.setString(7, ett.getDescription().trim());
            mStmt.setString(8, ett.getStatus());
            mStmt.execute();
            logAfterInsert("CB_SMS_COMMAND", "CMD_ID=" + idFromSequence);
            mConnection.commit();
        } catch (Exception var8) {
            ConnUtil.rollback(mConnection);
            throw var8;
        } finally {
            mConnection.setAutoCommit(true);
            this.close(this.mStmt);
            this.close();
        }

        return idFromSequence;
    }

    public void delete(List<SMSCommand> listSMS) throws Exception {
        try {
            this.open();
            mConnection.setAutoCommit(false);
            String strSQL = "DELETE FROM CB_SMS_COMMAND WHERE CMD_ID=?";
            Iterator var3 = listSMS.iterator();

            while (var3.hasNext()) {
                SMSCommand sms = (SMSCommand) var3.next();
                this.logBeforeDelete("CB_SMS_COMMAND", "CMD_ID=" + sms.getCmdId());
                this.mStmt = this.mConnection.prepareStatement(strSQL);
                this.mStmt.setLong(1, sms.getCmdId());
                this.mStmt.execute();
            }
            mConnection.commit();
        } catch (Exception var8) {
            ConnUtil.rollback(mConnection);
            throw var8;
        } finally {
            mConnection.setAutoCommit(true);
            this.close(this.mStmt);
            this.close(this.mConnection);
        }
    }


    public void update(SMSCommand ett) throws Exception {
        open();
        mConnection.setAutoCommit(false);
        List listChange = logBeforeUpdate("CB_SMS_COMMAND", "CMD_ID=" + ett.getCmdId());
        String strSQL = "UPDATE CB_SMS_COMMAND " +
                "SET CMD_CODE=?, CMD_TYPE=?, CMD_MSG_CONTENT=?, CMD_PARAM_COUNT=?, DESCRIPTION=?,CMD_REGEX=?,STATUS=?" +
                "  WHERE CMD_ID = ?";
        try {
            mStmt = mConnection.prepareStatement(strSQL);
            mStmt.setString(1, ett.getCmdCode().trim());
            mStmt.setString(2, ett.getCmdType());
            mStmt.setString(3, ett.getCmdMsgContent().trim());
            mStmt.setLong(4, ett.getCmdParamCount());
            mStmt.setString(5, ett.getDescription().trim());
            mStmt.setString(6, ett.getCmdRegex().trim());
            mStmt.setString(7, ett.getStatus());
            mStmt.setLong(8, ett.getCmdId());

            mStmt.executeUpdate();
            logAfterUpdate(listChange);
            mConnection.commit();
        } catch (Exception ex) {
            ConnUtil.rollback(mConnection);
            SystemLogger.getLogger().error(ex);
            throw new Exception(ex);

        } finally {
            mConnection.setAutoCommit(true);
            close(mStmt);
            close(mRs);
            close();
        }
    }

    public void edit(SMSCommand ett) throws Exception {
        try {
            this.open();
            mConnection.setAutoCommit(false);
            List listChange = logBeforeUpdate("CB_SMS_COMMAND", "CMD_ID=" + ett.getCmdId());
            String strSQL = "UPDATE CB_SMS_COMMAND " +
                    "SET CMD_CODE=?, CMD_TYPE=?, CMD_MSG_CONTENT=?, CMD_PARAM_COUNT=?, DESCRIPTION=?,CMD_REGEX=?,STATUS=?" +
                    "  WHERE CMD_ID = ?";
            this.mStmt = this.mConnection.prepareStatement(strSQL);
            this.mStmt.setString(1, ett.getCmdCode());
            this.mStmt.setString(2, ett.getCmdType());
            this.mStmt.setString(3, ett.getCmdMsgContent());
            this.mStmt.setLong(4, ett.getCmdParamCount());
            this.mStmt.setString(5, ett.getDescription());
            this.mStmt.setString(6, ett.getCmdRegex());
            this.mStmt.setString(7, ett.getStatus());
            this.mStmt.setLong(8, ett.getCmdId());
            this.mStmt.execute();
            this.close(this.mStmt);
            logAfterUpdate(listChange);
            mConnection.commit();
        } catch (Exception var7) {
            ConnUtil.rollback(mConnection);
            throw var7;
        } finally {
            mConnection.setAutoCommit(true);
            this.close(this.mStmt);
            this.close();
        }

    }


}
