//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.faplib.lib.admin.gui.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReportParam implements Serializable {
    private static String PARTERN_DEFAULT_DATETIME = "dd/MM/yyyy HH:mm:ss";
    private static String PARTERN_DEFAULT_DATE = "dd/MM/yyyy";
    private String cacheKey;
    private String name;
    private String label;
    private String helpText;
    private String dataType;
    private String controlType;
    private String paramType;
    private String datePattern;
    private boolean isRequired;
    private boolean hidden;
    private ReportDataset dataset;
    private String defaultValue;
    private String stringValue;
    private Integer intValue;
    private Date dateValue;
    private String[] listValue;
    private NameValueItem rowSelected = new NameValueItem();
    private List<NameValueItem> rowSelectedMulti = new ArrayList();
    private String rowSelectedLabel;
    private String cascadingNodeName;
    private String childNodeName;

    public ReportParam() {
    }

    public Object getValue() {
        if (!this.isInputNumber() && !this.isInputText() && !this.isSelectOneMenu() && !this.isAutoComplete() && !this.isDatatable()) {
            if (!this.isSelectCheckboxMenu() && !this.isDatatableMulti()) {
                return this.isCalendar() ? this.dateValue : null;
            } else {
                return this.listValue;
            }
        } else if (this.isInputNumber()) {
            return this.intValue;
        } else {
            return this.stringValue;
        }
    }

    public boolean isInputText() {
        return !this.isHidden() && !"SPACE".equalsIgnoreCase(this.getName()) && "text-box".equalsIgnoreCase(this.getControlType()) && !"date".equalsIgnoreCase(this.getDataType()) && !"dateTime".equalsIgnoreCase(this.getDataType()) && !"integer".equalsIgnoreCase(this.getDataType());
    }

    public boolean isCalendar() {
        return !this.isHidden() && !"SPACE".equalsIgnoreCase(this.getName()) && "text-box".equalsIgnoreCase(this.getControlType()) && ("date".equalsIgnoreCase(this.getDataType()) || "dateTime".equalsIgnoreCase(this.getDataType()));
    }

    public boolean isSelectOneMenu() {
        return !this.isHidden() && !"SPACE".equalsIgnoreCase(this.getName()) && "list-box".equalsIgnoreCase(this.getControlType()) && "simple".equalsIgnoreCase(this.getParamType()) && !"AutoComplete".equalsIgnoreCase(this.getHelpText()) && !"Datatable".equalsIgnoreCase(this.getHelpText());
    }

    public boolean isSelectCheckboxMenu() {
        return !this.isHidden() && !"SPACE".equalsIgnoreCase(this.getName()) && "list-box".equalsIgnoreCase(this.getControlType()) && "multi-value".equalsIgnoreCase(this.getParamType()) && !"AutoComplete".equalsIgnoreCase(this.getHelpText()) && !"Datatable".equalsIgnoreCase(this.getHelpText());
    }

    public boolean isAutoComplete() {
        return !this.isHidden() && !"SPACE".equalsIgnoreCase(this.getName()) && "list-box".equalsIgnoreCase(this.getControlType()) && "AutoComplete".equalsIgnoreCase(this.getHelpText());
    }

    public boolean isDatatable() {
        return !this.isHidden() && !"SPACE".equalsIgnoreCase(this.getName()) && "list-box".equalsIgnoreCase(this.getControlType()) && "simple".equalsIgnoreCase(this.getParamType()) && "Datatable".equalsIgnoreCase(this.getHelpText());
    }

    public boolean isDatatableMulti() {
        return !this.isHidden() && !"SPACE".equalsIgnoreCase(this.getName()) && "list-box".equalsIgnoreCase(this.getControlType()) && "multi-value".equalsIgnoreCase(this.getParamType()) && "Datatable".equalsIgnoreCase(this.getHelpText());
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDataType() {
        return this.dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getControlType() {
        return this.controlType;
    }

    public void setControlType(String controlType) {
        this.controlType = controlType;
    }

    public String getDatePattern() {
        if (this.datePattern == null && "date".equalsIgnoreCase(this.dataType)) {
            return PARTERN_DEFAULT_DATE;
        } else {
            return this.datePattern == null && "dateTime".equalsIgnoreCase(this.dataType) ? PARTERN_DEFAULT_DATETIME : this.datePattern;
        }
    }

    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }

    public boolean isRequired() {
        return this.isRequired;
    }

    public void setRequired(boolean required) {
        this.isRequired = required;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public ReportDataset getDataset() {
        return this.dataset;
    }

    public void setDataset(ReportDataset dataset) {
        this.dataset = dataset;
    }

    public String getStringValue() {
        return this.stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParamType() {
        return this.paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    public String[] getListValue() {
        return this.listValue;
    }

    public void setListValue(String[] listValue) {
        this.listValue = listValue;
    }

    public String getHelpText() {
        return this.helpText;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    public NameValueItem getRowSelected() {
        return this.rowSelected;
    }

    public void setRowSelected(NameValueItem rowSelected) {
        this.rowSelected = rowSelected;
        this.rowSelectedLabel = rowSelected != null ? rowSelected.getName() : null;
        this.stringValue = rowSelected != null ? rowSelected.getValue() : null;
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getCascadingNodeName() {
        return this.cascadingNodeName;
    }

    public void setCascadingNodeName(String cascadingNodeName) {
        this.cascadingNodeName = cascadingNodeName;
    }

    public String getChildNodeName() {
        return this.childNodeName;
    }

    public void setChildNodeName(String childNodeName) {
        this.childNodeName = childNodeName;
    }

    public List<NameValueItem> getRowSelectedMulti() {
        return this.rowSelectedMulti;
    }

    public void setRowSelectedMulti(List<NameValueItem> rowSelectedMulti) {
        this.rowSelectedMulti = rowSelectedMulti;
        this.listValue = new String[rowSelectedMulti.size()];
        this.rowSelectedLabel = "";

        for(int i = 0; i < rowSelectedMulti.size(); ++i) {
            this.listValue[i] = ((NameValueItem)rowSelectedMulti.get(i)).getValue();
            this.rowSelectedLabel = this.rowSelectedLabel + "[" + ((NameValueItem)rowSelectedMulti.get(i)).getName() + "] ";
        }

    }

    public String getRowSelectedLabel() {
        return this.rowSelectedLabel;
    }

    public void setRowSelectedLabel(String rowSelectedLabel) {
        this.rowSelectedLabel = rowSelectedLabel;
    }

    public Date getDateValue() {
        return this.dateValue;
    }

    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    public String getCacheKey() {
        return this.cacheKey;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    public boolean isInputNumber() {
        return !this.isHidden() && !"SPACE".equalsIgnoreCase(this.getName()) && "text-box".equalsIgnoreCase(this.getControlType()) && "integer".equalsIgnoreCase(this.getDataType());
    }

    public Integer getIntValue() {
        return intValue;
    }

    public void setIntValue(Integer intValue) {
        this.intValue = intValue;
    }
}
