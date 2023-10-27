package vn.com.telsoft.entity;

import com.telsoft.cbs.domain.CBCode;

import java.io.Serializable;
import java.util.Date;

public class CBRequest implements Serializable {

    private static final long serialVersionUID = -6335555705058403769L;

    private Date requestTime;
    private Date responseTime;
    private String isdn;
    private Long status;
    private String command;
    private Long amount;
    private String reqContent;
    private String resContent;
    private String address;
    private String storeCode;
    private String transactionId;
    private String clientTransactionId;
    private String storeTransactionId;
    private String requestId;
    private CBCode resultCode;
    private Long contentId;
    private Long amountFullTax;
    private String contentDescription;
    private String channelType;
    //client
    private Long vat;
    private String statusDisplay;
    private Date fromDate;
    private Date toDate;
    private String moMtType;
    private String content;
    //bo sung truong
    private String cpsTransactionId;
    private CBCode finalResultCode;
    private String referTransactionId;

    public CBRequest() {
    }

    public CBRequest(CBRequest obj) {
        this.requestTime = obj.requestTime;
        this.responseTime = obj.responseTime;
        this.isdn = obj.isdn;
        this.status = obj.status;
        this.command = obj.command;
        this.amount = obj.amount;
        this.reqContent = obj.reqContent;
        this.resContent = obj.resContent;
        this.address = obj.address;
        this.storeCode = obj.storeCode;
        this.transactionId = obj.transactionId;
        this.clientTransactionId = obj.clientTransactionId;
        this.storeTransactionId = obj.storeTransactionId;
        this.requestId = obj.requestId;
        this.resultCode = obj.resultCode;
        this.contentId = obj.contentId;
        this.amountFullTax = obj.amountFullTax;
        this.contentDescription = obj.contentDescription;
        this.vat = obj.vat;
        this.statusDisplay = obj.statusDisplay;
        this.fromDate = obj.fromDate;
        this.toDate = obj.toDate;
        this.channelType = obj.channelType;
        this.moMtType = obj.moMtType;
        this.content = obj.content;
        this.cpsTransactionId = obj.cpsTransactionId;
        this.referTransactionId = obj.referTransactionId;
        this.finalResultCode = obj.finalResultCode;
    }

    public Date getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Date requestTime) {
        this.requestTime = requestTime;
    }

    public Date getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Date responseTime) {
        this.responseTime = responseTime;
    }

    public String getIsdn() {
        return isdn;
    }

    public void setIsdn(String isdn) {
        this.isdn = isdn;
    }

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getReqContent() {
        return reqContent;
    }

    public void setReqContent(String reqContent) {
        this.reqContent = reqContent;
    }

    public String getResContent() {
        return resContent;
    }

    public void setResContent(String resContent) {
        this.resContent = resContent;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStoreCode() {
        return storeCode;
    }

    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getClientTransactionId() {
        return clientTransactionId;
    }

    public void setClientTransactionId(String clientTransactionId) {
        this.clientTransactionId = clientTransactionId;
    }

    public String getStoreTransactionId() {
        return storeTransactionId;
    }

    public void setStoreTransactionId(String storeTransactionId) {
        this.storeTransactionId = storeTransactionId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public CBCode getResultCode() {
        return resultCode;
    }

    public void setResultCode(CBCode resultCode) {
        this.resultCode = resultCode;
    }

    public Long getContentId() {
        return contentId;
    }

    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }

    public Long getAmountFullTax() {
        return amountFullTax;
    }

    public void setAmountFullTax(Long amountFullTax) {
        this.amountFullTax = amountFullTax;
    }

    public String getContentDescription() {
        return contentDescription;
    }

    public void setContentDescription(String contentDescription) {
        this.contentDescription = contentDescription;
    }

    public Long getVat() {
        return vat;
    }

    public void setVat(Long vat) {
        this.vat = vat;
    }

    public String getStatusDisplay() {
        return statusDisplay;
    }

    public void setStatusDisplay(String statusDisplay) {
        this.statusDisplay = statusDisplay;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public String getMoMtType() {
        return moMtType;
    }

    public void setMoMtType(String moMtType) {
        this.moMtType = moMtType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCpsTransactionId() {
        return cpsTransactionId;
    }

    public void setCpsTransactionId(String cpsTransactionId) {
        this.cpsTransactionId = cpsTransactionId;
    }

    public CBCode getFinalResultCode() {
        return finalResultCode;
    }

    public void setFinalResultCode(CBCode finalResultCode) {
        this.finalResultCode = finalResultCode;
    }

    public String getReferTransactionId() {
        return referTransactionId;
    }

    public void setReferTransactionId(String referTransactionId) {
        this.referTransactionId = referTransactionId;
    }
}
