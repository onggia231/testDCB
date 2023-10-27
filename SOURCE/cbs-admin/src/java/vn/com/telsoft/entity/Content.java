package vn.com.telsoft.entity;

import java.io.Serializable;

public class Content implements Serializable {

    private static final long serialVersionUID = 9195132141442362853L;

    private Integer contentId;
    private String contentDescription;
    private String status;


    public Content() {
    }

    public Content(Content ett) {
        this.contentId = ett.getContentId();
        this.contentDescription = ett.getContentDescription();
        this.status = ett.getStatus();
    }

    public Integer getContentId() {
        return contentId;
    }

    public void setContentId(Integer contentId) {
        this.contentId = contentId;
    }

    public String getContentDescription() {
        return contentDescription;
    }

    public void setContentDescription(String contentDescription) {
        this.contentDescription = contentDescription;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
