package com.telsoft.cbs.modules.cps_rtec;

import com.telsoft.cbs.utils.CBDispatcherManager;
import com.telsoft.thread.ParameterType;
import com.telsoft.util.AppException;
import com.telsoft.util.StringUtil;
import lombok.Getter;
import telsoft.gateway.core.dsp.Dispatcher;

import java.util.Vector;

public class CpsRtecDispatcher extends CBDispatcherManager {
    protected String host;
    protected int port;
    protected int timeout;
    protected String uri;
    @Getter
    private String username;
    @Getter
    private String password;
    private String cp_id;
    private int retryCount;

    protected Dispatcher prepareDispatcher() throws Exception {
        return new CpsRtecDispatcherSplitThread(this);
    }

    public Vector getParameterDefinition() {
        Vector vtReturn = new Vector();
        vtReturn.add(createParameter("Host", "", 7, "/\\[]{}()`~!@#$%^&*;,?'\"\t ", ""));
        vtReturn.add(createParameter("Port", "", 1, "99990", ""));
        vtReturn.add(createParameter("URI", "", 2, "50", ""));
        vtReturn.add(createParameter("Timeout", "", 1, "99990", ""));
        vtReturn.add(createParameter("cp_id", "", 2, "100", ""));
        vtReturn.add(createParameter("http-retry-count", "", 1, "99", ""));
        vtReturn.add(createParameter("Username", "", ParameterType.PARAM_TEXTBOX_MAX, "50", ""));
        vtReturn.add(createParameter("Password", "", ParameterType.PARAM_PASSWORD, "50", ""));
        vtReturn.addAll(super.getParameterDefinition());
        return vtReturn;
    }

    public void fillParameter() throws AppException {
        super.fillParameter();
        this.host = this.loadString("Host");
        this.port = this.loadUnsignedInteger("Port");
        this.timeout = this.loadUnsignedInteger("Timeout") * 1000;
        this.uri = this.loadString("URI");
        this.cp_id = StringUtil.nvl(this.getParameter("cp_id"), "");
        this.retryCount = Integer.parseInt(StringUtil.nvl(this.getParameter("http-retry-count"), "3"));
        username = loadString("Username");
        password = loadString("Password");
    }

    protected void fillDispatcherParameter(Dispatcher thr) throws Exception {
        super.fillDispatcherParameter(thr);
        CpsRtecDispatcherSplitThread splitthr = (CpsRtecDispatcherSplitThread) thr;
        splitthr.setHost(this.host);
        splitthr.setPort(this.port);
        splitthr.setTimeout(this.timeout);
        splitthr.setURI(this.uri);
        splitthr.setRTECParameter(this.cp_id);
        splitthr.setRetryCount(this.retryCount);
        splitthr.setUsername(getUsername());
        splitthr.setPassword(getPassword());
    }
}
