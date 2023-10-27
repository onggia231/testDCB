package com.telsoft.cbs.module.fortumo.domain;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@XmlRootElement(name = "AuthRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class AuthRequest extends Request {
    private UserAccount account;
    private CurrencyAmount purchaseAmount;
    private String correlationId;
    private String purchaseTime;
    private String storeTransactionId;
    private String productDescription;
    private String merchantInfo;

    @Override
    public Response createResponse() {
        return new AuthResponse();
    }

    public UserAccount getAccount() {
        return account;
    }

    public void setAccount(UserAccount account) {
        this.account = account;
    }

    public CurrencyAmount getPurchaseAmount() {
        return purchaseAmount;
    }

    public void setPurchaseAmount(CurrencyAmount purchaseAmount) {
        this.purchaseAmount = purchaseAmount;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getPurchaseTime() {
        return purchaseTime;
    }

    public void setPurchaseTime(String purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

    public String getStoreTransactionId() {
        return storeTransactionId;
    }

    public void setStoreTransactionId(String storeTransactionId) {
        this.storeTransactionId = storeTransactionId;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getMerchantInfo() {
        return merchantInfo;
    }

    public void setMerchantInfo(String merchantInfo) {
        this.merchantInfo = merchantInfo;
    }

//    @Override
//    public String toString() {
//        return "AuthRequest{" +
//                "account=" + account +
//                ", purchaseAmount=" + purchaseAmount +
//                ", correlationId='" + correlationId + '\'' +
//                ", purchaseTime='" + purchaseTime + '\'' +
//                ", productDescription='" + productDescription + '\'' +
//                ", merchantInfo='" + merchantInfo + '\'' +
//                '}';
//    }
}
