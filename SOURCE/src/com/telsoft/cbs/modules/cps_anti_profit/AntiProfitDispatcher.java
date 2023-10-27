package com.telsoft.cbs.modules.cps_anti_profit;

import com.telsoft.cbs.utils.CBDispatcherManager;
import com.telsoft.thread.ParameterType;
import com.telsoft.util.AppException;
import com.telsoft.util.StringUtil;
import lombok.Getter;
import telsoft.gateway.core.dsp.Dispatcher;

import java.util.Vector;

public class AntiProfitDispatcher extends CBDispatcherManager {
    protected String host;
    protected int port;
    protected int timeout;
    protected String uri;
    private int retryCount;
    private int retryDelay;
    @Getter
    private String username;
    @Getter
    private String password;
    @Getter
    private String gameListId;
    @Getter
    private String partnerId ;
    @Getter
    private String subId;

    @Override
    protected Dispatcher prepareDispatcher() throws Exception {
        return new AntiProfitDispatcherSplitThread(this);
    }
    public Vector getParameterDefinition() {
        Vector vtReturn = new Vector();
        vtReturn.add(createParameter("Host", "", 7, "/\\[]{}()`~!@#$%^&*;,?'\"\t ", ""));
        vtReturn.add(createParameter("Port", "", 1, "99990", ""));
        vtReturn.add(createParameter("URI", "", 2, "50", ""));
        vtReturn.add(createParameter("Username", "", ParameterType.PARAM_TEXTBOX_MAX, "50", ""));
        vtReturn.add(createParameter("Password", "", ParameterType.PARAM_PASSWORD, "50", ""));
        vtReturn.add(createParameter("Game List Id", "", ParameterType.PARAM_TEXTBOX_MAX, "50", ""));
        vtReturn.add(createParameter("Partner Id", "", ParameterType.PARAM_TEXTBOX_MAX, "50", ""));
        vtReturn.add(createParameter("Sub Id", "", ParameterType.PARAM_TEXTBOX_MAX, "50", ""));
        vtReturn.add(createParameter("Timeout", "", 1, "99990", ""));
        vtReturn.add(createParameter("http-retry-count", "", 1, "99", ""));
        vtReturn.add(createParameter("http-retry-delay", "", 1, "999999", ""));
        vtReturn.addAll(super.getParameterDefinition());
        return vtReturn;
    }

    public void fillParameter() throws AppException {
        super.fillParameter();
        this.host = this.loadString("Host");
        try {
            this.port = this.loadUnsignedInteger("Port");
        }catch (Exception e){
            this.port=-1;
        }
        this.timeout = this.loadUnsignedInteger("Timeout") * 1000;
        this.uri = this.loadString("URI");
        this.retryCount = Integer.parseInt(StringUtil.nvl(this.getParameter("http-retry-count"), "3"));
        this.retryDelay = Integer.parseInt(StringUtil.nvl(this.getParameter("http-retry-delay"), "50"));
        username = loadString("Username");
        password = loadString("Password");
        gameListId = loadString("Game List Id");
        partnerId = loadString("Partner Id");
        subId = loadString("Sub Id");
    }

    protected void fillDispatcherParameter(Dispatcher thr) throws Exception {
        super.fillDispatcherParameter(thr);
        AntiProfitDispatcherSplitThread splitthr = (AntiProfitDispatcherSplitThread) thr;
        splitthr.setHost(this.host);
        splitthr.setPort(this.port);
        splitthr.setMiTimeout(this.timeout);
        splitthr.setUri(this.uri);
        splitthr.setRetryCount(this.retryCount);
        splitthr.setRetryDelay(this.retryDelay);
        splitthr.setUsername(getUsername());
        splitthr.setPassword(getPassword());
        splitthr.setGameListId(getGameListId());
        splitthr.setPartnerId(getPartnerId());
        splitthr.setSubId(getSubId());
    }

}
