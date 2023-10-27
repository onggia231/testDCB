/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.com.telsoft.model;

import com.faplib.lib.SystemLogger;
import com.faplib.lib.admin.data.AMDataPreprocessor;
import vn.com.telsoft.entity.CBItemContent;
import vn.com.telsoft.entity.CBItemContentExt;
import vn.com.telsoft.util.ConnUtil;
import vn.com.telsoft.util.ForceSyncUtil;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author trieunv
 */
public class CbItemContentModel extends AMDataPreprocessor implements Serializable {

    private static final long serialVersionUID = -5671239002907377433L;

    public Boolean checkCbItemContentDup(CBItemContent dto) throws Exception {
        List lstParam = new ArrayList<>();
        int i = 0;
        try {
            open();
            String strSQL = "SELECT * " +
                    " FROM CB_ITEM_CONTENT where 1=1 ";
            if (dto != null) {
                if (dto.getListId() != null) {
                    strSQL += " and list_Id = ?";
                    lstParam.add(dto.getListId());
                }
                if (dto.getContentId() != null) {
                    strSQL += " and content_Id = ?";
                    lstParam.add(dto.getContentId());
                }
            }
            mStmt = mConnection.prepareStatement(strSQL);
            if (lstParam != null && lstParam.size() > 0) {
                for (Object obj : lstParam) {
                    mStmt.setObject(++i, obj);
                }
            }
            mRs = mStmt.executeQuery();
            while (mRs.next()) {
                return true;
            }
        } catch (Exception ex) {
            SystemLogger.getLogger().error(ex);
            throw ex;
        } finally {
            close(mRs);
            close(mStmt);
            close();
        }
        return false;
    }

    public void deleteCBItemContent(CBItemContent dto) throws Exception {

        try {
            open();
            mConnection.setAutoCommit(false);
            String strSQL = "DELETE FROM CB_ITEM_CONTENT WHERE list_Id = ? and content_Id = ? ";
            logBeforeDelete("CB_ITEM_CONTENT", "list_Id=" + dto.getListId() + " and content_id=" + dto.getContentId());
            mStmt = mConnection.prepareStatement(strSQL);
            mStmt.setLong(1, dto.getListId());
            mStmt.setLong(2, dto.getContentId());
            mStmt.execute();

            //Commit
            mConnection.commit();
            ForceSyncUtil.updateSystemApParam(mConnection);
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


    private Timestamp convertTimestamp(Date date) {
        if (date == null) {
            return null;
        }
        return new Timestamp(date.getTime());
    }

}
