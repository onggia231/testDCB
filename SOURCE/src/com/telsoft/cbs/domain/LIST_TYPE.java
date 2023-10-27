package com.telsoft.cbs.domain;

import org.apache.commons.dbutils.DbUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public enum LIST_TYPE {
    ISDN,
    CONTENT;

    public static LIST_TYPE fromCode(int code) {
        switch (code) {
            case 0:
                return ISDN;
            case 1:
                return CONTENT;
            default:
                return ISDN;
        }
    }

    public static boolean checkAccess(Connection connection, AccessList accessList, String store_id, String subject) throws CBException {
        if (accessList.isCached()) {
            for (Item item : accessList.getItems()) {
                boolean equals = item.match(store_id, subject);
                if (equals) {
                    switch (accessList.access_type) {
                        case BLACKLIST:
                            return false;
                        case WHITELIST:
                            return true;
                        default:
                            return false;
                    }
                }
            }
            switch (accessList.access_type) {
                case BLACKLIST:
                    return true;
                case WHITELIST:
                    return false;
                default:
                    return false;
            }
        } else {
            switch (accessList.getType()) {
                // compare subject,store_id with data in CB_ITEM_ISDN
                case ISDN: {
                    String sql = "SELECT 1 FROM CB_ITEM_ISDN WHERE LIST_ID = ? AND (STORE_ID IS NULL OR STORE_ID = ?) AND ISDN = ?";
                    PreparedStatement stmt = null;
                    ResultSet rs = null;
                    try {
                        stmt = connection.prepareStatement(sql);
                        stmt.setLong(1, accessList.listId);
                        stmt.setString(2, store_id);
                        stmt.setString(3, subject);
                        rs = stmt.executeQuery();
                        boolean isMatch = rs.next();
                        return returnResult(accessList, isMatch);
                    } catch (SQLException e) {
                        throw new CBException(CBCode.INTERNAL_SERVER_ERROR);
                    } finally {
                        DbUtils.closeQuietly(rs);
                        DbUtils.closeQuietly(stmt);
                    }
                }
                // compare subject,store_id with data in CB_ITEM_CONTENT

                case CONTENT: {
                    String sql = "SELECT 1 FROM CB_ITEM_CONTENT C,CB_CONTENT_RECOGNIZE R WHERE C.LIST_ID = ? AND (C.STORE_ID IS NULL OR C.STORE_ID = ?) " +
                            "AND C.CONTENT_ID = R.CONTENT_ID " +
                            "AND (CASE " +
                            " WHEN MATCHING_TYPE = 'CONTAINS' AND CASE_SENSITIVE = 1 AND ? LIKE '%' ||  KEYWORD || '%' THEN 1 " +
                            " WHEN MATCHING_TYPE = 'CONTAINS' AND CASE_SENSITIVE = 0 AND UPPER(?) LIKE '%' ||  UPPER(KEYWORD) || '%' THEN 1 " +
                            " WHEN MATCHING_TYPE = 'EQUALS' AND CASE_SENSITIVE = 1 AND KEYWORD= ? THEN 1 " +
                            " WHEN MATCHING_TYPE = 'EQUALS' AND CASE_SENSITIVE = 0 AND UPPER(KEYWORD) = UPPER(?) THEN 1 " +
                            " WHEN MATCHING_TYPE = 'START_WITH' AND CASE_SENSITIVE = 1 AND ? LIKE KEYWORD || '%' THEN 1 " +
                            " WHEN MATCHING_TYPE = 'START_WITH' AND CASE_SENSITIVE = 0 AND UPPER(?) LIKE UPPER(KEYWORD) || '%' THEN 1 " +
                            " WHEN MATCHING_TYPE = 'END_WITH' AND CASE_SENSITIVE = 1 AND ? LIKE '%' ||  KEYWORD THEN 1 " +
                            " WHEN MATCHING_TYPE = 'END_WITH' AND CASE_SENSITIVE = 0 AND UPPER(?) LIKE '%' ||  UPPER(KEYWORD) THEN 1 " +
                            " WHEN MATCHING_TYPE = 'MATCH_REGEX' AND CASE_SENSITIVE = 1 AND REGEXP_LIKE(?,KEYWORD, 'c') THEN 1 " +
                            " WHEN MATCHING_TYPE = 'MATCH_REGEX' AND CASE_SENSITIVE = 0 AND REGEXP_LIKE(?,KEYWORD, 'i') THEN 1 " +
                            " ELSE 0 " +
                            "END = 1)";
                    PreparedStatement stmt = null;
                    ResultSet rs = null;
                    try {
                        stmt = connection.prepareStatement(sql);
                        stmt.setLong(1, accessList.listId);
                        stmt.setString(2, store_id);
                        stmt.setString(3, subject);
                        stmt.setString(4, subject);
                        stmt.setString(5, subject);
                        stmt.setString(6, subject);
                        stmt.setString(7, subject);
                        stmt.setString(8, subject);
                        stmt.setString(9, subject);
                        stmt.setString(10, subject);
                        stmt.setString(11, subject);
                        stmt.setString(12, subject);
                        rs = stmt.executeQuery();
                        boolean isMatch = rs.next();
                        return returnResult(accessList, isMatch);
                    } catch (SQLException e) {
                        throw new CBException(CBCode.INTERNAL_SERVER_ERROR);
                    } finally {
                        DbUtils.closeQuietly(rs);
                        DbUtils.closeQuietly(stmt);
                    }
                }
            }
            return false;
        }
    }

    private static boolean returnResult(AccessList accessList, boolean isMatch) {
        if (isMatch) {
            switch (accessList.access_type) {
                case BLACKLIST:
                    return false;
                case WHITELIST:
                    return true;
                default:
                    return false;
            }
        } else {
            switch (accessList.access_type) {
                case BLACKLIST:
                    return true;
                case WHITELIST:
                    return false;
                default:
                    return false;
            }
        }
    }
}
