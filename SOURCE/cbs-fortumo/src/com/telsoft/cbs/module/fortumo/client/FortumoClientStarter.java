package com.telsoft.cbs.module.fortumo.client;

import com.telsoft.cbs.module.cbsrest.server.service.RestExceptionHandler;
import com.telsoft.httpservice.server.RestStarter;
import com.telsoft.thread.GroupParameter;
import com.telsoft.thread.ParameterType;
import com.telsoft.thread.ParameterUtil;
import com.telsoft.thread.ThreadConstant;
import com.telsoft.util.AppException;
import com.telsoft.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.Vector;

@Getter
public class FortumoClientStarter extends RestStarter {
    private FortumoClient fortumoClient = null;
    private String fortumoUrl;
    private String keyStorePath;
    private String keyStorePassword;
    private String trustStorePath;
    private String trustStorePassword;
    private String submitMOApi;
    private String accChangeApi;
    FortumoClientStarter fortumoClientStarter = this;

    /**
     * @throws AppException
     */

    public void fillParameter() throws AppException {
        super.fillParameter();
        fortumoUrl = loadString("FortumoUrl");
        GroupParameter gp = new GroupParameter(this, "ApiList");
        submitMOApi = StringUtil.nvl(gp.loadString("SubmitMOApiPath"), "/mo");
        accChangeApi = StringUtil.nvl(gp.loadString("AccountChangeApiPath"),"/accountchange");
        if (!submitMOApi.startsWith("/") || !accChangeApi.startsWith("/")) {
            submitMOApi = "/" + submitMOApi;
            accChangeApi = "/" + accChangeApi;
        }
        keyStorePath = loadString("Fortumo-KeyStorePath");
        keyStorePassword = loadString("Fortumo-KeyStorePassword");
        trustStorePath = loadString("Fortumo-TrustStorePath");
        trustStorePassword = loadString("Fortumo-TrustStorePassword");
    }

    /**
     * @return Vector
     */

    public Vector getParameterDefinition() {
        Vector vtReturn = new Vector();
        Vector vtDefinition = new Vector();
        vtReturn.addAll(super.getParameterDefinition());
        vtReturn.add(ParameterUtil.createParameter("FortumoUrl", "", ParameterType.PARAM_TEXTBOX_MAX, "999", ""));
        vtDefinition.add(ParameterUtil.createParameter("SubmitMOApiPath", "", ParameterType.PARAM_TEXTBOX_MAX, "999", "", "0"));
        vtDefinition.add(ParameterUtil.createParameter("AccountChangeApiPath", "", ParameterType.PARAM_TEXTBOX_MAX, "999", "", "1"));
        vtReturn.add(ParameterUtil.createParameter("ApiList", "Api List", ParameterType.PARAM_GROUP, vtDefinition, "Contain api's information"));
        vtReturn.add(ParameterUtil.createParameter("Fortumo-KeyStorePath", "", ParameterType.PARAM_TEXTBOX_MAX, "999", ""));
        vtReturn.add(ParameterUtil.createParameter("Fortumo-KeyStorePassword", "", ParameterType.PARAM_PASSWORD, "999", ""));
        vtReturn.add(ParameterUtil.createParameter("Fortumo-TrustStorePath", "", ParameterType.PARAM_TEXTBOX_MAX, "999", ""));
        vtReturn.add(ParameterUtil.createParameter("Fortumo-TrustStorePassword", "", ParameterType.PARAM_PASSWORD, "999", ""));
        return vtReturn;
    }

    @Override
    public ResourceConfig createResourceConfig() {
        ResourceConfig resourceConfig = super.createResourceConfig();
        resourceConfig.register(FortumoClientResource.class);
        resourceConfig.register(RestExceptionHandler.class);
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(getFortumoClient()).to(FortumoClient.class);
            }
        });
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(getFortumoClientStarter()).to(FortumoClientStarter.class);
            }
        });
        return resourceConfig;
    }

    @Override
    public String getLoggerName() {
        return "REST-FORTUMO";
    }

    /**
     *
     */
    @Override
    public void stopServer() {
        super.stopServer();
        if (fortumoClient != null) {
            fortumoClient.stop();
            fortumoClient = null;
        }
    }

    /**
     * @throws Exception
     */
    @Override
    public void startServer() throws Exception {
        fortumoClient = new FortumoClient(this.fortumoUrl);
        fortumoClient.setKeyStorePath(keyStorePath);
        fortumoClient.setKeyStorePassword(keyStorePassword);
        fortumoClient.setTrustStorePath(trustStorePath);
        fortumoClient.setTrustStorePassword(trustStorePassword);
        fortumoClient.start();
        super.startServer();
        logMonitor("Forturmo Client started at port: " + loadInteger("Port"));
    }
}
