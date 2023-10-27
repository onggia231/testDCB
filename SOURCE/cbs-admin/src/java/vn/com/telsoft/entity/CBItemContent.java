package vn.com.telsoft.entity;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author TRIEUNV
 */
public class CBItemContent implements Serializable {

    private static final long serialVersionUID = -3512433289541706401L;

    private Integer contentId;
    private String reason;
    private Date issueTime;
    private Long listId;
    private Long storeId;

    public CBItemContent(Integer contentId, String reason, Date issueTime, Long listId, Long storeId) {
        this.contentId = contentId;
        this.reason = reason;
        this.issueTime = issueTime;
        this.listId = listId;
        this.storeId = storeId;
    }

    public CBItemContent() {

    }

    public Integer getContentId() {
        return contentId;
    }

    public void setContentId(Integer contentId) {
        this.contentId = contentId;
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
