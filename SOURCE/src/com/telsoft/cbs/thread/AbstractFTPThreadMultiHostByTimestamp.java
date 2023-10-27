package com.telsoft.cbs.thread;

import java.io.IOException;
import java.net.SocketException;
import java.util.Vector;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import com.telsoft.thread.ManageableThread;
import com.telsoft.thread.ParameterType;
import com.telsoft.util.AppException;
import com.telsoft.util.DateUtil;
import com.telsoft.util.StringUtil;

public abstract class AbstractFTPThreadMultiHostByTimestamp extends ManageableThread {
	protected FTPClient mftpMain;
	protected int miPort;
	protected String mstrTransferType;
	protected String mstrConnectMode;
	protected int miTimeOut;
	protected String mstrListingMode;
	protected String mstrFTPStyle;
	protected String mstrLocalStyle;
	protected String mstrBackupStyle;
	protected String mstrDateFormat;
	protected Vector mvtFTPSetting;
	protected Vector mvtCurrentParams;
	protected String mstrFTPName;
	protected String mstrHost;
	protected String mstrUser;
	protected String mstrPassword;
	protected String mstrFTPDir;
	protected String mstrLocalDir;
	protected String mstrWildcard;
	protected String mstrTempDir;
	protected String mstrThreadStatus;

	protected int miFTPNameIndex;
	protected int miHostIndex;
	protected int miPortIndex;
	protected int miUserIndex;
	protected int miPasswordIndex;
	protected int miFTPDirIndex;
	protected int miRecursiveIndex;
	protected int miLocalDirIndex;
	protected int miLocalFileFormatIndex;
	protected int miBackupDirIndex;
	protected int miBackupFileFormatIndex;
	protected int miTempDirIndex;
	protected int miLastProcessFileStampIndex;
	protected int miWildcardIndex;
	protected int miMaxTimeGetLastFileIndex;
	protected int miProcessDateIndex;
	protected int miLastTimeScanFileIndex;
	protected int miLastFileStampIndex;
	protected int miLastFileNameIndex;
	protected int miFTPStatus;

	public void processFTP() throws Exception {
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Vector getParameterDefinition() {
		Vector vtReturn = new Vector();
		Vector vtValue = new Vector();
		vtValue.addElement("BINARY");
		vtValue.addElement("ASCII");
		vtReturn.addElement(createParameter("TranferType", "", ParameterType.PARAM_COMBOBOX, vtValue, "TranferType"));
		vtValue = new Vector();
		vtValue.addElement("PASSIVE");
		vtValue.addElement("ACTIVE");
		vtReturn.addElement(createParameter("ConnectMode", "", ParameterType.PARAM_COMBOBOX, vtValue, "ConnectMode"));
		vtValue = new Vector();
		vtValue.addElement("Auto detect");
		vtValue.addElement("Unix");
		vtValue.addElement("EnterpriseUnix");
		vtValue.addElement("NT");
		vtValue.addElement("OS2");
		vtValue.addElement("OS400");
		vtValue.addElement("VMS");
		vtValue.addElement("VMSVersioning");
		vtReturn.addElement(createParameter("ListingMode", "", ParameterType.PARAM_COMBOBOX, vtValue, "ListingMode"));

		vtReturn.addElement(createParameter("TimeOut", "", ParameterType.PARAM_TEXTBOX_MASK, "99990", "TimeOut"));
		vtValue = new Vector();
		vtValue.addElement("");
		vtValue.addElement("Directly");
		vtValue.addElement("Daily");
		vtValue.addElement("Monthly");
		vtValue.addElement("Yearly");
		vtReturn.addElement(
				createParameter("FtpStyle", "", ParameterType.PARAM_COMBOBOX, vtValue, "Style Store File in Server"));
		vtReturn.addElement(
				createParameter("LocalStyle", "", ParameterType.PARAM_COMBOBOX, vtValue, "Style Store File in Local"));
		vtReturn.addElement(createParameter("BackupStyle", "", ParameterType.PARAM_COMBOBOX, vtValue,
				"Style Store FileBackup in Server"));
		vtReturn.addElement(createParameter("DateFormat", "", ParameterType.PARAM_TEXTBOX_MAX, "256",
				"Format of Directory store File"));
		vtReturn.addElement(createParameter("FTPSetting", "", ParameterType.PARAM_TABLE, getFTPParameters(),
				"Contain FTP information"));
		vtReturn.addAll(super.getParameterDefinition());
		return vtReturn;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Vector getFTPParameters() {
		Vector vtDefinition = new Vector();
		vtDefinition
				.addElement(createParameter("FTPName", "", ParameterType.PARAM_TEXTBOX_MAX, "256", "FTP Name", "0"));

		vtDefinition.addElement(createParameter("Host", "", ParameterType.PARAM_TEXTBOX_FILTER,
				"/\\[]{}()`~!@#$%^&*;,?'\"\t ", "Host Address to connect", "1"));
		vtDefinition.addElement(
				createParameter("Port", "", ParameterType.PARAM_TEXTBOX_MASK, "99990", "Port FTP = 21", "2"));
		vtDefinition.addElement(
				createParameter("User", "", ParameterType.PARAM_TEXTBOX_MAX, "256", "UserName to connect", "3"));
		vtDefinition.addElement(
				createParameter("Password", "", ParameterType.PARAM_PASSWORD, "100", "Password to connect", "4"));
		vtDefinition.addElement(createParameter("FTPDir", "", ParameterType.PARAM_TEXTBOX_MAX, "256",
				"Directory store file in server", "5"));
		Vector vtValue = new Vector();
		vtValue.addElement("Y");
		vtValue.addElement("N");
		vtDefinition.addElement(createParameter("Recursive", "", ParameterType.PARAM_COMBOBOX, vtValue, "Recursive","6"));
		vtDefinition.addElement(createParameter("LocalDir", "", ParameterType.PARAM_TEXTBOX_MAX, "256",
				"Directory store file in Local", "7"));
		vtDefinition.addElement(createParameter("LocalFileFormat", "", ParameterType.PARAM_TEXTBOX_MAX, "256",
				"Format local file name.\r\n Can use $FileName,$BaseFileName,$FileExtension as parameter", "8"));
		vtDefinition.addElement(createParameter("BackupDir", "", ParameterType.PARAM_TEXTBOX_MAX, "256",
				"Directory store filebackup in server", "9"));
		vtDefinition.addElement(createParameter("BackupFileFormat", "", ParameterType.PARAM_TEXTBOX_MAX, "256",
				"Format local file name.\r\n Can use $FileName,$BaseFileName,$FileExtension as parameter", "10"));
		vtDefinition.addElement(createParameter("TempDir", "", ParameterType.PARAM_TEXTBOX_MAX, "256",
				"Temp Directory in Local", "11"));
		vtDefinition.addElement(createParameter("LastProcessFileStamp", "", ParameterType.PARAM_TEXTBOX_MAX, "256",
				"CreateDateTime of Last File processed, default format = 'dd/MM/yyyy HH:mm:ss'", "12"));
		vtDefinition.addElement(
				createParameter("Wildcard", "", ParameterType.PARAM_TEXTBOX_MAX, "256", "Filler File Name", "13"));
		vtDefinition.addElement(createParameter("MaxTimeGetLastFile", "", ParameterType.PARAM_TEXTBOX_MAX, "256",
				"Time to wait get LastFile, > 1 minutes, second unit", "14"));
		vtDefinition.addElement(createParameter("ProcessDate", "", ParameterType.PARAM_TEXTBOX_MAX, "256",
				"Start Directory to Get file, FTPStyle equal Daily or Monthly or Yearly", "15"));
		vtDefinition.addElement(createParameter("LastTimeScanFile", "", ParameterType.PARAM_READONLY, "256",
				"Last Time Scan File, default format = 'dd/MM/yyyy HH:mm:ss'", "16"));
		vtDefinition.addElement(createParameter("LastFileStamp", "", ParameterType.PARAM_READONLY, "256",
				"Last Time Scan File, default format = 'dd/MM/yyyy HH:mm:ss'", "17"));
		vtDefinition.addElement(createParameter("LastFileName", "", ParameterType.PARAM_READONLY, "256",
				"Last Time Scan File, default format = 'dd/MM/yyyy HH:mm:ss'", "18"));
		vtValue = new Vector();
		vtValue.addElement("Active");
		vtValue.addElement("DeActive");
		vtDefinition.addElement(
				createParameter("Status", "", ParameterType.PARAM_COMBOBOX, vtValue, "Status of Thread", "19"));
		return vtDefinition;
	}

	@SuppressWarnings("rawtypes")
	public void fillParameter() throws AppException {
		mstrTransferType = loadString("TranferType");
		mstrConnectMode = loadString("ConnectMode");
		mstrListingMode = loadString("ListingMode");
		miTimeOut = (loadUnsignedInteger("TimeOut") * 1000);
		mstrFTPStyle = StringUtil.nvl(getParameter("FtpStyle"), "");
		mstrLocalStyle = StringUtil.nvl(getParameter("LocalStyle"), "");
		mstrBackupStyle = StringUtil.nvl(getParameter("BackupStyle"), "");
		mstrDateFormat = StringUtil.nvl(getParameter("DateFormat"), "");
		Object obj = getParameter("FTPSetting");
		if ((obj != null) && ((obj instanceof Vector))) {
			mvtFTPSetting = ((Vector) obj);
		} else {
			mvtFTPSetting = new Vector();
		}
		if (mvtFTPSetting == null) {
			mvtFTPSetting = new Vector();
		}

		super.fillParameter();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void validateParameter() throws Exception {
		miFTPNameIndex = 0;
		miHostIndex = 1;
		miPortIndex = 2;
		miUserIndex = 3;
		miPasswordIndex = 4;
		miFTPDirIndex = 5;
		miRecursiveIndex = 6;
		miLocalDirIndex = 7;
		miLocalFileFormatIndex = 8;
		miBackupDirIndex = 9;
		miBackupFileFormatIndex = 10;
		miTempDirIndex = 11;
		miLastProcessFileStampIndex = 12;
		miWildcardIndex = 13;
		miMaxTimeGetLastFileIndex = 14;
		miProcessDateIndex = 15;
		miLastTimeScanFileIndex = 16;
		miLastFileStampIndex = 17;
		miLastFileNameIndex = 18;
		miFTPStatus = 19;

		for (int i = 0; i < mvtFTPSetting.size(); i++) {
			Vector vtRow = (Vector) mvtFTPSetting.elementAt(i);
			if (vtRow.size() < miFTPStatus) {
				for (int j = vtRow.size(); j < miFTPStatus; j++) {
					vtRow.addElement("");
				}
			}
			miPort = loadInteger("FTPSetting.Port", (String) vtRow.elementAt(miPortIndex));
			mstrFTPName = loadString("FTPSetting.FTPName", (String) vtRow.elementAt(miFTPNameIndex));
			mstrHost = loadString("FTPSetting.Host", (String) vtRow.elementAt(miHostIndex));
			mstrUser = loadString("FTPSetting.User", (String) vtRow.elementAt(miUserIndex));
			mstrPassword = loadString("FTPSetting.Password", (String) vtRow.elementAt(miPasswordIndex));
			mstrFTPDir = loadString("FTPSetting.FTPDir", (String) vtRow.elementAt(miFTPDirIndex));
			mstrLocalDir = loadDirectory("FTPSetting.LocalDir", (String) vtRow.elementAt(miLocalDirIndex), true, true);
			mstrTempDir = loadDirectory("FTPSetting.TempDir", (String) vtRow.elementAt(miTempDirIndex), true, true);
			mstrWildcard = loadString("FTPSetting.Wildcard", (String) vtRow.elementAt(miWildcardIndex));
			String strProcessDate = StringUtil.nvl((String) vtRow.elementAt(miProcessDateIndex), "");
			mstrThreadStatus = loadString("FTPSetting.Status", (String) vtRow.elementAt(miFTPStatus));

			if ((mstrFTPStyle != null) && (mstrFTPStyle.length() > 0) && (!mstrFTPStyle.equals("Directly"))) {
				if ((mstrDateFormat == null) || (mstrDateFormat.length() == 0)) {
					throw new AppException("DateFormat cannot be null when FTPStyle='" + mstrFTPStyle + "'",
							"FTPReceiver.validateParameter", "DateFormat");
				}

				if ((mstrFTPStyle.equals("Daily")) && (mstrDateFormat.indexOf("dd") < 0)) {
					throw new AppException("DateFormat must contain 'dd' when FTPStyle='" + mstrFTPStyle + "'",
							"FTPReceiver.validateParameter", "DateFormat");
				}

				if ((mstrFTPStyle.equals("Monthly")) && (mstrDateFormat.indexOf("MM") < 0)) {
					throw new AppException("DateFormat must contain 'MM' when FTPStyle='" + mstrFTPStyle + "'",
							"FTPReceiver.validateParameter", "DateFormat");
				}

				if ((mstrFTPStyle.equals("Yearly")) && (mstrDateFormat.indexOf("yyyy") < 0)) {
					throw new AppException("DateFormat must contain 'yyyy' when FTPStyle='" + mstrFTPStyle + "'",
							"FTPReceiver.validateParameter", "DateFormat");
				}

				if ((strProcessDate == null) || (strProcessDate.length() == 0)) {
					throw new AppException("FTPSetting.ProcessDate cannot be null when FTPStyle='" + mstrFTPStyle + "'",
							"FTPReceiver.validateParameter", "ProcessDate");
				}

				if (!DateUtil.isDate(strProcessDate, mstrDateFormat)) {
					throw new AppException("FTPSetting.ProcessDate does not match DateFormat",
							"FTPReceiver.validateParameter", "ProcessDate");
				}
			}
		}
	}

	protected void beforeFTP() throws Exception {
		try {
			mstrHost = loadString("FTPSetting.Host", (String) mvtCurrentParams.elementAt(miHostIndex));
			mstrUser = loadString("FTPSetting.User", (String) mvtCurrentParams.elementAt(miUserIndex));
			mstrPassword = loadString("FTPSetting.Password", (String) mvtCurrentParams.elementAt(miPasswordIndex));
			miPort = loadInteger("FTPSetting.Port", (String) mvtCurrentParams.elementAt(miPortIndex));
			try {
				if (mftpMain != null) {
					mftpMain.quit();
				}
			} catch (Exception e) {
			}

			mftpMain = getFtpClient(mstrHost, miPort, mstrUser, mstrPassword);
			mftpMain.setConnectTimeout(miTimeOut);

			if (mstrTransferType.equalsIgnoreCase("ASCII")) {
				mftpMain.setFileTransferMode(FTP.ASCII_FILE_TYPE);
			} else {
				mftpMain.setFileTransferMode(FTP.BINARY_FILE_TYPE);
			}
			if (mstrConnectMode.equalsIgnoreCase("ACTIVE")) {
				mftpMain.enterLocalActiveMode();
			} else {
				mftpMain.enterLocalPassiveMode();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	protected void afterFTP() throws Exception {
		try {
			closeFtpClient(mftpMain);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			mftpMain = null;
		}
	}

	public void beforeSession() throws Exception {
		super.beforeSession();
	}

	public void processSession() throws Exception {
		for (int i = 0; (i < mvtFTPSetting.size()) && (miThreadCommand != 2); i++) {
			mvtCurrentParams = ((Vector) mvtFTPSetting.elementAt(i));
			if (mvtCurrentParams.elementAt(miFTPStatus).equals("Active")) {
				try {
					beforeFTP();
					processFTP();
					afterFTP();
				} catch (Exception e) {
					e.printStackTrace();
					logMonitor("Error occured:" + e.getMessage());
					throw e;
				}
			}
		}
	}

	public void afterSession() throws Exception {
		super.afterSession();
	}

	public static FTPClient getFtpClient(String host, int port, String username, String password)
			throws SocketException, IOException {
		String LOCAL_CHARSET = "GB18030";
		FTPClient ftpClient = new FTPClient();
		ftpClient.connect(host, port);
		if (FTPReply.isPositiveCompletion(ftpClient.sendCommand("OPTS UTF8", "ON"))) {
			LOCAL_CHARSET = "UTF-8";
		}
		ftpClient.setControlEncoding(LOCAL_CHARSET);
		ftpClient.login(username, password);
		ftpClient.setBufferSize(1024 * 1024 * 16);
		ftpClient.enterLocalPassiveMode();
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		ftpClient.setControlKeepAliveTimeout(60);
		return ftpClient;
	}

	public static void closeFtpClient(FTPClient ftpClient) throws Exception {
		if (ftpClient != null) {
			ftpClient.quit();
		}
	}
}
