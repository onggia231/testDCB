package vn.com.telsoft.entity;

import java.io.Serializable;

public class CBLimitProfile implements Serializable {

    private static final long serialVersionUID = -1031680976433005495L;

    private Integer limitProfileId;
    private String limitProfileCode;
    private String limitProfileName;
    private String limitProfileDesc;
    private Long yearlyLimit;
    private Long monthlyLimit;
    private Long weeklyLimit;
    private Long dailyLimit;
    private Long transLimit;

    public CBLimitProfile() {
    }

    public CBLimitProfile(CBLimitProfile ett) {
        this.limitProfileId = ett.getLimitProfileId();
        this.limitProfileCode = ett.getLimitProfileCode();
        this.limitProfileName = ett.getLimitProfileName();
        this.limitProfileDesc = ett.getLimitProfileDesc();
        this.yearlyLimit = ett.getYearlyLimit();
        this.monthlyLimit = ett.getMonthlyLimit();
        this.weeklyLimit = ett.getWeeklyLimit();
        this.dailyLimit = ett.getDailyLimit();
        this.transLimit = ett.getTransLimit();
    }

    public Integer getLimitProfileId() {
        return limitProfileId;
    }

    public void setLimitProfileId(Integer limitProfileId) {
        this.limitProfileId = limitProfileId;
    }

    public String getLimitProfileCode() {
        return limitProfileCode;
    }

    public void setLimitProfileCode(String limitProfileCode) {
        this.limitProfileCode = limitProfileCode;
    }

    public String getLimitProfileName() {
        return limitProfileName;
    }

    public void setLimitProfileName(String limitProfileName) {
        this.limitProfileName = limitProfileName;
    }

    public String getLimitProfileDesc() {
        return limitProfileDesc;
    }

    public void setLimitProfileDesc(String limitProfileDesc) {
        this.limitProfileDesc = limitProfileDesc;
    }

    public Long getYearlyLimit() {
        return yearlyLimit;
    }

    public void setYearlyLimit(Long yearlyLimit) {
        this.yearlyLimit = yearlyLimit;
    }

    public Long getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(Long monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public Long getWeeklyLimit() {
        return weeklyLimit;
    }

    public void setWeeklyLimit(Long weeklyLimit) {
        this.weeklyLimit = weeklyLimit;
    }

    public Long getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(Long dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public Long getTransLimit() {
        return transLimit;
    }

    public void setTransLimit(Long transLimit) {
        this.transLimit = transLimit;
    }
}
