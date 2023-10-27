package com.telsoft.cbs.module.sms;

import com.telsoft.thread.ManageableThread;
import com.telsoft.thread.ParameterType;
import com.telsoft.util.AppException;
import com.telsoft.util.StringUtil;
import org.jsmpp.bean.*;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.extra.SessionState;
import org.jsmpp.session.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import telsoft.gateway.commons.GWUtil;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class SmsTransmitter extends ManageableThread {
    private static final Logger log = LoggerFactory.getLogger(SmsTransmitter.class);

    private String host;
    private int port;
    private String systemId;
    private String password;
    private String systemType;
    private SMPPSession session = null;
    private TypeOfNumber address_ton;
    private NumberingPlanIndicator address_npi;
    private TypeOfNumber source_address_ton;
    private NumberingPlanIndicator source_address_npi;
    private String sourceAddress;
    private Random random = new Random();
    private String addressRange;
    private int keepaliveInterval;
    private int outgoingInterval;
    private Map<Integer, List<UDH>> messageQueue = new ConcurrentHashMap<>();
    private Comparator<UDH> udhSorter = (o1, o2) -> o1.seq - o2.seq;

    private static List<byte[]> splitByWidth(byte[] strContent, int width) throws Exception {

        if (strContent == null || strContent.length == 0) {
            return Collections.EMPTY_LIST;
        }
        if (width == 0 || strContent.length <= width) {
            return Arrays.asList(strContent);
        }

        List<byte[]> segment = new ArrayList<byte[]>();

        int NumSeg = strContent.length / width + 1;

        int startPos = 0;
        for (int i = 0; i < NumSeg - 1; i++) {
            byte[] data = new byte[((width * (i + 1))) - startPos];
            System.arraycopy(strContent, startPos, data, 0, data.length);
            segment.add(data);
            startPos = (i + 1) * width;
        }
        if (startPos < strContent.length) {
            byte[] data = new byte[strContent.length - startPos];
            System.arraycopy(strContent, startPos, data, 0, data.length);
            segment.add(data);
        }
        return segment;
    }

    public static UDH decodeUDH(byte[] sm) {
        byte iei = sm[1];
        if (iei != 0 && iei != 8) {
            return null;
        }

        UDH udh = new UDH();

        if (iei == 0) {
            udh.num = sm[4];
            udh.seq = sm[5];
            udh.id = sm[3];
            udh.sm = Arrays.copyOfRange(sm, 6, sm.length);
        } else {
            udh.num = sm[5];
            udh.seq = sm[6];
            udh.id = sm[3] << 8 | sm[4];
            udh.sm = Arrays.copyOfRange(sm, 7, sm.length);
        }
        return udh;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    @Override
    public void fillParameter() throws AppException {
        super.fillParameter();
        host = loadString("Host");
        port = loadUnsignedInteger("Port");
        systemId = loadString("SystemId");
        password = loadString("Password");
        systemType = loadString("SystemType");
        address_ton = TypeOfNumber.valueOf(loadString("address-ton"));
        address_npi = NumberingPlanIndicator.valueOf(loadString("address-npi"));
        addressRange = StringUtil.nvl(getParameter("address-range"), "");

        source_address_ton = TypeOfNumber.valueOf(loadString("source-address-ton"));
        source_address_npi = NumberingPlanIndicator.valueOf(loadString("source-address-npi"));
        sourceAddress = loadString("source-address");

        keepaliveInterval = loadInteger("keepalive-internal");
        outgoingInterval = loadInteger("outgoing-internal");
    }

    @Override
    public Vector getParameterDefinition() {
        Vector vtReturn = new Vector();
        vtReturn.add(createParameter("Host", "", ParameterType.PARAM_TEXTBOX_MAX, "50", "SMPP Host"));
        vtReturn.add(createParameter("Port", "", ParameterType.PARAM_TEXTBOX_MASK, "99999", "SMPP Port"));

        vtReturn.add(createParameter("SystemId", "", ParameterType.PARAM_TEXTBOX_MAX, "50", "SystemId"));
        vtReturn.add(createParameter("Password", "", ParameterType.PARAM_PASSWORD, "50", "Password"));
        vtReturn.add(createParameter("SystemType", "", ParameterType.PARAM_TEXTBOX_MAX, "50", "System Type"));

        vtReturn.add(createParameter("address-ton", "", ParameterType.PARAM_COMBOBOX, TypeOfNumber.class, ""));
        vtReturn.add(createParameter("address-npi", "", ParameterType.PARAM_COMBOBOX, NumberingPlanIndicator.class, ""));
        vtReturn.add(createParameter("address-range", "", ParameterType.PARAM_TEXTBOX_MAX, "20", ""));

        vtReturn.add(createParameter("source-address-ton", "", ParameterType.PARAM_COMBOBOX, TypeOfNumber.class, ""));
        vtReturn.add(createParameter("source-address-npi", "", ParameterType.PARAM_COMBOBOX, NumberingPlanIndicator.class, ""));
        vtReturn.add(createParameter("source-address", "", ParameterType.PARAM_TEXTBOX_MAX, "20", ""));

        vtReturn.add(createParameter("keepalive-internal", "", ParameterType.PARAM_TEXTBOX_MASK, "00000", ""));
        vtReturn.add(createParameter("outgoing-internal", "", ParameterType.PARAM_TEXTBOX_MASK, "00000", ""));

        vtReturn.addAll(super.getParameterDefinition());
        return vtReturn;
    }

    public String getShortMessage(DeliverSm deliverSm) throws UnsupportedEncodingException {
        byte[] sm = deliverSm.getShortMessage();
        if (deliverSm.isUdhi()) {
            UDH udh = decodeUDH(sm);
            sm = mergeLongMessage(udh);
            if (sm == null) {
                return null;
            }
        }

        Alphabet encode = Alphabet.valueOf(deliverSm.getDataCoding());
        String charsetEncode = "";
        switch (encode) {
            case ALPHA_DEFAULT:
                charsetEncode = "";
                break;
            case ALPHA_UCS2:
                charsetEncode = "UTF-16BE";
                break;
            default:
                charsetEncode = "";
        }
        if (charsetEncode == null || charsetEncode.equals("")) {
            return new String(sm);
        } else {
            return new String(sm, charsetEncode);
        }
    }

    private byte[] mergeLongMessage(UDH udh) {
        if (udh.num <= 1) {
            return udh.sm;
        } else {
            List<UDH> udhList = messageQueue.get(udh.id);
            if (udhList == null) {
                udhList = new ArrayList<>();
                messageQueue.put(udh.id, udhList);
            }
            udhList.add(udh);

            if (udhList.size() == udh.num) {
                Collections.sort(udhList, udhSorter);
                messageQueue.remove(udh.id);
                int size = 0;
                for (UDH u : udhList) {
                    size += u.sm.length;
                }
                byte[] sm = new byte[size];
                int iIndex = 0;
                for (UDH u : udhList) {
                    System.arraycopy(u.sm, 0, sm, iIndex, u.sm.length);
                    iIndex += u.sm.length;
                }
                return sm;
            } else {
                return null;
            }
        }
    }

    @Override
    protected void processSession() throws Exception {
        try {
            start();
            while (isRunning()) {
                processOutgoingMessage();
                Thread.sleep(outgoingInterval);
                fillLogFile();
                validateParameter();
            }
        } finally {
            stop();
        }
    }

    public boolean isRunning() {
        return session != null & session.getSessionState().isBound();
    }


    public void start() throws Exception {
        BindParameter bindParameter = new BindParameter(BindType.BIND_TRX, systemId, password, systemType, address_ton, address_npi, addressRange);

        session = new SMPPSession();
        if (keepaliveInterval > 0) {
            session.setEnquireLinkTimer(keepaliveInterval);
        }
        session.addSessionStateListener((newState, oldState, source) -> {
            if (newState == SessionState.CLOSED) {
                try {
                    stop();
                } catch (Exception e) {
                }
            }
        });

        session.setMessageReceiverListener(new MessageReceiverListener() {
            @Override
            public void onAcceptDeliverSm(DeliverSm deliverSm) throws ProcessRequestException {
                SmsMessage message = null;
                try {
                    String shortMessage = getShortMessage(deliverSm);
                    if (shortMessage != null) {
                        message = new SmsMessage(deliverSm.getSourceAddr(), deliverSm.getDestAddress(), shortMessage);
                    }
                } catch (UnsupportedEncodingException e) {
                    log.error("Failed getting delivery receipt.", e);
                }
                if (message != null) {
                    logMonitor("SMSC --> " + message.toString());
                    messageIncomming(message);
                }
            }

            @Override
            public void onAcceptAlertNotification(AlertNotification alertNotification) {

            }

            @Override
            public DataSmResult onAcceptDataSm(DataSm dataSm, Session session) throws ProcessRequestException {
                return null;
            }
        });
        session.connectAndBind(host, port, bindParameter);
    }

    public void stop() throws Exception {
        if (session != null) {
            SMPPSession tmp = session;
            session = null;
            try {
                tmp.unbindAndClose();
            } catch (Throwable ex) {
                logMonitor(GWUtil.decodeException(ex));
            }
        }
    }

    protected abstract void messageIncomming(SmsMessage message);

    protected abstract void processOutgoingMessage();

    public boolean sendMessage(SmsMessage message) {
        try {
            Alphabet alphabet = Alphabet.ALPHA_DEFAULT;
            String encode = "";
            int splitSize = 150;

            int l1 = message.getContent().length();
            int l2 = message.getContent().getBytes("UTF-8").length;
            if (l1 != l2) {
                alphabet = Alphabet.ALPHA_UCS2;
                encode = "UTF-16BE";
                splitSize = 70;
            }


            if (message.getContent().length() <= splitSize) {
                String messageId = session.submitShortMessage(systemType, source_address_ton,
                        source_address_npi, message.getOriginator(), address_ton, address_npi, message.getReceiver(),
                        new ESMClass(), (byte) 0, (byte) 1, null, null,
                        new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT), (byte) 0,
                        new GeneralDataCoding(false, true, MessageClass.CLASS1, alphabet),
                        (byte) 0, encode.equals("") ? message.getContent().getBytes() : message.getContent().getBytes(encode));

                log.debug("Message submitted, message_id is {}", messageId);
                return true;
            } else {
                List<byte[]> strMessageSegment = splitByWidth(encode.equals("") ? message.getContent().getBytes() : message.getContent().getBytes(encode), splitSize);

                OptionalParameter sarMsgRefNum = OptionalParameters.newSarMsgRefNum((short) random.nextInt());
                OptionalParameter sarTotalSegments = OptionalParameters.newSarTotalSegments(strMessageSegment.size());

                for (int i = 0; i < strMessageSegment.size(); i++) {

                    final int seqNum = i + 1;
                    byte[] msg = strMessageSegment.get(i);
                    OptionalParameter sarSegmentSeqnum = OptionalParameters.newSarSegmentSeqnum(seqNum);

                    String messageId = session.submitShortMessage(systemType, source_address_ton,
                            source_address_npi, message.getOriginator(), address_ton, address_npi, message.getReceiver(),
                            new ESMClass(), (byte) 0, (byte) 1, null, null,
                            new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT), (byte) 0,
                            new GeneralDataCoding(false, true, MessageClass.CLASS1, alphabet),
                            (byte) 0, msg, sarMsgRefNum,
                            sarSegmentSeqnum, sarTotalSegments);
                    log.debug("Message segment submitted, message_id is {}", messageId);
                }
                return true;
            }
        } catch (Exception ex) {
            logMonitor(GWUtil.decodeException(ex));
            return false;
        }
    }

    public static class UDH {
        int id;
        byte[] sm;
        int num;
        int seq;
    }
}
