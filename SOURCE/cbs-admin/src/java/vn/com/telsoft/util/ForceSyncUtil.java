package vn.com.telsoft.util;

import com.faplib.lib.admin.data.AMDataPreprocessor;

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;

public class ForceSyncUtil extends AMDataPreprocessor implements Serializable {

    public static void updateSystemApParam(Connection conn) throws Exception {
        String sql = "{ call FORCE_SYNC() }";
        try (CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
