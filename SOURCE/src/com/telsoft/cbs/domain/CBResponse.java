package com.telsoft.cbs.domain;

public class CBResponse extends CBMessage {
    public static final String CODE = "code";
    public static final String MESSAGE = "msg";


    public CBCode getCode() {
        return (CBCode) this.getValues().get(CODE);
    }

    public void setCode(CBCode code) {
        this.getValues().put(CODE, code);
    }

    public String getMessage() {
        return (String) this.getValues().get(MESSAGE);
    }

    public void setMessage(String message) {
        this.getValues().put(MESSAGE, message);
    }
}
