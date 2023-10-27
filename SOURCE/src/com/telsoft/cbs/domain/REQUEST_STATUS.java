package com.telsoft.cbs.domain;

import lombok.Getter;
import telsoft.gateway.core.log.MessageContext;

public enum REQUEST_STATUS {
    IN_PROCESSING("0"),
    SUCCESS("1"),
    FAILED("2"),
    TIMEOUT("3")

    ;

    @Getter
    private final String code;

    REQUEST_STATUS(String code) {
        this.code = code;
    }

    public static REQUEST_STATUS convertMessageContextStatus(String msgContextStatus){
        if(msgContextStatus == null){
            return FAILED;
        }
        switch (msgContextStatus){
            case MessageContext.STATUS_SUCCESS:
                return REQUEST_STATUS.SUCCESS;
            case "3":
                return REQUEST_STATUS.TIMEOUT;
            default:
                return FAILED;
        }
    }
}
