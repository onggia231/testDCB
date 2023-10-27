//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.faplib.lib.admin.gui.data;

import com.faplib.admin.security.AdminUser;
import com.faplib.lib.TelsoftException;
import com.faplib.lib.admin.data.AMDataPreprocessor;
import com.faplib.lib.admin.gui.entity.AccessRight;
import com.faplib.lib.admin.gui.entity.GroupDTL;
import com.faplib.lib.admin.gui.entity.ModuleRightGUI;
import com.faplib.lib.admin.gui.entity.UserDTL;
import com.faplib.lib.admin.gui.entity.UserGUI;
import com.faplib.lib.config.Config;
import com.faplib.lib.util.SQLUtil;
import com.faplib.util.DateUtil;
import com.faplib.util.StringUtil;
import com.faplib.ws.client.ClientRequestProcessor;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UserGuiModel extends AMDataPreprocessor implements Serializable {
	private static final long serialVersionUID = 7834496090001287205L;

	public UserGuiModel() {
	}

	public String lockUser(String strUsername) throws Exception {
		try {
			this.open();
			String strSQL = "UPDATE am_user SET locked_date=SYSDATE, status=0 WHERE user_name=upper(?)";
			this.mStmt = this.mConnection.prepareStatement(strSQL);
			this.mStmt.setString(1, strUsername);
			this.mStmt.execute();
		} catch (Exception var6) {
			throw var6;
		} finally {
			this.close(this.mStmt);
			this.close();
		}

		return "";
	}

	public String updatePassword(long userID, String strOldPassword, String strPassword) throws Exception {
		if (Config.isWSEnabled()) {
			ClientRequestProcessor.changePassword(userID, strOldPassword, strPassword);
			return "";
		} else {
			try {
				this.open();
				long exStt = AdminUser.getUserLogged().getExpireStatus() == 2L ? 2L : 1L;
				String strSQL = "UPDATE am_user SET password=?, expire_status = ?, modified_password=sysdate WHERE user_id=?";
				this.mStmt = this.mConnection.prepareStatement(strSQL);
				this.mStmt.setString(1, strPassword);
				this.mStmt.setLong(2, exStt);
				this.mStmt.setLong(3, userID);
				this.mStmt.execute();
			} catch (Exception var11) {
				throw var11;
			} finally {
				this.close(this.mStmt);
				this.close();
			}

			return "";
		}
	}

	public String updateLoginFaile(String strUsername) throws Exception {
		try {
			this.open();
			String strSQL = "UPDATE am_user SET failure_count = failure_count + 1 WHERE user_name=upper(?)";
			this.mStmt = this.mConnection.prepareStatement(strSQL);
			this.mStmt.setString(1, strUsername);
			this.mStmt.execute();
		} catch (Exception var6) {
			throw var6;
		} finally {
			this.close(this.mStmt);
			this.close();
		}

		return "";
	}

	public UserDTL getInfByUserName(String strUserName) throws Exception {
		UserDTL returnValue = new UserDTL();
		if (Config.isWSEnabled()) {
			return ClientRequestProcessor.getUserInfo(strUserName);
		} else {
			try {
				this.open();
				String strSQL = "SELECT a.user_id, a.user_name, a.password, a.expire_status, a.full_name,        a.status, a.phone, a.mobile, a.fax, a.email, a.address,        a.modified_password, a.locked_date, a.failure_count,        a.created_id, a.deleted_id, a.modified_id, a.config   FROM am_user a WHERE user_name = UPPER(?)";
				this.mStmt = this.mConnection.prepareStatement(strSQL);
				this.mStmt.setString(1, strUserName);
				this.mRs = this.mStmt.executeQuery();

				while(this.mRs.next()) {
					returnValue = new UserDTL();
					returnValue.setUserId(this.mRs.getLong(1));
					returnValue.setUserName(this.mRs.getString(2));
					returnValue.setPassword(this.mRs.getString(3));
					returnValue.setExpireStatus(this.mRs.getLong(4));
					returnValue.setFullName(this.mRs.getString(5));
					returnValue.setStatus(this.mRs.getLong(6));
					returnValue.setPhone(this.mRs.getString(7));
					returnValue.setMobile(this.mRs.getString(8));
					returnValue.setFax(this.mRs.getString(9));
					returnValue.setEmail(this.mRs.getString(10));
					returnValue.setAddress(this.mRs.getString(11));
					returnValue.setModifiedPassword(this.mRs.getTimestamp(12));
					returnValue.setLockedDate(this.mRs.getTimestamp(13));
					returnValue.setFailureCount(this.mRs.getLong(14));
					returnValue.setCreatedId(this.mRs.getLong(15));
					returnValue.setDeletedId(this.mRs.getLong(16));
					returnValue.setModifiedId(this.mRs.getLong(17));
					returnValue.setConfig(this.mRs.getString(18));
				}
			} catch (Exception var7) {
				throw var7;
			} finally {
				this.close(this.mRs);
				this.close(this.mStmt);
				this.close();
			}

			return returnValue;
		}
	}

	public String delete(List<UserGUI> listUser) throws Exception {
		try {
			this.open();
			this.mConnection.setAutoCommit(false);
			long superAdminId = AdminUser.getSuperAdminId();
			Iterator var5 = listUser.iterator();

			while(var5.hasNext()) {
				UserGUI userGUI = (UserGUI)var5.next();
				this.checkDeletePermission(userGUI, superAdminId);

				String strSQL;
				try {
					this.logBeforeDelete("am_user_branch", "user_id=" + userGUI.getUser().getUserId());
					strSQL = "DELETE FROM am_user_branch WHERE user_id=" + userGUI.getUser().getUserId();
					this.mStmt = this.mConnection.prepareStatement(strSQL);
					this.mStmt.executeUpdate();
					this.logBeforeDelete("am_user_district", "user_id=" + userGUI.getUser().getUserId());
					strSQL = "DELETE FROM am_user_district WHERE user_id=" + userGUI.getUser().getUserId();
					this.mStmt = this.mConnection.prepareStatement(strSQL);
					this.mStmt.executeUpdate();
				} catch (Exception var12) {
					if (!var12.getMessage().contains("ORA-00942")) {
						throw var12;
					}
				}

				this.logBeforeDelete("am_group_user", "user_id=" + userGUI.getUser().getUserId());
				strSQL = "DELETE FROM am_group_user WHERE user_id=" + userGUI.getUser().getUserId();
				this.mStmt = this.mConnection.prepareStatement(strSQL);
				this.mStmt.executeUpdate();
				this.logBeforeDelete("am_user_object", "user_id=" + userGUI.getUser().getUserId());
				strSQL = "DELETE FROM am_user_object a WHERE a.user_id=" + userGUI.getUser().getUserId();
				this.mStmt = this.mConnection.prepareStatement(strSQL);
				this.mStmt.execute();
				this.close(this.mStmt);
				this.logBeforeDelete("am_user", "user_id=" + userGUI.getUser().getUserId());
				strSQL = "DELETE FROM am_user WHERE user_id=" + userGUI.getUser().getUserId();
				this.mStmt = this.mConnection.prepareStatement(strSQL);
				this.mStmt.executeQuery();
				this.close(this.mStmt);
			}

			this.mConnection.commit();
			return "";
		} catch (Exception var13) {
			this.mConnection.rollback();
			throw var13;
		} finally {
			this.close(this.mStmt);
			this.close();
		}
	}

	private boolean checkDeletePermission(UserGUI user, long superAdminId) throws Exception {
		if (user.getUser().getUserId() == superAdminId) {
			throw new TelsoftException("cant_delete_admin_user");
		} else if (user.getUser().getStatus() == 2L) {
			throw new TelsoftException("cant_delete_system_user");
		} else if (user.getUser().getUserId() == AdminUser.getUserLogged().getUserId()) {
			throw new TelsoftException("cant_delete_self_user");
		} else {
			return true;
		}
	}

	public long add(UserGUI userGUI, List<ModuleRightGUI> listModuleRight, String strBranch, String strDistrict, String strType) throws Exception {
		long idFromSequence;
		try {
			this.open();
			this.mConnection.setAutoCommit(false);
			idFromSequence = SQLUtil.getSequenceValue(this.mConnection, "SEQ_AM_USER_ID");
			userGUI.getUser().setUserId(idFromSequence);
			String strSQL = "INSERT INTO AM_USER(USER_ID, USER_NAME, PASSWORD, EXPIRE_STATUS, FULL_NAME, STATUS, PHONE, MOBILE, FAX, EMAIL, ADDRESS, MODIFIED_PASSWORD, LOCKED_DATE, FAILURE_COUNT, CREATED_ID) VALUES (?,?,?,?,?,?,?,?,?,?,?,sysdate,?,?,?)";
			this.mStmt = this.mConnection.prepareStatement(strSQL);
			this.mStmt.setLong(1, idFromSequence);
			this.mStmt.setString(2, userGUI.getUser().getUserName());
			this.mStmt.setString(3, userGUI.getUser().getPassword());
			this.mStmt.setLong(4, userGUI.getUser().getExpireStatus());
			this.mStmt.setString(5, userGUI.getUser().getFullName());
			this.mStmt.setLong(6, userGUI.getUser().getStatus());
			this.mStmt.setString(7, userGUI.getUser().getPhone());
			this.mStmt.setString(8, userGUI.getUser().getMobile());
			this.mStmt.setString(9, userGUI.getUser().getFax());
			this.mStmt.setString(10, userGUI.getUser().getEmail());
			this.mStmt.setString(11, userGUI.getUser().getAddress());
			this.mStmt.setTimestamp(12, DateUtil.getSqlTimestamp(userGUI.getUser().getLockedDate()));
			this.mStmt.setLong(13, userGUI.getUser().getFailureCount());
			this.mStmt.setLong(14, AdminUser.getUserLogged().getUserId());
			this.mStmt.execute();
			this.logAfterInsert("am_user", "user_id=" + idFromSequence);
			this.close(this.mStmt);
			strSQL = "INSERT INTO am_group_user(group_id, user_id) VALUES(?, ?)";
			this.mStmt = this.mConnection.prepareStatement(strSQL);
			Iterator var9 = userGUI.getListGroup().iterator();

			while(var9.hasNext()) {
				GroupDTL group = (GroupDTL)var9.next();
				this.mStmt.setLong(1, group.getGroupId());
				this.mStmt.setLong(2, idFromSequence);
				this.mStmt.addBatch();
			}

			this.mStmt.executeBatch();
			this.mConnection.commit();
			this.logAfterInsert(Config.getCurrentModule(), "am_group_user", "user_id=" + idFromSequence);
			String strListModuleId = "";
			strSQL = "INSERT INTO am_user_object(user_id, object_id, right_code, access_type) VALUES(?,?,?,?)";
			this.mStmt = this.mConnection.prepareStatement(strSQL);

			ModuleRightGUI moduleRight;
			for(Iterator var20 = listModuleRight.iterator(); var20.hasNext(); strListModuleId = strListModuleId + moduleRight.getModuleId() + ",") {
				moduleRight = (ModuleRightGUI)var20.next();
				Iterator var12 = moduleRight.getListAccess().iterator();

				while(var12.hasNext()) {
					AccessRight accessRight = (AccessRight)var12.next();
					if (accessRight.getAccess() != 2) {
						this.mStmt.setLong(1, idFromSequence);
						this.mStmt.setLong(2, moduleRight.getModuleId());
						this.mStmt.setString(3, accessRight.getRightCode());
						this.mStmt.setInt(4, accessRight.getAccess());
						this.mStmt.addBatch();
					}
				}
			}

			this.mStmt.executeBatch();
			strListModuleId = StringUtil.removeLastChar(strListModuleId);
			if ("copy".equals(strType)) {
				strSQL = "INSERT INTO am_user_object(user_id, object_id, right_code, access_type)     SELECT " + idFromSequence + ", object_id, right_code, access_type     FROM am_user_object WHERE user_id = ?     AND object_id NOT IN (" + strListModuleId + ")";
				this.mStmt = this.mConnection.prepareStatement(strSQL);
				this.mStmt.setLong(1, userGUI.getUser().getUserId());
				this.mStmt.execute();
			}

			this.logAfterInsert("am_user_object", "user_id=" + idFromSequence);
			strSQL = "SELECT count(1) FROM user_tables WHERE table_name IN ('AM_USER_BRANCH', 'AM_USER_DISTRICT')";
			this.mStmt = this.mConnection.prepareStatement(strSQL);
			this.mRs = this.mStmt.executeQuery();
			if (this.mRs.next() && this.mRs.getInt(1) == 2) {
				if (strBranch != null && !strBranch.isEmpty()) {
					strSQL = "INSERT INTO am_user_branch(user_id, branch_code) VALUES(?, ?)";
					this.mStmt = this.mConnection.prepareStatement(strSQL);
					this.mStmt.setLong(1, idFromSequence);
					this.mStmt.setString(2, strBranch);
					this.mStmt.execute();
					this.logAfterInsert("am_user_branch", "user_id=" + idFromSequence);
					this.close(this.mStmt);
				}

				if (strDistrict != null && !strDistrict.isEmpty()) {
					strSQL = "INSERT INTO am_user_district(user_id, district_code) VALUES(?, ?)";
					this.mStmt = this.mConnection.prepareStatement(strSQL);
					this.mStmt.setLong(1, idFromSequence);
					this.mStmt.setString(2, strDistrict);
					this.mStmt.execute();
					this.logAfterInsert("am_user_district", "user_id=" + idFromSequence);
					this.close(this.mStmt);
				}
			}

			this.mConnection.commit();
		} catch (Exception var17) {
			this.mConnection.rollback();
			throw var17;
		} finally {
			this.close(this.mStmt);
			this.close();
		}

		return idFromSequence;
	}

	public String update(UserGUI account, List<ModuleRightGUI> listModuleRight, String strBranch, String strDistrict) throws Exception {
		try {
			this.open();
			this.mConnection.setAutoCommit(false);
			List listChange = this.logBeforeUpdate(Config.getCurrentModule(), "am_user", "user_id=" + account.getUser().getUserId());
			String strSQL = "UPDATE am_user set user_name=?, password=?, expire_status=?, full_name=?, status=?, phone=?, mobile=?, fax=?, email=?, address=?, locked_date=?, failure_count=?, modified_id=? WHERE user_id=?";
			this.mStmt = this.mConnection.prepareStatement(strSQL);
			this.mStmt.setString(1, account.getUser().getUserName());
			this.mStmt.setString(2, account.getUser().getPassword());
			this.mStmt.setLong(3, account.getUser().getExpireStatus());
			this.mStmt.setString(4, account.getUser().getFullName());
			this.mStmt.setLong(5, account.getUser().getStatus());
			this.mStmt.setString(6, account.getUser().getPhone());
			this.mStmt.setString(7, account.getUser().getMobile());
			this.mStmt.setString(8, account.getUser().getFax());
			this.mStmt.setString(9, account.getUser().getEmail());
			this.mStmt.setString(10, account.getUser().getAddress());
			this.mStmt.setTimestamp(11, DateUtil.getSqlTimestamp(account.getUser().getLockedDate()));
			this.mStmt.setLong(12, account.getUser().getFailureCount());
			this.mStmt.setLong(13, AdminUser.getUserLogged().getUserId());
			this.mStmt.setLong(14, account.getUser().getUserId());
			this.mStmt.executeUpdate();
			this.logAfterUpdate(listChange);
			this.close(this.mStmt);
			this.logBeforeDelete("am_group_user", "user_id=" + account.getUser().getUserId());
			strSQL = "DELETE FROM am_group_user WHERE user_id=?";
			this.mStmt = this.mConnection.prepareStatement(strSQL);
			this.mStmt.setLong(1, account.getUser().getUserId());
			this.mStmt.executeUpdate();
			this.close(this.mStmt);
			strSQL = "INSERT INTO am_group_user(group_id, user_id) VALUES(?, ?)";
			this.mStmt = this.mConnection.prepareStatement(strSQL);
			Iterator var7 = account.getListGroup().iterator();

			while(var7.hasNext()) {
				GroupDTL group = (GroupDTL)var7.next();
				this.mStmt.setLong(1, group.getGroupId());
				this.mStmt.setLong(2, account.getUser().getUserId());
				this.mStmt.addBatch();
			}

			this.mStmt.executeBatch();
			this.logAfterInsert("am_group_user", "user_id=" + account.getUser().getUserId());
			this.close(this.mStmt);
			String strModuleIds = "";

			ModuleRightGUI moduleright;
			Iterator var18;
			for(var18 = listModuleRight.iterator(); var18.hasNext(); strModuleIds = strModuleIds + moduleright.getModuleId() + ",") {
				moduleright = (ModuleRightGUI)var18.next();
			}

			strModuleIds = StringUtil.removeLastChar(strModuleIds);
			this.logBeforeDelete("am_user_object", "object_id IN (" + strModuleIds + ") AND user_id=" + account.getUser().getUserId());
			strSQL = "DELETE FROM am_user_object WHERE object_id IN (" + strModuleIds + ") AND user_id=" + account.getUser().getUserId();
			this.mStmt = this.mConnection.prepareStatement(strSQL);
			this.mStmt.execute();
			this.close(this.mStmt);
			strSQL = "INSERT INTO am_user_object(user_id, object_id, right_code, access_type) VALUES(?,?,?,?)";
			this.mConnection.setAutoCommit(false);
			this.mStmt = this.mConnection.prepareStatement(strSQL);
			var18 = listModuleRight.iterator();

			while(var18.hasNext()) {
				moduleright = (ModuleRightGUI)var18.next();
				Iterator var10 = moduleright.getListAccess().iterator();

				while(var10.hasNext()) {
					AccessRight accessRight = (AccessRight)var10.next();
					if (accessRight.getAccess() != 2) {
						this.mStmt.setLong(1, account.getUser().getUserId());
						this.mStmt.setLong(2, moduleright.getModuleId());
						this.mStmt.setString(3, accessRight.getRightCode());
						this.mStmt.setInt(4, accessRight.getAccess());
						this.mStmt.addBatch();
					}
				}
			}

			this.mStmt.executeBatch();
			this.logAfterInsert("am_user_object", "user_id=" + account.getUser().getUserId());
			strSQL = "SELECT count(1) FROM user_tables WHERE table_name IN ('AM_USER_BRANCH', 'AM_USER_DISTRICT')";
			this.mStmt = this.mConnection.prepareStatement(strSQL);
			this.mRs = this.mStmt.executeQuery();
			if (this.mRs.next() && this.mRs.getInt(1) == 2) {
				this.logBeforeDelete("am_user_branch", "user_id=" + account.getUser().getUserId());
				strSQL = "DELETE FROM am_user_branch WHERE user_id = ?";
				this.mStmt = this.mConnection.prepareStatement(strSQL);
				this.mStmt.setLong(1, account.getUser().getUserId());
				this.mStmt.execute();
				this.close(this.mStmt);
				if (strBranch != null && !strBranch.isEmpty()) {
					strSQL = "INSERT INTO am_user_branch(user_id, branch_code) VALUES(?, ?)";
					this.mStmt = this.mConnection.prepareStatement(strSQL);
					this.mStmt.setLong(1, account.getUser().getUserId());
					this.mStmt.setString(2, strBranch);
					this.mStmt.execute();
					this.logAfterInsert("am_user_branch", "user_id=" + account.getUser().getUserId());
					this.close(this.mStmt);
				}

				this.logBeforeDelete("am_user_district", "user_id=" + account.getUser().getUserId());
				strSQL = "DELETE FROM am_user_district WHERE user_id = ?";
				this.mStmt = this.mConnection.prepareStatement(strSQL);
				this.mStmt.setLong(1, account.getUser().getUserId());
				this.mStmt.execute();
				this.close(this.mStmt);
				if (strDistrict != null && !strDistrict.isEmpty()) {
					strSQL = "INSERT INTO am_user_district(user_id, district_code) VALUES(?, ?)";
					this.mStmt = this.mConnection.prepareStatement(strSQL);
					this.mStmt.setLong(1, account.getUser().getUserId());
					this.mStmt.setString(2, strDistrict);
					this.mStmt.execute();
					this.logAfterInsert("am_user_district", "user_id=" + account.getUser().getUserId());
					this.close(this.mStmt);
				}
			}

			this.mConnection.commit();
		} catch (Exception var15) {
			this.mConnection.rollback();
			throw var15;
		} finally {
			this.close(this.mRs);
			this.close(this.mStmt);
			this.close();
		}

		return "";
	}

	public List<UserGUI> getListAmUser() throws Exception {
		ArrayList returnValue = new ArrayList();

		try {
			this.open();
			String strSQL = "SELECT user_id, user_name, password, expire_status, full_name, status, phone, mobile, fax, email, address, modified_password, locked_date, failure_count, created_id, deleted_id, modified_id FROM am_user ORDER by user_name";
			this.mStmt = this.mConnection.prepareStatement(strSQL);
			this.mRs = this.mStmt.executeQuery();

			while(this.mRs.next()) {
				UserGUI tmpUserGUI = new UserGUI();
				tmpUserGUI.getUser().setUserId(this.mRs.getLong(1));
				tmpUserGUI.getUser().setUserName(this.mRs.getString(2));
				tmpUserGUI.getUser().setPassword(this.mRs.getString(3));
				tmpUserGUI.getUser().setExpireStatus(this.mRs.getLong(4));
				tmpUserGUI.getUser().setFullName(this.mRs.getString(5));
				tmpUserGUI.getUser().setStatus(this.mRs.getLong(6));
				tmpUserGUI.getUser().setPhone(this.mRs.getString(7));
				tmpUserGUI.getUser().setMobile(this.mRs.getString(8));
				tmpUserGUI.getUser().setFax(this.mRs.getString(9));
				tmpUserGUI.getUser().setEmail(this.mRs.getString(10));
				tmpUserGUI.getUser().setAddress(this.mRs.getString(11));
				tmpUserGUI.getUser().setModifiedPassword(this.mRs.getTimestamp(12));
				tmpUserGUI.getUser().setLockedDate(this.mRs.getTimestamp(13));
				tmpUserGUI.getUser().setFailureCount(this.mRs.getLong(14));
				tmpUserGUI.getUser().setCreatedId(this.mRs.getLong(15));
				tmpUserGUI.getUser().setModifiedId(this.mRs.getLong(17));
				returnValue.add(tmpUserGUI);
			}
		} catch (Exception var7) {
			throw var7;
		} finally {
			this.close(this.mRs);
			this.close(this.mStmt);
			this.close();
		}

		return returnValue;
	}

	public UserGUI getUserGroupById(long userId) throws Exception {
		UserGUI returnVal = new UserGUI();

		try {
			this.open();
			String strSQL = "SELECT a.group_id, b.name FROM am_group_user a, am_group b WHERE a.user_id= ? AND a.group_id=b.group_id";
			this.mStmt = this.mConnection.prepareStatement(strSQL);
			this.mStmt.setLong(1, userId);
			this.mRs = this.mStmt.executeQuery();
			this.mRs = this.mStmt.executeQuery();

			while(this.mRs.next()) {
				GroupDTL tmpGroup = new GroupDTL();
				tmpGroup.setGroupId(this.mRs.getLong(1));
				tmpGroup.setName(this.mRs.getString(2));
				returnVal.getListGroup().add(tmpGroup);
			}

			this.mRs.close();
			this.mStmt.close();

			try {
				strSQL = "SELECT (SELECT a.branch_code FROM am_user_branch a WHERE a.user_id = ?) branch_code, (SELECT a.district_code FROM am_user_district a WHERE a.user_id = ?) district_code FROM dual";
				this.mStmt = this.mConnection.prepareStatement(strSQL);
				this.mStmt.setLong(1, userId);
				this.mStmt.setLong(2, userId);
				this.mRs = this.mStmt.executeQuery();

				while(this.mRs.next()) {
					returnVal.setBranch(this.mRs.getString(1));
					returnVal.setDistrict(this.mRs.getString(2));
				}
			} catch (Exception var16) {
				returnVal.setBranch("");
				returnVal.setDistrict("");
			} finally {
				this.close(this.mRs);
			}

			UserGUI var20 = returnVal;
			return var20;
		} catch (Exception var18) {
			throw var18;
		} finally {
			this.close(this.mStmt);
			this.close(this.mRs);
			this.close();
		}
	}

	public List<UserGUI> getListAmUserByGroupId(long groupID, boolean bIsIncludeChild) throws Exception {
		List<UserGUI> returnValue = new ArrayList();
		String strWhere;
		if (bIsIncludeChild) {
			strWhere = " WHERE user_id IN (     SELECT user_id FROM am_group_user WHERE group_id IN (         SELECT a.GROUP_ID FROM am_group a          CONNECT BY PRIOR GROUP_ID = a.parent_id          START WITH a.GROUP_ID = ?))";
		} else {
			strWhere = " WHERE user_id in(SELECT user_id FROM am_group_user WHERE group_id = ?)";
		}

		try {
			this.open();
			String strSQL = "SELECT a.user_id, a.user_name, a.password, a.expire_status, a.full_name, a.status, a.phone, a.mobile, a.fax, a.email, a.address, a.modified_password, a.locked_date, a.failure_count,a.created_id, a.modified_id FROM am_user a" + strWhere + " ORDER by lower(user_name)";
			this.mStmt = this.mConnection.prepareStatement(strSQL);
			this.mStmt.setLong(1, groupID);
			this.mRs = this.mStmt.executeQuery();

			while(this.mRs.next()) {
				UserGUI tmpUserGUI = new UserGUI();
				tmpUserGUI.getUser().setUserId(this.mRs.getLong(1));
				tmpUserGUI.getUser().setUserName(this.mRs.getString(2));
				tmpUserGUI.getUser().setPassword(this.mRs.getString(3));
				tmpUserGUI.getUser().setExpireStatus(this.mRs.getLong(4));
				tmpUserGUI.getUser().setFullName(this.mRs.getString(5));
				tmpUserGUI.getUser().setStatus(this.mRs.getLong(6));
				tmpUserGUI.getUser().setPhone(this.mRs.getString(7));
				tmpUserGUI.getUser().setMobile(this.mRs.getString(8));
				tmpUserGUI.getUser().setFax(this.mRs.getString(9));
				tmpUserGUI.getUser().setEmail(this.mRs.getString(10));
				tmpUserGUI.getUser().setAddress(this.mRs.getString(11));
				tmpUserGUI.getUser().setModifiedPassword(this.mRs.getTimestamp(12));
				tmpUserGUI.getUser().setLockedDate(this.mRs.getTimestamp(13));
				tmpUserGUI.getUser().setFailureCount(this.mRs.getLong(14));
				tmpUserGUI.getUser().setCreatedId(this.mRs.getLong(15));
				tmpUserGUI.getUser().setModifiedId(this.mRs.getLong(16));
				returnValue.add(tmpUserGUI);
			}
		} catch (Exception var11) {
			throw var11;
		} finally {
			this.close(this.mRs);
			this.close(this.mStmt);
			this.close();
		}

		return returnValue;
	}

	public List<UserGUI> getListAllByGroupId(long groupID, boolean bIsIncludeChild) throws Exception {
		List<UserGUI> returnValue = new ArrayList();
		PreparedStatement stmtGroupUser = null;
		ResultSet rsGroupUser = null;
		PreparedStatement stmtDistrictBranch = null;
		ResultSet rsDistrictBranch = null;
		String strWhere = "";
		if (groupID != -1L && bIsIncludeChild) {
			strWhere = " WHERE user_id IN (     SELECT user_id FROM am_group_user WHERE group_id IN (         SELECT a.GROUP_ID FROM am_group a          CONNECT BY PRIOR GROUP_ID = a.parent_id          START WITH a.GROUP_ID = ?))";
		} else if (groupID != -1L && !bIsIncludeChild) {
			strWhere = " WHERE user_id in(SELECT user_id FROM am_group_user WHERE group_id = ?)";
		}

		try {
			this.open();
			this.mConnection.setAutoCommit(false);
			String strSQL = "SELECT a.user_id, a.user_name, a.password, a.expire_status, a.full_name, a.status, a.phone, a.mobile, a.fax, a.email, a.address, to_char(a.modified_password, 'dd/MM/yyyy'), to_char(a.locked_date, 'dd/MM/yyyy'), a.failure_count, (SELECT user_name FROM am_user WHERE user_id = a.created_id) created_user, (SELECT user_name FROM am_user WHERE user_id = a.modified_id) modified_user FROM am_user a" + strWhere + " ORDER by lower(user_name)";
			String strSqlGroupUser = "SELECT a.group_id, b.name FROM am_group_user a, am_group b WHERE a.user_id=? AND a.group_id=b.group_id";
			String strSqlBranch = "SELECT (SELECT a.branch_code FROM am_user_branch a WHERE a.user_id = ?) branch_code, (SELECT a.district_code FROM am_user_district a WHERE a.user_id = ?) district_code FROM dual";
			this.mStmt = this.mConnection.prepareStatement(strSQL);
			stmtGroupUser = this.mConnection.prepareStatement(strSqlGroupUser);
			stmtDistrictBranch = this.mConnection.prepareStatement(strSqlBranch);
			this.mStmt.setFetchSize(5000);
			stmtGroupUser.setFetchSize(5000);
			stmtDistrictBranch.setFetchSize(5000);
			if (groupID != -1L) {
				this.mStmt.setLong(1, groupID);
			}

			UserGUI tmpUserGUI;
			for(this.mRs = this.mStmt.executeQuery(); this.mRs.next(); returnValue.add(tmpUserGUI)) {
				tmpUserGUI = new UserGUI();
				tmpUserGUI.getUser().setUserId(this.mRs.getLong(1));
				tmpUserGUI.getUser().setUserName(this.mRs.getString(2));
				tmpUserGUI.getUser().setPassword(this.mRs.getString(3));
				tmpUserGUI.getUser().setExpireStatus(this.mRs.getLong(4));
				tmpUserGUI.getUser().setFullName(this.mRs.getString(5));
				tmpUserGUI.getUser().setStatus(this.mRs.getLong(6));
				tmpUserGUI.getUser().setPhone(this.mRs.getString(7));
				tmpUserGUI.getUser().setMobile(this.mRs.getString(8));
				tmpUserGUI.getUser().setFax(this.mRs.getString(9));
				tmpUserGUI.getUser().setEmail(this.mRs.getString(10));
				tmpUserGUI.getUser().setAddress(this.mRs.getString(11));
				tmpUserGUI.getUser().setModifiedPassword(this.mRs.getTimestamp(12));
				tmpUserGUI.getUser().setLockedDate(this.mRs.getTimestamp(13));
				tmpUserGUI.getUser().setFailureCount(this.mRs.getLong(14));
				tmpUserGUI.getUser().setCreatedId(this.mRs.getLong(15));
				tmpUserGUI.getUser().setModifiedId(this.mRs.getLong(16));
				stmtGroupUser.setLong(1, tmpUserGUI.getUser().getUserId());
				rsGroupUser = stmtGroupUser.executeQuery();

				while(rsGroupUser.next()) {
					GroupDTL tmpGroup = new GroupDTL();
					tmpGroup.setGroupId(rsGroupUser.getLong(1));
					tmpGroup.setName(rsGroupUser.getString(2));
					tmpUserGUI.getListGroup().add(tmpGroup);
				}

				this.close(rsGroupUser);

				try {
					stmtDistrictBranch.setLong(1, tmpUserGUI.getUser().getUserId());
					stmtDistrictBranch.setLong(2, tmpUserGUI.getUser().getUserId());
					rsDistrictBranch = stmtDistrictBranch.executeQuery();

					while(rsDistrictBranch.next()) {
						tmpUserGUI.setBranch(rsDistrictBranch.getString(1));
						tmpUserGUI.setDistrict(rsDistrictBranch.getString(2));
					}
				} catch (Exception var25) {
					tmpUserGUI.setBranch("");
					tmpUserGUI.setDistrict("");
				} finally {
					this.close(rsDistrictBranch);
				}
			}
		} catch (Exception var27) {
			throw var27;
		} finally {
			this.close(rsGroupUser);
			this.close(stmtGroupUser);
			this.close(stmtDistrictBranch);
			this.close(rsDistrictBranch);
			this.close(this.mRs);
			this.close(this.mStmt);
			this.close();
		}

		return returnValue;
	}

	public boolean isExistEmail(UserGUI userGUI, boolean isEdit) throws Exception {
		boolean var4;
		try {
			this.open();
			String strSQL = "SELECT user_id, email FROM am_user WHERE email = ?";
			this.mStmt = this.mConnection.prepareStatement(strSQL);
			this.mStmt.setString(1, userGUI.getUser().getEmail());
			this.mRs = this.mStmt.executeQuery();

			do {
				if (!this.mRs.next()) {
					return false;
				}
			} while(this.mRs.getLong(1) == userGUI.getUser().getUserId() && isEdit);

			var4 = true;
		} catch (Exception var8) {
			throw var8;
		} finally {
			this.close(this.mStmt);
			this.close(this.mRs);
			this.close();
		}

		return var4;
	}

	public void updateUserConfg(long userId, String strConfig) throws Exception {
		if (Config.isWSEnabled()) {
			ClientRequestProcessor.updateUserConfg(AdminUser.getUserLogged().getUserId(), AdminUser.getUserLogged().getPassword(), strConfig);
		} else {
			try {
				this.open();
				String strSQL = "UPDATE am_user SET config = ? WHERE user_id = ?";
				this.mStmt = this.mConnection.prepareStatement(strSQL);
				this.mStmt.setString(1, strConfig);
				this.mStmt.setLong(2, userId);
				this.mStmt.execute();
			} catch (Exception var8) {
				throw var8;
			} finally {
				this.close(this.mStmt);
				this.close();
			}

		}
	}
}
