package com.telsoft.cbs.domain;

public enum CDR_CPS_STATUS {
    UNKNOWN(-1),
    NOT_SUCCESS(0), //0: không thành công;
    SUCCESS(1), //1: thành công
    NEED_REFUND_UNDER_60_DAY(2), //2: cần hoàn cước tự động trong vòng từ 0 đến 60 ngày đối với thuê bao trả sau, từ 5 đến 60 ngày với thuê bao trả trước, kể từ khi giao dịch trừ cước
    NEED_REFUND_OVER_60_DAY(3), //3: cần hoàn cước nhưng đã quá 60 ngày kể từ thời điểm trừ cước giao dịch
    NEED_REFUND_UNDER_5_DAY_WHEN_REFUND_ONLINE_ERROR(4); //4: cần hoàn cước tự động trong vòng 5 ngày đối với thuê bao trả trước (do hệ thống GlobalPay gọi lệnh Refund bị lỗi)


    int code;

    CDR_CPS_STATUS(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public static CDR_CPS_STATUS valueOfCode(int iCode) {

        try {
            for (CDR_CPS_STATUS value : values()) {
                if (value.getCode() == iCode) {
                    return value;
                }
            }
        } catch (Exception ignored) {
        }
        return UNKNOWN;
    }

    public static CDR_CPS_STATUS valueOfName(String name) {

        try {
            for (CDR_CPS_STATUS value : values()) {
                if (value.name().equals(name)) {
                    return value;
                }
            }
        } catch (Exception ignored) {
        }
        return UNKNOWN;
    }
}
