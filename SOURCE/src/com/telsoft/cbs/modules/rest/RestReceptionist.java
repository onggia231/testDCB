package com.telsoft.cbs.modules.rest;

import com.telsoft.cbs.domain.CBCode;
import com.telsoft.cbs.domain.CBCommand;
import com.telsoft.cbs.modules.rest.service.RestApplicationConfig;
import com.telsoft.thread.ParameterType;
import com.telsoft.thread.ParameterUtil;
import com.telsoft.util.AppException;
import com.telsoft.util.StringUtil;
import org.glassfish.jersey.servlet.ServletContainer;
import telsoft.gateway.commons.MutableParameter;
import telsoft.gateway.core.excp.ExceptionHelper;
import telsoft.gateway.core.gw.Gateway;
import telsoft.gateway.core.gw.Receptionist;

import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Title: Core Gateway System</p>
 *
 * <p>Description: A part of TELSOFT Gateway System</p>
 *
 * <p>Copyright: Copyright (c) 2009</p>
 *
 * <p>Company: TELSOFT</p>
 *
 * @author Nguyen Cong Khanh
 * @version 1.0
 */
public class RestReceptionist extends Receptionist implements MutableParameter {
    private String mstrServletContext;
    private String mstrContextPath;
    private int miPort;
    private volatile boolean bClosing = false;
    private int miTcpMaxIdleTime;
    private int miTcpAcceptors;
    private int miSoLingerTime;
    private int miMaxThreads;
    private int miMinThreads;
    private HttpService server;


    private boolean https;
    private String keyStorePath;
    private String keyStorePassword;
    private String keyManagerPassword;
    private String trustStorePath;
    private String trustStorePassword;

    private ConcurrentHashMap timeoutTable = new ConcurrentHashMap();
    private ConcurrentHashMap timeoutTableReturnCode = new ConcurrentHashMap();

    /**
     * @param gateway Gateway
     */

    public RestReceptionist(Gateway gateway) {
        super(gateway);
    }

    /**
     * @throws AppException
     */

    public void fillParameter() throws AppException {
        // Fill parameter
        miPort = getGateway().loadUnsignedInteger("Port");
        mstrServletContext = getGateway().loadString("ServletContext");
        mstrContextPath = getGateway().loadString("ContextPath");
        miTcpAcceptors = getGateway().loadUnsignedInteger("Tcp-Acceptors");
        miTcpMaxIdleTime = getGateway().loadUnsignedInteger("Tcp-MaxIdleTime");
        miSoLingerTime = getGateway().loadInteger("Tcp-SoLingerTime");
        miMaxThreads = getGateway().loadUnsignedInteger("Tcp-MaxThread");
        miMinThreads = getGateway().loadUnsignedInteger("Tcp-MinThread");

        https = getGateway().loadBoolean("HTTPS");
        if (https) {
            keyStorePath = getGateway().loadString("KeyStorePath");
            keyStorePassword = getGateway().loadString("KeyStorePassword");
            keyManagerPassword = getGateway().loadString("KeyManagerPassword");
            trustStorePath = getGateway().loadString("TrustStorePath");
            trustStorePassword = getGateway().loadString("TrustStorePassword");
        }

        //Timeout Table
        Object obj = getGateway().getParameter("TimeoutConfigTable");
        if (obj != null && obj instanceof Vector) {
            Vector vtObj = (Vector) obj;
            for (int i = 0; i < vtObj.size(); i++) {
                Vector vtRow = (Vector) vtObj.get(i);
                String storeCode = getGateway().loadString("TimeoutConfigTable.StoreCode", StringUtil.nvl(vtRow.get(0), ""));
                String command = getGateway().loadString("TimeoutConfigTable.Command", StringUtil.nvl(vtRow.get(1), ""));
                Integer timeout = getGateway().loadInteger("TimeoutConfigTable.Timeout", StringUtil.nvl(vtRow.get(2), "0"));
                CBCode code = CBCode.valueOfCode(getGateway().loadInteger("TimeoutConfigTable.ReturnCode", StringUtil.nvl(vtRow.get(3), "5000")),CBCode.PROCESS_TIMEOUT);
                timeoutTable.put(command + "_" + storeCode, timeout);
                timeoutTableReturnCode.put(command + "_" + storeCode, code);
            }
        }
    }

    /**
     * @return Vector
     */

    public Vector getParameterDefinition() {
        Vector vtReturn = new Vector();
        vtReturn.add(ParameterUtil.createParameter("ContextPath", "", ParameterType.PARAM_TEXTBOX_MAX, "256", ""));
        vtReturn.add(ParameterUtil.createParameter("ServletContext", "", ParameterType.PARAM_TEXTBOX_MAX, "256", ""));
        vtReturn.add(ParameterUtil.createParameter("Port", "", ParameterType.PARAM_TEXTBOX_MASK, "99990", ""));
        vtReturn.add(ParameterUtil.createParameter("Tcp-Acceptors", "", ParameterType.PARAM_TEXTBOX_MASK, "999", ""));
        vtReturn.add(ParameterUtil.createParameter("Tcp-SoLingerTime", "", ParameterType.PARAM_TEXTBOX_MAX, "10", ""));
        vtReturn.add(ParameterUtil.createParameter("Tcp-MaxIdleTime", "", ParameterType.PARAM_TEXTBOX_MASK, "999990", ""));
        vtReturn.add(ParameterUtil.createParameter("Tcp-MaxThread", "", ParameterType.PARAM_TEXTBOX_MASK, "999", ""));
        vtReturn.add(ParameterUtil.createParameter("Tcp-MinThread", "", ParameterType.PARAM_TEXTBOX_MASK, "999", ""));

        vtReturn.add(ParameterUtil.createParameter("HTTPS", "", ParameterType.PARAM_COMBOBOX, Boolean.class, ""));

        vtReturn.add(ParameterUtil.createParameter("KeyStorePath", "", ParameterType.PARAM_TEXTBOX_MAX, "999", ""));
        vtReturn.add(ParameterUtil.createParameter("KeyStorePassword", "", ParameterType.PARAM_PASSWORD, "999", ""));
        vtReturn.add(ParameterUtil.createParameter("KeyManagerPassword", "", ParameterType.PARAM_PASSWORD, "999", ""));
        vtReturn.add(ParameterUtil.createParameter("TrustStorePath", "", ParameterType.PARAM_TEXTBOX_MAX, "999", ""));
        vtReturn.add(ParameterUtil.createParameter("TrustStorePassword", "", ParameterType.PARAM_PASSWORD, "999", ""));

        Vector vtTimeoutConfig = new Vector();
        vtTimeoutConfig.add(ParameterUtil.createParameter("StoreCode", "", ParameterType.PARAM_TEXTBOX_MAX, "256", ""));
        vtTimeoutConfig.add(ParameterUtil.createParameter("Command", "", ParameterType.PARAM_COMBOBOX, CBCommand.class, ""));
        vtTimeoutConfig.add(ParameterUtil.createParameter("Timeout", "", ParameterType.PARAM_TEXTBOX_MASK, "999999999", "Timeout in millisecond"));
        vtTimeoutConfig.add(ParameterUtil.createParameter("ReturnCode", "", ParameterType.PARAM_TEXTBOX_MASK, "999999999", "Return code when timeout. Default is 5000 (PROCESS_TIMEOUT)"));
        vtReturn.add(ParameterUtil.createParameter("TimeoutConfigTable", "", ParameterType.PARAM_TABLE, vtTimeoutConfig, "Timeout Config by Command and Store Code. Default is MessageTimeout parameter"));

        return vtReturn;
    }

    /**
     * @return boolean
     */

    public boolean isRunning() {
        return (server != null && server.isRunning());
    }

    /**
     *
     */
    public void stopRunning() {
        if (server != null) {
            try {
                server.stop();
            } catch (Exception e) {
                ExceptionHelper.printStackTrace(this, e);
            }
            server = null;
        }
    }

    @Override
    public void prepareStop() {

    }

    /**
     * @throws Exception
     */
    public void startListener() throws Exception {
        bClosing = false;
        server = new HttpService(new ServletContainer(new RestApplicationConfig(this)));
        server.setPort(miPort);
        server.setHttps(https);
        server.setKeyStore(keyStorePath, keyStorePassword, keyManagerPassword, trustStorePath, trustStorePassword);
        server.setServletContext(mstrServletContext);
        server.setContextPath(mstrContextPath);
        server.start();
    }

    public RestMessageChannel createChannel() throws Exception {
        return new RestMessageChannel(this.getGateway());
    }

    public ConcurrentHashMap getTimeoutTable() {
        return timeoutTable;
    }

    public ConcurrentHashMap getTimeoutTableReturnCode() {
        return timeoutTableReturnCode;
    }
}
