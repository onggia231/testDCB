package com.telsoft.cbs.modules.rest.service;

import com.telsoft.cbs.domain.CBCode;
import com.telsoft.cbs.modules.rest.domain.Request;
import lombok.Getter;

public class RestException extends Exception {
    @Getter
    private final Request request;

    @Getter
    private CBCode errorCode = CBCode.UNKNOWN;

    public RestException(Request request, String message) {
        super(message);
        this.request = request;
    }

    public RestException(Request request, Exception cause) {
        super(cause);
        this.request = request;
    }

    public RestException(Request request, String message, Exception cause) {
        super(message, cause);
        this.request = request;
    }

    public RestException(Request request, CBCode errorCode) {
        this.request = request;
        this.errorCode = errorCode;
    }
}
