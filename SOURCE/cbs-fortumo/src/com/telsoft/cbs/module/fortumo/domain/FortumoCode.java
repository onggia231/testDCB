package com.telsoft.cbs.module.fortumo.domain;

import com.telsoft.cbs.module.cbsrest.domain.CBCode;
import lombok.Getter;

/**
 * Reference: Fortumo_Carrier_Reference_API_v1.9.pdf
 */
public enum FortumoCode {
    FORTUMO_OK(0, "Success"),
    FORTUMO_PARAMETER_ERROR(2, "Data format or validation error"),
    FORTUMO_AUTHENTICATION_ERROR(3, "Authentication error"),
    FORTUMO_INTERNAL_SERVER_ERROR(4, "Internal server error"),
    FORTUMO_REQUEST_ID_OP_MISMATCH(5, "Idempotent requestID operation mismatch"),
    FORTUMO_REQUEST_ID_PM_MISMATCH(6, "Idempotent requestID parameter mismatch"),
    FORTUMO_UNKNOWN(7, "Unknown status"),
    FORTUMO_INSUFFICIENT_FUNDS(103, "Insufficient funds"),
    FORTUMO_USER_IN_INACTIVE_STATE(104, "User account is in an inactive state"),
    FORTUMO_USER_UNKNOWN(105, "User unknown"),
    FORTUMO_CHARGE_ALREADY_REVERSED(106, "Charge already reversed"),
    FORTUMO_TRANSACTION_NOT_FOUND(107, "Transaction not found"),
    FORTUMO_CHARGE_EXCEEDS_TRANSACTION_LIMIT(108, "Charge exceeds transaction limit"),
    FORTUMO_SPEND_LIMIT_REACHED(109, "Spend limit reached"),
    FORTUMO_REFUND_WINDOW_EXPIRED(110, "Refund window expired"),
    FORTUMO_REVERSE_WINDOW_EXPIRED(111, "Reverse window expired"),
    FORTUMO_TRANSACTION_ALREADY_REFUNDED(112, "Transaction already refunded"),
    FORTUMO_REFUND_AMOUNT_EXCEEDS_ORIGINAL(113, "Refund amount cannot exceed the original transaction amount"),
    FORTUMO_TRANSACTION_HAS_BEEN_REVERSED(114, "Transaction has been reversed"),
    FORTUMO_GLOBAL_RATE_LIMIT_REACHED(115, "Global rate limit reached"),
    FORTUMO_USER_RATE_LIMIT_REACHED(116, "Rate limit reached for user"),
    FORTUMO_USER_IN_STATE_OF_INELIGIBILITY(117, "User in a permanent state of ineligibility"),
    FORTUMO_USER_NOT_ELIGIBLE_FOR_CARRIER_BILLING(118, "User is not eligible for carrier billing"),
    FORTUMO_USER_RELATED_FAILURE(119, "User-related failure"),
    FORTUMO_BAD_DEBT(120, "Bad debt"),
    FORTUMO_USER_BARRED(121, "User is barred"),
    FORTUMO_USER_SELF_BARRED(122, "User self-barred"),
    FORTUMO_IN_PROGRESS(123, "In Progress"),
    FORTUMO_PAYMENT_IS_NOT_SUCCESS(124, "Payment is not successful"),
    FORTUMO_OTP_INCORRECT(200, "OTP value incorrect"),
    FORTUMO_OTP_EXPIRED(201, "OTP expired"),
    FORTUMO_MAXIMUM_OTP_ATTEMPTS_REACHED(202, "Maximum OTP validation attempts reached"),
    FORTUMO_OTP_INVALID_SESSION(203, "OTP invalid session"),
    FORTUMO_MAXIMUM_OTP_RESEND_REACHED(204, "Maximum OTP resend attempts reached"),
    FORTUMO_ACR_HAS_BEEN_DEACTIVATED(210, "ACR known but has been deactivated"),
    FORTUMO_CARRIER_MAINTENANCE(300, "Carrier Maintenance"),
    FORTUMO_INVALID_CURRENCY(400, "Only use VND currency in transactions"),
    FORTUMO_IN_OTHER_PROGRESS(423, "In Other Progress "),
    FORTUMO_CONTENT_IS_BLOCKED(424, "Content is blocked");

    private static FortumoCode[] values = FortumoCode.values();
    @Getter
    private final int code;
    @Getter
    private final String description;

    FortumoCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String getDescription(FortumoCode code) {
        return code.getDescription();
    }

    public static String getDescription(int code) {
        for (FortumoCode value : values) {
            if (value.getCode() == code)
                return getDescription(value);
        }
        return getDescription(FortumoCode.FORTUMO_UNKNOWN);
    }

    public static FortumoCode convertFromCBCode(CBCode cbCode) {
        switch (cbCode) {
            case OK:
                return FortumoCode.FORTUMO_OK;
            case PARAMETER_ERROR:
                return FortumoCode.FORTUMO_PARAMETER_ERROR;
            case INTERNAL_SERVER_ERROR:
                return FortumoCode.FORTUMO_INTERNAL_SERVER_ERROR;
            case UNKNOWN:
                return FortumoCode.FORTUMO_UNKNOWN;
            case INSUFFICIENT_FUNDS:
                return FortumoCode.FORTUMO_INSUFFICIENT_FUNDS;
            case USER_IN_INACTIVE_STATE:
                return FortumoCode.FORTUMO_USER_IN_INACTIVE_STATE;
            case USER_UNKNOWN:
                return FortumoCode.FORTUMO_USER_UNKNOWN;
            case CHARGE_ALREADY_REVERSED:
                return FortumoCode.FORTUMO_CHARGE_ALREADY_REVERSED;
            case TRANSACTION_NOT_FOUND:
                return FortumoCode.FORTUMO_TRANSACTION_NOT_FOUND;
            case CHARGE_EXCEEDS_TRANSACTION_LIMIT:
                return FortumoCode.FORTUMO_CHARGE_EXCEEDS_TRANSACTION_LIMIT;
            case SPEND_LIMIT_REACHED:
                return FortumoCode.FORTUMO_SPEND_LIMIT_REACHED;
            case SPEND_LIMIT_REACHED_YEAR:
                return FortumoCode.FORTUMO_SPEND_LIMIT_REACHED;
            case SPEND_LIMIT_REACHED_MONTH:
                return FortumoCode.FORTUMO_SPEND_LIMIT_REACHED;
            case SPEND_LIMIT_REACHED_WEEK:
                return FortumoCode.FORTUMO_SPEND_LIMIT_REACHED;
            case SPEND_LIMIT_REACHED_DAY:
                return FortumoCode.FORTUMO_SPEND_LIMIT_REACHED;
            case REFUND_WINDOW_EXPIRED:
                return FortumoCode.FORTUMO_REFUND_WINDOW_EXPIRED;
            case REVERSE_WINDOW_EXPIRED:
                return FortumoCode.FORTUMO_REVERSE_WINDOW_EXPIRED;
            case TRANSACTION_ALREADY_REFUNDED:
                return FortumoCode.FORTUMO_TRANSACTION_ALREADY_REFUNDED;
            case REFUND_AMOUNT_EXCEEDS_ORIGINAL:
                return FortumoCode.FORTUMO_REFUND_AMOUNT_EXCEEDS_ORIGINAL;
            case TRANSACTION_HAS_BEEN_REVERSED:
                return FortumoCode.FORTUMO_TRANSACTION_HAS_BEEN_REVERSED;
            case GLOBAL_RATE_LIMIT_REACHED:
                return FortumoCode.FORTUMO_GLOBAL_RATE_LIMIT_REACHED;
            case USER_RATE_LIMIT_REACHED:
                return FortumoCode.FORTUMO_USER_RATE_LIMIT_REACHED;
            case USER_IN_STATE_OF_INELIGIBILITY:
                return FortumoCode.FORTUMO_USER_IN_STATE_OF_INELIGIBILITY;
            case USER_NOT_ELIGIBLE_FOR_CARRIER_BILLING:
                return FortumoCode.FORTUMO_USER_NOT_ELIGIBLE_FOR_CARRIER_BILLING;
            case USER_RELATED_FAILURE:
                return FortumoCode.FORTUMO_USER_RELATED_FAILURE;
            case BAD_DEBT:
                return FortumoCode.FORTUMO_BAD_DEBT;
            case USER_BARRED:
                return FortumoCode.FORTUMO_USER_BARRED;
            case USER_BARRED_ANTI_PROFIT:
                return FortumoCode.FORTUMO_USER_BARRED;
            case USER_SELF_BARRED:
                return FortumoCode.FORTUMO_USER_SELF_BARRED;
            case IN_PROGRESS:
                return FortumoCode.FORTUMO_IN_PROGRESS;
            case PAYMENT_IS_NOT_SUCCESS:
                return FortumoCode.FORTUMO_PAYMENT_IS_NOT_SUCCESS;
            case CARRIER_MAINTENANCE:
                return FortumoCode.FORTUMO_CARRIER_MAINTENANCE;
            case UNKNOWN_COMMAND:
                return FortumoCode.FORTUMO_PARAMETER_ERROR;
            case INVALID_CURRENCY:
                return FortumoCode.FORTUMO_INVALID_CURRENCY;
            case STORE_UNKNOWN:
                return FortumoCode.FORTUMO_INTERNAL_SERVER_ERROR;
            case USER_EXISTED:
                return FortumoCode.FORTUMO_UNKNOWN;
            case CONTENT_IS_BLOCKED:
                return FortumoCode.FORTUMO_CONTENT_IS_BLOCKED;
            case IN_OTHER_PROGRESS:
                return FortumoCode.FORTUMO_IN_OTHER_PROGRESS;
            default:
                return FortumoCode.FORTUMO_UNKNOWN;
        }
    }

    @Override
    public String toString() {
        return getDescription();
    }
}
