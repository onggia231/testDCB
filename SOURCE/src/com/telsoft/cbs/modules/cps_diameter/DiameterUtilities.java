package com.telsoft.cbs.modules.cps_diameter;

import com.telsoft.dictionary.DictionaryNode;
import com.telsoft.util.StringUtil;
import org.apache.commons.codec.binary.Hex;
import org.jdiameter.api.*;
import org.jdiameter.api.validation.AvpRepresentation;
import org.jdiameter.client.api.IMessage;
import telsoft.jdiameter.message.AvpDictionary;

import java.io.ByteArrayInputStream;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Vector;

/**
 * Created by khanhnc on 9/16/15.
 */
public final class DiameterUtilities {
    public static final String[] TYPES = {"UINT", "UINT64", "INT",
            "INT64", "OString", "UString", "URI",
            "Enum", "Time", "IP", "BYTE"};

    // KHANHNC 20150615
    public static void removeAVP(DictionaryNode ndRemove, AvpSet avpSet) {
        if (ndRemove != null) {
            Vector<DictionaryNode> vt = ndRemove.getChildList();
            for (DictionaryNode node : vt) {
                String code = node.mstrName;
                int avpCode = Integer.parseInt(code);
                avpSet.removeAvp(avpCode);
            }
        }
    }

    /**
     * @param strPath String
     * @return Avp
     */
    public static Avp getAvp(String strPath, Message message) throws Exception {
        String[] path = StringUtil.toStringArray(strPath, ".");
        if (path.length <= 0) {
            return null;
        }
        int[] intPath = new int[path.length];
        for (int i = 0; i < path.length; i++) {
            intPath[i] = Integer.parseInt(path[i]);
        }
        AvpSet currentSet = message.getAvps();
        int iLen = path.length;
        int iLastIndex = iLen - 1;
        for (int i = 0; i < iLen; i++) {
            if (currentSet == null) {
                return null;
            }
            if (i < iLastIndex) {
                Avp avp = currentSet.getAvp(intPath[i]);
                if (avp == null) {
                    return null;
                }
                int iCode = avp.getCode();
                long lVendorId = avp.getVendorId();
                AvpRepresentation present = AvpDictionary.INSTANCE.getAvp(iCode, lVendorId);
                if (present == null || !present.isGrouped()) {
                    throw new Exception("Invalid avp path");
                } else {
                    currentSet = avp.getGrouped();
                }
            } else {
                Avp result = currentSet.getAvp(intPath[i]);
                return result;
            }
        }
        // never call here
        return null;
    }

    /**
     * @param strPath String
     * @param bParent boolean
     * @return AvpSet
     */
    public static AvpSet getAvpSet(String strPath, boolean bParent, Message message) throws Exception {
        String[] path = StringUtil.toStringArray(strPath, ".");
        if (path.length <= 0) {
            return null;
        }
        int[] intPath = new int[path.length];
        for (int i = 0; i < path.length; i++) {
            intPath[i] = Integer.parseInt(path[i]);
        }
        AvpSet currentSet = message.getAvps();
        int iLen = path.length;
        int iLastIndex = iLen - 1;
        for (int i = 0; i < iLen; i++) {
            if (currentSet == null) {
                return null;
            }
            if (i < iLastIndex) {
                Avp avp = currentSet.getAvp(intPath[i]);
                if (avp == null) {
                    return null;
                }
                int iCode = avp.getCode();
                long lVendorId = avp.getVendorId();
                AvpRepresentation present = AvpDictionary.INSTANCE.getAvp(iCode, lVendorId);
                if (present == null || !present.isGrouped()) {
                    throw new Exception("Invalid avp path");
                } else {
                    currentSet = avp.getGrouped();
                }
            } else {
                if (bParent) {
                    return currentSet;
                }
                Avp avp = currentSet.getAvp(intPath[i]);
                if (avp == null) {
                    return null;
                }

                AvpSet result = null;
                int iCode = avp.getCode();
                long lVendorId = avp.getVendorId();
                AvpRepresentation present = AvpDictionary.INSTANCE.getAvp(iCode, lVendorId);
                if (present == null || !present.isGrouped()) {
                    throw new Exception("Invalid avp path");
                } else {
                    result = avp.getGrouped();
                }
                return result;
            }
        }
        // never call here
        return null;
    }

    /**
     * @param strCommand
     * @param sessionFactory
     * @param mstrRealm
     * @param mstrServerHost
     * @return
     * @throws Exception
     */
    public static RequestRecord analyzerCommand(String strCommand, SessionFactory sessionFactory, String mstrRealm, String mstrServerHost) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(strCommand.getBytes());
        com.telsoft.dictionary.Dictionary dic = new com.telsoft.dictionary.Dictionary(in);
        return analyzerCommand(dic, sessionFactory, mstrRealm, mstrServerHost);
    }

    /**
     * @param dic
     * @param sessionFactory
     * @param mstrRealm
     * @param mstrServerHost
     * @return
     * @throws Exception
     */
    public static RequestRecord analyzerCommand(com.telsoft.dictionary.Dictionary dic, SessionFactory sessionFactory,
                                                String mstrRealm, String mstrServerHost) throws
            Exception {
        DictionaryNode ndHeader = dic.getChildIgnoreCase("header");
        String mstrCommandCode = ndHeader.getStringIgnoreCase("CommandCode");
        String mstrAppID = ndHeader.getStringIgnoreCase("ApplicationId");
        String mstrVendorId = ndHeader.getStringIgnoreCase("VendorId");
        String mstrSessionId = ndHeader.getStringIgnoreCase("SessionId");
        Session session = null;
        if (mstrSessionId != null && !mstrSessionId.equals("")) {
            session = sessionFactory.getNewSession(mstrSessionId);
        } else {
            session = sessionFactory.getNewSession();
        }

        long appID = Long.parseLong(mstrAppID);
        Request request = null;
        if (mstrVendorId != null && mstrVendorId.length() > 0) {
            long vendorID = Long.parseLong(mstrVendorId);
            request = session.createRequest(Integer.parseInt(mstrCommandCode),
                    ApplicationId.createByAccAppId(vendorID, appID),
                    mstrRealm, mstrServerHost);
        } else {
            request = session.createRequest(Integer.parseInt(mstrCommandCode),
                    ApplicationId.createByAccAppId(appID), mstrRealm,
                    mstrServerHost);
        }
        AvpSet reqAvpSet = request.getAvps();
        reqAvpSet.removeAvp(Avp.SESSION_ID);
        reqAvpSet.addAvp(Avp.SESSION_ID, mstrSessionId, false);

        DictionaryNode ndAVP = dic.getChildIgnoreCase("avp");
        analysisAVP(ndAVP, request.getAvps());
        RequestRecord rr = new RequestRecord();
        rr.request = request;
        rr.session = session;
        return rr;
    }

    /**
     * @param ndAVP  DictionaryNode
     * @param parent AvpSet
     * @throws Exception
     */
    public static void analysisAVP(DictionaryNode ndAVP, AvpSet parent) throws Exception {
        Vector<DictionaryNode> vt = ndAVP.getChildList();
        for (DictionaryNode node : vt) {
            String avpCode = node.mstrValue;
            int iCode = Integer.parseInt(avpCode);

            String avpFlags = node.getStringIgnoreCase("flags").toLowerCase();
            boolean mFlag = (avpFlags != null) && avpFlags.contains("m");
            boolean pFlag = (avpFlags != null) && avpFlags.contains("p");

            if (node.mstrName.equalsIgnoreCase("avp")) {

                String avpValue = node.getStringIgnoreCase("value");
                int iType = -1;
                String avpType = node.getStringIgnoreCase("type");
                for (int i = 0; i < TYPES.length; i++) {
                    if (TYPES[i].equalsIgnoreCase(avpType)) {
                        iType = i;
                        break;
                    }
                }
                String avpVendor = node.getStringIgnoreCase("vendor");
                if (avpVendor != null && avpVendor.length() > 0) {
                    long iVendor = Integer.parseInt(avpVendor);
                    switch (iType) {
                        case 0:
                            parent.addAvp(iCode, Long.parseLong(avpValue), iVendor, mFlag, pFlag, true);
                            break;
                        case 1:
                            parent.addAvp(iCode, Long.parseLong(avpValue), iVendor, mFlag, pFlag);
                            break;
                        case 2:
                            parent.addAvp(iCode, Integer.parseInt(avpValue), iVendor, mFlag, pFlag);
                            break;
                        case 3:
                            parent.addAvp(iCode, Long.parseLong(avpValue), iVendor, mFlag, pFlag);
                            break;
                        case 4:
                            parent.addAvp(iCode, avpValue, iVendor, mFlag, pFlag, true);
                            break;
                        case 5:
                            parent.addAvp(iCode, avpValue, iVendor, mFlag, pFlag, false);
                            break;
                        case 6:
                            parent.addAvp(iCode, new URI(avpValue), iVendor, mFlag, pFlag);
                            break;
                        case 7:
                            parent.addAvp(iCode, Integer.parseInt(avpValue), iVendor, mFlag, pFlag);
                            break;
                        case 8:
                            SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                            parent.addAvp(iCode, fmt.parse(avpValue), iVendor, mFlag, pFlag);
                            break;
                        case 9:
                            parent.addAvp(iCode, InetAddress.getByName(avpValue), iVendor, mFlag, pFlag);
                            break;
                        case 10:
                            parent.addAvp(iCode, avpValue.getBytes(), iVendor, mFlag, pFlag);
                            break;
                        default:
                            parent.addAvp(iCode, Hex.decodeHex(avpValue.toCharArray()), iVendor, mFlag, pFlag);
                    }
                } else {
                    switch (iType) {
                        case 0:
                            parent.addAvp(iCode, Long.parseLong(avpValue), true);
                            break;
                        case 1:
                            parent.addAvp(iCode, Long.parseLong(avpValue));
                            break;
                        case 2:
                            parent.addAvp(iCode, Integer.parseInt(avpValue));
                            break;
                        case 3:
                            parent.addAvp(iCode, Long.parseLong(avpValue));
                            break;
                        case 4:
                            parent.addAvp(iCode, avpValue, true);
                            break;
                        case 5:
                            parent.addAvp(iCode, avpValue, false);
                            break;
                        case 6:
                            parent.addAvp(iCode, new URI(avpValue));
                            break;
                        case 7:
                            parent.addAvp(iCode, Integer.parseInt(avpValue));
                            break;
                        case 8:
                            SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                            parent.addAvp(iCode, fmt.parse(avpValue));
                            break;
                        case 9:
                            parent.addAvp(iCode, InetAddress.getByName(avpValue));
                            break;
                        case 10:
                            parent.addAvp(iCode, avpValue.getBytes());
                            break;
                        default:
                            parent.addAvp(iCode, Hex.decodeHex(avpValue.toCharArray()));
                    }
                }
            } else if (node.mstrName.equalsIgnoreCase("avp-set")) {
                String avpVendor = node.getStringIgnoreCase("vendor");
                if (avpVendor != null && avpVendor.length() > 0) {
                    long iVendor = Integer.parseInt(avpVendor);
                    AvpSet set = parent.addGroupedAvp(iCode, iVendor, mFlag, pFlag);
                    analysisAVP(node, set);
                } else {
                    AvpSet set = parent.addGroupedAvp(iCode, mFlag, pFlag);
                    analysisAVP(node, set);
                }
            }
        }
    }


    public static void addOriginAvps(IMessage request, IMessage response) {
        try {
            String strRequestDesnationHost = "";
            String strRequestDesnationRealm = "";
            String strRequestOriginHost = "";
            String strRequestOriginRealm = "";

            if (request.getAvps().getAvp(Avp.DESTINATION_HOST) != null) {
                strRequestDesnationHost = request.getAvps().getAvp(Avp.DESTINATION_HOST).getDiameterIdentity();
            }

            if (request.getAvps().getAvp(Avp.DESTINATION_REALM) != null) {
                strRequestDesnationRealm = request.getAvps().getAvp(Avp.DESTINATION_REALM).getDiameterIdentity();
            }

            if (request.getAvps().getAvp(Avp.ORIGIN_HOST) != null) {
                strRequestOriginHost = request.getAvps().getAvp(Avp.ORIGIN_HOST).getDiameterIdentity();
            }

            if (request.getAvps().getAvp(Avp.ORIGIN_REALM) != null) {
                strRequestOriginRealm = request.getAvps().getAvp(Avp.ORIGIN_REALM).getDiameterIdentity();
            }

            response.getAvps().removeAvp(Avp.ORIGIN_HOST);
            response.getAvps().removeAvp(Avp.ORIGIN_REALM);
            response.getAvps().removeAvp(Avp.DESTINATION_HOST);
            response.getAvps().removeAvp(Avp.DESTINATION_REALM);

            response.getAvps().insertAvp(2, Avp.ORIGIN_HOST, strRequestDesnationHost, true, false, true);
            response.getAvps().insertAvp(3, Avp.ORIGIN_REALM, strRequestDesnationRealm, true, false, true);
            response.getAvps().insertAvp(4, Avp.DESTINATION_HOST, strRequestOriginHost, true, false, true);
            response.getAvps().insertAvp(5, Avp.DESTINATION_REALM, strRequestOriginRealm, true, false, true);


            response.setHeaderApplicationId(request.getHeaderApplicationId());
            response.setHopByHopIdentifier(request.getHopByHopIdentifier());
            response.setEndToEndIdentifier(request.getEndToEndIdentifier());

        } catch (Exception e) {

        }
    }

    public static class RequestRecord {
        public Request request;
        public Session session;
    }
}
