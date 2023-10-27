package com.telsoft.cbs.modules.cps_diameter;

import com.telsoft.thread.ParameterLoader;
import com.telsoft.util.AppException;
import com.telsoft.util.StringUtil;
import org.jdiameter.client.impl.helpers.AppConfiguration;
import org.jdiameter.client.impl.helpers.EmptyConfiguration;

import java.util.Vector;

import static org.jdiameter.client.impl.helpers.ExtensionPoint.InternalPeerController;
import static org.jdiameter.client.impl.helpers.ExtensionPoint.InternalRouterEngine;
import static org.jdiameter.client.impl.helpers.Parameters.*;

/**
 * Created by khanhnc on 9/16/15.
 */
public class ClientConfiguration extends EmptyConfiguration {
    private final CpsDiameterDispatcher dispatcher;
    private final CpsDiameterDispatcherSplitThread thread;

    public ClientConfiguration(CpsDiameterDispatcher dispatcher, CpsDiameterDispatcherSplitThread thread) throws AppException {
        super();
        this.dispatcher = dispatcher;
        this.thread = thread;

        add(UseUriAsFqdn, Boolean.TRUE);

        add(Assembler, Assembler.defValue());
        String strOwnUri = getDispatcher().loadString(OwnDiameterURI.name());
        add(OwnDiameterURI, strOwnUri);

        add(MessageTimeOut, getDispatcher().loadLong("TimeOut") * 1000);
        add(StopTimeOut, getDispatcher().loadLong("TimeOut") * 1000);
        add(CeaTimeOut, getDispatcher().loadLong("TimeOut") * 1000);
        add(IacTimeOut, getDispatcher().loadLong("TimeOut") * 1000);
        add(DwaTimeOut, getDispatcher().loadLong("TimeOut") * 1000);
        add(DpaTimeOut, getDispatcher().loadLong("TimeOut") * 1000);
        add(RecTimeOut, getDispatcher().loadLong("TimeOut") * 1000);

        add(OwnProductName, getDispatcher().loadString(OwnProductName.name()));
        add(OwnRealm, getDispatcher().loadString(OwnRealm.name()));
        add(OwnVendorID, getDispatcher().loadUnsignedLong(OwnVendorID.name()));
        Object objApplicationId = getDispatcher().getParameter(ApplicationId.name());
        if (objApplicationId != null && objApplicationId instanceof Vector) {
            Vector vtApplicationId = (Vector) objApplicationId;
            for (int iIndex = 0; iIndex < vtApplicationId.size(); iIndex++) {
                Vector vtRow = (Vector) vtApplicationId.elementAt(iIndex);
                long lVendorId = ParameterLoader.loadLong(VendorId.name(), StringUtil.nvl(vtRow.elementAt(0), null));
                long lAuthApplId = ParameterLoader.loadLong(AuthApplId.name(),
                        StringUtil.nvl(vtRow.elementAt(1), null));
                long lAcctApplId = ParameterLoader.loadLong(AcctApplId.name(),
                        StringUtil.nvl(vtRow.elementAt(2), null));
                add(ApplicationId, getInstance().add(VendorId, lVendorId).add(
                        AcctApplId, lAcctApplId));
            }
        }

        String strServerHost = getDispatcher().loadString("ServerHost");
        String strServerRealm = getDispatcher().loadString("ServerRealm");
        int iServerPort = getDispatcher().loadUnsignedInteger("ServerPort");
        add(PeerTable, getInstance().add(PeerRating, 1).add(PeerName,
                "aaa://" + strServerHost + ":" + iServerPort));
        add(RealmTable,
                // Realm 1
                getInstance().add(RealmEntry, strServerRealm + ":" + strServerHost));

        AppConfiguration extensionPoints = (AppConfiguration) getChildren(Extensions.ordinal())[0];
        extensionPoints.add(InternalPeerController, PeerTableImpl.class.getName());
        extensionPoints.add(InternalRouterEngine, telsoft.jdiameter.client.impl.router.RouterImpl.class.getName());
    }

    public CpsDiameterDispatcher getDispatcher() {
        return dispatcher;
    }

    public CpsDiameterDispatcherSplitThread getDispatcherSplitThread() {
        return thread;
    }
}
