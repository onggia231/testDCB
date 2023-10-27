package com.telsoft.cbs.domain;

public enum PaymentStatus {
    CHARGED(0),
    CAPTURED(1),
    REVERSED(2),
    REFUNDED(3),
    DOING_REVERSE(12),
    DOING_REFUND(13),
    ;

    int code;

    PaymentStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public static PaymentStatus valueOfCode(int iCode) {

        try {
            for (PaymentStatus value : values()) {
                if (value.getCode() == iCode) {
                    return value;
                }
            }
        } catch (Exception ignored) {
        }
        return CHARGED;
    }

    public static PaymentStatus valueOfName(String name) {

        try {
            for (PaymentStatus value : values()) {
                if (value.name().equals(name)) {
                    return value;
                }
            }
        } catch (Exception ignored) {
        }
        return CHARGED;
    }
}
