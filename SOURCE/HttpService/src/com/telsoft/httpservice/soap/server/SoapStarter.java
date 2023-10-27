package com.telsoft.httpservice.soap.server;

import com.telsoft.httpservice.server.RestServer;
import com.telsoft.httpservice.server.service.DefaultResourceConfig;
import com.telsoft.thread.ManageableThread;
import com.telsoft.thread.ParameterType;
import com.telsoft.thread.ParameterUtil;
import com.telsoft.thread.ThreadConstant;
import com.telsoft.util.AppException;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.server.ResourceConfig;

import javax.jws.WebService;
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
public class SoapStarter extends ManageableThread {
    private String mstrServletContext;
    private String mstrContextPath;
    private int miPort;
    private int miTcpMaxIdleTime;
    private int miTcpAcceptors;
    private int miSoLingerTime;
    private int miMaxThreads;
    private int miMinThreads;
    private SoapServer server;


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
        miSoLingerTime = loadInteger("Tcp-SoLingerTime");
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

        return vtReturn;
    }

    @Override
    protected void processSession() throws Exception {
        startServer();
        try {
            while (miThreadCommand != ThreadConstant.THREAD_STOPPED && server.isRunning()) {
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

 /*   public ResourceConfig createResourceConfig() {
        return new DefaultResourceConfig(this);
    }*/

    /**
     * @throws Exception
     */
    public void startServer() throws Exception {
        server = new SoapServer(new MyWebService());
        server.setPort(miPort);
        server.setHttps(https);
        server.setKeyStore(keyStorePath, keyStorePassword, keyManagerPassword, trustStorePath, trustStorePassword);
        server.setServletContext(mstrServletContext);
        server.setContextPath(mstrContextPath);
        server.start();
    }

    public String getLoggerName() {
        return "SOAP-SERVER";
    }

    @WebService
    public static class MyWebService {

        public String hello(String s) {
            return "hi " + s;
        }
    }
}
