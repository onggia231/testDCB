package vn.com.telsoft.entity;

import java.io.Serializable;

public class CBStoreAttr implements Serializable {

    private static final long serialVersionUID = -3216805889027858227L;
    private long storeID;
    private String name;
    private String value;
    private long attrID;

    public long getAttrID() {
        return attrID;
    }

    public void setAttrID(long attrID) {
        this.attrID = attrID;
    }

    public long getStoreID() {
        return storeID;
    }

    public void setStoreID(long storeID) {
        this.storeID = storeID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
