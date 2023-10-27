//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package telsoft.gateway.core.log;

import com.telsoft.thread.ThreadParameter;
import com.telsoft.util.AppException;
import com.telsoft.util.StringUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import org.codehaus.commons.compiler.CompilerFactoryFactory;
import org.codehaus.commons.compiler.IClassBodyEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import telsoft.gateway.commons.GWUtil;
import telsoft.gateway.commons.PriorityList;
import telsoft.gateway.core.GatewayManager;
import telsoft.gateway.core.log.data.DefaultLogger;
import telsoft.gateway.core.log.data.InitParameter;
import telsoft.gateway.core.log.data.TranslatorLogger;
import telsoft.gateway.thread.AbstractGatewayThread;

public abstract class AbstractTransactionLogger extends AbstractGatewayThread {
    protected static final int ATTACH_FIRST_LEVEL_STORAGE = 1;
    protected static final int ATTACH_NEXT_LEVEL_STORAGE = 2;
    protected static final int ATTACH_CURRENT_LEVEL_STORAGE = 3;
    private static final Logger log = LoggerFactory.getLogger(AbstractTransactionLogger.class);
    private static final String[] DEFAULT_IMPORT = new String[]{"java.util.*", "org.slf4j.*", "telsoft.util.*", "com.telsoft.util.*", "telsoft.gateway.commons.*", "telsoft.gateway.core.*", "telsoft.gateway.core.dsp.*", "telsoft.gateway.core.excp.*", "telsoft.gateway.core.gw.*", "telsoft.gateway.core.log.*", "telsoft.gateway.core.log.data.*", "telsoft.gateway.core.message.*", "telsoft.gateway.core.queue.*", "telsoft.gateway.core.translator.*", "telsoft.gateway.core.component.*", "telsoft.gateway.core.cmp.*"};
    protected Vector mvtReloadCommand;
    protected int miStorageLevel;
    protected int miEmptyDuration;
    protected Date mdtCheckEmptyFrom;
    protected Date mdtCheckEmptyUntil;
    protected int miFailureAction;
    protected int miSuccessAction;
    protected boolean mbDisplayStackTrace;
    protected long mlLastAvaiable = 0L;
    protected long mlCheckAvaiable = 0L;
    protected boolean mbIsLogBeforeClose = false;
    private PriorityList plLogger = new PriorityList();
    private GatewayLogger gatewayLogger;
    private Vector<Vector> vtClasses = new Vector();

    public AbstractTransactionLogger() {
    }

    public static boolean registerLogger(PriorityList list, int iPriority, TranslatorLogger log) {
        if (log == null) {
            return false;
        } else if (list.contains(log)) {
            return false;
        } else {
            list.add(iPriority, log);
            return true;
        }
    }

    public static void unregisterLogger(PriorityList list, TranslatorLogger log) {
        if (log != null) {
            if (list.contains(log)) {
                list.remove(log);
            }
        }
    }

    public final SimpleDateFormat TIMESTAMP_FORMAT() {
        return new SimpleDateFormat("yyyyMMddHHmmssSSS");
    }

    public final SimpleDateFormat DEFAULT_FORMAT() {
        return new SimpleDateFormat("yyyyMMddHHmmss");
    }

    public TranslatorLogger getLogger(String strServerProtocol, String strClientProtocol) {
        int iSize = this.plLogger.size();

        for (int i = 0; i < iSize; ++i) {
            TranslatorLogger log = (TranslatorLogger) this.plLogger.get(i);
            if (strServerProtocol == null) {
                strServerProtocol = "";
            }

            if (strClientProtocol == null) {
                strClientProtocol = "";
            }

            if (log.isValidTranslator(strServerProtocol, strClientProtocol)) {
                return log;
            }
        }

        return null;
    }

    public Vector getParameterDefinition() {
        Vector<ThreadParameter> vtReturn = new Vector();
        String[] value = new String[]{"0", "1", "2", "3"};
        vtReturn.add(createParameter("StorageLevel", "", 4, value, "Specify storage level containt logrecord belong to this logger", "", "Logging"));
        vtReturn.add(createParameter("CheckEmptyFrom", "", 1, "90:90:90", "The time system will start checking empty log", "", "Logging"));
        vtReturn.add(createParameter("CheckEmptyUntil", "", 1, "90:90:90", "The time system will end checking empty log", "", "Logging"));
        vtReturn.add(createParameter("EmptyDuration", "", 1, "999990", "Amount of time that togging storage can be empty. If over, system will send error to user. Value of 0 mean unlimited", "", "Logging"));
        value = new String[]{"Do nothing", "Attach record to first level storage", "Attach record to next level storage", "Attach record to current level storage"};
        vtReturn.add(createParameter("FailureAction", "", 4, value, "Action will be performed with failure log record", "", "Logging"));
        value = new String[]{"Do nothing", "Attach record to next level storage"};
        vtReturn.add(createParameter("SuccessAction", "", 4, value, "Action will be performed with success log record", "", "Logging"));
        value = new String[]{"Y", "N"};
        vtReturn.add(createParameter("DisplayStackTrace", "", 4, value, "Display stacktrace if error occured?", "", "Logging"));
        vtReturn.add(createParameter("ReloadCommand", "", 2, "512", "List of command_id will be treated as reload command (such as recharge, m); separated by comma (',') or semi colon (';')", "", "Logging"));
        Vector vtDefinition = new Vector();
        vtDefinition.add(createParameter("Class", "", 8, "999999"));
        vtDefinition.add(createParameter("Priority", "", 1, "99"));
        vtReturn.add(createParameter("LoggerClasses", "", 6, vtDefinition));
        vtReturn.addAll(super.getParameterDefinition());
        return vtReturn;
    }

    public void fillParameter() throws AppException {
        String strReloadCommand = this.loadString("ReloadCommand");
        this.mvtReloadCommand = StringUtil.toStringVector(StringUtil.replaceAll(strReloadCommand, ";", ","));
        this.miStorageLevel = this.loadUnsignedInteger("StorageLevel");
        this.mdtCheckEmptyFrom = this.loadTime("CheckEmptyFrom");
        this.mdtCheckEmptyUntil = this.loadTime("CheckEmptyUntil");
        this.miEmptyDuration = this.loadUnsignedInteger("EmptyDuration");
        String strFailureAction = this.loadString("FailureAction");
        this.miFailureAction = 0;
        if (strFailureAction.equals("Attach record to first level storage")) {
            this.miFailureAction = 1;
        } else if (strFailureAction.equals("Attach record to next level storage")) {
            this.miFailureAction = 2;
        } else if (strFailureAction.equals("Attach record to current level storage")) {
            this.miFailureAction = 3;
        }

        this.miSuccessAction = 0;
        String strSuccessAction = this.loadString("SuccessAction");
        if (strSuccessAction.equals("Attach record to next level storage")) {
            this.miSuccessAction = 2;
        }

        this.mbDisplayStackTrace = this.loadYesNo("DisplayStackTrace").equals("Y");
        if (this.miStorageLevel >= 3 && this.miFailureAction == 2) {
            throw new AppException("Cannot set failure action attach next level when storage level is last level");
        } else if (this.miStorageLevel >= 3 && this.miSuccessAction == 2) {
            throw new AppException("Cannot set success action attach next level when storage level is last level");
        } else {
            Object obj = this.getParameter("LoggerClasses");
            if (obj instanceof Vector) {
                this.vtClasses = (Vector) obj;
            } else {
                this.vtClasses = new Vector();
            }

            super.fillParameter();
        }
    }

    public void validateParameter() throws Exception {
        super.validateParameter();
        if (this.mdtCheckEmptyFrom != null && this.mdtCheckEmptyUntil != null && this.mdtCheckEmptyFrom.compareTo(this.mdtCheckEmptyUntil) > 0) {
            throw new AppException("Value of 'CheckEmptyFrom' can not be greater than value of 'CheckEmptyUntil'", "", "CheckEmptyUntil");
        } else {
            PriorityList plTemp = new PriorityList();
            if (this.vtClasses != null) {
                Iterator i$ = this.vtClasses.iterator();

                while (i$.hasNext()) {
                    Vector vtRow = (Vector) i$.next();

                    try {
                        String strClass = (String) vtRow.get(0);
                        int iPriority = Integer.parseInt((String) vtRow.get(1));
                        strClass = strClass.trim();
                        if (strClass.startsWith("//SCRIPT")) {
                            IClassBodyEvaluator cbe = CompilerFactoryFactory.getDefaultCompilerFactory().newClassBodyEvaluator();
                            cbe.setDefaultImports(DEFAULT_IMPORT);
                            cbe.setImplementedInterfaces(new Class[]{TranslatorLogger.class});
                            cbe.setClassName("LOGGER_" + System.currentTimeMillis());
                            cbe.cook(strClass);
                            Class clz = cbe.getClazz();
                            Object obj = clz.newInstance();
                            if (obj instanceof TranslatorLogger) {
                                registerLogger(plTemp, iPriority, (TranslatorLogger) obj);
                            }
                        } else {
                            int pos = strClass.indexOf(35);
                            String p = null;
                            if (pos > 0) {
                                String c = strClass.substring(0, pos);
                                p = strClass.substring(pos + 1);
                                strClass = c.trim();
                            }

                            Class clz = Class.forName(strClass);
                            Object obj = clz.newInstance();
                            if (obj instanceof InitParameter) {
                                ((InitParameter) obj).init(p);
                            }

                            if (obj instanceof TranslatorLogger) {
                                registerLogger(plTemp, iPriority, (TranslatorLogger) obj);
                            }
                        }
                    } catch (Exception var10) {
                        this.logMonitor("Error occured :" + GWUtil.decodeException(var10));
                        log.error("Error occured", var10);
                    }
                }
            }

            registerLogger(plTemp, -1, new DefaultLogger());
            this.plLogger = plTemp;
        }
    }

    public void setIsLogBeforeClose() {
        this.mbIsLogBeforeClose = true;
    }

    public int getStorageLevel() {
        return this.miStorageLevel;
    }

    protected void reject(MessageContext record) {
        if (this.gatewayLogger == null) {
            this.gatewayLogger = ((GatewayManager) this.mmgrMain).getLogger();
        }

        if (this.miFailureAction == 1) {
            record.setAttached(false);
            this.gatewayLogger.attachLogRecord(record);
        } else if (this.miFailureAction == 2) {
            record.setAttached(false);
            this.gatewayLogger.attachLogRecord(record, this.miStorageLevel + 1);
        } else if (this.miFailureAction == 3) {
            record.setAttached(false);
            this.gatewayLogger.attachLogRecord(record, this.miStorageLevel);
        }

    }

    protected void accepted(MessageContext record) {
        if (this.gatewayLogger == null) {
            this.gatewayLogger = ((GatewayManager) this.mmgrMain).getLogger();
        }

        if (this.miSuccessAction == 2) {
            record.setAttached(false);
            this.gatewayLogger.attachLogRecord(record, this.miStorageLevel + 1);
        }

    }

    public Vector getReloadCommand() {
        return this.mvtReloadCommand;
    }

    public final String formatDate(Date dt) {
        return dt == null ? "" : this.DEFAULT_FORMAT().format(dt);
    }

    public final String formatTimestamp(Date dt) {
        return dt == null ? "" : this.TIMESTAMP_FORMAT().format(dt);
    }

    /**
     * @deprecated
     */
    public Date getFirstServerRequestTime(MessageContext ctx) {
        return ctx.getServerCommand() != null && ctx.getServerCommand().size() != 0 ? ((ServerCommand) ctx.getServerCommand().get(0)).dtServerRequestTime : null;
    }

    public Date getLastServerResponseTime(MessageContext ctx) {
        return ctx.getServerCommand() != null && ctx.getServerCommand().size() != 0 ? ((ServerCommand) ctx.getServerCommand().get(ctx.getServerCommand().size() - 1)).dtServerResponseTime : null;
    }
}
