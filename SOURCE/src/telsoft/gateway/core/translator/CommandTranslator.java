package telsoft.gateway.core.translator;

import com.telsoft.util.CollectionUtil;
import com.telsoft.util.DataStatistician;
import telsoft.gateway.commons.DBSynchronizable;
import telsoft.gateway.core.GatewayManager;
import telsoft.gateway.core.component.GWComponent;
import telsoft.gateway.core.log.MessageContext;
import telsoft.gateway.core.translator.CommandTranslationDefinition;

import java.sql.Connection;
import java.util.List;
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
public class CommandTranslator extends GWComponent implements DBSynchronizable {
    private Vector mvtTranslationTable = new Vector();

    public CommandTranslator(GatewayManager gateway) {
        super(gateway);
    }

    /**
     * @param cn        Connection
     * @param iSyncCode int
     * @throws Exception
     */
    public void syncData(Connection cn, int iSyncCode) throws Exception {
        loadCommandTranslationTable(cn);
    }

    /**
     * @param cn Connection
     * @throws Exception
     */
    private void loadCommandTranslationTable(Connection cn) throws Exception {
        Vector<Vector> vtReturn = new Vector<Vector>();
        // Merge data for fast searching
        mvtTranslationTable = CollectionUtil.mergeSorted2DVector(vtReturn, new int[]{0, 1, 2}, new DataStatistician() {
            public void summariseData(List lstData, List lstDetailData) {
                lstData.add(((List) lstDetailData.get(0)).get(3));
            }
        });
    }

    /**
     * @param ctx        MessageContext
     * @param strCommand String
     * @return CommandTranslationDefinition
     */
    public CommandTranslationDefinition translateCommand(MessageContext ctx, String strCommand) {
        String strGatewayClass = ctx.getGatewayTypeId();
        String strDispatcherClass = ctx.getDispatcherTypeId();

        // Search gateway
        Vector vt = mvtTranslationTable;
        boolean bFound = false;
        for (int iIndex = 0; iIndex < vt.size(); iIndex++) {
            Vector vtRow = (Vector) vt.get(iIndex);
            if (strGatewayClass.equals(vtRow.get(0))) {
                bFound = true;
                vt = (Vector) vtRow.get(1);
                break;
            }
        }
        if (!bFound) {
            return null;
        }

        // Search dispatcher
        bFound = false;
        for (int iIndex = 0; iIndex < vt.size(); iIndex++) {
            Vector vtRow = (Vector) vt.get(iIndex);
            if (strDispatcherClass.equals(vtRow.get(0))) {
                bFound = true;
                vt = (Vector) vtRow.get(1);
                break;
            }
        }
        if (!bFound) {
            return null;
        }

        // Search command
        for (int iIndex = 0; iIndex < vt.size(); iIndex++) {
            Vector vtRow = (Vector) vt.get(iIndex);
            if (strCommand.equalsIgnoreCase((String) vtRow.get(0))) {
                return (CommandTranslationDefinition) vtRow.get(1);
            }
        }
        return null;
    }
}
