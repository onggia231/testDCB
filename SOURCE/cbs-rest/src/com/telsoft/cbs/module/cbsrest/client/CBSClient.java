package com.telsoft.cbs.module.cbsrest.client;

import com.telsoft.cbs.module.cbsrest.domain.Attribute;
import com.telsoft.cbs.module.cbsrest.domain.CBCommand;
import com.telsoft.cbs.module.cbsrest.domain.RestRequest;
import com.telsoft.cbs.module.cbsrest.domain.RestResponse;
import com.telsoft.httpservice.client.RestClient;
import lombok.Getter;

import javax.servlet.http.HttpServletRequest;

public class CBSClient extends RestClient {

    @Getter
    private final String source;
    private final String storeCode;
    private final String msisdn;

    public CBSClient(String source, String storeCode, String url) {
        super(url);
        this.source = source;
        this.storeCode = storeCode;
        this.msisdn = null;
    }

    public CBSClient(String source, String storeCode, String url, String msisdnTest) {
        super(url);
        this.source = source;
        this.storeCode = storeCode;
        this.msisdn = msisdnTest;
    }

    public String getLoggerName() {
        return "REST-CBS";

    }

    public RestResponse execute(HttpServletRequest httpServletRequest, RestRequest request) {
        String xForwardFor = null;

        if (httpServletRequest != null) {
            xForwardFor = httpServletRequest.getHeader("X-Forward-For");
            if (xForwardFor == null || xForwardFor.length() == 0) {
                xForwardFor = httpServletRequest.getRemoteHost();
            }
        }
        return super.post(xForwardFor, "/execute", request, RestResponse.class);
    }

    //tuanla - begin
    public RestResponse fortumoAuth(HttpServletRequest httpServletRequest,
                                    RestRequest request) {
        RestResponse restResponse = execute(httpServletRequest, request);
        return restResponse;
    }

    public RestRequest createFortumoAuthRequest(
            String correlationId,
            String purchaseTime,
            String storeTransactionId,
            String account,
            String purchaseAmount,
            String productDescription,
            String merchantInfo) {
        RestRequest request = new RestRequest();
        request.setTransaction_id(correlationId);
        request.setIsdn(account);
        request.setName(CBCommand.CHARGE);
        request.setStore_code(storeCode);
        request.setSource(source);
        request.getParameters().add(new Attribute("store_transaction_id", storeTransactionId));
        request.getParameters().add(new Attribute("client_request_id", storeTransactionId));
        request.getParameters().add(new Attribute("purchase_time", purchaseTime));
        request.getParameters().add(new Attribute("msisdn", account));
        request.getParameters().add(new Attribute("amount", String.valueOf(purchaseAmount)));
        request.getParameters().add(new Attribute("content", productDescription));
        request.getParameters().add(new Attribute("merchant_information", merchantInfo));
        request.getParameters().add(new Attribute("channel_type", "API"));

        return request;
    }

    public RestResponse fortumoCharge(HttpServletRequest httpServletRequest,
                                      RestRequest request) {

        RestResponse restResponse = execute(httpServletRequest, request);
        return restResponse;
    }

    public RestRequest createFortumoChargeRequest(
            String authCorrelationId,
            String authPurchaseTime,
            String storeTransactionId) {
        RestRequest request = new RestRequest();
        request.setTransaction_id(authCorrelationId);
        request.setName(CBCommand.CAPTURE);
        request.setSource(source);
        request.setStore_code(storeCode);
        request.getParameters().add(new Attribute("store_transaction_id", storeTransactionId));
        request.getParameters().add(new Attribute("client_request_id", storeTransactionId));
        request.getParameters().add(new Attribute("purchase_time", authPurchaseTime));
        //request.getParameters().add(new Attribute("payment_transaction_id", issuerPaymentId));
        request.getParameters().add(new Attribute("channel_type", "API"));

        return request;
    }

    public RestRequest createFortumoReverseRequest(
            String authCorrelationId,
            String authPurchaseTime,
            String storeTransactionId) {
        RestRequest request = new RestRequest();
        request.setTransaction_id(authCorrelationId);
        request.setName(CBCommand.REVERSE);
        request.setSource(source);
        request.setStore_code(storeCode);
        request.getParameters().add(new Attribute("store_transaction_id", storeTransactionId));
        request.getParameters().add(new Attribute("client_request_id", storeTransactionId));
        request.getParameters().add(new Attribute("purchase_time", authPurchaseTime));
        //request.getParameters().add(new Attribute("payment_transaction_id", issuerPaymentId));
        request.getParameters().add(new Attribute("channel_type", "API"));

        return request;
    }

    public RestRequest createFortumoRefundRequest(
            String authCorrelationId,
            String authPurchaseTime,
            String storeTransactionId,
            //String issuerPaymentId,
            String refundReason) {
        RestRequest request = new RestRequest();
        request.setTransaction_id(authCorrelationId);
        request.setName(CBCommand.REFUND);
        request.setSource(source);
        request.setStore_code(storeCode);
        request.getParameters().add(new Attribute("store_transaction_id", storeTransactionId));
        request.getParameters().add(new Attribute("client_request_id", storeTransactionId));
        request.getParameters().add(new Attribute("purchase_time", authPurchaseTime));
        //request.getParameters().add(new Attribute("payment_transaction_id", issuerPaymentId));
        request.getParameters().add(new Attribute("refund_reason", refundReason));
        request.getParameters().add(new Attribute("channel_type", "API"));

        return request;
    }

    public RestResponse fortumoRefundV2(HttpServletRequest httpServletRequest,
                                      String authCorrelationId,
                                      String authPurchaseTime,
                                      //String issuerPaymentId,
                                      String refundReason, double amount) {
        RestRequest request = new RestRequest();
        request.setTransaction_id(authCorrelationId);
        request.setName(CBCommand.REFUND);
        request.setSource(source);
        request.setStore_code(storeCode);
        request.getParameters().add(new Attribute("store_transaction_id", authCorrelationId));
        request.getParameters().add(new Attribute("client_request_id", authCorrelationId));
        request.getParameters().add(new Attribute("purchase_time", authPurchaseTime));
        //request.getParameters().add(new Attribute("payment_transaction_id", issuerPaymentId));
        request.getParameters().add(new Attribute("refund_reason", refundReason));
        request.getParameters().add(new Attribute("amount", String.valueOf(amount)));
        request.getParameters().add(new Attribute("channel_type", "API"));

        RestResponse restResponse = execute(httpServletRequest, request);
        return restResponse;
    }

    public RestResponse fortumoGetProfile(HttpServletRequest httpServletRequest, String msisdn, String transactionId, String type) {
        RestRequest request = new RestRequest();

        request.setName(CBCommand.ACCOUNT_PROFILE);
        request.setIsdn(msisdn);
        request.setStore_code(storeCode);
        request.setTransaction_id(transactionId);
        request.setSource(this.source);
        request.getParameters().add(new Attribute("channel_type", type));

        RestResponse restResponse = execute(httpServletRequest, request);
        return restResponse;
    }

    public RestResponse subscriberLookup(HttpServletRequest httpServletRequest, String msisdn, String transactionId, String type) {
        RestRequest request = new RestRequest();

        request.setName(CBCommand.SUBSCRIBER_LOOKUP);
        request.setIsdn(msisdn);
        request.setStore_code(storeCode);
        request.setTransaction_id(transactionId);
        request.setSource(this.source);
        request.getParameters().add(new Attribute("channel_type", type));

        RestResponse restResponse = execute(httpServletRequest, request);
        return restResponse;
    }

    public RestResponse checkEligibility(HttpServletRequest httpServletRequest, String msisdn, String transactionId, String type) {
        RestRequest request = new RestRequest();

        request.setName(CBCommand.CHECK_ELIGIBILITY);
        request.setIsdn(msisdn);
        request.setStore_code(storeCode);
        request.setTransaction_id(transactionId);
        request.setSource(this.source);
        request.getParameters().add(new Attribute("channel_type", type));

        RestResponse restResponse = execute(httpServletRequest, request);
        return restResponse;
    }

    public RestResponse fortumoPaymentStatus(HttpServletRequest httpServletRequest, String paymentTransactionId, String purchaseTIme) {
        RestRequest request = new RestRequest();

        request.setName(CBCommand.PAYMENT_STATUS);
        request.setStore_code(storeCode);
        request.setTransaction_id(paymentTransactionId);
        request.setSource(this.source);
        request.getParameters().add(new Attribute("store_transaction_id", paymentTransactionId));
        request.getParameters().add(new Attribute("client_request_id", paymentTransactionId));
        request.getParameters().add(new Attribute("purchase_time", purchaseTIme));
        request.getParameters().add(new Attribute("channel_type", "API"));

        RestResponse restResponse = execute(httpServletRequest, request);
        return restResponse;
    }

    public RestResponse fortumoRefundStatus(HttpServletRequest httpServletRequest, String refundRequestId, String purchaseTIme) {
        RestRequest request = new RestRequest();

        request.setName(CBCommand.REFUND_STATUS);
        request.setStore_code(storeCode);
        request.setTransaction_id(refundRequestId);
        request.setSource(this.source);
        request.getParameters().add(new Attribute("store_transaction_id", refundRequestId));
        request.getParameters().add(new Attribute("client_request_id", refundRequestId));
        request.getParameters().add(new Attribute("purchase_time", purchaseTIme));
        request.getParameters().add(new Attribute("channel_type", "API"));

        RestResponse restResponse = execute(httpServletRequest, request);
        return restResponse;
    }
    //tuanla - end

    public RestResponse charge(HttpServletRequest httpServletRequest, String msisdn, String content, double amount, String clientTransactionId, String requestId) {
        RestRequest request = new RestRequest();

        request.setName(CBCommand.CHARGE);
        request.setIsdn(msisdn);
        request.setStore_code(storeCode);
        request.setTransaction_id(clientTransactionId);
        request.setSource(this.source);
        request.getParameters().add(new Attribute("amount", String.valueOf(amount)));
        request.getParameters().add(new Attribute("content", content));
        request.getParameters().add(new Attribute("client_request_id", requestId));

        RestResponse restResponse = execute(httpServletRequest, request);
        return restResponse;
    }

    public RestResponse getProfile(HttpServletRequest httpServletRequest, String msisdn, String transactionId, String type) {
        RestRequest request = new RestRequest();

        request.setName(CBCommand.GET_PROFILE);
        request.setIsdn(msisdn);
        request.setStore_code(storeCode);
        request.setTransaction_id(transactionId);
        request.setSource(this.source);
        request.getParameters().add(new Attribute("type", type));

        RestResponse restResponse = execute(httpServletRequest, request);
        return restResponse;
    }

    public RestResponse paymentStatus(HttpServletRequest httpServletRequest, String payment_transaction_id, String transactionId) {
        RestRequest request = new RestRequest();

        request.setName(CBCommand.PAYMENT_STATUS);
        request.setStore_code(storeCode);
        request.setTransaction_id(transactionId);
        request.setSource(this.source);
        request.getParameters().add(new Attribute("payment_transaction_id", payment_transaction_id));

        RestResponse restResponse = execute(httpServletRequest, request);
        return restResponse;
    }

    public RestResponse refundStatus(HttpServletRequest httpServletRequest, String refund_transaction_id, String transactionId) {
        RestRequest request = new RestRequest();

        request.setName(CBCommand.REFUND_STATUS);
        request.setStore_code(storeCode);
        request.setTransaction_id(transactionId);
        request.setSource(this.source);
        request.getParameters().add(new Attribute("refund_transaction_id", refund_transaction_id));

        RestResponse restResponse = execute(httpServletRequest, request);
        return restResponse;
    }

    public RestResponse refund(HttpServletRequest httpServletRequest, String payment_transaction_id, double amount, String content, String transactionId) {
        RestRequest request = new RestRequest();

        request.setName(CBCommand.REFUND);
        request.setStore_code(storeCode);
        request.setTransaction_id(transactionId);
        request.setSource(this.source);
        request.getParameters().add(new Attribute("payment_transaction_id", payment_transaction_id));
        request.getParameters().add(new Attribute("amount", String.valueOf(amount)));
        request.getParameters().add(new Attribute("reason", content));

        RestResponse restResponse = execute(httpServletRequest, request);
        return restResponse;
    }

    public RestResponse registerProfile(HttpServletRequest httpServletRequest, String msisdn, String transactionId) {
        RestRequest request = new RestRequest();

        request.setName(CBCommand.PAYMENT_STATUS);
        request.setStore_code(storeCode);
        request.setIsdn(msisdn);
        request.setTransaction_id(transactionId);
        request.setSource(this.source);

        RestResponse restResponse = execute(httpServletRequest, request);
        return restResponse;
    }

    public RestResponse submitMT(HttpServletRequest httpServletRequest, String msisdn, String content, String transactionId, String originator, Integer validity) {
        RestRequest request = new RestRequest();

        request.setName(CBCommand.SUBMIT_MT);
        request.setStore_code(storeCode);
        request.setTransaction_id(transactionId);
        request.setSource(this.source);
        request.setIsdn(msisdn);
        request.getParameters().add(new Attribute("content", content));
        request.getParameters().add(new Attribute("originator", originator));
        request.getParameters().add(new Attribute("validity", String.valueOf(validity)));

        RestResponse restResponse = execute(httpServletRequest, request);
        return restResponse;
    }

    public RestResponse unregisterProfile(HttpServletRequest httpServletRequest, String msisdn, String transactionId) {
        RestRequest request = new RestRequest();

        request.setName(CBCommand.UNREGISTER_PROFILE);
        request.setStore_code(storeCode);
        request.setIsdn(msisdn);
        request.setTransaction_id(transactionId);
        request.setSource(this.source);

        RestResponse restResponse = execute(httpServletRequest, request);
        return restResponse;
    }

    public String getSource() {
        return source;
    }

    public String getStoreCode() {
        return storeCode;
    }

    public String getMsisdn() {
        return msisdn;
    }
}
