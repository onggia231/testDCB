package com.telsoft.cbs.domain;

public enum LIST_ACCESS_TYPE {
    BLACKLIST,
    WHITELIST;


    public static LIST_ACCESS_TYPE fromCode(int code) {
        switch (code) {
            case 0:
                return BLACKLIST;
            case 1:
                return WHITELIST;
            default:
                return BLACKLIST;
        }
    }

    public static LIST_ACCESS_TYPE valueOfName(String name) {

        try {
            for (LIST_ACCESS_TYPE value : values()) {
                if (value.name().equals(name)) {
                    return value;
                }
            }
        } catch (Exception ignored) {
        }
        return BLACKLIST;
    }
}
