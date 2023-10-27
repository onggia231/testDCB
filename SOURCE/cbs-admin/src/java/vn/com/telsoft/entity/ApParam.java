package vn.com.telsoft.entity;

import java.io.Serializable;


public class ApParam implements Serializable {

    private static final long serialVersionUID = 6889783095248341502L;
    private String parGroup;
    private String parName;
    private String description;
    private String parType;
    private String parValue;

    public ApParam() {
        super();
        // TODO Auto-generated constructor stub
    }

    public ApParam(String parName, String description, String parType, String parValue, String parGroup) {
        super();
        this.parName = parName;
        this.description = description;
        this.parType = parType;
        this.parValue = parValue;
        this.parGroup = parGroup;
    }

    public ApParam(ApParam apParam) {
        super();
        this.parName = apParam.parName;
        this.description = apParam.description;
        this.parType = apParam.parType;
        this.parValue = apParam.parValue;
        this.parGroup = apParam.parGroup;
    }

    public String getParGroup() {
        return parGroup;
    }

    public void setParGroup(String parGroup) {
        this.parGroup = parGroup;
    }

    public String getParName() {
        return parName;
    }

    public void setParName(String parName) {
        this.parName = parName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getParType() {
        return parType;
    }

    public void setParType(String parType) {
        this.parType = parType;
    }

    public String getParValue() {
        return parValue;
    }

    public void setParValue(String parValue) {
        this.parValue = parValue;
    }

}
