package com.telsoft.cbs.modules.cps_diameter;

import com.telsoft.cbs.utils.CBDispatcherManager;
import com.telsoft.thread.ParameterType;
import com.telsoft.util.AppException;
import com.telsoft.util.StringUtil;
import org.jdiameter.client.impl.helpers.Parameters;
import telsoft.gateway.core.dsp.Dispatcher;
import telsoft.jdiameter.message.AvpDictionary;

import java.util.Vector;

import static org.jdiameter.client.impl.helpers.Parameters.*;

/**
 * <p>Title: DIAMETER Protocol</p>
 * <p/>
 * <p>Description: A part TELSOFT Gateway System</p>
 * <p/>
 * <p>Copyright: Copyright (c) 2009</p>
 * <p/>
 * <p>Company: TELSOFT</p>
 *
 * @author Nguyen Cong Khanh, Do Khac Sy
 * @version 1.0
 */
public class CpsDiameterDispatcher extends CBDispatcherManager {
    static {
        AvpDictionary.INSTANCE.loadDictionary("configuration/dictionary.xml");
        AvpDictionary.INSTANCE.loadExtrasDictionary("configuration/extra-dictionary.xml");
    }

    private String realm;
    private String serverRealm;
    private String serverHost;
    private String username = null;
    private String password = null;
    protected int timeout;

    protected Dispatcher prepareDispatcher() throws Exception {
        // TODO Auto-generated method stub
        return new CpsDiameterDispatcherSplitThread(this);
    }

    public Vector getParameterDefinition() {
        Vector vtReturn = new Vector();

        vtReturn.add(createParameter(OwnDiameterURI.name(), "", ParameterType.PARAM_TEXTBOX_MAX, "128", "Client owner uri"));
        vtReturn.add(createParameter(OwnProductName.name(), "", ParameterType.PARAM_TEXTBOX_MAX, "128", "Client product name"));
        vtReturn.add(createParameter(OwnVendorID.name(), "", ParameterType.PARAM_TEXTBOX_MASK, "999999", "Vendor ID"));
        vtReturn.add(createParameter(Parameters.OwnRealm.name(), "", ParameterType.PARAM_TEXTBOX_MAX, "128", "Realm name"));
        vtReturn.add(createParameter("ServerRealm", "", ParameterType.PARAM_TEXTBOX_MAX, "128", "Server realm name"));
        vtReturn.add(createParameter("ServerHost", "", ParameterType.PARAM_TEXTBOX_MAX, "128", "Server host name"));
        vtReturn.add(createParameter("ServerPort", "", ParameterType.PARAM_TEXTBOX_MASK, "999999", "Server port client."));
        vtReturn.add(createParameter("TimeOut", "", ParameterType.PARAM_TEXTBOX_MASK, "999999", "Connection timeout (seconds)."));
        vtReturn.add(createParameter("DictionaryEnabled", "", ParameterType.PARAM_COMBOBOX, boolean.class));

        Vector vtApplicationIdDefine = new Vector();
        vtApplicationIdDefine.add(createParameter(VendorId.name(), "", ParameterType.PARAM_TEXTBOX_MASK, "999999", "", "0"));
        vtApplicationIdDefine.add(createParameter(AuthApplId.name(), "", ParameterType.PARAM_TEXTBOX_MASK, "999999", "", "1"));
        vtApplicationIdDefine.add(createParameter(AcctApplId.name(), "", ParameterType.PARAM_TEXTBOX_MASK, "999999", "", "2"));
        // Common application id
        vtReturn.add(createParameter(ApplicationId.name(), "", ParameterType.PARAM_TABLE, vtApplicationIdDefine, "Common application id."));

        vtReturn.add(createParameter("Username", "", ParameterType.PARAM_TEXTBOX_MAX, "50"));
        vtReturn.add(createParameter("Password", "", ParameterType.PARAM_TEXTBOX_MAX, "50"));

        vtReturn.addAll(super.getParameterDefinition());
        return vtReturn;
    }

    /**
     * @throws AppException
     */
    public void fillParameter() throws AppException {
        super.fillParameter();
        new ClientConfiguration(this, null);
        username = StringUtil.nvl(getParameter("Username"), "");
        password = StringUtil.nvl(getParameter("Password"), "");
        realm = StringUtil.nvl(getParameter("OwnRealm"), "");
        serverRealm = StringUtil.nvl(getParameter("ServerRealm"), "");
        serverHost = StringUtil.nvl(getParameter("ServerHost"), "");
        this.timeout = this.loadUnsignedInteger("TimeOut") * 1000;
    }

    /**
     * @param thr Dispatcher
     * @throws Exception
     */
    protected void fillDispatcherParameter(Dispatcher thr) throws Exception {
        super.fillDispatcherParameter(thr);
        CpsDiameterDispatcherSplitThread diameterThread = (CpsDiameterDispatcherSplitThread) thr;
        diameterThread.setServerHost(serverHost);
        diameterThread.setServerRealm(serverRealm);
        diameterThread.setUsername(username);
        diameterThread.setPassword(password);
        diameterThread.setRealm(realm);
        diameterThread.setConfig(new ClientConfiguration(this, diameterThread));
    }
}
