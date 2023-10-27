package com.telsoft.cbs.utils;

import com.telsoft.thread.ParameterType;
import com.telsoft.util.AppException;
import telsoft.gateway.core.component.GWDataLayer;
import telsoft.gateway.core.dsp.DispatcherManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public abstract class CBDispatcherManager extends DispatcherManager {
    private String serverName;

    public Vector getParameterDefinition() {
        Vector vtReturn = new Vector();
        vtReturn.add(createParameter("ServerName", "", ParameterType.PARAM_TEXTBOX_MAX, "128"));
        vtReturn.addAll(super.getParameterDefinition());
        return vtReturn;
    }

    /**
     * @throws AppException
     */
    public void fillParameter() throws AppException {
        super.fillParameter();
        serverName = loadString("ServerName");
    }

    protected void queryServerInformation(GWDataLayer dataLayer) throws Exception {
        List vtServerID = new ArrayList();
        List vtServerName = new ArrayList();
        vtServerID.add(serverName);
        vtServerName.add(serverName);
        mvtServerId = vtServerID;
        mvtServerName = vtServerName;
    }

}
