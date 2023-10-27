package com.telsoft.cbs.domain;

public enum SubscriberStatus {


    NOT_EXISTS(0),
    BLOCKED_1_WAY(1), //Barring_outgoing_call??
    BLOCKED_2_WAY(2), //Barring_all
    OPEN_1_WAY(3), //Unbarring
    OPEN_2_WAY(4),
    DESTROYED(5), //barred
    TO_PREPAID(6),
    POSTPAID_TO_PREPAID(7);


    int code;

    SubscriberStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public static SubscriberStatus valueOfCode(int iCode) {

        try {
            for (SubscriberStatus value : values()) {
                if (value.getCode() == iCode) {
                    return value;
                }
            }
        } catch (Exception ignored) {
        }
        return NOT_EXISTS;
    }

    public static SubscriberStatus valueOfName(String name) {

        try {
            for (SubscriberStatus value : values()) {
                if (value.name().equals(name)) {
                    return value;
                }
            }
        } catch (Exception ignored) {
        }
        return NOT_EXISTS;
    }
}
