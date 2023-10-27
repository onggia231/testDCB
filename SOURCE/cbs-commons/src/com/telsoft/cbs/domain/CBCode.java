package com.telsoft.cbs.domain;

import lombok.Getter;

public enum CBCode {
    /*OK(0),
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
	STORE_ATTRIBUTE_ERROR(9001),*/

	OK(0, "Thành công"),
	PARAMETER_ERROR(2, "Lỗi sai giá trị tham số"),
	AUTHENTICATION_ERROR(3, "Lỗi xác thực"),
	INTERNAL_SERVER_ERROR(4, "Hệ thống đang bận"),
	UNKNOWN(7, "Lỗi không xác định"),
	INSUFFICIENT_FUNDS(103, "Thuê bao không đủ tiền"),
	USER_IN_INACTIVE_STATE(104, "Thuê bao bị chặn 1C hoặc 2C hoặc cắt hẳn"),
	USER_UNKNOWN(105, "Thuê bao chưa đăng ký hoặc không thuộc mạng Mobifone"),
	CHARGE_ALREADY_REVERSED(106, "Giao dịch đã được hủy"),
	TRANSACTION_NOT_FOUND(107, "Không tìm thấy giao dịch"),
	CHARGE_EXCEEDS_TRANSACTION_LIMIT(108, "Giao dịch vượt quá hạn mức giao dịch"),
	SPEND_LIMIT_REACHED(109, "Giao dịch vượt quá hạn mức theo khoảng thời gian"),
	REFUND_WINDOW_EXPIRED(110, "Hoàn tiền ngoài thời gian quy định"),
	REVERSE_WINDOW_EXPIRED(111, "Hủy giao dịch ngoài thời gian quy định"),
	TRANSACTION_ALREADY_REFUNDED(112, "Giao dịch đã được hoàn tiền"),
	REFUND_AMOUNT_EXCEEDS_ORIGINAL(113, "Số tiền hoàn vượt quá số tiền giao dịch ban đầu"),
	TRANSACTION_HAS_BEEN_REVERSED(114, "Giao dịch đã được hủy"),
	GLOBAL_RATE_LIMIT_REACHED(115, "Vượt ngưỡng giới hạn gọi API"),
	USER_RATE_LIMIT_REACHED(116, "Vượt ngưỡng giới hạn giao dịch theo khoảng thời gian nhất định"),
	USER_IN_STATE_OF_INELIGIBILITY(117, "Thuê bao không đủ điều kiện thanh toán lâu dài"),
	USER_NOT_ELIGIBLE_FOR_CARRIER_BILLING(118, "Thuê bao không đủ điều kiện thanh toán"),
	USER_RELATED_FAILURE(119, "Tài khoản không xác định"),
	BAD_DEBT(120, "Thuê bao không đủ điều kiện do không thanh toán"),
	USER_BARRED(121, "Thuê bao nằm trong Blacklist"),
	USER_SELF_BARRED(122, "Thuê bao tự chặn thanh toán"),
	IN_PROGRESS(123, "Giao dịch đang trong quá trình xử lý"),
	PAYMENT_IS_NOT_SUCCESS(124, "Thanh toán không thành công"),
	CARRIER_MAINTENANCE(300, "Hệ thống đang bảo trì"),
	IN_OTHER_PROGRESS(423, "Giao dịch đang trong quá trình xử lý khác"),
	CONTENT_IS_BLOCKED(424, "Nội dung mua không được phép"),
	UNKNOWN_COMMAND(3001, "Lệnh không xác định"),
	STORE_UNKNOWN(4000, "Kho ứng dụng không xác định"),
	USER_EXISTED(4001, "Tài khoản đã tồn tại"),
	SPEND_LIMIT_REACHED_YEAR(1001, "Giao dịch vượt quá hạn mức giao dịch năm"),
	SPEND_LIMIT_REACHED_MONTH(1002, "Giao dịch vượt quá hạn mức giao dịch tháng"),
	SPEND_LIMIT_REACHED_WEEK(1003, "Giao dịch vượt quá hạn mức giao dịch tuần"),
	SPEND_LIMIT_REACHED_DAY(1004, "Giao dịch vượt quá hạn mức giao dịch ngày"),
	USER_BARRED_ANTI_PROFIT(1101, "Thuê bao nằm trong danh sách trục lợi của Mobifone"),
	PROCESS_TIMEOUT(5000, "Lỗi timeout"),


	//only use internal error
	EXTERNAL_RESOURCE_ERROR(9000,"Lỗi kết nối hệ thống ngoài"),
	STORE_ATTRIBUTE_ERROR(9001,"Lỗi cấu hình kho ứng dụng")

	;


	private static CBCode[] values = CBCode.values();

	@Getter
	private int code;

	@Getter
	private String title;

	CBCode(int code, String title) {
		this.code = code;
		this.title = title;
	}

    public static CBCode valueOfCode(String code) {
        int iCode;
        try {
            iCode = Integer.parseInt(code);
            for (CBCode value : values) {
                if (value.getCode() == iCode) {
                    return value;
                }
            }
        } catch (Exception ignored) {
        }
        return UNKNOWN;
    }

    public static CBCode valueOfCode(int code, CBCode defaultCode) {
        try {
            for (CBCode value : values) {
                if (value.getCode() == code) {
                    return value;
                }
            }
        } catch (Exception ignored) {
        }
        return defaultCode;
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
}
