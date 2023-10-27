package com.telsoft.cbs.module.cbsrest.domain;

import lombok.Getter;

public enum CBCode {
    OK(0),
    PARAMETER_ERROR(2),
    AUTHENTICATION_ERROR(3),
    INTERNAL_SERVER_ERROR(4),
    UNKNOWN(7),
    INSUFFICIENT_FUNDS(103),
    USER_IN_INACTIVE_STATE(104),
    USER_UNKNOWN(105),
    CHARGE_ALREADY_REVERSED(106),
    TRANSACTION_NOT_FOUND(107),
    CHARGE_EXCEEDS_TRANSACTION_LIMIT(108),
    SPEND_LIMIT_REACHED(109),
    REFUND_WINDOW_EXPIRED(110),
    REVERSE_WINDOW_EXPIRED(111),
    TRANSACTION_ALREADY_REFUNDED(112),
    REFUND_AMOUNT_EXCEEDS_ORIGINAL(113),
    TRANSACTION_HAS_BEEN_REVERSED(114),
    GLOBAL_RATE_LIMIT_REACHED(115),
    USER_RATE_LIMIT_REACHED(116),
    USER_IN_STATE_OF_INELIGIBILITY(117),
    USER_NOT_ELIGIBLE_FOR_CARRIER_BILLING(118),
    USER_RELATED_FAILURE(119),
    BAD_DEBT(120),
    USER_BARRED(121),
    USER_SELF_BARRED(122),
    IN_PROGRESS(123),
    PAYMENT_IS_NOT_SUCCESS(124),
    CARRIER_MAINTENANCE(300),
    INVALID_CURRENCY(400),
    IN_OTHER_PROGRESS(423),
    CONTENT_IS_BLOCKED(424),
    UNKNOWN_COMMAND(3001),
    STORE_UNKNOWN(4000),
    USER_EXISTED(4001),

    SPEND_LIMIT_REACHED_YEAR(1001),
    SPEND_LIMIT_REACHED_MONTH(1002),
    SPEND_LIMIT_REACHED_WEEK(1003),
    SPEND_LIMIT_REACHED_DAY(1004),
    USER_BARRED_ANTI_PROFIT(1101),

    PROCESS_TIMEOUT(5000),

    //only use internal error
    EXTERNAL_RESOURCE_ERROR(9000),
    STORE_ATTRIBUTE_ERROR(9001),


    ;
    private static CBCode[] values = CBCode.values();

    @Getter
    private int code;

    CBCode(int code) {
        this.code = code;
    }

    public static String getDescription(int code) {
        for (CBCode value : values) {
            if (value.getCode() == code) {
                return value.name();
            }
        }
        return UNKNOWN.name();
    }

    public static String getDescription(CBCode code) {
        return code.name();
    }

    public static CBCode getCode(int code) {
        for (CBCode value : values) {
            if (value.getCode() == code) {
                return value;
            }
        }
        return UNKNOWN;
    }
}
