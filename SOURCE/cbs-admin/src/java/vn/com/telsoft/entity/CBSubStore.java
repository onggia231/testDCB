package vn.com.telsoft.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * @author TUNGLM
 */
public class CBSubStore implements Serializable {

    private static final long serialVersionUID = 3258887845977171307L;

    private Long subId;
    private Long storeId;
    private Long yearlyLimit;
    private Long monthlyLimit;
    private Long weeklyLimit;
    private Long dailyLimit;
    private Long transLimit;
    private Date regDate;
    private Long totalYear;
    private Long totalMonth;
    private Long totalWeek;
    private Long totalDay;
    private Date startYear;
    private Date startMonth;
    private Date startWeek;
    private Date startDay;
    private Long reservedCharge;
    private Long limitProfileId;

    public Long getSubId() {
        return subId;
    }

    public void setSubId(Long subId) {
        this.subId = subId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
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

    public Date getRegDate() {
        return regDate;
    }

    public void setRegDate(Date regDate) {
        this.regDate = regDate;
    }

    public Long getTotalYear() {
        return totalYear;
    }

    public void setTotalYear(Long totalYear) {
        this.totalYear = totalYear;
    }

    public Long getTotalMonth() {
        return totalMonth;
    }

    public void setTotalMonth(Long totalMonth) {
        this.totalMonth = totalMonth;
    }

    public Long getTotalWeek() {
        return totalWeek;
    }

    public void setTotalWeek(Long totalWeek) {
        this.totalWeek = totalWeek;
    }

    public Long getTotalDay() {
        return totalDay;
    }

    public void setTotalDay(Long totalDay) {
        this.totalDay = totalDay;
    }

    public Date getStartYear() {
        return startYear;
    }

    public void setStartYear(Date startYear) {
        this.startYear = startYear;
    }

    public Date getStartMonth() {
        return startMonth;
    }

    public void setStartMonth(Date startMonth) {
        this.startMonth = startMonth;
    }

    public Date getStartWeek() {
        return startWeek;
    }

    public void setStartWeek(Date startWeek) {
        this.startWeek = startWeek;
    }

    public Date getStartDay() {
        return startDay;
    }

    public void setStartDay(Date startDay) {
        this.startDay = startDay;
    }

    public Long getReservedCharge() {
        return reservedCharge;
    }

    public void setReservedCharge(Long reservedCharge) {
        this.reservedCharge = reservedCharge;
    }

    public Long getLimitProfileId() {
        return limitProfileId;
    }

    public void setLimitProfileId(Long limitProfileId) {
        this.limitProfileId = limitProfileId;
    }
}
