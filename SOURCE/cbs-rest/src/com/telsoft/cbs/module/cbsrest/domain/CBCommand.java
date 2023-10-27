package com.telsoft.cbs.module.cbsrest.domain;

import lombok.Getter;

public enum CBCommand {
    CHARGE(1),
    GET_PROFILE(2),
    PAYMENT_STATUS(3),
    REFUND(4),
    REGISTER_PROFILE(5),
    SUBMIT_DN(6),
    SUBMIT_MO(7),
    SUBMIT_MT(8),
    UNREGISTER_PROFILE(9),
    ACCOUNT_STATUS_CHANGE_NOTIF(10),
    CAPTURE(11),
    REVERSE(12),
    ACCOUNT_CHANGE(13),
    SUBSCRIBER_LOOKUP(14),
    CHECK_ELIGIBILITY(15),
    ACCOUNT_PROFILE(16),
    REFUND_STATUS(17),
    TEST(99);


    @Getter
    private final int shortCode;

    CBCommand(int shortCode) {
        this.shortCode = shortCode;
    }

    public static CBCommand valueOf(int cmdCode) {
        for (CBCommand d : CBCommand.values()) {
            if (d.getShortCode() == cmdCode) {
                return d;
            }
        }
        return null;
    }
}
