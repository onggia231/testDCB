package com.telsoft.cbs.domain;

import lombok.Getter;

public enum FLOW_STATUS {
    IN_PROCESSING("0"),
    SUCCESS("1"),
    FAILED("2"),
    TIMEOUT("3");

    @Getter
    private final String code;

    FLOW_STATUS(String code) {
        this.code = code;
    }

}
