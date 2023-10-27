package com.telsoft.cbs.module.sms;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by khanhnc on 10/28/15.
 */
@Getter
@Setter
public class SmsMessage {
    private String originator;
    private String receiver;
    private String content;
    private String requestId;

    public SmsMessage(String sourceAddr, String destAddress, String text) {
        this.originator = sourceAddr;
        this.receiver = destAddress;
        this.content = text;
    }

    public SmsMessage() {

    }

    @Override
    public String toString() {
        return "SmsMessage{" +
                "originator='" + originator + '\'' +
                ", receiver='" + receiver + '\'' +
                ", content='" + content + '\'' +
                ", requestId='" + requestId + '\'' +
                '}';
    }
}
