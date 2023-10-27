package vn.com.telsoft.entity;

import java.io.Serializable;
import java.util.Date;

public class CBMtHistory implements Serializable {

    private static final long serialVersionUID = 6335416806826137315L;
    private Date requestTime;
    private String isdn;
    private String content;
    private String storeCode;
    private String transId;
    private Date issueTime;
    private Date sendTime;
    private Long retried;
    private String channelType;
    private Long queueId;
    private Long commandId;
    private String commandCode;
    private String type;

    public CBMtHistory() {
    }

    public CBMtHistory(CBMtHistory obj) {
        this.requestTime = obj.requestTime;
        this.isdn = obj.isdn;
        this.content = obj.content;
        this.storeCode = obj.storeCode;
        this.transId = obj.transId;
        this.issueTime = obj.issueTime;
        this.sendTime = obj.sendTime;
        this.retried = obj.retried;
        this.channelType = obj.channelType;
        this.queueId = obj.queueId;
        this.commandId = obj.commandId;
        this.commandCode = obj.commandCode;
        this.type = obj.type;
    }

    public Date getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Date requestTime) {
        this.requestTime = requestTime;
    }

    public String getIsdn() {
        return isdn;
    }

    public void setIsdn(String isdn) {
        this.isdn = isdn;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStoreCode() {
        return storeCode;
    }

    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId) {
        this.transId = transId;
    }

    public Date getIssueTime() {
        return issueTime;
    }

    public void setIssueTime(Date issueTime) {
        this.issueTime = issueTime;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public Long getRetried() {
        return retried;
    }

    public void setRetried(Long retried) {
        this.retried = retried;
    }

    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public Long getQueueId() {
        return queueId;
    }

    public void setQueueId(Long queueId) {
        this.queueId = queueId;
    }

    public Long getCommandId() {
        return commandId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }

    public String getCommandCode() {
        return commandCode;
    }

    public void setCommandCode(String commandCode) {
        this.commandCode = commandCode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
