package com.telsoft.cbs.module.cbsrest.server.service;

import com.telsoft.cbs.module.cbsrest.domain.CBCode;
import com.telsoft.cbs.module.cbsrest.domain.RestRequest;
import lombok.Getter;

public class RestException extends Exception {
    @Getter
    private final RestRequest request;

    @Getter
    private CBCode errorCode = CBCode.UNKNOWN;

    public RestException(RestRequest request, String message) {
        super(message);
        this.request = request;
    }

    public RestException(RestRequest request, Exception cause) {
        super(cause);
        this.request = request;
    }

    public RestException(RestRequest request, String message, Exception cause) {
        super(message, cause);
        this.request = request;
    }

    public RestException(RestRequest request, CBCode errorCode) {
        this.request = request;
        this.errorCode = errorCode;
    }
}
