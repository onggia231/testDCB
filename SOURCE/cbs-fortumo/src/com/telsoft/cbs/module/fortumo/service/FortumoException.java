package com.telsoft.cbs.module.fortumo.service;

import com.telsoft.cbs.module.fortumo.domain.FortumoCode;
import com.telsoft.cbs.module.fortumo.domain.Request;
import lombok.Getter;

public class FortumoException extends Exception {
    @Getter
    private final Request request;

    @Getter
    private FortumoCode errorCode = FortumoCode.FORTUMO_UNKNOWN;

    public FortumoException(Request request, String message) {
        super(message);
        this.request = request;
    }

    public FortumoException(Request request, Exception cause) {
        super(cause);
        this.request = request;
    }

    public FortumoException(Request request, String message, Exception cause) {
        super(message, cause);
        this.request = request;
    }

    public FortumoException(Request request, FortumoCode errorCode) {
        this.request = request;
        this.errorCode = errorCode;
    }
}
