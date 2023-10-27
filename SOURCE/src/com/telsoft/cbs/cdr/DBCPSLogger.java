package com.telsoft.cbs.cdr;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.CBStore;
import com.telsoft.cbs.domain.REQUEST_STATUS;
import com.telsoft.database.Database;
import com.telsoft.database.batch.BatchError;
import com.telsoft.database.batch.BatchStatement;
import com.telsoft.thread.ManageableThread;
import com.telsoft.thread.ParameterType;
import com.telsoft.thread.ThreadConstant;
import com.telsoft.util.AppException;
import com.telsoft.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import telsoft.gateway.DBParameter;
import telsoft.gateway.core.GatewayManager;
import telsoft.gateway.core.component.StatusClassifier;
import telsoft.gateway.core.log.AbstractTransactionLogger;
import telsoft.gateway.core.log.MessageContext;
import telsoft.gateway.core.log.data.LogDataHolder;
import telsoft.gateway.core.log.data.SessionRequestInfo;
import telsoft.gateway.core.log.data.TranslatorLogger;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class DBCPSLogger extends AbstractTransactionLogger {
	private final Logger logger = LoggerFactory.getLogger(DBCPSLogger.class);
	private final String DEFAULT_DELEMITER = ",";
	private int miSuccessCount = 0;
	private int miFailureCount = 0;
	private int miTotalCount = 0;
	private int miLogCount = 0;
	private DBParameter dbpool = new DBParameter();
	private String mLoggerName = "";
	private Map<String, MapCDRCPSEnt> mMapMapingCDRField = new LinkedHashMap();

	public static void StatementSetString(BatchStatement stat, int iIndex, String str, int iLimit) throws SQLException {
		if (iLimit > 0) {
			stat.setString(iIndex, correctParameterLength(str, iLimit));
		} else {
			stat.setString(iIndex, str);
		}

	}

	public static String correctParameterLength(String strParame, int iLimit) {
		if (strParame == null) {
			return "";
		} else {
			return strParame.length() > iLimit ? strParame.substring(0, iLimit) : strParame;
		}
	}

	public static String buildSQLInsertColumns(Map<String, MapCDRCPSEnt> mapMapingCDRField) {
		StringBuilder result = new StringBuilder();
		if (mapMapingCDRField != null && !mapMapingCDRField.isEmpty()) {
			for (MapCDRCPSEnt mapColumn : mapMapingCDRField.values()) {
				if (mapColumn != null && !StringUtils.isEmpty(mapColumn.getDbColumnName()) && !StringUtils.isEmpty(mapColumn.getDataField())) {
					result.append(",").append(mapColumn.getDbColumnName());
				}
			}
		}
		return result.toString();
	}

	public static String buildSQLValueColumns(Map<String, MapCDRCPSEnt> mapMapingCDRField) {
		StringBuilder result = new StringBuilder();
		if (mapMapingCDRField != null && !mapMapingCDRField.isEmpty()) {
			for (MapCDRCPSEnt mapColumn : mapMapingCDRField.values()) {
				if (mapColumn != null && !StringUtils.isEmpty(mapColumn.getDbColumnName()) && !StringUtils.isEmpty(mapColumn.getDataField())) {
					if (MAPPING_DATA_TYPE.DATE.name().equalsIgnoreCase(mapColumn.getType())) {
						result.append(",TO_TIMESTAMP(?,'YYYYMMDDHH24MISSFF')");
					} else {
						result.append(",?");
					}
				}

			}
		}
		return result.toString();
	}

	public Vector getParameterDefinition() {
		Vector vtReturn = new Vector();
		this.dbpool.getParameterDefinition(this, vtReturn);


		Vector vtMapLoger = new Vector();
		vtMapLoger.add(ManageableThread.createParameter("DataField", "", ParameterType.PARAM_TEXTBOX_MAX, "200"));
		vtMapLoger.add(ManageableThread.createParameter("DBColumnName", "", ParameterType.PARAM_TEXTBOX_MAX, "100"));
		vtMapLoger.add(ManageableThread.createParameter("Type", "", ParameterType.PARAM_COMBOBOX, MAPPING_DATA_TYPE.class));
		vtReturn.add(ManageableThread.createParameter("MappingCDRFields", "", ParameterType.PARAM_TABLE, vtMapLoger, "Mapping field to DB columns", "", "CDR Definition"));
		vtReturn.add(ManageableThread.createParameter("CDRRejectedLoggerName", "", ParameterType.PARAM_TEXTBOX_MAX, "200", "Logger log4j write CDR is rejected", "", "CDR Definition"));

		vtReturn.addAll(super.getParameterDefinition());
		return vtReturn;
	}

	public void validateParameter() throws Exception {
		super.validateParameter();
		this.dbpool.validateParameter(this);
	}

	public void fillParameter() throws AppException {
		super.fillParameter();
		this.dbpool.fillParameter(this);

		mLoggerName = StringUtil.nvl(getParameter("CDRRejectedLoggerName"),"");

		Object o = getParameter("MappingCDRFields");
		if (o != null && o instanceof Vector) {
			Map mapMapingCDRField = new LinkedHashMap();
			Vector<Vector<String>> vt = (Vector) o;
			Iterator i$ = vt.iterator();
			while (i$.hasNext()) {
				Vector<String> row = (Vector) i$.next();
				String fieldCDRName = StringUtil.nvl(row.get(0), "");
				String dBColumnName = StringUtil.nvl(row.get(1), "");
				String type = StringUtil.nvl(row.get(2), "");
				if (!StringUtils.isEmpty(fieldCDRName) && !StringUtils.isEmpty(dBColumnName)) {
					MapCDRCPSEnt ent = new MapCDRCPSEnt();
					ent.setDataField(fieldCDRName);
					ent.setDbColumnName(dBColumnName);
					ent.setType(type);
					mapMapingCDRField.put(fieldCDRName, ent);
				}
			}

			this.mMapMapingCDRField = mapMapingCDRField;
		}

	}

	public void run() {
		try {
			super.run();
		} finally {
			try {
				this.dbpool.closePool(this);
			} catch (Exception var7) {
			}

		}

	}

	protected void processSession() throws Exception {
		this.mlLastAvaiable = System.currentTimeMillis();

		while (this.miThreadCommand != 2 && !this.mmgrMain.isServerLocked()) {
			this.fillLogFile();
			validateParameter();
			this.doLogging();

			for (int iIndex = 0; iIndex < this.miDelayTime && this.miThreadCommand != 2 && !this.mbIsLogBeforeClose; ++iIndex) {
				Thread.sleep(1000L);
			}

			if (this.mbIsLogBeforeClose) {
				break;
			}
		}

	}

	private void doLogging() throws Exception {
		Connection cn = null;
		BatchStatement stmt = null;
		this.miSuccessCount = 0;
		this.miFailureCount = 0;
		this.miTotalCount = 0;
		this.miLogCount = 0;
		Vector vtLogged = new Vector();

		try {
			if (!this.dbpool.hasInited()) {
				this.dbpool.initPool(1, this);
			}

			cn = this.dbpool.getDBConnection();
			String strSQL = "insert into log_cps_cdr (request_time,response_time,isdn,store_id,status,command,transaction_id,store_transaction_id,request_id\n" +
					buildSQLInsertColumns(mMapMapingCDRField) +
					") \n" +
					"    values(TO_TIMESTAMP(?,'YYYYMMDDHH24MISSFF'),TO_TIMESTAMP(?,'YYYYMMDDHH24MISSFF'),?,?,?,?,?,?,?" +
					buildSQLValueColumns(mMapMapingCDRField) +
					")";

			stmt = new BatchStatement(cn, strSQL);
			Map mapSession = this.logAllAvailableRecord(stmt, vtLogged);
			Iterator iterator = mapSession.entrySet().iterator();
			if (iterator.hasNext()) {
				this.mlLastAvaiable = System.currentTimeMillis();
			} else {
				long lExpire = System.currentTimeMillis() - (long) (this.miEmptyDuration * 1000);
				if (this.miEmptyDuration > 0 && this.mlLastAvaiable < lExpire && this.mlCheckAvaiable < lExpire) {
					Date dt = new Date();
					SimpleDateFormat FORMAT_TIME = new SimpleDateFormat("HH:mm:ss");
					if ((this.mdtCheckEmptyFrom == null || FORMAT_TIME.format(dt).compareTo(FORMAT_TIME.format(this.mdtCheckEmptyFrom)) >= 0) && (this.mdtCheckEmptyUntil == null || FORMAT_TIME.format(dt).compareTo(FORMAT_TIME.format(this.mdtCheckEmptyUntil)) <= 0)) {
						this.logMonitor("There is no log record available for " + (System.currentTimeMillis() - this.mlLastAvaiable) + " miliseconds");
					}

					if (this.mlCheckAvaiable < this.mlLastAvaiable) {
						this.mlCheckAvaiable = this.mlLastAvaiable;
					}

					this.mlCheckAvaiable += (long) (this.miEmptyDuration * 1000);
				}
			}

			Vector<BatchError> vtError = stmt.executeBatch();
			this.miFailureCount += vtError.size();
			this.miSuccessCount = this.miLogCount - vtError.size();

			List<MessageContext> listErrorRejected = new ArrayList<MessageContext>();
			while (vtError.size() > 0) {
				BatchError recordError = (BatchError) vtError.remove(0);
				this.logMonitor(recordError.getFullMessage());
				MessageContext ctx = (MessageContext) recordError.getUserObject();
				if(!listErrorRejected.contains(ctx)) {
					this.reject(ctx);
					vtLogged.remove(ctx);
					listErrorRejected.add(ctx);
				}
			}

			while (vtLogged.size() > 0) {
				MessageContext record = (MessageContext) vtLogged.remove(0);
				this.accepted(record);
			}

			cn.commit();
			cn.setAutoCommit(true);
			if (this.miSuccessCount > 0 || this.miFailureCount > 0) {
				this.logMonitor("Logging completed" + (this.miTotalCount > 0 ? "\r\n\t" + this.miTotalCount + " records was detach" : "") + (this.miLogCount > 0 ? "\r\n\t" + this.miLogCount + " records was logged" : "") + (this.miSuccessCount > 0 ? "\r\n\t" + this.miSuccessCount + " records was imported" : "") + (this.miFailureCount > 0 ? "\r\n\t" + this.miFailureCount + " records was rejected" : ""));
			}
		} catch (Exception var15) {
			logMonitor("Error when doLogging: " + var15.getMessage());
			this.logger.error("transaction log {}", var15);

			while (vtLogged.size() > 0) {
				MessageContext record = (MessageContext) vtLogged.remove(0);
				this.reject(record);
				++this.miFailureCount;
			}

			if (cn != null) {
				cn.rollback();
				cn.setAutoCommit(true);
			}

			if (this.miFailureCount > 0) {
				this.logMonitor("Logging completed\r\n\t" + this.miFailureCount + " records was rejected");
			}
		} finally {
			Database.closeObject(stmt);
			Database.closeObject(cn);
		}

	}

	private Map logAllAvailableRecord(BatchStatement stmt, Vector vtLogged) {
		Map mapSession = new LinkedHashMap();
		MessageContext record = null;
		StatusClassifier sc = ((GatewayManager) this.mmgrMain).getStatusClassifier();

		while (miThreadCommand != ThreadConstant.THREAD_STOP && miThreadStatus != ThreadConstant.THREAD_STOPPED
				&& (record = ((GatewayManager) this.mmgrMain).getLogger().detachLogRecord(this.miStorageLevel)) != null) {
			++this.miTotalCount;

			try {
				record.correctData(sc);
				SessionRequestInfo inf = (SessionRequestInfo) mapSession.get(record.getSessionID());
				if (inf == null) {
					inf = new SessionRequestInfo();
					mapSession.put(record.getSessionID(), inf);
				}

				if (this.logOneRecord(record, stmt, inf)) {
					++this.miLogCount;
					vtLogged.add(record);
				}
			} catch (Exception var7) {
				++this.miFailureCount;
				if (this.mbDisplayStackTrace) {
					this.logger.error("log transaction error {}", var7);
				}

				this.reject(record);
			}
		}

		return mapSession;
	}

	private boolean logOneRecord(MessageContext record, BatchStatement stmt, SessionRequestInfo inf) throws Exception {
		// Don't log emty request
		if (record.getRequest() == null) {
			return false;
		}

		String strRequest = record.getRequest().getContent();
		if (strRequest == null || strRequest.trim().length() == 0) {
			return false;
		}

		// BEGIN Original code
		TranslatorLogger log = getLogger(record.getServerProtocol(), record.getClientProtocol());
		if (log == null) {
			return false;
		}

		LogDataHolder logDataHolder = log.getLogData(this, record);
		if (logDataHolder == null) {
			return false;
		}
		String command = "";
		if (record.getRequest() != null && !StringUtils.isEmpty(record.getRequest().getCommandName())) {
			command = record.getRequest().getCommandName();
		} else {
			command = record.getCommand();
		}
		CBStore store = (CBStore) record.getProperty(CbsContansts.STORE);
		Map mapFullRequest = (ConcurrentHashMap) record.getProperty(CbsContansts.MAP_FULL_REQUEST);

		if (mapFullRequest == null) {
			return false;
		}
		List<Map> listCDR = (List<Map>) mapFullRequest.get(CbsContansts.CPS_CDR_LIST);

		if (listCDR == null) {
			return false;
		}
//        List<ServerCommand> vtServerCommands = record.getServerCommand();

		for (int i = 0; i < listCDR.size(); i++) {
			// set transaction value
			StatementSetString(stmt, 1, this.formatDate1(record.getClientRequestDate()), 0);
			StatementSetString(stmt, 2, this.formatDate1(record.getClientResponseDate()), 0);
			StatementSetString(stmt, 3, StringUtil.nvl(record.getIsdn(), ""), 20);
			StatementSetString(stmt, 4, StringUtil.nvl(store != null ? store.getStoreId() : null, ""), 10);
			StatementSetString(stmt, 5, REQUEST_STATUS.convertMessageContextStatus(record.getStatus()).getCode(), 10);
			StatementSetString(stmt, 6, StringUtil.nvl(command, ""), 30);
			StatementSetString(stmt, 7, StringUtil.nvl(record.getTransID(), ""), 128);
			StatementSetString(stmt, 8, record.getPropertyNvl(CbsContansts.STORE_TRANSACTION_ID, ""), 128);
			StatementSetString(stmt, 9, record.getPropertyNvl(CbsContansts.REQUEST_ID, ""), 128);

			// set cdr value
			int iDefaultIndex = 10;
			Map mapCPSCDR = listCDR.get(i);
			for (Map.Entry<String, MapCDRCPSEnt> entry : mMapMapingCDRField.entrySet()) {
				if (!StringUtils.isEmpty(entry.getKey()) && entry.getValue() != null) {
					Object value = mapCPSCDR.get(entry.getKey());
					if (value != null && value instanceof Date) {
						StatementSetString(stmt, iDefaultIndex, this.formatDate1((Date) value), 0);
					} else {
						StatementSetString(stmt, iDefaultIndex, StringUtil.nvl(value, ""), 500);
					}
					iDefaultIndex++;
				}

			}


			StringBuffer strError = new StringBuffer();
			if (!record.getStatus().equals("0")) {
				++inf.miFailureCount;
			} else {
				++inf.miSuccessCount;
			}

			try {
				stmt.addBatch(record);
			} catch (SQLException var10) {
				this.logger.error("ADD BATCH {}", var10);
				strError.append(var10.toString());
				strError.append("\n");
			}

			if (strError.length() > 0) {
				throw new Exception(strError.toString());
			}
		}
		return true;

	}

	public final SimpleDateFormat DATE_FORMAT() {
		return new SimpleDateFormat("yyyyMMddHHmmssSSS");
	}

	public final String formatDate1(Date dt) {
		return dt == null ? "" : this.DATE_FORMAT().format(dt);
	}

	public boolean writeLogRejected(MessageContext record) {
		StringBuilder logContent = new StringBuilder();
		try {

			if (record.getRequest() == null) {
				return false;
			}

			String strRequest = record.getRequest().getContent();
			if (strRequest == null || strRequest.trim().length() == 0) {
				return false;
			}

			String command = "";
			if (record.getRequest() != null && !StringUtils.isEmpty(record.getRequest().getCommandName())) {
				command = record.getRequest().getCommandName();
			} else {
				command = record.getCommand();
			}
			CBStore store = (CBStore) record.getProperty(CbsContansts.STORE);
			Map mapFullRequest = (ConcurrentHashMap) record.getProperty(CbsContansts.MAP_FULL_REQUEST);

			if (mapFullRequest == null) {
				return false;
			}
			List<Map> listCDR = (List<Map>) mapFullRequest.get(CbsContansts.CPS_CDR_LIST);

			if (listCDR == null) {
				return false;
			} else if (!listCDR.isEmpty()) {
				logContent.append("\n--REJECTED_RECORDS---\n");
			}

			for (int i = 0; i < listCDR.size(); i++) {
				StringBuilder aLog = new StringBuilder();
				// set transaction value
				aLog.append(this.formatDate1(record.getClientRequestDate())).append(DEFAULT_DELEMITER);
				aLog.append(this.formatDate1(record.getClientResponseDate())).append(DEFAULT_DELEMITER);
				aLog.append(correctParameterLength(StringUtil.nvl(record.getIsdn(), ""), 20)).append(DEFAULT_DELEMITER);
				aLog.append(correctParameterLength(StringUtil.nvl(store != null ? store.getStoreId() : null, ""), 10)).append(DEFAULT_DELEMITER);
				aLog.append(correctParameterLength(REQUEST_STATUS.convertMessageContextStatus(record.getStatus()).getCode(), 10)).append(DEFAULT_DELEMITER);
				aLog.append(correctParameterLength(StringUtil.nvl(command, ""), 30)).append(DEFAULT_DELEMITER);
				aLog.append(correctParameterLength(StringUtil.nvl(record.getTransID(), ""), 128)).append(DEFAULT_DELEMITER);
				aLog.append(correctParameterLength(record.getPropertyNvl(CbsContansts.STORE_TRANSACTION_ID, ""), 128)).append(DEFAULT_DELEMITER);
				aLog.append(correctParameterLength(record.getPropertyNvl(CbsContansts.REQUEST_ID, ""), 128));

				// set cdr value
				int iDefaultIndex = 10;
				Map mapCPSCDR = listCDR.get(i);
				for (Map.Entry<String, MapCDRCPSEnt> entry : mMapMapingCDRField.entrySet()) {
					if (!StringUtils.isEmpty(entry.getKey()) && entry.getValue() != null) {
						Object value = mapCPSCDR.get(entry.getKey());
						if (value != null && value instanceof Date) {
							aLog.append(DEFAULT_DELEMITER).append(this.formatDate1((Date) value));
						} else {
							aLog.append(DEFAULT_DELEMITER).append(correctParameterLength(StringUtil.nvl(value, ""), 500));
						}
						iDefaultIndex++;
					}

				}

				if(!StringUtils.isEmpty(mLoggerName)){
					Logger loggerCDR = LoggerFactory.getLogger(mLoggerName);
					loggerCDR.info(aLog.toString());
				}
				logContent.append(aLog).append("\n");

			}
			if (!listCDR.isEmpty()) {
				logContent.append("--REJECTED_RECORDS_END---\n");
			}

			logMonitor(logContent.toString());
			logger.error(logContent.toString());


			return true;
		} catch (Exception ex) {
			logMonitor("Error when writeLogRejected: " + ex.getMessage() + "\nRecord:" + record.toString());
			logger.error("Error when writeLogRejected {} \n" +
                    "Record:{}", ex, record);
		}
		return false;
	}

	protected void reject(MessageContext record) {
		if (this.miFailureAction == 2 || this.miFailureAction == 0) {
			writeLogRejected(record);
		}
		super.reject(record);
	}

	private enum MAPPING_DATA_TYPE {
		STRING,
		NUMBER,
		DATE
	}

	private class MapCDRCPSEnt {
		private String dataField;
		private String dbColumnName;
		private String type;

		public String getDataField() {
			return dataField;
		}

		public void setDataField(String dataField) {
			this.dataField = dataField;
		}

		public String getDbColumnName() {
			return dbColumnName;
		}

		public void setDbColumnName(String dbColumnName) {
			this.dbColumnName = dbColumnName;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}
	}
}
