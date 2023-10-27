package vn.com.telsoft.entity;

import java.io.Serializable;

/**
 * @author TUNGLM
 */
public class CBStore implements Serializable {

    private static final long serialVersionUID = 8635224616550941358L;

    private Long id;
    private String storeCode;
    private String name;
    private Long status;
    private String description;
    private Long yearlyLimit;
    private Long monthlyLimit;
    private Long weeklyLimit;
    private Long dailyLimit;
    private Long transLimit;
    private Long postpaidLimitProfile;
    private Long prepaidLimitProfile;

    public Long getPostpaidLimitProfile() {
        return postpaidLimitProfile;
    }

    public void setPostpaidLimitProfile(Long postpaidLimitProfile) {
        this.postpaidLimitProfile = postpaidLimitProfile;
    }

    public Long getPrepaidLimitProfile() {
        return prepaidLimitProfile;
    }

    public void setPrepaidLimitProfile(Long prepaidLimitProfile) {
        this.prepaidLimitProfile = prepaidLimitProfile;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStoreCode() {
        return storeCode;
    }

    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
