package com.telsoft.cbs.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Properties;

@Getter
@Setter
@Data
@Builder
public class CBStore {
    private String storeId;
    private String storeCode;

    private String name;
    private String status;

    private Long yearlyLimits;
    private Long monthlyLimits;
    private Long weeklyLimits;
    private Long dailyLimits;
    private Long transactionLimits;

    private Properties attributes;
}
