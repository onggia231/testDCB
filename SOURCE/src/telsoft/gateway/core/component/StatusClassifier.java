package telsoft.gateway.core.component;

import com.telsoft.util.CollectionUtil;
import com.telsoft.util.StringUtil;
import telsoft.gateway.commons.DBSynchronizable;
import telsoft.gateway.core.GatewayManager;
import telsoft.gateway.core.log.MessageContext;
import telsoft.gateway.core.log.ServerCommand;
import telsoft.gateway.core.message.GatewayMessage;

import java.sql.Connection;
import java.util.List;
import java.util.Vector;

/**
 * <p>Title: Core Gateway System</p>
 * <p/>
 * <p>Description: A part of TELSOFT Gateway System</p>
 * <p/>
 * <p>Copyright: Copyright (c) 2009</p>
 * <p/>
 * <p>Company: TELSOFT</p>
 *
 * @author Nguyen Cong Khanh
 * @version 1.0
 */
public class StatusClassifier extends GWComponent implements DBSynchronizable {
    private static final int INCLUDE = 0;
    private static final int START_WITH = 1;
    private static final int END_WITH = 2;

    protected Vector mvtCache = null;
    protected Vector mvtUniqueCache = null;

    public StatusClassifier(GatewayManager gateway) {
        super(gateway);
    }

    /**
     * classifyIntError
     *
     * @param str    String
     * @param string String
     * @return int
     */
    public int classifyIntError(String str, String string) throws Exception {
        return getIntStatus(string, "", str);
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     * @param record LogRecord
     * @throws Exception
     */
    ////////////////////////////////////////////////////////////////////////////
    public void classifyError(MessageContext record) throws Exception {
        List<ServerCommand> vtServerCommand = record.getServerCommand();
        for (int iIndex = 0; iIndex < vtServerCommand.size(); iIndex++) {
            ServerCommand sc = vtServerCommand.get(iIndex);
            GatewayMessage msgRequest = sc.msgServerRequest;
            String[] strError = null;
            if (sc.msgServerResponse != null) {
                strError = getStatus(record.getDispatcherTypeId(), msgRequest.getContent(),
                        sc.msgServerResponse.getContent());
            } else {
                strError = getStatus(record.getDispatcherTypeId(), msgRequest.getContent(), "");
            }
            if (strError != null) {
                record.setStatus(strError[0]);
                record.setErrorLevel(strError[1]);
                return;
            }
        }

        String strRequest = record.getRequest().getContent();
        String[] strError = null;
        if (record.getClientResponse() != null) {
            strError = getStatus(record.getDispatcherTypeId(), strRequest,
                    record.getClientResponse().getContent());
        } else {
            strError = getStatus(record.getDispatcherTypeId(), strRequest, "");
        }
        if (strError != null) {
            record.setStatus(strError[0]);
            record.setErrorLevel(strError[1]);
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     * @param strDispatcherID String
     * @param strRequest      String
     * @param strResponse     String
     * @return String
     * @throws Exception
     */
    ////////////////////////////////////////////////////////////////////////////
    public int getIntStatus(String strDispatcherID, String strRequest, String strResponse) throws Exception {
        if (mvtCache != null && mvtCache.size() > 0) {
            strRequest = strRequest.toUpperCase();
            strResponse = strResponse.toUpperCase();
            if (strDispatcherID == null || strDispatcherID.equals("")) {
                return getIntStatus(mvtUniqueCache, strRequest, strResponse);
            } else {
                int[] iColumn = {0};
                Object[] objKey = {strDispatcherID};
                int iIndex = CollectionUtil.binarySearchSorted2DVector(mvtCache, objKey, iColumn);
                if (iIndex >= 0) {
                    Vector vtError = (Vector) ((Vector) mvtCache.get(iIndex)).elementAt(1);
                    for (int i = 0; i < vtError.size(); i++) {
                        Vector vtRow = (Vector) ((Vector) vtError.elementAt(i)).elementAt(1);
                        int iReturn = getIntStatus(vtRow, strRequest, strResponse);
                        if (iReturn != -1) {
                            return iReturn;
                        }
                    }
                }
            }
        }
        return -1;
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     * @param strDispatcherID String
     * @param strRequest      String
     * @param strResponse     String
     * @return String
     * @throws Exception
     */
    ////////////////////////////////////////////////////////////////////////////
    public String[] getStatus(String strDispatcherID, String strRequest, String strResponse) throws Exception {
        if (mvtCache != null && mvtCache.size() > 0) {
            strRequest = strRequest.toUpperCase();
            strResponse = strResponse.toUpperCase();
            if (strDispatcherID == null || strDispatcherID.equals("")) {
                return getStatus(mvtUniqueCache, strRequest, strResponse);
            } else {
                int[] iColumn = {0};
                Object[] objKey = {strDispatcherID};
                int iIndex = CollectionUtil.binarySearchSorted2DVector(mvtCache, objKey, iColumn);
                if (iIndex >= 0) {
                    Vector vtError = (Vector) ((Vector) mvtCache.get(iIndex)).elementAt(1);
                    for (int i = 0; i < vtError.size(); i++) {
                        Vector vtRow = (Vector) ((Vector) vtError.elementAt(i)).elementAt(1);
                        String strReturn[] = getStatus(vtRow, strRequest, strResponse);
                        if (strReturn != null) {
                            return strReturn;
                        }
                    }
                }
            }
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     * @param vtRecognition Vector
     * @param strRequest    String
     * @param strResponse   String
     * @return String
     * @throws Exception
     */
    ////////////////////////////////////////////////////////////////////////////
    private int getIntStatus(List vtRecognition, String strRequest, String strResponse) throws Exception {
        for (int i = 0; i < vtRecognition.size(); i++) {
            Vector vtRow = (Vector) vtRecognition.get(i);
            String strReqValue = (String) vtRow.elementAt(5);
            String strResValue = (String) vtRow.elementAt(7);
            if (strReqValue.equals("") && strResValue.equals("")) {
                continue;
            }
            if (!strReqValue.equals("")) {
                Integer intReqType = (Integer) vtRow.elementAt(4);
                if (!checkInContentString(strRequest, intReqType.intValue(), strReqValue)) {
                    continue;
                }
            }
            if (!strResValue.equals("")) {
                Integer intResType = (Integer) vtRow.elementAt(6);
                if (!checkInContentString(strResponse, intResType.intValue(), strResValue)) {
                    continue;
                }
            }
            return ((Integer) vtRow.elementAt(1)).intValue();
        }
        return -1;
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     * @param vtRecognition Vector
     * @param strRequest    String
     * @param strResponse   String
     * @return String
     * @throws Exception
     */
    ////////////////////////////////////////////////////////////////////////////
    private String[] getStatus(List vtRecognition, String strRequest, String strResponse) throws Exception {
        for (int i = 0; i < vtRecognition.size(); i++) {
            Vector vtRow = (Vector) vtRecognition.get(i);
            String strReqValue = (String) vtRow.elementAt(5);
            String strResValue = (String) vtRow.elementAt(7);
            if (strReqValue.equals("") && strResValue.equals("")) {
                continue;
            }
            if (!strReqValue.equals("")) {
                Integer intReqType = (Integer) vtRow.elementAt(4);
                if (!checkInContentString(strRequest, intReqType.intValue(), strReqValue)) {
                    continue;
                }
            }
            if (!strResValue.equals("")) {
                Integer intResType = (Integer) vtRow.elementAt(6);
                if (!checkInContentString(strResponse, intResType.intValue(), strResValue)) {
                    continue;
                }
            }
            return new String[]{String.valueOf(vtRow.elementAt(1)), (String) vtRow.elementAt(3)};
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     * check value have in content allow command
     *
     * @param strContent String
     * @param iCommand   int
     * @param strValue   String
     * @return true if and only if value is command in content otherwise return false
     */
    ////////////////////////////////////////////////////////////////////////////
    public boolean checkInContentString(String strContent,
                                        int iCommand, String strValue) {
        String strNValue = StringUtil.nvl(strValue, "").toLowerCase();
        String strNContent = StringUtil.nvl(strContent, "").toLowerCase();
        switch (iCommand) {
            case START_WITH:
                return strNContent.startsWith(strNValue);
            case INCLUDE:
                return strNContent.indexOf(strNValue) >= 0;
            case END_WITH:
                return strNContent.endsWith(strNValue);
            default:
                return false;
        }
    }


    public void syncData(Connection cn, int _int) throws Exception {
        Vector vtCache = new Vector();
        mvtUniqueCache = CollectionUtil.filterUnique2DVector(vtCache, new int[]{4, 5, 6, 7});
        mvtCache = CollectionUtil.merge2DVector(vtCache, new int[]{0, 1});
    }
}
