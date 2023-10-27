package com.telsoft.cbs.domain;

public enum CHANNEL_TYPE {
    SYS, API, SMS, WAP, WEB, APP;

    public static CHANNEL_TYPE valueOfName(String name) {

        try {
            for (CHANNEL_TYPE value : values()) {
                if (value.name().equals(name)) {
                    return value;
                }
            }
        } catch (Exception ignored) {
        }
        return SYS;
    }
}
