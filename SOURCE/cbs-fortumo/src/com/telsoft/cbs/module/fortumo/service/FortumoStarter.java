package com.telsoft.cbs.module.fortumo.service;

import com.telsoft.cbs.module.cbsrest.client.CBSClient;
import com.telsoft.httpservice.server.RestStarter;
import com.telsoft.thread.ParameterType;
import com.telsoft.thread.ParameterUtil;
import com.telsoft.thread.ThreadConstant;
import com.telsoft.util.AppException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
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
public class FortumoStarter extends RestStarter {
    @Getter
    FortumoStarter fortumoStarter = this;

    @Getter
    private CBSClient cbsClient = null;

    private String cbsUrl;
    private String storeCode;
    private String source;
    private String apiPath;
    private String msisdn;

    /**
     * @throws AppException
     */

    public void fillParameter() throws AppException {
        super.fillParameter();
        cbsUrl = loadString("CBSUrl");
        source = loadString("Source");
        storeCode = loadString("StoreCode");
        msisdn = loadString("MSISDN");
    }

    /**
     * @return Vector
     */

    public Vector getParameterDefinition() {
        Vector vtReturn = new Vector();
        vtReturn.addAll(super.getParameterDefinition());
        vtReturn.add(ParameterUtil.createParameter("MSISDN", "", ParameterType.PARAM_TEXTBOX_MAX, "256", ""));
        vtReturn.add(ParameterUtil.createParameter("CBSUrl", "", ParameterType.PARAM_TEXTBOX_MAX, "256", ""));
        vtReturn.add(ParameterUtil.createParameter("Source", "", ParameterType.PARAM_TEXTBOX_MAX, "256", ""));
        vtReturn.add(ParameterUtil.createParameter("StoreCode", "", ParameterType.PARAM_TEXTBOX_MAX, "256", ""));
        return vtReturn;
    }

    @Override
    public ResourceConfig createResourceConfig() {
        ResourceConfig resourceConfig = super.createResourceConfig();
        resourceConfig.register(FortumoResource.class);
        resourceConfig.registerClasses(FortumoExceptionHandler.class);
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(getCbsClient()).to(CBSClient.class);
            }
        });
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(getFortumoStarter()).to(FortumoStarter.class);
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
        if (cbsClient != null) {
            cbsClient.stop();
            cbsClient = null;
        }
    }

    /**
     * @throws Exception
     */
    @Override
    public void startServer() throws Exception {
        cbsClient = new CBSClient(this.source, this.storeCode, this.cbsUrl, this.msisdn);
        //fortumoResource = new FortumoResource(this.msisdn);
        cbsClient.start();
        super.startServer();
        logMonitor("Forturmo started at port: " + loadInteger("Port"));
    }
}
