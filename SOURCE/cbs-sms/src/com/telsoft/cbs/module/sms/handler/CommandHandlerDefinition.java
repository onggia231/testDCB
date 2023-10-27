package com.telsoft.cbs.module.sms.handler;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CommandHandlerDefinition {
    private CommandHandler commandHandler;
    private List<KeyValue> keyValues = new ArrayList<>();

    @Getter
    @Setter
    public static class KeyValue {
        private String k;
        private String v;
    }

}
