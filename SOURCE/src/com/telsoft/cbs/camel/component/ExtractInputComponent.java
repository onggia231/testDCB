package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.camel.service.cache.CacheService;
import com.telsoft.cbs.domain.*;
import com.telsoft.cbs.utils.CbsUtils;
import com.telsoft.cbs.utils.CustomUUID;
import com.telsoft.util.StringUtil;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.annotations.Component;
import org.apache.commons.lang3.StringUtils;
import telsoft.gateway.core.log.MessageContext;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Check Input Component
 * <p>
 */

@Component("cbs-extract-input")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-extract-input",
        title = "Extract input parameters",
        syntax = "cbs-extract-input:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)
public class ExtractInputComponent extends ProcessorComponent {
    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
        extractClientRequestId(request, exchange, parameters, messageContext);
        extractMsisdn(request, exchange, parameters, messageContext);
        extractStore(request, exchange, parameters, messageContext);
        extractAmount(request, exchange, parameters, messageContext);
        extractClientTransId(request, exchange, parameters, messageContext,mapFullRequest);
        extractStoreTransId(request, exchange, parameters, messageContext,mapFullRequest);
        extractContent(request, exchange, parameters, messageContext);
        extractRefundReason(request, exchange, parameters, messageContext);
        extractPaymentRequestId(request, exchange, parameters, messageContext);
        extractPaymentTransId(request, exchange, parameters, messageContext,mapFullRequest);
        extractPurchaseTime(request, exchange, parameters, messageContext,mapFullRequest);
        extractChannelType(request, exchange, parameters, messageContext,mapFullRequest);
        extractShortCode(request, exchange, parameters, messageContext);
    }

    private void extractChannelType(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext, Map mapFullRequest) {
        String strChannelType = request.get(CbsContansts.CHANNEL_TYPE);
        CHANNEL_TYPE channel_type = CHANNEL_TYPE.valueOf(strChannelType);
        messageContext.setProperty(CbsContansts.CHANNEL_TYPE, channel_type);
        CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest,CbsContansts.CHANNEL_TYPE,channel_type);//CHANNEL_TYPE
    }

    private void extractPurchaseTime(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext, Map mapFullRequest) {
        String strPurchaseTime = request.get(CbsContansts.PURCHASE_TIME);
        if(strPurchaseTime == null || strPurchaseTime.isEmpty()){
            return;
        }
        try{
            SimpleDateFormat sdf = new SimpleDateFormat(CbsContansts.PURCHASE_TIME_FORMAT);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date purchaseTime = sdf.parse(strPurchaseTime);

            messageContext.setProperty(CbsContansts.PAYMENT_DATE, purchaseTime);
            CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest,CbsContansts.PAYMENT_DATE,purchaseTime);//Payment_date
        }catch (Exception ex){
            log.warn("Can't get PurchaseTime from request " + request.toString(), ex);
        }
    }

    /**
     * @param request
     * @param exchange
     * @param parameters
     * @param messageContext
     * @throws CBException
     */
    private void extractStore(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        String storeCode = request.get(CbsContansts.STORE_CODE);
        messageContext.setProperty(CbsContansts.STORE_CODE, storeCode);

        if (storeCode == null) {
            return;
        }

        CacheService service = getService(CacheService.class);
        if (service == null) {
            log.error("Cache Service is not installed");
            return;
        }

        Map<String, CBStore> mapStore = service.getMap(CbsContansts.STORE);
        if (mapStore == null) {
            log.error("There is no 'store' in CacheService");
            return;
        }


        CBStore store = mapStore.get(storeCode);
        if (store == null) {
            log.error("Store '{}' is unknown", storeCode);
            return;
        }

        messageContext.setProperty(CbsContansts.STORE, store);
    }

    /**
     * @param request
     * @param exchange
     * @param parameters
     * @param messageContext
     * @throws CBException
     */
    private void extractClientTransId(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext, Map mapFullRequest) throws CBException {
        String client_transaction_id = request.get(CbsContansts.CLIENT_TRANSACTION_ID);
        exchange.setProperty(CbsContansts.CLIENT_TRANSACTION_ID, client_transaction_id);
        messageContext.setOrigin(client_transaction_id);
        CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest,CbsContansts.CLIENT_TRANSACTION_ID,client_transaction_id);//Client transaction ID
    }

    private void extractStoreTransId(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext, Map mapFullRequest) throws CBException {
        String store_transaction_id = request.get(CbsContansts.STORE_TRANSACTION_ID);
        exchange.setProperty(CbsContansts.STORE_TRANSACTION_ID, store_transaction_id);
        messageContext.setProperty(CbsContansts.STORE_TRANSACTION_ID, store_transaction_id);
        CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest,CbsContansts.STORE_TRANSACTION_ID,store_transaction_id);//Store transaction ID
    }

    /**
     * @param request
     * @param exchange
     * @param parameters
     * @param messageContext
     * @throws CBException
     */
    private void extractClientRequestId(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        String requestId = request.get(CbsContansts.REQUEST_ID);
        exchange.setProperty(CbsContansts.REQUEST_ID, requestId);
        messageContext.setProperty(CbsContansts.REQUEST_ID, requestId);
    }

    /**
     * @param request
     * @param exchange
     * @param parameters
     * @param messageContext
     * @throws CBException
     */
    private void extractPaymentRequestId(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        String requestId = request.get(CbsContansts.PAYMENT_REQUEST_ID);
        exchange.setProperty(CbsContansts.PAYMENT_REQUEST_ID, requestId);
        messageContext.setProperty(CbsContansts.PAYMENT_REQUEST_ID, requestId);
    }

    /**
     * @param request
     * @param exchange
     * @param parameters
     * @param messageContext
     * @throws CBException
     *//*
    private void extractPaymentTransId(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext, Map mapFullRequest) throws CBException {
        String payment_transaction_id = request.get(CbsContansts.PAYMENT_TRANSACTION_ID);
        exchange.setProperty(CbsContansts.PAYMENT_TRANSACTION_ID, payment_transaction_id);
        messageContext.setProperty(CbsContansts.PAYMENT_TRANSACTION_ID, payment_transaction_id);

        // missing msisdn parameter
        if (payment_transaction_id == null) {
            return;
        }

        String[] transIdParts = StringUtil.toStringArray(payment_transaction_id, "F");
        if (transIdParts.length != 3)
            return;

        Long datePart = Long.parseLong(transIdParts[0]);
        String msisdn = transIdParts[1];
        // third part is raw transaction id, which is not seperated used

        long cmdCode = datePart >> 22 & 0b1111_1111;
        long year = datePart >> 9 & 0b1_1111_1111_1111;
        long month = datePart >> 5 & 0b1111;
        long date = datePart & 0b1_1111;


        messageContext.setIsdn(msisdn);
        messageContext.setProperty(CbsContansts.PAYMENT_COMMAND, CBCommand.valueOf((int) cmdCode));

        Calendar dt = Calendar.getInstance();
        dt.set(Calendar.YEAR, (int) year);
        dt.set(Calendar.MONTH, (int) month - 1);
        dt.set(Calendar.DATE, (int) date);
        messageContext.setProperty(CbsContansts.PAYMENT_DATE, dt.getTime());
        CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest,CbsContansts.PAYMENT_DATE, dt.getTime());//Payment_date
    }*/

    /**
     * @param request
     * @param exchange
     * @param parameters
     * @param messageContext
     * @throws CBException
     */
    private void extractPaymentTransId(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext, Map mapFullRequest) throws CBException {
        String payment_transaction_id = request.get(CbsContansts.PAYMENT_TRANSACTION_ID);
        exchange.setProperty(CbsContansts.PAYMENT_TRANSACTION_ID, payment_transaction_id);
        messageContext.setProperty(CbsContansts.PAYMENT_TRANSACTION_ID, payment_transaction_id);

        // missing msisdn parameter
        if (StringUtils.isEmpty(payment_transaction_id)) {
            return;
        }

        String[] transIdParts = StringUtil.toStringArray(payment_transaction_id, "F");
        if (transIdParts.length < 3)
            return;

        String uuid = transIdParts[0];
        int cmdCode = Integer.parseInt(transIdParts[1]);
        String msisdn = transIdParts[2];

        messageContext.setIsdn(msisdn);
        messageContext.setProperty(CbsContansts.PAYMENT_COMMAND, CBCommand.valueOf(cmdCode));

        Date requestTime = CustomUUID.getDateFromCustomCodecUUID(uuid);

        messageContext.setProperty(CbsContansts.PAYMENT_DATE, requestTime);
        CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest,CbsContansts.PAYMENT_DATE, requestTime);//Payment_date
    }


    /**
     * @param request
     * @param exchange
     * @param parameters
     * @param messageContext
     * @throws CBException
     */
    private void extractAmount(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        String strAmount = request.get(CbsContansts.AMOUNT);

        // missing msisdn parameter
        if (strAmount == null) {
            return;
        }

        strAmount = String.valueOf(Math.round(Double.parseDouble(strAmount)));
        Long amount = Long.parseLong(strAmount);
        exchange.setProperty(CbsContansts.AMOUNT, strAmount);
        messageContext.setAmount(amount);
        Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
        CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest,CbsContansts.AMOUNT,strAmount);

        CBStore store = (CBStore) messageContext.getProperty(CbsContansts.STORE);
        int taxRate = 0;
        try {
            taxRate = Integer.parseInt(StringUtil.nvl(store.getAttributes().getProperty(CbsContansts.TAX_CONFIG_KEY), "0"));
        } catch (Exception ignored) {

        }

        Long amountFullTax = amount;
        String tax = "0";
        if (taxRate != 0) {
            try {
                double dAmount = amount; //String.valueOf(Math.round(Double.parseDouble(amount)))
                amountFullTax = Math.round(dAmount + dAmount * taxRate / 100.0);
                tax = String.valueOf(Math.round(dAmount * taxRate / 100.0));
            } catch (Exception ignored) {

            }
        }
        exchange.setProperty(CbsContansts.AMOUNT_FULL_TAX, amountFullTax);
        CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.AMOUNT_FULL_TAX, amountFullTax);
        exchange.setProperty(CbsContansts.TAX, tax);
        CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.TAX, tax);
    }

    /**
     * @param request
     * @param exchange
     * @param parameters
     * @param messageContext
     * @throws CBException
     */
    private void extractContent(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        String content = request.get(CbsContansts.CONTENT);
        exchange.setProperty(CbsContansts.CONTENT_DESCRIPTION, content);
        messageContext.setProperty(CbsContansts.CONTENT_DESCRIPTION, content);
        Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
        CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest,CbsContansts.CONTENT_DESCRIPTION,content);
    }

    private void extractRefundReason(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        String refundReason = request.get(CbsContansts.REFUND_REASON);
        exchange.setProperty(CbsContansts.REFUND_REASON, refundReason);
        messageContext.setProperty(CbsContansts.REFUND_REASON, refundReason);
        Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
        CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest,CbsContansts.REFUND_REASON,refundReason);
    }

    /**
     * @param request
     * @param exchange
     * @param parameters
     * @param messageContext
     * @throws CBException
     */
    private void extractMsisdn(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        String msisdn = request.get(CbsContansts.MSISDN);
        exchange.setProperty(CbsContansts.MSISDN, msisdn);
        messageContext.setIsdn(msisdn);
        Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
        CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest,CbsContansts.MSISDN,msisdn);
    }

    /**
     * @param request
     * @param exchange
     * @param parameters
     * @param messageContext
     * @throws CBException
     */
    private void extractShortCode(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        String shortCode = request.get(CbsContansts.SHORT_CODE);
        exchange.setProperty(CbsContansts.SHORT_CODE, shortCode);
        messageContext.setProperty(CbsContansts.SHORT_CODE, shortCode);
        Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
        CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest,CbsContansts.SHORT_CODE,shortCode);
    }


}
