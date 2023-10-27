package com.telsoft.cbs.domain;

public class CBRequest extends CBMessage {

    public CBResponse createResponse() {
        CBResponse response = new CBResponse();
        response.setCommand(this.getCommand());
        return response;
    }
}
