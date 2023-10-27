package com.telsoft.cbs.domain;

import lombok.Getter;
import lombok.Setter;
import telsoft.gateway.core.message.GatewayMessage;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

@Setter
@Getter
public abstract class CBMessage extends GatewayMessage {
    private CBCommand command;

    public CBMessage() {
    }

    public static <K, V> String getMapValues(Map<K, V> map) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        map.forEach((k, v) -> {
            if (k != null && v != null)
                sb.append(k).append("=").append(v).append(",");
        });
        if (sb.length() > 1) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.append('}').toString();
    }

    public <T> T get(String key) {
        return (T) getValues().get(key);
    }

    public <T> T set(String key, T value) {
        return (T) getValues().put(key, value);
    }

    @Override
    public void load(InputStream is) throws Exception {

    }

    @Override
    public void store(OutputStream os) throws Exception {

    }

    @Override
    public String getContent(String strSeparator) {
        return this.getClass().getSimpleName() + "(command=" + this.getCommand() + ", parameters=" + getMapValues(getValues()) + ")";
    }

    @Override
    public String getCommandName() {
        if (command != null)
            return command.name();
        return "";
    }

    public String toString() {
        return getContent();
    }
}
