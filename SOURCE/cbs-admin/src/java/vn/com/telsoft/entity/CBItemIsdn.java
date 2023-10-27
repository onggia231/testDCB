package vn.com.telsoft.entity;

import java.io.Serializable;
import java.util.Date;

public class CBItemIsdn implements Serializable {

    private static final long serialVersionUID = 3377561069733249729L;

    private String isdn;
    private String reason;
    private Date issueTime;
    private Long listId;
    private Long storeId;
    private String issueTimeString;

    public CBItemIsdn(CBItemIsdn itemIsdn) {
        isdn = itemIsdn.getIsdn();
        reason = itemIsdn.getReason();
        issueTime = itemIsdn.getIssueTime();
        listId = itemIsdn.getListId();
        storeId = itemIsdn.getStoreId();
        issueTimeString = itemIsdn.getIssueTimeString();
    }

    public CBItemIsdn() {

    }

    public String getIssueTimeString() {
        return issueTimeString;
    }

    public void setIssueTimeString(String issueTimeString) {
        this.issueTimeString = issueTimeString;
    }

    public String getIsdn() {
        return isdn;
    }

    public void setIsdn(String isdn) {
        this.isdn = isdn;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Date getIssueTime() {
        return issueTime;
    }

    public void setIssueTime(Date issueTime) {
        this.issueTime = issueTime;
    }

    public Long getListId() {
        return listId;
    }

    public void setListId(Long listId) {
        this.listId = listId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }
}
