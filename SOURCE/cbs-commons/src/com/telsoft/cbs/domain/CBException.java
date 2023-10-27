package com.telsoft.cbs.domain;

import lombok.Getter;

public class CBException extends Exception {
    @Getter
    private CBCode code;

    public CBException(CBCode code) {
        super();
        this.code = code;
    }

    public CBException(CBCode code, Exception cause) {
        super(cause);
        this.code = code;
    }

    @Override
    public String getMessage() {
        return code.getCode() + "-" + CBCode.getDescription(code);
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
