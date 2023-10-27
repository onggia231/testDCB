package com.telsoft.cbs.modules.selfcare.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public class RequestCharge {

    @Getter
    @Setter
    String requestId;

    @Getter
    @Setter
    String storeTransactionId;

    @Getter
    @Setter
    String clientTransactionId;

    @Getter
    @Setter
    Date requestTime;

    @Getter
    @Setter
    String isdn;
}
