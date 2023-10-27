package com.telsoft.cbs.modules.soap;

import com.telsoft.cbs.utils.CBDispatcherManager;
import com.telsoft.util.AppException;
import com.telsoft.util.StringUtil;
import telsoft.gateway.core.dsp.Dispatcher;
import telsoft.gateway.core.dsp.DispatcherManager;

import java.util.Vector;

public class AntiProfitDispatcher extends CBDispatcherManager {
    protected String host;
    protected int port;
    protected int timeout;
    protected String uri;
    private int retryCount;

    @Override
    protected Dispatcher prepareDispatcher() throws Exception {
        return new AntiProfitDispatcherSplitThread(this);
    }
    public Vector getParameterDefinition() {
        Vector vtReturn = new Vector();
        vtReturn.add(createParameter("Host", "", 7, "/\\[]{}()`~!@#$%^&*;,?'\"\t ", ""));
        vtReturn.add(createParameter("Port", "", 1, "99990", ""));
        vtReturn.add(createParameter("URI", "", 2, "50", ""));
        vtReturn.add(createParameter("Timeout", "", 1, "99990", ""));
        vtReturn.add(createParameter("http-retry-count", "", 1, "99", ""));
        vtReturn.addAll(super.getParameterDefinition());
        return vtReturn;
    }

    public void fillParameter() throws AppException {
        super.fillParameter();
        this.host = this.loadString("Host");
        this.port = this.loadUnsignedInteger("Port");
        this.timeout = this.loadUnsignedInteger("Timeout") * 1000;
        this.uri = this.loadString("URI");
        this.retryCount = Integer.parseInt(StringUtil.nvl(this.getParameter("http-retry-count"), "3"));
    }

    protected void fillDispatcherParameter(Dispatcher thr) throws Exception {
        super.fillDispatcherParameter(thr);
        AntiProfitDispatcherSplitThread splitthr = (AntiProfitDispatcherSplitThread) thr;
        splitthr.setHost(this.host);
        splitthr.setPort(this.port);
        splitthr.setMiTimeout(this.timeout);
        splitthr.setUri(this.uri);
        splitthr.setRetryCount(this.retryCount);
    }

}
