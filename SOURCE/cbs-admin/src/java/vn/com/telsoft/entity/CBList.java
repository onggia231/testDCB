package vn.com.telsoft.entity;

import java.io.Serializable;
/**
 *
 * @author HoangNH
 */
public class CBList implements Serializable {

    private static final long serialVersionUID = 4444976970890118591L;

    private Long listId;
    private String listName;
    private Integer accessType;
    private String description;
    private Integer status;
    private Integer listType;
    private Integer cached;

    public CBList(CBList cbList) {
        this.listId = cbList.getListId();
        this.listName = cbList.getListName();
        this.accessType = cbList.getAccessType();
        this.description = cbList.getDescription();
        this.status = cbList.getStatus();
        this.listType = cbList.getListType();
        this.cached = cbList.getCached();
    }

    public CBList() {}

    public Long getListId() {
        return listId;
    }

    public void setListId(Long listId) {
        this.listId = listId;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public Integer getAccessType() {
        return accessType;
    }

    public void setAccessType(Integer accessType) {
        this.accessType = accessType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getListType() {
        return listType;
    }

    public void setListType(Integer listType) {
        this.listType = listType;
    }

    public Integer getCached() {
        return cached;
    }

    public void setCached(Integer cached) {
        this.cached = cached;
    }
}
