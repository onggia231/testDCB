package vn.com.telsoft.model;

import com.faplib.lib.admin.data.AMDataPreprocessor;
import com.faplib.util.DateUtil;
import com.telsoft.cbs.domain.CBCode;
import com.telsoft.cbs.domain.CBTypeMoMt;
import vn.com.telsoft.entity.CBMtHistory;
import vn.com.telsoft.entity.CBRequest;
import vn.com.telsoft.entity.CBSubscriber;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CBRequestModel extends AMDataPreprocessor implements Serializable {
    private static final long serialVersionUID = -6721491408317256239L;

    public List<CBRequest> searchRequest(CBRequest objInput) throws Exception {
        CBCode code ;
        List<CBRequest> listReturn = new ArrayList<>();
        try {
            open();
            String strSql = "select STORE_TRANSACTION_ID,REFER_TRANSACTION_ID,RESULT_CODE,FINAL_RESULT_CODE,CPS_TRANSACTION_ID, isdn, store_code, transaction_id,request_time,response_time,COMMAND,AMOUNT, AMOUNT_FULL_TAX - AMOUNT VAT, DECODE(STATUS,0,'Đang thực hiện',1,'Thành công',2,'Thất bại',3,'Time out') status, content_description from cb_request where request_time >= ? " +
                    " and request_time < trunc(?,'DDD') + 1 " +
                    " and (store_code = ? or ? is null) and isdn = ? ORDER BY request_time desc";
            mStmt = mConnection.prepareCall(strSql);
            mStmt.setTimestamp(1, DateUtil.getSqlTimestamp(objInput.getFromDate()));
            mStmt.setTimestamp(2, DateUtil.getSqlTimestamp(objInput.getToDate()));
            mStmt.setString(3, objInput.getStoreCode().isEmpty() ? null : objInput.getStoreCode());
            mStmt.setString(4, objInput.getStoreCode().isEmpty() ? null : objInput.getStoreCode());
            mStmt.setString(5, objInput.getIsdn());
            mRs = mStmt.executeQuery();
            while (mRs.next()) {
                CBRequest request = new CBRequest();
                request.setIsdn(mRs.getString("isdn"));
                request.setStoreCode(mRs.getString("store_code"));
                request.setTransactionId(mRs.getString("transaction_id"));
                request.setRequestTime(mRs.getTimestamp("request_time"));
                request.setResponseTime(mRs.getTimestamp("response_time"));
                request.setCommand(mRs.getString("command"));
                request.setAmount(mRs.getLong("amount"));
                request.setVat(mRs.getLong("vat"));
                request.setStatusDisplay(mRs.getString("status"));
                request.setContentDescription(mRs.getString("content_description"));
                //Bo sung truong
                request.setStoreTransactionId(mRs.getString("STORE_TRANSACTION_ID"));
                request.setReferTransactionId(mRs.getString("REFER_TRANSACTION_ID"));
                request.setResultCode(CBCode.valueOfCode(mRs.getString("RESULT_CODE")));
                request.setFinalResultCode(CBCode.valueOfCode(mRs.getString("FINAL_RESULT_CODE")));
                request.setCpsTransactionId(mRs.getString("CPS_TRANSACTION_ID"));
                listReturn.add(request);
            }

        } finally {
            close(mConnection, mStmt, mRs);
        }

        return listReturn;
    }

    public List<CBRequest> searchRequestRefund(CBRequest objInput) throws Exception {
        List<CBRequest> listReturn = new ArrayList<>();
        try {
            open();
            String strSql = "select STORE_TRANSACTION_ID,REFER_TRANSACTION_ID,RESULT_CODE,FINAL_RESULT_CODE,CPS_TRANSACTION_ID, isdn, store_code,transaction_id,request_time,response_time,COMMAND,AMOUNT, AMOUNT_FULL_TAX - AMOUNT VAT, DECODE(STATUS,0,'Đang thực hiện',1,'Thành công',2,'Thất bại',3,'Time out') status, content_description, channel_type from cb_request_refund where request_time >= ? " +
                    " and request_time < trunc(?,'DDD') + 1 " +
                    " and (store_code = ? or ? is null) and isdn = ?  ORDER BY request_time desc";
            mStmt = mConnection.prepareCall(strSql);
            mStmt.setTimestamp(1, DateUtil.getSqlTimestamp(objInput.getFromDate()));
            mStmt.setTimestamp(2, DateUtil.getSqlTimestamp(objInput.getToDate()));
            mStmt.setString(3, objInput.getStoreCode().isEmpty() ? null : objInput.getStoreCode());
            mStmt.setString(4, objInput.getStoreCode().isEmpty() ? null : objInput.getStoreCode());
            mStmt.setString(5, objInput.getIsdn());
            mRs = mStmt.executeQuery();
            while (mRs.next()) {
                CBRequest r = new CBRequest();
                r.setIsdn(mRs.getString("isdn"));
                r.setTransactionId(mRs.getString("transaction_id"));
                r.setRequestTime(mRs.getTimestamp("request_time"));
                r.setResponseTime(mRs.getTimestamp("response_time"));
                r.setCommand(mRs.getString("command"));
                r.setAmount(mRs.getLong("amount"));
                r.setVat(mRs.getLong("vat"));
                r.setStatusDisplay(mRs.getString("status"));
                r.setStoreCode(mRs.getString("store_code"));
                r.setContentDescription(mRs.getString("content_description"));
                r.setChannelType(mRs.getString("channel_type"));
                //Bo sung truong
                r.setStoreTransactionId(mRs.getString("STORE_TRANSACTION_ID"));
                r.setReferTransactionId(mRs.getString("REFER_TRANSACTION_ID"));
                r.setResultCode(CBCode.valueOfCode(mRs.getString("RESULT_CODE")));
                r.setFinalResultCode(CBCode.valueOfCode(mRs.getString("FINAL_RESULT_CODE")));
                r.setCpsTransactionId(mRs.getString("CPS_TRANSACTION_ID"));
                listReturn.add(r);
            }

        } finally {
            close(mConnection, mStmt, mRs);
        }

        return listReturn;
    }

    public List<CBMtHistory> searchMtMoHistory(CBRequest objInput) throws Exception {
        List<CBMtHistory> listReturn = new ArrayList<>();
        try {
            open();
            String strSQLmt = "select 'MT' type, channel_type,store_code,isdn,issue_time, (select c.cmd_code from cb_sms_command c where c.cmd_id = command_id) cmd_code, content " +
                    " from cb_mt_history where issue_time >= ? and issue_time < trunc(?,'DDD') + 1 and (store_code = ? or ? is null) and isdn = ? and (channel_type = ? or ? is null) and (lower(content) like lower(?) or lower(?) is null) order by issue_time desc";
            String strSQLmo = "select 'MO' type, channel_type,store_code,isdn,REQUEST_TIME issue_time, (select c.cmd_code from cb_sms_command c where c.cmd_id = command_id) cmd_code, content " +
                    " from cb_mo_history where REQUEST_TIME >= ? and REQUEST_TIME < trunc(?,'DDD') + 1 and (store_code = ? or ? is null) and isdn = ? and (channel_type = ? or ? is null) and (lower(content) like lower(?) or lower(?) is null) order by issue_time desc";
            String strSqlUnion = "select 'MT' type, channel_type,store_code,isdn,issue_time, (select c.cmd_code from cb_sms_command c where c.cmd_id = command_id) cmd_code, content "
                    + " from cb_mt_history where issue_time >= ? and issue_time < trunc(?,'DDD') + 1 and (store_code = ? or ? is null) and isdn = ? and (channel_type = ? or ? is null) and (lower(content) like lower(?) or lower(?) is null) "
                    + " union all " + strSQLmo;
            if (CBTypeMoMt.MO.name().equals(objInput.getMoMtType())) {
                mStmt = mConnection.prepareCall(strSQLmo);
            } else if (CBTypeMoMt.MT.name().equals(objInput.getMoMtType())) {
                mStmt = mConnection.prepareCall(strSQLmt);
            } else {
                mStmt = mConnection.prepareCall(strSqlUnion);
            }
            mStmt.setTimestamp(1, DateUtil.getSqlTimestamp(objInput.getFromDate()));
            mStmt.setTimestamp(2, DateUtil.getSqlTimestamp(objInput.getToDate()));
            mStmt.setString(3, objInput.getStoreCode().isEmpty() ? null : objInput.getStoreCode());
            mStmt.setString(4, objInput.getStoreCode().isEmpty() ? null : objInput.getStoreCode());
            mStmt.setString(5, objInput.getIsdn());
            mStmt.setString(6, objInput.getChannelType().isEmpty() ? null : objInput.getChannelType());
            mStmt.setString(7, objInput.getChannelType().isEmpty() ? null : objInput.getChannelType());
            mStmt.setString(8, objInput.getContent().isEmpty() ? null : "%" + objInput.getContent() + "%");
            mStmt.setString(9, objInput.getContent().isEmpty() ? null : objInput.getContent());
            if (CBTypeMoMt.ALL.name().equals(objInput.getMoMtType())) {
                mStmt.setTimestamp(10, DateUtil.getSqlTimestamp(objInput.getFromDate()));
                mStmt.setTimestamp(11, DateUtil.getSqlTimestamp(objInput.getToDate()));
                mStmt.setString(12, objInput.getStoreCode().isEmpty() ? null : objInput.getStoreCode());
                mStmt.setString(13, objInput.getStoreCode().isEmpty() ? null : objInput.getStoreCode());
                mStmt.setString(14, objInput.getIsdn());
                mStmt.setString(15, objInput.getChannelType().isEmpty() ? null : objInput.getChannelType());
                mStmt.setString(16, objInput.getChannelType().isEmpty() ? null : objInput.getChannelType());
                mStmt.setString(17, objInput.getContent().isEmpty() ? null : "%" + objInput.getContent() + "%");
                mStmt.setString(18, objInput.getContent().isEmpty() ? null : objInput.getContent());
            }
            mRs = mStmt.executeQuery();
            while (mRs.next()) {
                CBMtHistory h = new CBMtHistory();
                h.setType(mRs.getString("type"));
                h.setIsdn(mRs.getString("isdn"));
                h.setIssueTime(mRs.getTimestamp("issue_time"));
                h.setCommandCode(mRs.getString("cmd_code"));
                h.setStoreCode(mRs.getString("store_code"));
                h.setContent(mRs.getString("content"));
                h.setChannelType(mRs.getString("channel_type"));
                listReturn.add(h);
            }
        } finally {
            close(mConnection, mStmt, mRs);
        }
        return listReturn;
    }

    public CBSubscriber getSubscriberInfo(String isdn) throws Exception {
        CBSubscriber result = new CBSubscriber();
        String strSQL = "SELECT\n" +
                "    a.id,\n" +
                "    a.isdn,\n" +
                "    a.reg_date,\n" +
                "    a.sub_type,\n" +
                "    a.status\n" +
                "FROM\n" +
                "    cb_subscriber a\n" +
                "WHERE\n" +
                "    a.isdn = ? order by a.status DESC, a.reg_date DESC";
        try {
            open();
            mStmt = mConnection.prepareCall(strSQL);
            mStmt.setString(1, isdn);
            mRs = mStmt.executeQuery();
            if(mRs.next()){
                result.setId(mRs.getLong(1));
                result.setIsdn(mRs.getString(2));
                result.setRegDate(mRs.getTimestamp(3));
                result.setSubType(mRs.getString(4));
                result.setStatus(mRs.getLong(5));
            }
        } finally {
            close(mConnection, mStmt, mRs);
        }
        return result;
    }
}
