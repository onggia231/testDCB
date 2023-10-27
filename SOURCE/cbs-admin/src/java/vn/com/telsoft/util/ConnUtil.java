package vn.com.telsoft.util;

import com.faplib.lib.admin.data.AMDataPreprocessor;

import java.io.Serializable;
import java.sql.Connection;

public class ConnUtil extends AMDataPreprocessor implements Serializable {

    public static void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (Exception ex) {
        }
    }
}
