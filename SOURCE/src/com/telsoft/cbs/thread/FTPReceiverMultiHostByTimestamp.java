package com.telsoft.cbs.thread;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import com.telsoft.util.DateUtil;
import com.telsoft.util.FileUtil;
import com.telsoft.util.StringUtil;

public class FTPReceiverMultiHostByTimestamp extends AbstractFTPThreadMultiHostByTimestamp {
	protected int miListItemCount;
	protected Vector mvtFileList;
	protected Hashtable mprtDirectoryList;
	protected String mstrLocalFileFormat;
	protected String mstrBackupDir;
	protected String mstrBackupFileFormat;
	protected String mstrLastProcessFileStamp;
	protected String mstrMaxTimeGetLastFile;
	protected String mstrProcessDate;
	protected String mstrCurrScanDir;
	protected String mstrLastTimeScanFile;
	protected String mstrDirectBackupDir;
	protected String mstrStorageDir;
	protected String mstrLastFileStamp;
	protected String mstrLastFileName;
	protected boolean mbLastDir;
	protected boolean mbRecursive;
	protected String mstrNextProcessDate;
	protected boolean mblnChangeProcessDate;
	public void beforeListFile() throws Exception {
		
	}

	public void listfile() throws Exception {
		try {
			beforeListFile();
			mprtDirectoryList = new Hashtable();
			mvtFileList = new Vector();
			listFile("");
			sortFileList();
			afterListFile();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	protected void listFile(String strAdditionPath) throws Exception {
		if ((mstrFTPStyle != null) && (mstrFTPStyle.length() > 0) && (!mstrFTPStyle.equals("Directly"))) {
			mstrCurrScanDir = (mstrFTPDir + mstrProcessDate + "/");
		} else {
			mstrCurrScanDir = mstrFTPDir;
		}
		try {
			mftpMain.changeWorkingDirectory(mstrCurrScanDir + strAdditionPath);
		} catch (Exception ex) {
			throw new Exception(mstrFTPName + " :Could not change working directory to remote directory ("
					+ mstrCurrScanDir + strAdditionPath + ")");
		}
		listFile( mftpMain, mstrCurrScanDir + strAdditionPath, mstrWildcard, mbRecursive);
	}
	public void listFile(FTPClient ftpClient, String path, String filenameRegexp,
			boolean recursive) throws IOException {
		for (FTPFile ftpFile : ftpClient.listFiles(path)) {
			if (ftpFile.isFile()) {
				Date dtFileStamp = ftpFile.getTimestamp().getTime();
				String strRemoteFilePath = StringUtils.appendIfMissing(mstrCurrScanDir + path, "/", "/")+ ftpFile.getName();
				String strPath = StringUtils.removeStart(StringUtils.appendIfMissing( path, "/", "/")+ ftpFile.getName(),mstrCurrScanDir);
				Date dtLastStamp = DateUtil.toDate(mstrLastProcessFileStamp, "dd/MM/yyyy HH:mm:ss");
				if (Pattern.matches(filenameRegexp,strPath) && dtFileStamp.compareTo(dtLastStamp)>=0 && !strRemoteFilePath.equals(mstrLastFileName)) {
					mvtFileList.addElement(ftpFile);
					mprtDirectoryList.put(ftpFile, StringUtils.removeStart(path, mstrCurrScanDir));
				}
			} else if (recursive && ftpFile.isDirectory()) {
				listFile(ftpClient, path + ftpFile.getName()+ "/" , filenameRegexp, recursive);
			}
		}
	}
	
	public void afterListFile() throws Exception {
		mbLastDir = true;
		mblnChangeProcessDate = false;
		if ((mstrFTPStyle != null) && (!mstrFTPStyle.equals("")) && (!mstrFTPStyle.equals("Directly"))) {
			Date dt = DateUtil.toDate(mstrProcessDate, mstrDateFormat);
			if (mstrFTPStyle.equals("Daily")) {
				dt = DateUtil.addDay(dt, 1);
			} else if (mstrFTPStyle.equals("Monthly")) {
				dt = DateUtil.addMonth(dt, 1);
			} else if (mstrFTPStyle.equals("Yearly")) {
				dt = DateUtil.addYear(dt, 1);
			}
			mstrNextProcessDate = StringUtil.format(dt, mstrDateFormat);
			FTPFile[] listFile = mftpMain.listDirectories(mstrFTPDir + mstrNextProcessDate);
			if ((listFile != null) && (listFile.length > 0)) {
				mbLastDir = false;
			}
		}
		if (mbLastDir) {
			Date dtCurrent = new Date();
			if (mvtFileList.size() > 0) {
				Date dtStampFirstFile = ((FTPFile) mvtFileList.firstElement()).getTimestamp().getTime();
				Date dtStampLastFile = ((FTPFile) mvtFileList.lastElement()).getTimestamp().getTime();
				Date dtLastFileStamp = null;
				if (!mstrLastFileStamp.equals("")) {
					dtLastFileStamp = DateUtil.toDate(mstrLastFileStamp, "dd/MM/yyyy HH:mm:ss");
				}
				Date dtLastScan;
				if (!mstrLastTimeScanFile.equals("")) {
					dtLastScan = DateUtil.toDate(mstrLastTimeScanFile, "dd/MM/yyyy HH:mm:ss");
				} else {
					dtLastScan = dtCurrent;
				}

				if (dtStampFirstFile.compareTo(dtStampLastFile) != 0) {
					for (int i = mvtFileList.size() - 1; i >= 0; i--) {
						FTPFile fl = (FTPFile) mvtFileList.elementAt(i);
						if (fl.getTimestamp().getTime().compareTo(dtStampLastFile) == 0) {
							mvtFileList.removeElementAt(i);
						} else {
							dtStampFirstFile = fl.getTimestamp().getTime();
							break;
						}
					}
					mvtCurrentParams.setElementAt(StringUtil.format(dtCurrent, "dd/MM/yyyy HH:mm:ss"),
							miLastTimeScanFileIndex);

					mvtCurrentParams.setElementAt(StringUtil.format(dtStampLastFile, "dd/MM/yyyy HH:mm:ss"),
							miLastFileStampIndex);

					setParameter("FTPSetting", mvtFTPSetting);
				} else {
					if ((dtCurrent.getTime() - dtLastScan.getTime()) / 1000L < Integer
							.parseInt(StringUtil.nvl(mstrMaxTimeGetLastFile, "0"))) {
						mvtFileList.removeAllElements();
					} else if ((mstrLastFileStamp.equals("")) || (dtLastFileStamp.compareTo(dtStampLastFile) != 0)) {
						mvtFileList.removeAllElements();
						mvtCurrentParams.setElementAt(StringUtil.format(dtStampLastFile, "dd/MM/yyyy HH:mm:ss"),
								miLastFileStampIndex);

						mvtCurrentParams.setElementAt(StringUtil.format(dtCurrent, "dd/MM/yyyy HH:mm:ss"),
								miLastTimeScanFileIndex);
					} else {
						mvtCurrentParams.setElementAt(StringUtil.format(dtCurrent, "dd/MM/yyyy HH:mm:ss"),
								miLastTimeScanFileIndex);
					}

					setParameter("FTPSetting", mvtFTPSetting);
				}
			} else {
				if ((!mstrFTPStyle.equals("")) && (!mstrFTPStyle.equals("Directly"))) {
					mblnChangeProcessDate = true;
				}
				mvtCurrentParams.setElementAt(StringUtil.format(dtCurrent, "dd/MM/yyyy HH:mm:ss"),
						miLastTimeScanFileIndex);

				setParameter("FTPSetting", mvtFTPSetting);
			}

		} else {
			Date dtCurrent = new Date();
			mvtCurrentParams.setElementAt(StringUtil.format(dtCurrent, "dd/MM/yyyy HH:mm:ss"), miLastTimeScanFileIndex);
			if (mvtFileList.size() > 0) {
				Date dtStampLastFile = ((FTPFile) mvtFileList.lastElement()).getTimestamp().getTime();
				mvtCurrentParams.setElementAt(StringUtil.format(dtStampLastFile, "dd/MM/yyyy HH:mm:ss"),
						miLastFileStampIndex);
			}
			setParameter("FTPSetting", mvtFTPSetting);
		}
	}

	protected void sortFileList() throws Exception {
		Collections.sort(mvtFileList, new Comparator() {
			public int compare(Object obj1, Object obj2) {
				return ((FTPFile) obj1).getTimestamp().compareTo(((FTPFile) obj2).getTimestamp());
			}
		});
	}

	public void beforeProcessFileList() throws Exception {
		logMonitor("=========================================");
		logMonitor("Start Processing " + mstrFTPName);
	}

	public void afterProcessFileList() throws Exception {
	}

	public void getParam() throws Exception {
		try {
			mstrFTPName = loadString("FTPSetting.FTPName", (String) mvtCurrentParams.elementAt(miFTPNameIndex));
			mstrFTPDir = loadString("FTPSetting.FTPDir", (String) mvtCurrentParams.elementAt(miFTPDirIndex));
			if ((!mstrFTPDir.endsWith("/")) && (!mstrFTPDir.endsWith("\\")) && (!mstrFTPDir.equals(""))) {
				mstrFTPDir += "/";
			}
			mstrLocalDir = loadDirectory("FTPSetting.LocalDir", (String) mvtCurrentParams.elementAt(miLocalDirIndex),
					true, true);

			mstrTempDir = loadDirectory("FTPSetting.TempDir", (String) mvtCurrentParams.elementAt(miTempDirIndex), true,
					true);

			mstrWildcard = loadString("FTPSetting.Wildcard", (String) mvtCurrentParams.elementAt(miWildcardIndex));
			mstrLocalFileFormat = ((String) mvtCurrentParams.elementAt(miLocalFileFormatIndex));
			mstrBackupDir = ((String) mvtCurrentParams.elementAt(miBackupDirIndex));
			if ((!mstrBackupDir.endsWith("/")) && (!mstrBackupDir.endsWith("\\")) && (!mstrBackupDir.equals(""))) {
				mstrBackupDir += "/";
			}
			mstrBackupFileFormat = ((String) mvtCurrentParams.elementAt(miBackupFileFormatIndex));
			mstrLastProcessFileStamp = ((String) mvtCurrentParams.elementAt(miLastProcessFileStampIndex));
			if (mstrLastProcessFileStamp.equals("")) {
				mstrLastProcessFileStamp = "01/01/2000 01:01:01";
			}
			mstrMaxTimeGetLastFile = ((String) mvtCurrentParams.elementAt(miMaxTimeGetLastFileIndex));

			mstrProcessDate = ((String) mvtCurrentParams.elementAt(miProcessDateIndex));
			mstrLastTimeScanFile = StringUtil.nvl(mvtCurrentParams.elementAt(miLastTimeScanFileIndex), "");
			mstrLastFileStamp = StringUtil.nvl(mvtCurrentParams.elementAt(miLastFileStampIndex), "");
			mstrLastFileName=StringUtil.nvl(mvtCurrentParams.elementAt(miLastFileNameIndex), "");
			mbRecursive=mvtCurrentParams.elementAt(miRecursiveIndex).equals("Y");
		} catch (Exception e) {
			throw e;
		}
	}

	public void processFTP() throws Exception {
		try {
			getParam();
			listfile();
			miListItemCount = mvtFileList.size();
			if (miListItemCount > 0) {
				beforeProcessFileList();
				for (int iIndex = 0; (iIndex < miListItemCount) && (miThreadCommand != 2); /* 325 */ iIndex++) {
					process(iIndex);
				}
				afterProcessFileList();
			}
			if ((!mbLastDir) && (miThreadCommand != 2)) {
				mvtCurrentParams.setElementAt(mstrNextProcessDate, miProcessDateIndex);
				setParameter("FTPSetting", mvtFTPSetting);
			}
			if ((mblnChangeProcessDate) && (miThreadCommand != 2)) {
				Date dt = DateUtil.toDate(mstrProcessDate, mstrDateFormat);
				Date dtCurrent = new Date();
				dtCurrent = DateUtil.toDate(StringUtil.format(dtCurrent, mstrDateFormat), mstrDateFormat);
				while (dt.before(dtCurrent)) {
					if (mstrFTPStyle.equals("Daily")) {
						dt = DateUtil.addDay(dt, 1);
					} else if (mstrFTPStyle.equals("Monthly")) {
						dt = DateUtil.addMonth(dt, 1);
					} else if (mstrFTPStyle.equals("Yearly")) {
						dt = DateUtil.addYear(dt, 1);
					}
					mstrNextProcessDate = StringUtil.format(dt, mstrDateFormat);
					try {
						mftpMain.changeWorkingDirectory(mstrFTPDir + mstrNextProcessDate);
						mvtCurrentParams.setElementAt(mstrNextProcessDate, miProcessDateIndex);
						setParameter("FTPSetting", mvtFTPSetting);
					} catch (Exception ex1) {
						ex1.printStackTrace();
					}
				}
			}

			storeConfig();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	protected String validateFile(FTPFile ffl) throws Exception {
		return "";
	}

	public void process(int iFileIndex) throws Exception {
		FTPFile ffl = (FTPFile) mvtFileList.elementAt(iFileIndex);
		String strValidateResult = validateFile(ffl);
		boolean bResult = (strValidateResult == null) || (strValidateResult.length() == 0);
		if (!bResult) {
			logMonitor(strValidateResult);
		} else {
			getFile(ffl);
		}
	}

	protected void beforeGetFile(FTPFile ffl, String strRemoteDir, String strHost, String strRemoteStyle)
			throws Exception {
		mstrStorageDir = mstrLocalDir;
		if (mstrLocalStyle.equals("Daily")) {
			mstrStorageDir = (mstrStorageDir + StringUtil.format(new Date(), mstrDateFormat) + "/");
		} else if (mstrLocalStyle.equals("Monthly")) {
			mstrStorageDir = (mstrStorageDir + StringUtil.format(new Date(), mstrDateFormat) + "/");
		} else if (mstrLocalStyle.equals("Yearly")) {
			mstrStorageDir = (mstrStorageDir + StringUtil.format(new Date(), mstrDateFormat) + "/");
		}
		FileUtil.forceFolderExist(mstrStorageDir);
		logMonitor("Start getting file " + strRemoteDir+ffl.getName() + " from ftp server");
	}

	protected void getFile(FTPFile ffl) throws Exception {
		try {
			String strAdditionPath = StringUtil.nvl(mprtDirectoryList.get(ffl), "");
			beforeGetFile(ffl, mstrCurrScanDir + strAdditionPath, mstrHost, mstrFTPStyle);
			String strRemoteFilePath = StringUtils.appendIfMissing(mstrCurrScanDir + strAdditionPath, "/", "/")+ ffl.getName();
			
			FileOutputStream os = null;
			try {
				os = new FileOutputStream(mstrTempDir + ffl.getName());
				mftpMain.retrieveFile(strRemoteFilePath, os);
			} catch (Exception e) {
				throw new Exception(mstrFTPName + " :Download file failed:\r\n\t\t" + e.getMessage());
			} finally {
				FileUtil.safeClose(os);
			}

			File fl = new File(mstrTempDir + ffl.getName());
			if (!fl.exists()) {
				throw new Exception(mstrFTPName + " :Download file failed, file does not exist");
			}
			if (fl.length() != ffl.getSize()) {
				throw new Exception(mstrFTPName + " :Getted file size does not equals to ftp file size");
			}

			String strGettedFilePath = FileUtil.backup(mstrTempDir, mstrLocalDir, ffl.getName(),
					FileUtil.formatFileName(ffl.getName(), mstrLocalFileFormat), mstrLocalStyle, StringUtils.appendIfMissing(strAdditionPath, "/", "/"));
			try {
				afterGetFile(ffl, mstrLocalDir + strAdditionPath,strRemoteFilePath);
			} catch (Exception e) {
				FileUtil.deleteFile(strGettedFilePath);
				throw e;
			}
		} catch (Exception e) {
			logMonitor("Error: " + e.getMessage());
			throw e;
		}
	}

	protected void afterGetFile(FTPFile ffl, String strLocalDir,String strRemoteFilePath) throws Exception {
		Date dtLastFileStampProcess = ffl.getTimestamp().getTime();
		mvtCurrentParams.setElementAt(StringUtil.format(dtLastFileStampProcess, "dd/MM/yyyy HH:mm:ss"),
				miLastProcessFileStampIndex);
		mvtCurrentParams.setElementAt(strRemoteFilePath, miLastFileNameIndex);
		setParameter("FTPSetting", mvtFTPSetting);
		storeConfig();
		logMonitor("Complete store file to '"+strLocalDir + ffl.getName() + "'. (Size: " +  ffl.getSize() + " bytes, TimeStamp: "
				+ StringUtil.format(ffl.getTimestamp().getTime(), "yyyy/MM/dd HH:mm:ss") + ")");
	}
}
