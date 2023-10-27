package com.telsoft.httpservice.server;

import com.telsoft.httpservice.server.service.DefaultResourceConfig;
import com.telsoft.thread.ManageableThread;
import com.telsoft.thread.ParameterType;
import com.telsoft.thread.ParameterUtil;
import com.telsoft.thread.ThreadConstant;
import com.telsoft.util.AppException;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.Vector;

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

@Slf4j
public class RestStarter extends ManageableThread {
    private String mstrServletContext;
    private String mstrContextPath;
    private int miPort;
    private int miTcpMaxIdleTime;
    private int miTcpAcceptors;
    private int miMaxAcceptQueueSize;
    private int miMaxThreads;
    private int miMinThreads;
    private RestServer server;


    private boolean https;
    private String keyStorePath;
    private String keyStorePassword;
    private String keyManagerPassword;
    private String trustStorePath;
    private String trustStorePassword;

    /**
     * @throws AppException
     */

    public void fillParameter() throws AppException {
        super.fillParameter();
        // Fill parameter
        miPort = loadUnsignedInteger("Port");
        mstrServletContext = loadString("ServletContext");
        mstrContextPath = loadString("ContextPath");
        miTcpAcceptors = loadUnsignedInteger("Tcp-Acceptors");
        miTcpMaxIdleTime = loadUnsignedInteger("Tcp-MaxIdleTime");
        miMaxAcceptQueueSize = loadInteger("Jetty-MaxQueueSize");
        miMaxThreads = loadUnsignedInteger("Tcp-MaxThread");
        miMinThreads = loadUnsignedInteger("Tcp-MinThread");

        https = loadBoolean("HTTPS");
        if (https) {
            keyStorePath = loadString("KeyStorePath");
            keyStorePassword = loadString("KeyStorePassword");
            keyManagerPassword = loadString("KeyManagerPassword");
            trustStorePath = loadString("TrustStorePath");
            trustStorePassword = loadString("TrustStorePassword");
        }
    }

    /**
     * @return Vector
     */

    public Vector getParameterDefinition() {
        Vector vtReturn = new Vector();
        vtReturn.addAll(super.getParameterDefinition());
        vtReturn.add(ParameterUtil.createParameter("ContextPath", "", ParameterType.PARAM_TEXTBOX_MAX, "256", ""));
        vtReturn.add(ParameterUtil.createParameter("ServletContext", "", ParameterType.PARAM_TEXTBOX_MAX, "256", ""));
        vtReturn.add(ParameterUtil.createParameter("Port", "", ParameterType.PARAM_TEXTBOX_MASK, "99990", ""));
        vtReturn.add(ParameterUtil.createParameter("Tcp-Acceptors", "", ParameterType.PARAM_TEXTBOX_MASK, "999", ""));
        vtReturn.add(ParameterUtil.createParameter("Jetty-MaxQueueSize", "", ParameterType.PARAM_TEXTBOX_MASK, "99990", ""));
        vtReturn.add(ParameterUtil.createParameter("Tcp-MaxIdleTime", "", ParameterType.PARAM_TEXTBOX_MASK, "999990", ""));
        vtReturn.add(ParameterUtil.createParameter("Tcp-MaxThread", "", ParameterType.PARAM_TEXTBOX_MASK, "999", ""));
        vtReturn.add(ParameterUtil.createParameter("Tcp-MinThread", "", ParameterType.PARAM_TEXTBOX_MASK, "999", ""));

        vtReturn.add(ParameterUtil.createParameter("HTTPS", "", ParameterType.PARAM_COMBOBOX, Boolean.class, ""));

        vtReturn.add(ParameterUtil.createParameter("KeyStorePath", "", ParameterType.PARAM_TEXTBOX_MAX, "999", ""));
        vtReturn.add(ParameterUtil.createParameter("KeyStorePassword", "", ParameterType.PARAM_PASSWORD, "999", ""));
        vtReturn.add(ParameterUtil.createParameter("KeyManagerPassword", "", ParameterType.PARAM_PASSWORD, "999", ""));
        vtReturn.add(ParameterUtil.createParameter("TrustStorePath", "", ParameterType.PARAM_TEXTBOX_MAX, "999", ""));
        vtReturn.add(ParameterUtil.createParameter("TrustStorePassword", "", ParameterType.PARAM_PASSWORD, "999", ""));

        return vtReturn;
    }

    @Override
    protected void processSession() throws Exception {
        try {
            startServer();
            while (miThreadCommand != ThreadConstant.THREAD_STOPPED && server.isRunning()) {
                fillLogFile();
                validateParameter();
                Thread.sleep(1000);
            }
        } finally {
            stopServer();
        }
    }

    /**
     *
     */
    public void stopServer() {
        if (server != null) {
            try {
                server.stop();
            } catch (Exception e) {
                log.warn("Stop server error", e);
            }
            server = null;
        }
    }

    public ResourceConfig createResourceConfig() {
        return new DefaultResourceConfig(this);
    }

    /**
     * @throws Exception
     */
    public void startServer() throws Exception {
        server = new RestServer(createResourceConfig());
        server.setPort(miPort);
        server.setHttps(https);
        server.setKeyStore(keyStorePath, keyStorePassword, keyManagerPassword, trustStorePath, trustStorePassword);
        server.setServletContext(mstrServletContext);
        server.setContextPath(mstrContextPath);
//        private int miTcpMaxIdleTime;
//        private int miTcpAcceptors;
//        private int miMaxQueueSize;
//        private int miMaxThreads;
//        private int miMinThreads;
        server.setMiTcpMaxIdleTime(miTcpMaxIdleTime);
        server.setMiTcpAcceptors(miTcpAcceptors);
        server.setMiMaxAcceptQueueSize(miMaxAcceptQueueSize);
        server.setMiMaxThreads(miMaxThreads);
        server.setMiMinThreads(miMinThreads);

        server.start();
    }

    public String getLoggerName() {
        return "REST-SERVER";

    }
}
