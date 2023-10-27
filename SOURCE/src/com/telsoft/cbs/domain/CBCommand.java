package com.telsoft.cbs.domain;

import lombok.Getter;

//Todo Đưa ra tham số cấu hình, file cấu hình để dễ thêm
public enum CBCommand {
    CHARGE(1, true),
    GET_PROFILE(2, false),
    PAYMENT_STATUS(3, false),
    REFUND(4, true),
    REGISTER_PROFILE(5, false),
    SUBMIT_DN(6, false),
    SUBMIT_MO(7, true),
    SUBMIT_MT(8, true),
    UNREGISTER_PROFILE(9, false),
    ACCOUNT_STATUS_CHANGE_NOTIF(10, false),
    CAPTURE(11, true),
    REVERSE(12, true),
    ACCOUNT_CHANGE(13, true),
    SUBSCRIBER_LOOKUP(14,false),
    CHECK_ELIGIBILITY(15,false),
    ACCOUNT_PROFILE(16,false),
    REFUND_STATUS(17,false),
    TEST(99,true)
    ;


    @Getter
    private final int shortCode;

    @Getter
    private final boolean idempotent;

    CBCommand(int shortCode, boolean idempotent) {
        this.shortCode = shortCode;
        this.idempotent = idempotent;
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
