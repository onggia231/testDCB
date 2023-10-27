package com.telsoft.cbs.utils;

import com.telsoft.database.Database;
import com.telsoft.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.Vector;

@Slf4j
public class DbUtils {
    public static String getValue(Connection cn, String strTableName, String strFieldName, String strCondition) throws Exception {
        String strSQL = "SELECT " + strFieldName + " FROM " + strTableName + " WHERE " + strCondition;
        try {
            Statement stmt = cn.createStatement();
            ResultSet rs = stmt.executeQuery(strSQL);
            if (!rs.next()) {
                rs.close();
                stmt.close();
                return null;
            } else {
                String strReturn = rs.getString(1);
                rs.close();
                stmt.close();
                return strReturn;
            }
        } catch (SQLException var8) {
            log.error(strSQL, var8);
            throw var8;
        }
    }


    public static long getSequenceValue(Connection cn, String mstrSequence) throws Exception {
        String strSQL = "SELECT " + mstrSequence + ".nextval FROM dual";
        try (PreparedStatement stmt = cn.prepareStatement(strSQL);
             ResultSet rs = stmt.executeQuery()){
            if(rs.next()) {
                long i = rs.getLong(1);
                return i;
            }else {
                throw new Exception("Can't get Sequence Value " + mstrSequence);
            }
        }

    }

    public static void setAutocommitConnection(Connection conn, boolean bAutocommit){
        try{
            conn.setAutoCommit(bAutocommit);
        }catch (Exception e){
            log.error("Can't set Autocommit", e);
        }
    }

    public static void rollbackTransaction(Connection conn){
        try{
            conn.rollback();
        }catch (Exception e){
            log.error("Can't rollback", e);
        }
    }

}
