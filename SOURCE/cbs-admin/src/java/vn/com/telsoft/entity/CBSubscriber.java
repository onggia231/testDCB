package vn.com.telsoft.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author TUNGLM
 */
public class CBSubscriber implements Serializable {

    private static final long serialVersionUID = -4879532508280639240L;

    private Long id;
    private String isdn;
    private String subType;
    private Date regDate;
    private Long status;
    private List<CBStore> lstStore;
    private Map<Long, CBSubStore> mapSubStore = new HashMap<Long, CBSubStore>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIsdn() {
        return isdn;
    }

    public void setIsdn(String isdn) {
        this.isdn = isdn;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public Date getRegDate() {
        return regDate;
    }

    public void setRegDate(Date regDate) {
        this.regDate = regDate;
    }

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public List<CBStore> getLstStore() {
        return lstStore;
    }

    public void setLstStore(List<CBStore> lstStore) {
        this.lstStore = lstStore;
    }

    public Map<Long, CBSubStore> getMapSubStore() {
        return mapSubStore;
    }

    public void setMapSubStore(Map<Long, CBSubStore> mapSubStore) {
        this.mapSubStore = mapSubStore;
    }
}
