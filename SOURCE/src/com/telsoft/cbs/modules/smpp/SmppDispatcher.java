package com.telsoft.cbs.modules.smpp;

import com.telsoft.cbs.utils.CBDispatcherManager;
import com.telsoft.thread.ParameterType;
import com.telsoft.util.AppException;
import org.jsmpp.bean.*;
import org.jsmpp.examples.MessageReceiverListenerImpl;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.DataSmResult;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.session.Session;
import org.jsmpp.util.AbsoluteTimeFormatter;
import org.jsmpp.util.InvalidDeliveryReceiptException;
import org.jsmpp.util.TimeFormatter;
import telsoft.gateway.core.dsp.Dispatcher;

import java.io.IOException;
import java.util.Date;
import java.util.Vector;

/**
 * <p>Title: IN SOAP Protocol</p>
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
public class SmppDispatcher extends CBDispatcherManager {
    private static TimeFormatter timeFormatter = new AbsoluteTimeFormatter();
    private SMPPSession session = null;
    private String ipAddress = null;
    private int port = 0;
    private String systemId = null;
    private String password = null;
    private BindType bindMode = BindType.BIND_TX;
    private String mstrSystemType = "";
    private long receiveTimeout;
    private String addr;
    private int enquireInterval;
    private TypeOfNumber addrTON;
    private NumberingPlanIndicator addrNPI;
    private TypeOfNumber srcTON;
    private NumberingPlanIndicator srcNPI;
    private TypeOfNumber dstTON;
    private NumberingPlanIndicator dstNPI;

    public SmppDispatcher() {
    }

    /**
     * @return Dispatcher
     * @throws Exception
     */
    protected Dispatcher prepareDispatcher() throws Exception {
        return new SmppDispatcherSplitThread(this);
    }

    @Override
    protected void beforeSession() throws Exception {
        super.beforeSession();
        bind();
    }

    @Override
    protected void afterSession() throws Exception {
        unbind();
        super.afterSession();
    }

    public void bind() throws IOException {
        if (session != null) {
            unbind();
        }
        session = new SMPPSession();
        session.setMessageReceiverListener(new MessageReceiverListenerImpl() {
            public void onAcceptDeliverSm(DeliverSm deliverSm) throws ProcessRequestException {
                if (MessageType.SMSC_DEL_RECEIPT.containedIn(deliverSm.getEsmClass())) {
                    // Delivery notification, put in queue to notify to Boku
                    try {
                        DeliveryReceipt delReceipt = deliverSm.getShortMessageAsDeliveryReceipt();
                        long id = Long.parseLong(delReceipt.getId()) & -1L;
                        String messageId = Long.toString(id, 16).toUpperCase();
                        System.out.println("Receiving delivery receipt for message '" + messageId + " ' from " + deliverSm.getSourceAddr() + " to " + deliverSm.getDestAddress() + " : " + delReceipt);
                    } catch (InvalidDeliveryReceiptException var6) {
                        System.err.println("Failed getting delivery receipt");
                        var6.printStackTrace();
                    }
                } else {
                    // Received message from customer, submit mo to rest
                    System.out.println("Receiving message : " + new String(deliverSm.getShortMessage()));
                }

            }

            public DataSmResult onAcceptDataSm(DataSm dataSm, Session source) throws ProcessRequestException {
                System.out.println("Receiving DataSm : " + dataSm);
                return null;
            }

            public void onAcceptAlertNotification(AlertNotification alertNotification) {
            }

        });
        session.connectAndBind(ipAddress, port,
                new BindParameter(bindMode, systemId, password, mstrSystemType,
                        addrTON, addrNPI, addr));
        session.setTransactionTimer(receiveTimeout);
        session.setEnquireLinkTimer(enquireInterval);
    }

    public void unbind() {
        if (session != null) {
            session.unbindAndClose();
            session = null;
        }
    }

    /**
     * @return Vector
     */
    public Vector getParameterDefinition() {
        Vector vtReturn = new Vector();
        vtReturn.add(createParameter("ip-address", "", ParameterType.PARAM_TEXTBOX_MAX, "15", ""));
        vtReturn.add(createParameter("port", "", ParameterType.PARAM_TEXTBOX_MASK, "99999", ""));
        vtReturn.add(createParameter("system-id", "", ParameterType.PARAM_TEXTBOX_MAX, "30", ""));
        vtReturn.add(createParameter("password", "", ParameterType.PARAM_PASSWORD, "30", ""));
        vtReturn.add(createParameter("system-type", "", ParameterType.PARAM_TEXTBOX_MAX, "30", ""));
        vtReturn.add(createParameter("addr-ton", "", ParameterType.PARAM_COMBOBOX, TypeOfNumber.class, ""));
        vtReturn.add(createParameter("addr-npi", "", ParameterType.PARAM_COMBOBOX, NumberingPlanIndicator.class, ""));
        vtReturn.add(createParameter("address-range", "", ParameterType.PARAM_TEXTBOX_MAX, "15", ""));
        vtReturn.add(createParameter("source-ton", "", ParameterType.PARAM_COMBOBOX, TypeOfNumber.class, ""));
        vtReturn.add(createParameter("source-npi", "", ParameterType.PARAM_COMBOBOX, NumberingPlanIndicator.class, ""));
        vtReturn.add(createParameter("destination-ton", "", ParameterType.PARAM_COMBOBOX, TypeOfNumber.class, ""));
        vtReturn.add(createParameter("destination-npi", "", ParameterType.PARAM_COMBOBOX, NumberingPlanIndicator.class, ""));
        vtReturn.add(createParameter("bind-mode", "", ParameterType.PARAM_COMBOBOX, BindType.class, ""));
        vtReturn.add(createParameter("receive-timeout", "", ParameterType.PARAM_TEXTBOX_MASK, "999"));
        vtReturn.add(createParameter("enquire-interval", "", ParameterType.PARAM_TEXTBOX_MASK, "999"));

        vtReturn.addAll(super.getParameterDefinition());
        return vtReturn;
    }

    /**
     * @throws AppException
     */
    public void fillParameter() throws AppException {
        super.fillParameter();

        ipAddress = loadString("ip-address");
        port = loadUnsignedInteger("port");
        systemId = loadString("system-id");
        password = loadString("password");
        mstrSystemType = loadString("system-type");
        addrTON = TypeOfNumber.valueOf(loadString("addr-ton"));
        addrNPI = NumberingPlanIndicator.valueOf(loadString("addr-npi"));
        addr = loadString("address-range");

        srcTON = TypeOfNumber.valueOf(loadString("source-ton"));
        srcNPI = NumberingPlanIndicator.valueOf(loadString("source-npi"));

        dstTON = TypeOfNumber.valueOf(loadString("destination-ton"));
        dstNPI = NumberingPlanIndicator.valueOf(loadString("destination-npi"));

        bindMode = BindType.valueOf(loadString("bind-mode"));

        int rcvTimeout;
        rcvTimeout = loadInteger("receive-timeout");
        receiveTimeout = rcvTimeout * 1000;

        enquireInterval = loadInteger("enquire-interval");
    }

    /**
     * @param thr Dispatcher
     * @throws Exception
     */
    protected void fillDispatcherParameter(Dispatcher thr) throws Exception {
        super.fillDispatcherParameter(thr);
        SmppDispatcherSplitThread SOAPthr = (SmppDispatcherSplitThread) thr;
        SOAPthr.setSmppSession(session);
    }

    public String sendMessage(String message, String sender, String receiver, String validity) throws Exception {
        if (message.length() >= 160) {
            message = message.substring(0, 160);
        }

        String messageId = session.submitShortMessage("CMT",
                srcTON, srcNPI, sender,
                dstTON, dstNPI, receiver,
                new ESMClass(),
                (byte) 0,
                (byte) 1,
                timeFormatter.format(new Date()),
                validity,
                new RegisteredDelivery(SMSCDeliveryReceipt.SUCCESS_FAILURE),
                (byte) 0,
                new GeneralDataCoding(false, true, MessageClass.CLASS1, Alphabet.ALPHA_DEFAULT),
                (byte) 0,
                message.getBytes());

        logMonitor(String.format("Message from %s to %s submitted, message_id is %s", sender, receiver, messageId));
        return messageId;
    }
}
