package com.telsoft.cbs.module.sms.handler;

import com.telsoft.cbs.module.sms.SmsMessage;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class CommandContext {
    private Map<String, String> attributes = new HashMap<>();
    private SmsMessage message;

    public CommandContext(SmsMessage message) {
        this.message = message;
    }
}
