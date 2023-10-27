/* JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */
package com.telsoft.cbs.modules.cps_diameter;


import com.telsoft.util.StringUtil;
import org.jdiameter.api.*;
import org.jdiameter.api.app.StateChangeListener;
import org.jdiameter.client.api.IMessage;
import org.jdiameter.client.api.IMetaData;
import org.jdiameter.client.api.controller.IPeer;
import org.jdiameter.client.api.fsm.*;
import org.jdiameter.client.api.io.*;
import org.jdiameter.client.api.parser.IMessageParser;
import org.jdiameter.client.api.router.IRouter;
import org.jdiameter.client.impl.AbstractStateChangeListener;
import org.jdiameter.client.impl.DictionarySingleton;
import org.jdiameter.common.api.concurrent.IConcurrentFactory;
import org.jdiameter.common.api.data.ISessionDatasource;
import org.jdiameter.common.api.statistic.IStatistic;
import org.jdiameter.common.api.statistic.IStatisticFactory;
import org.jdiameter.common.impl.controller.AbstractPeer;
import org.jdiameter.server.impl.PeerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import telsoft.jdiameter.client.ClientConfig;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.jdiameter.api.Avp.*;
import static org.jdiameter.api.Message.*;
import static org.jdiameter.client.api.fsm.EventTypes.*;
import static org.jdiameter.client.impl.helpers.Parameters.SecurityRef;
import static org.jdiameter.client.impl.helpers.Parameters.UseUriAsFqdn;

/**
 * Client Peer implementation
 *
 * @author erick.svenson@yahoo.com
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public class ClientPeerImpl extends AbstractPeer implements IPeer {

    private static final Logger logger = LoggerFactory.getLogger(PeerImpl.class);
    // XXX: FT/HA // protected Map<String, NetworkReqListener> slc;
    protected final Map<Long, IMessage> peerRequests = new ConcurrentHashMap<Long, IMessage>();
    protected final org.jdiameter.api.validation.Dictionary dictionary = DictionarySingleton.getDictionary();
    // Properties
    protected InetAddress[] addresses;
    protected String realmName;
    protected long vendorID;
    protected String productName;
    protected int firmWare;
    protected Set<ApplicationId> commonApplications = new HashSet<ApplicationId>();
    protected AtomicLong hopByHopId = new AtomicLong(uid.nextInt());
    protected int rating;
    protected boolean stopping = false;
    // Members
    protected IMetaData metaData;
    protected PeerTableImpl table;
    // Facilities
    protected IRouter router;
    // FSM layer
    protected IStateMachine fsm;
    protected IMessageParser parser;
    // Feature
    protected boolean useUriAsFQDN = false; // Use URI as orign host name into CER command

    //session store and data
    protected ISessionDatasource sessionDataSource;

    protected CpsDiameterDispatcherSplitThread dispatcher;

    // Transport layer
    protected IConnection connection;
    protected IConnectionListener connListener = new IConnectionListener() {

        public void connectionOpened(String connKey) {
            dispatcher.connectionOpened(connKey);
            logger.debug("Connection to {} is opened", uri);
            try {
                fsm.handleEvent(new FsmEvent(CONNECT_EVENT, connKey));
            } catch (Exception e) {
                logger.warn("Can not run start procedure", e);
            }
        }

        @SuppressWarnings("unchecked")
        public void connectionClosed(String connKey, List notSended) {
            logger.debug("Connection from {} is closed", uri);
            dispatcher.connectionClosed(connKey, notSended);
            for (IMessage request : peerRequests.values()) {
                if (request.getState() == IMessage.STATE_SENT) {
                    request.setReTransmitted(true);
                    request.setState(IMessage.STATE_NOT_SENT);
                    try {
                        peerRequests.remove(request.getHopByHopIdentifier());
                        table.sendMessage(request);
                    } catch (Throwable exc) {
                        request.setReTransmitted(false);
                    }
                }
            }
            try {
                fsm.handleEvent(new FsmEvent(DISCONNECT_EVENT, connKey));
            } catch (Exception e) {
                logger.warn("Can not run stopping procedure", e);
            }
        }

        public void messageReceived(String connKey, IMessage message) {
            dispatcher.messageReceived(connKey, message);
            boolean req = message.isRequest();
            try {
                int type = message.getCommandCode();
                logger.debug("Receive message type {} to peer {}", new Object[]{type, connKey});
                switch (type) {
                    case CAPABILITIES_EXCHANGE_REQUEST:
                        fsm.handleEvent(new FsmEvent(req ? CER_EVENT : CEA_EVENT, message, connKey));
                        break;
                    case DEVICE_WATCHDOG_REQUEST:
                        fsm.handleEvent(new FsmEvent(req ? DWR_EVENT : DWA_EVENT, message, connKey));
                        break;
                    case DISCONNECT_PEER_REQUEST:
                        fsm.handleEvent(new FsmEvent(req ? DPR_EVENT : DPA_EVENT, message));
                        break;
                    default:
                        fsm.handleEvent(new FsmEvent(RECEIVE_MSG_EVENT, message));
                        break;
                }
            } catch (Exception e) {
                logger.warn("Error during processing incomming message", e);
                if (req) {
                    try {
                        message.setRequest(false);
                        message.setError(true);
                        message.getAvps().addAvp(Avp.RESULT_CODE, ResultCode.TOO_BUSY, true);
                        connection.sendMessage(message);
                    } catch (Exception exc) {
                        logger.warn("Can not send error answer", exc);
                    }
                }
            }
        }

        public void internalError(String connKey, IMessage message, TransportException cause) {
            dispatcher.internalError(connKey, message, cause);
            try {
                logger.debug("internalError ", cause);
                fsm.handleEvent(new FsmEvent(INTERNAL_ERROR, message));
            } catch (Exception e) {
                logger.debug("Can not run internalError procedure", e);
            }
        }
    };

    public ClientPeerImpl(final PeerTableImpl table, int rating, URI remotePeer, String ip, String portRange,
                          IMetaData metaData, Configuration config,
                          Configuration peerConfig, IFsmFactory fsmFactory, ITransportLayerFactory trFactory,
                          IStatisticFactory statisticFactory,
                          IConcurrentFactory concurrentFactory, IMessageParser parser,
                          final ISessionDatasource sessionDataSource) throws InternalException, TransportException {
        this(table, rating, remotePeer, ip, portRange, metaData, config, peerConfig, fsmFactory, trFactory, parser,
                statisticFactory, concurrentFactory, null, sessionDataSource);
    }

    @SuppressWarnings("unchecked")
    protected ClientPeerImpl(final PeerTableImpl table, int rating, URI remotePeer, String ip, String portRange,
                             IMetaData metaData,
                             Configuration config, Configuration peerConfig, IFsmFactory fsmFactory,
                             ITransportLayerFactory trFactory,
                             IMessageParser parser, IStatisticFactory statisticFactory,
                             IConcurrentFactory concurrentFactory,
                             IConnection connection, final ISessionDatasource sessionDataSource) throws
            InternalException,
            TransportException {
        super(remotePeer, statisticFactory);
        this.table = table;
        this.dispatcher = ((ClientConfiguration) config).getDispatcherSplitThread();
        this.rating = rating;

        try {
            Field f = PeerTableImpl.class.getDeclaredField("router");
            if (f != null) {
                f.setAccessible(true);
            }
            this.router = (IRouter) f.get(table);
        } catch (Exception ex) {
            throw new InternalException(ex);
        }
        this.metaData = metaData;
        // XXX: FT/HA // this.slc = table.getSessionReqListeners();
        this.sessionDataSource = sessionDataSource;

        int port = remotePeer.getPort();
        InetAddress remoteAddress;
        try {
            remoteAddress = InetAddress.getByName(ip != null ? ip : remotePeer.getFQDN());
        } catch (UnknownHostException e) {
            throw new TransportException("Can not found host", TransportError.Internal, e);
        }
        IContext actionContext = getContext();
        this.fsm = fsmFactory.createInstanceFsm(actionContext, concurrentFactory, config);
        this.fsm.addStateChangeNotification(
                new AbstractStateChangeListener() {
                    public void stateChanged(Enum oldState, Enum newState) {
                        PeerState s = (PeerState) newState;
                        if (PeerState.DOWN.equals(s)) {
                            stopping = false;
                        }
                    }
                }
        );
        if (connection == null) {
            String ref = peerConfig.getStringValue(SecurityRef.ordinal(), null);
            InetAddress localAddress = null;

            int localPort = 0;

            String strPort = StringUtil.nvl(dispatcher.getParameter("LocalPort"), "0");
            if (strPort.equals("")) {
                strPort = "0";
            }
            localPort = Integer.parseInt(strPort);

            if (localPort != 0 && portRange != null) {
                try {
                    Peer local = metaData.getLocalPeer();
                    if (local.getIPAddresses() != null && local.getIPAddresses().length > 0) {
                        localAddress = local.getIPAddresses()[0];
                    } else {
                        localAddress = InetAddress.getByName(metaData.getLocalPeer().getUri().getFQDN());
                    }
                } catch (Exception e) {
                    logger.warn("Can not get local address", e);
                }
                try {
                    String[] rng = portRange.trim().split("-");
                    int strRange = Integer.parseInt(rng[0]);
                    int endRange = Integer.parseInt(rng[1]);
                    localPort = strRange + new Random().nextInt(endRange - strRange + 1);
                } catch (Exception exc) {
                    logger.warn("Can not get local port", exc);
                }
                logger.debug("Create conn with localAddress={}; localPort={}", localAddress, localPort);
            }
            this.connection = trFactory.createConnection(remoteAddress, concurrentFactory, port, localAddress,
                    localPort, connListener, ref);
        } else {
            this.connection = connection;
            this.connection.addConnectionListener(connListener);
        }
        this.parser = parser;
        this.addresses = new InetAddress[]{remoteAddress};
        this.useUriAsFQDN = config.getBooleanValue(UseUriAsFqdn.ordinal(), (Boolean) UseUriAsFqdn.defValue());
    }

    public IContext getContext() {
        return new ActionContext();
    }

    private boolean isRedirectAnswer(Avp avpResCode, IMessage answer) {
        try {
            return (answer.getFlags() & 0x20) != 0 && avpResCode != null &&
                    avpResCode.getInteger32() == ResultCode.REDIRECT_INDICATION;
        } catch (AvpDataException e) {
            return false;
        }
    }

    public IStatistic getStatistic() {
        return statistic;
    }

    @SuppressWarnings("unchecked")
    public void addPeerStateListener(final PeerStateListener listener) {
        fsm.addStateChangeNotification(new AbstractStateChangeListener() {

            public void stateChanged(Enum oldState, Enum newState) {
                listener.stateChanged((PeerState) oldState, (PeerState) newState);
            }

            public int hashCode() {
                return listener.hashCode();
            }

            public boolean equals(Object obj) {
                return listener.equals(obj);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public void removePeerStateListener(final PeerStateListener listener) {
        if (listener != null) {
            fsm.remStateChangeNotification(new AbstractStateChangeListener() {
                public void stateChanged(Enum oldState, Enum newState) {
                    listener.stateChanged((PeerState) oldState, (PeerState) newState);
                }

                public int hashCode() {
                    return listener.hashCode();
                }

                public boolean equals(Object obj) {
                    return listener.equals(obj);
                }
            });
        }
    }

    private IMessage processRedirectAnswer(IMessage answer) {
        int resultCode = ResultCode.SUCCESS;
        // Update redirect information
        try {
            router.updateRedirectInformation(answer);
        } catch (RouteException exc) {
            // Loop detected (may be stack must send error response to redirect host)
            resultCode = ResultCode.LOOP_DETECTED;
        } catch (Throwable exc) {
            // Incorrect redirect message
            resultCode = ResultCode.UNABLE_TO_DELIVER;
        }
        // Update destination avps
        if (resultCode == ResultCode.SUCCESS) {
            // Clear avps
            answer.getAvps().removeAvp(RESULT_CODE);
            // Update flags
            answer.setRequest(true);
            answer.setError(false);
            try {
                table.sendMessage(answer);
                answer = null;
            } catch (Exception e) {
                logger.warn("Unable to deliver due to error", e);
                resultCode = ResultCode.UNABLE_TO_DELIVER;
            }
        }
        if (resultCode != ResultCode.SUCCESS) {
            // Restore answer flag
            answer.setRequest(false);
            answer.setError(true);
            answer.getAvps().removeAvp(RESULT_CODE);
            answer.getAvps().addAvp(RESULT_CODE, resultCode, true, false, true);
        }
        return answer;
    }

    public void connect() throws InternalException, IOException, IllegalDiameterStateException {
        if (getState(PeerState.class) != PeerState.DOWN) {
            throw new IllegalDiameterStateException("Invalid state:" + getState(PeerState.class));
        }
        try {
            fsm.handleEvent(new FsmEvent(EventTypes.START_EVENT));
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

    public void disconnect() throws InternalException, IllegalDiameterStateException {
        if (getState(PeerState.class) != PeerState.DOWN) {
            stopping = true;
            try {
                fsm.handleEvent(new FsmEvent(STOP_EVENT));
            } catch (OverloadException e) {
                stopping = false;
                logger.warn("Error during stopping procedure", e);
            }
        }
    }

    public <E> E getState(Class<E> enumc) {
        return fsm.getState(enumc);
    }

    public URI getUri() {
        return uri;
    }

    public InetAddress[] getIPAddresses() {
        return addresses;
    }

    public String getRealmName() {
        return realmName;
    }

    public long getVendorId() {
        return vendorID;
    }

    public String getProductName() {
        return productName;
    }

    public long getFirmware() {
        return firmWare;
    }

    public Set<ApplicationId> getCommonApplications() {
        return commonApplications;
    }

    public long getHopByHopIdentifier() {
        return hopByHopId.incrementAndGet();
    }

    public void addMessage(IMessage message) {
        peerRequests.put(message.getHopByHopIdentifier(), message);
    }

    public void remMessage(IMessage message) {
        peerRequests.remove(message.getHopByHopIdentifier());
    }

    public IMessage[] remAllMessage() {
        IMessage[] m = peerRequests.values().toArray(new IMessage[peerRequests.size()]);
        peerRequests.clear();
        return m;
    }

    public boolean handleMessage(EventTypes type, IMessage message, String key) throws TransportException,
            OverloadException, InternalException {
        return !stopping && fsm.handleEvent(new FsmEvent(type, message, key));
    }

    public boolean sendMessage(IMessage message) throws TransportException, OverloadException, InternalException {
        if (dictionary != null && dictionary.isEnabled()) {
            dictionary.validate(message, false);
        }
        return !stopping && fsm.handleEvent(new FsmEvent(EventTypes.SEND_MSG_EVENT, message));
    }

    public boolean hasValidConnection() {
        return connection != null && connection.isConnected();
    }

    public void setRealm(String realm) {
        realmName = realm;
    }

    @SuppressWarnings("unchecked")
    public void addStateChangeListener(StateChangeListener listener) {
        fsm.addStateChangeNotification(listener);
    }

    @SuppressWarnings("unchecked")
    public void remStateChangeListener(StateChangeListener listener) {
        fsm.remStateChangeNotification(listener);
    }

    public void addConnectionListener(IConnectionListener listener) {
        if (connection != null) {
            connection.addConnectionListener(listener);
        }
    }

    public void remConnectionListener(IConnectionListener listener) {
        if (connection != null) {
            connection.remConnectionListener(listener);
        }
    }

    public int getRating() {
        return rating;
    }

    public String toString() {
        return "Peer{" + "Uri=" + uri + "; State=" + fsm.getState(PeerState.class).toString() + "}";
    }

    protected void fillIPAddressTable(IMessage message) {
        AvpSet avps = message.getAvps().getAvps(HOST_IP_ADDRESS);
        if (avps != null) {
            ArrayList<InetAddress> t = new ArrayList<InetAddress>();
            for (int i = 0; i < avps.size(); i++) {
                try {
                    t.add(avps.getAvpByIndex(i).getAddress());
                } catch (AvpDataException e) {
                    logger.warn("Can not get ip address from HOST_IP_ADDRESS avp");
                }
            }
            addresses = t.toArray(new InetAddress[t.size()]);
        }
    }

    protected Set<ApplicationId> getCommonApplicationIds(IMessage message) {
        Set<ApplicationId> newAppId = new HashSet<ApplicationId>();
        Set<ApplicationId> locAppId = metaData.getLocalPeer().getCommonApplications();
        Set<ApplicationId> remAppId = message.getApplicationIdAvps();
        logger.debug("Checking common applications. Remote applications: {}. Local applications: {}",
                new Object[]{remAppId, locAppId});
        // check common application
        for (ApplicationId l : locAppId) {
            for (ApplicationId r : remAppId) {
                if (l.equals(r)) {
                    newAppId.add(l);
                } else if (r.getAcctAppId() == INT_COMMON_APP_ID || r.getAuthAppId() == INT_COMMON_APP_ID ||
                        l.getAcctAppId() == INT_COMMON_APP_ID || l.getAuthAppId() == INT_COMMON_APP_ID) {
                    newAppId.add(r);
                }
            }
        }
        return newAppId;
    }

    protected void preProcessRequest(IMessage answer) {
    }

    protected class ActionContext implements IContext {

        public void connect() throws InternalException, IOException, IllegalDiameterStateException {
            try {
                connection.connect();
                logger.debug("Connected to peer {}", ClientPeerImpl.this.getUri());
            } catch (TransportException e) {
                switch (e.getCode()) {
                    case NetWorkError:
                        throw new IOException("Can not connect to " + connection.getKey() + " - " + e.getMessage());
                    case FailedSendMessage:
                        throw new IllegalDiameterStateException(e);
                    default:
                        throw new InternalException(e);
                }
            }
        }

        public void disconnect() throws InternalException, IllegalDiameterStateException {
            if (connection != null) {
                connection.disconnect();
                logger.debug("Disconnected from peer {}", ClientPeerImpl.this.getUri());
            }
        }

        public String getPeerDescription() {
            return uri.toString();
        }

        public boolean isConnected() {
            return (connection != null) && connection.isConnected();
        }

        public boolean sendMessage(IMessage message) throws TransportException, OverloadException {
            // Check message
            if (message.isTimeOut()) {
                logger.debug("Message {} skipped (timeout)", message);
                return false;
            }
            if (message.getState() == IMessage.STATE_SENT) {
                logger.debug("Message {} already sent", message);
                return false;
            }
            // Remove destination information from answer messages
            if (!message.isRequest()) {
                message.getAvps().removeAvp(DESTINATION_HOST);
                message.getAvps().removeAvp(DESTINATION_REALM);

                int commandCode = message.getCommandCode();
                // We don't want this for CEx/DWx/DPx
                if (commandCode != 257 && commandCode != 280 && commandCode != 282) {
                    PeerTableImpl peerTable = (PeerTableImpl) table;
//                    if (peerTable.isDuplicateProtection()) {
//                        String[] originInfo = router.getRequestRouteInfo(message.getHopByHopIdentifier());
//                        if (originInfo != null) {
//                            // message.getDuplicationKey() doesn't work because it's answer
//                            peerTable.saveToDuplicate(message.getDuplicationKey(originInfo[0],
//                                                                                message.getEndToEndIdentifier()),
//                                                      message);
//                        }
//                    }
                }
            }

            // Send to network
            message.setState(IMessage.STATE_SENT);
            connection.sendMessage(message);

            logger.debug("Send message {} to peer {}", message, ClientPeerImpl.this.getUri());
            return true;
        }

        public void sendCerMessage() throws TransportException, OverloadException {
            logger.debug("Send CER message");
            IMessage message = parser.createEmptyMessage(CAPABILITIES_EXCHANGE_REQUEST, 0);
            message.setRequest(true);
            message.setHopByHopIdentifier(getHopByHopIdentifier());

            if (useUriAsFQDN) {
                message.getAvps().addAvp(ORIGIN_HOST, metaData.getLocalPeer().getUri().toString(), true, false, true);
            } else {
                message.getAvps().addAvp(ORIGIN_HOST, metaData.getLocalPeer().getUri().getFQDN(), true, false, true);
            }

            message.getAvps().addAvp(ORIGIN_REALM, metaData.getLocalPeer().getRealmName(), true, false, true);
            for (InetAddress ia : metaData.getLocalPeer().getIPAddresses()) {
                message.getAvps().addAvp(HOST_IP_ADDRESS, ia, true, false);
            }
            message.getAvps().addAvp(VENDOR_ID, metaData.getLocalPeer().getVendorId(), true, false, true);
            message.getAvps().addAvp(PRODUCT_NAME, metaData.getLocalPeer().getProductName(), false);
            for (ApplicationId appId : metaData.getLocalPeer().getCommonApplications()) {
                addAppId(appId, message);
            }

            if (ClientConfig.ADD_AUTH_APPLICATION_ID_TO_CER) {
                message.getAvps().addAvp(AUTH_APPLICATION_ID, ClientConfig.AUTH_APPLICATION_ID, true, false, true);
            }
            message.getAvps().addAvp(FIRMWARE_REVISION, metaData.getLocalPeer().getFirmware(), true);
            message.getAvps().addAvp(ORIGIN_STATE_ID, metaData.getLocalHostStateId(), true, false, true);
            sendMessage(message);
        }

        public void sendCeaMessage(int resultCode, Message cer, String errMessage) throws TransportException,
                OverloadException {

        }

        public void sendDwrMessage() throws TransportException, OverloadException {
            logger.debug("Send DWR message");
            IMessage message = parser.createEmptyMessage(DEVICE_WATCHDOG_REQUEST, 0);
            message.setRequest(true);
            message.setHopByHopIdentifier(getHopByHopIdentifier());
            // Set content
            message.getAvps().addAvp(ORIGIN_HOST, metaData.getLocalPeer().getUri().getFQDN(), true, false, true);
            message.getAvps().addAvp(ORIGIN_REALM, metaData.getLocalPeer().getRealmName(), true, false, true);
            message.getAvps().addAvp(ORIGIN_STATE_ID, metaData.getLocalHostStateId(), true, false, true);
            // Remove trash avp
            message.getAvps().removeAvp(DESTINATION_HOST);
            message.getAvps().removeAvp(DESTINATION_REALM);
            // Send
            sendMessage(message);
        }

        public void sendDwaMessage(IMessage dwr, int resultCode, String errorMessage) throws TransportException,
                OverloadException {
            logger.debug("Send DWA message");
            IMessage message = parser.createEmptyMessage(dwr);
            message.setRequest(false);
            message.setHopByHopIdentifier(dwr.getHopByHopIdentifier());
            message.setEndToEndIdentifier(dwr.getEndToEndIdentifier());
            // Set content
            message.getAvps().addAvp(RESULT_CODE, resultCode, true, false, true);
            message.getAvps().addAvp(ORIGIN_HOST, metaData.getLocalPeer().getUri().getFQDN(), true, false, true);
            message.getAvps().addAvp(ORIGIN_REALM, metaData.getLocalPeer().getRealmName(), true, false, true);
            if (errorMessage != null) {
                message.getAvps().addAvp(ERROR_MESSAGE, errorMessage, false);
            }
            // Remove trash avp
            message.getAvps().removeAvp(DESTINATION_HOST);
            message.getAvps().removeAvp(DESTINATION_REALM);
            // Send
            sendMessage(message);
        }

        public boolean isRestoreConnection() {
            return false;
        }

        public void sendDprMessage(int disconnectCause) throws TransportException, OverloadException {
            logger.debug("Send DPR message");
            IMessage message = parser.createEmptyMessage(DISCONNECT_PEER_REQUEST, 0);
            message.setRequest(true);
            message.setHopByHopIdentifier(getHopByHopIdentifier());
            message.getAvps().addAvp(ORIGIN_HOST, metaData.getLocalPeer().getUri().getFQDN(), true, false, true);
            message.getAvps().addAvp(ORIGIN_REALM, metaData.getLocalPeer().getRealmName(), true, false, true);
            message.getAvps().addAvp(DISCONNECT_CAUSE, disconnectCause, true, false);
            sendMessage(message);
        }

        public void sendDpaMessage(IMessage dpr, int resultCode, String errorMessage) throws TransportException,
                OverloadException {
            logger.debug("Send DPA message");
            IMessage message = parser.createEmptyMessage(dpr);
            message.setRequest(false);
            message.setHopByHopIdentifier(dpr.getHopByHopIdentifier());
            message.setEndToEndIdentifier(dpr.getEndToEndIdentifier());
            message.getAvps().addAvp(RESULT_CODE, resultCode, true, false, true);
            message.getAvps().addAvp(ORIGIN_HOST, metaData.getLocalPeer().getUri().getFQDN(), true, false, true);
            message.getAvps().addAvp(ORIGIN_REALM, metaData.getLocalPeer().getRealmName(), true, false, true);
            if (errorMessage != null) {
                message.getAvps().addAvp(ERROR_MESSAGE, errorMessage, false);
            }
            sendMessage(message);
        }

        public int processCerMessage(String key, IMessage message) {
            return 0;
        }

        public boolean processCeaMessage(String key, IMessage message) {
            boolean rc = true;
            try {
                Avp origHost = message.getAvps().getAvp(ORIGIN_HOST);
                Avp origRealm = message.getAvps().getAvp(ORIGIN_REALM);
                Avp vendorId = message.getAvps().getAvp(VENDOR_ID);
                Avp prdName = message.getAvps().getAvp(PRODUCT_NAME);
                Avp resCode = message.getAvps().getAvp(RESULT_CODE);
                Avp frmId = message.getAvps().getAvp(FIRMWARE_REVISION);
                if (origHost == null || origRealm == null || vendorId == null) {
                    logger.warn("Incorrect CEA message (missing mandatory AVPs)");
                } else {
                    if (realmName == null) {
                        realmName = origRealm.getOctetString();
                    }
                    if (vendorID == 0) {
                        vendorID = vendorId.getUnsigned32();
                    }
                    fillIPAddressTable(message);
                    if (productName == null && prdName != null) {
                        productName = prdName.getUTF8String();
                    }
                    if (resCode != null) {
                        int mrc = resCode.getInteger32();
                        if (mrc != ResultCode.SUCCESS) {
                            logger.debug("Result code value {}", mrc);
                            return false;
                        }
                    }
                    Set<ApplicationId> cai = getCommonApplicationIds(message);
                    if (cai.size() > 0) {
                        commonApplications.clear();
                        commonApplications.addAll(cai);
                    } else {
                        logger.debug("CEA did not containe appId, therefore  set local appids to common-appid field");
                        commonApplications.clear();
                        commonApplications.addAll(metaData.getLocalPeer().getCommonApplications());
                    }

                    if (firmWare == 0 && frmId != null) {
                        firmWare = frmId.getInteger32();
                    }
                }
            } catch (Exception exc) {
                logger.debug("Incorrect CEA message", exc);
                rc = false;
            }
            return rc;
        }

        @SuppressWarnings("unchecked")
        public boolean receiveMessage(IMessage message) {
            boolean isProcessed = false;
            if (message.isRequest()) {
                // Server request
                String avpSessionId = message.getSessionId();
                if (avpSessionId != null) {
                    // XXX: FT/HA // NetworkReqListener client = slc.get(avpSessionId);
                    NetworkReqListener listener = (NetworkReqListener) sessionDataSource.getSessionListener(
                            avpSessionId);
                    if (listener != null) {
                        router.registerRequestRouteInfo(message);
                        preProcessRequest(message);
                        IMessage answer = (IMessage) listener.processRequest(message);
                        if (answer != null) {
                            try {
                                sendMessage(answer);
                                statistic.getRecordByName(IStatistic.Counters.AppGenResponse.name()).inc();
                            } catch (Exception e) {
                                logger.warn("Can not send immediate answer {}", answer);
                            }
                        }
                        statistic.getRecordByName(IStatistic.Counters.NetGenRequest.name()).inc();
                        isProcessed = true;
                    } else {
                        statistic.getRecordByName(IStatistic.Counters.NetGenRejectedRequest.name()).inc();
                    }
                }
            } else {
                IMessage request = peerRequests.remove(message.getHopByHopIdentifier());
                if (request != null && !request.isTimeOut()) {
                    request.clearTimer();
                    request.setState(IMessage.STATE_ANSWERED);
                    Avp avpResCode = message.getAvps().getAvp(RESULT_CODE);
                    if (isRedirectAnswer(avpResCode, message)) {
                        message.setListener(request.getEventListener());
                        message = processRedirectAnswer(message);
                        isProcessed = message == null;
                    }

                    if (message != null) {
                        if (request.getEventListener() != null) {
                            request.getEventListener().receivedSuccessMessage(request, message);
                        } else {
                            logger.debug("Can not call answer client for request {} because client not set",
                                    message);
                            statistic.getRecordByName(IStatistic.Counters.NetGenRejectedResponse.name()).inc();
                        }

                        isProcessed = true;
                        statistic.getRecordByName(IStatistic.Counters.NetGenResponse.name()).inc();
                    } else {
                        statistic.getRecordByName(IStatistic.Counters.NetGenRejectedResponse.name()).inc();
                    }
                } else {
                    statistic.getRecordByName(IStatistic.Counters.NetGenRejectedResponse.name()).inc();
                }
            }
            return isProcessed;
        }

        public int processDwrMessage(IMessage iMessage) {
            return ResultCode.SUCCESS;
        }

        public int processDprMessage(IMessage iMessage) {
            return ResultCode.SUCCESS;
        }

        protected void addAppId(ApplicationId appId, IMessage message) { // todo duplicate code look SessionImpl 225 line
            if (appId.getVendorId() == 0) {
                if (appId.getAuthAppId() != 0) {
                    message.getAvps().addAvp(AUTH_APPLICATION_ID, appId.getAuthAppId(), true, false, true);
                } else if (appId.getAcctAppId() != 0) {
                    message.getAvps().addAvp(ACCT_APPLICATION_ID, appId.getAcctAppId(), true, false, true);
                }
            } else {
                message.getAvps().addAvp(SUPPORTED_VENDOR_ID, appId.getVendorId(), true, false, true);
                AvpSet vendorApp = message.getAvps().addGroupedAvp(VENDOR_SPECIFIC_APPLICATION_ID, true, false);
                vendorApp.addAvp(VENDOR_ID, appId.getVendorId(), true, false, true);
                if (appId.getAuthAppId() != 0) {
                    vendorApp.addAvp(AUTH_APPLICATION_ID, appId.getAuthAppId(), true, false, true);
                }
                if (appId.getAcctAppId() != 0) {
                    vendorApp.addAvp(ACCT_APPLICATION_ID, appId.getAcctAppId(), true, false, true);
                }
            }
        }

        public void removeStatistics() {
        }

        public void createStatistics() {
        }

    }
}
