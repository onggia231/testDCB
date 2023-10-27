package com.telsoft.cbs.camel;

public class CbsContansts {
    public static final String REQUEST_ID = "client_request_id"; // from Client for each request
    public static final String CLIENT_TRANSACTION_ID = "client_transaction_id"; // from Client for each transaction
    public static final String STORE_TRANSACTION_ID = "store_transaction_id"; // from Store(if any)
    public static final String TRANSACTION_ID = "cbs_transaction_id"; // from CBS for each transaction

    public static final String REFERENCE_IDEMPOTENT_ID = "reference_idempotent_id"; // transaction_id of other request
    public static final String PAYMENT_REQUEST_ID = "payment_request_id"; // transaction_id of previous payment transaction
    public static final String PAYMENT_TRANSACTION_ID = "payment_transaction_id"; // transaction_id of previous payment transaction
    public static final String PAYMENT_COMMAND = "payment_command";
    public static final String PAYMENT_DATE = "payment_date";
    public static final String PURCHASE_TIME = "purchase_time";
    public static final String PURCHASE_TIME_FORMAT = "yyyyMMddHHmmss";
    public static final String CHANNEL_TYPE = "channel_type";
    public static final String REFUND_REASON = "refund_reason";

    public static final String CB_REQUEST = "cb_request";
    public static final String MSISDN = "msisdn";
    public static final String AMOUNT = "amount";
    public static final String AMOUNT_FULL_TAX = "amount_full_tax";
    public static final String PAYMENT_AMOUNT = "payment_amount";
    public static final String PAYMENT_AMOUNT_FULL_TAX = "payment_amount_full_tax";
    public static final String TAX = "tax";
    public static final String CB_MSG_CONTEXT = "CBMsgContext";
    public static final String CB_MSG_EXCHANGE = "CBMsgExchange";
    public static final String CB_CAMEL_HISTORY = "CBCamelHistory";
    public static final String SERVICES = "services";
    public static final String ISDN_LIST = "ISDN_LIST";
    public static final String MANAGER = "manager";
    public static final String RESULT_EXTRAS = "result_extras";

    public static final String ORIGINATOR = "originator";
    public static final String STATUS = "status";
    public static final String STORE_CODE = "store_code";
    public static final String STORE = "store";
    public static final String CONTENT = "content";
    public static final String ACCESS_MAP = "access_map";
    public static final String CONTENT_DESCRIPTION = "content_description";
    public static final String COMMAND = "command";
    public static final String SUB_ID = "sub_id";
    public static final String SHORT_CODE = "short_code";

    //Common
    public static final String GATEWAY_MANAGER = "GATEWAY_MANAGER";
    public static final Object RESERVED_CHARGE_SUCCESS = "RESERVED_CHARGE_SUCCESS";
    public static final String MAP_FULL_REQUEST = "map_full_request";  /* Transaction ID do kho ứng dụng đẩy sang*/
//    public static final String STORE_TRANSACTION_ID = "store_transaction_id";  /* Transaction ID do kho ứng dụng đẩy sang*/
//    public static final String TRANSACTION_ID = "transaction_id";  /* Transaction ID do hệ thống CBS tạo ra cho mỗi yêu cầu*/
//    public static final String STORE_CODE = "store_code";  /* Mã kho ứng dụng */
//    public static final String MSISDN = "msisdn";  /* Số thuê bao chuẩn quốc tế có 84*/
//    public static final String COMMAND = "command";  /* Lệnh xử lý (hàm xử lý)*/
    public static final String STORE_START_TIME = "store_request_time";  /* */
    public static final String STORE_END_TIME = "store_response_time";  /* */
    public static final String DATE_FORMAT_JAVA_MILISECOND = "yyyyMMddHHmmss.SSS";  /* */
    public static final String DATE_FORMAT_JAVA_SECOND = "yyyyMMddHHmmss";  /* */
    public static final String TAX_CONFIG_KEY = "tax_rate";  /* */
    public static final Object FLOW_STATUS = "flow_status";
    public static final Object EXCEPTION_MESSAGE = "exception_message";
    public static final String LIST_EXCEPTIONS = "list_exceptions";
    public static final String TIMEOUT_RESPONSE = "timeout_response";

    //Subscriber Info
    public static final String SUB_VAS_STATUS = "subscriber_vas_status";  /* */
    public static final String SUB_PAY_TYPE = "subscriber_pay_type";  /* 0: postpaid /  1: prepaid*/
    public static final String SUB_LIMIT_PROFILE = "subscriber_limit_profile";  /* 0: postpaid /  1: prepaid*/

    //List CDR CPS DIAMETER
    public static final String LIST_CPS_DIAMETER_CDR = "list_cps_diameter_cdr";  /* */
    //Charge - Charging proxy - Diameter
    public static final String MSISDN_INTERNAL = "msisdn_internal";  /* Số thuê bao nội bộ Mobifone không có 84, 0*/
    public static final String CPS_B_ISDN = "cps_b_isdn";  /* 0000000001: Nội dung khác /  0000000002: Nội dung Google /  */
    public static final String CPS_SHORTCODE = "cps_shortcode";  /* Fix: 041268*/
    public static final String CPS_SPID = "cps_spid";  /* Fix: 001*/
    public static final String CPS_CPID = "cps_cpid";  /* Fix: 002*/
    public static final String CPS_CATEGORYID = "cps_categoryid";  /* 000001: Trừ tiền mua nội dung /  000002: Refund /  000003: Book debit /  000004: View IN /  000005: Cancel /  */
    public static final String CPS_CONTENTID = "cps_contentid";  /* Transaction ID do hệ thống CBS tạo ra phục vụ đối soát nội bộ TT TC.*/
    public static final String CPS_EXTRA_INFO = "cps_extra_info";  /* Gộp thông tin của 4 tham số: Google Correlation_ID(20)-Thời gian giao dịch của Google(14)-Số tiền(20)-Phân biệt nội dung Google/khác(10)*/
    public static final String CPS_REQUEST_TIME = "cps_request_time";  /* */
    //Charge result - Charging proxy - Diameter
    public static final String CPS_R_IN_RESULT_CODE = "cps_r_in_result_code";  /* 2001: Thanh cong*/
    public static final String CPS_R_RESULT_CODE = "cps_r_result_code";  /* CPS-0000: Thanh cong*/
    public static final String CPS_R_DESCRIPTION = "cps_r_description";  /* */
    public static final String CPS_R_REFUND_INFORMATION = "refund_information";  /* ID cần khi gọi hàm refund online*/
    public static final String CPS_R_RESPONSE_TIME = "cps_r_response_time";  /* */

    //Charge - Charging proxy - Diameter - CDR

    public static final String CPS_CDR_LIST = "cps_cdr_list";  /* Danh sách CDR sẽ gửi sang TTTC bao gồm cả các lệnh diameter và các trường hợp hoàn cước offline*/

    public static final String CPS_CDR_CPS_CALL_TIME = "cps_cdr_cps_call_time";  /* Ngày giờ thực hiện gọi lệnh*/
    public static final String CPS_CDR_DATETIME = "cps_cdr_datetime";  /* Ngày giờ thực hiện gọi lệnh*/
    public static final String CPS_CDR_A_NUMBER = "cps_cdr_a_number";  /* */
    public static final String CPS_CDR_B_NUMBER = "cps_cdr_b_number";  /* Là short code khi truyền vào hàm charge*/
    public static final String CPS_CDR_EVENTID = "cps_cdr_eventid";  /* Loại giao dịch*/
    public static final String CPS_CDR_SPID = "cps_cdr_spid";  /* Ghép CPS_SPID và CPS_CPID*/
    public static final String CPS_CDR_CPID = "cps_cdr_cpid";  /* Ghép CPS_SPID và CPS_CPID*/
    public static final String CPS_CDR_CONTENTID = "cps_cdr_contentid";  /* CPSTransactionID*/
    public static final String CPS_CDR_STATUS = "cps_cdr_status";  /* Là Trạng thái CDR*/
    public static final String CPS_CDR_COST = "cps_cdr_cost";  /* Số tiền khi truyền vào hàm charge*/
    public static final String CPS_CDR_CHANNEL_TYPE = "cps_cdr_channel_type";  /* SYS, API, SMS,WAP, WEB, APP*/
    public static final String CPS_CDR_INFORMATION = "cps_cdr_information";  /* Là trường CPS_EXTRA_INFO*/
    public static final String CPS_CDR_COST_TK1 = "cps_cdr_cost_tk1";  /* */
    public static final String CPS_CDR_KM2 = "cps_cdr_km2";  /* */
    public static final String CPS_CDR_KM3 = "cps_cdr_km3";  /* */


    public static final String CPS_TRANSACTION_SEQUENCE = "SEQ_CPS_TRANSACTION" ;
    public static final Object CONTENT_TYPE = "content_type";
    public static final String PAYMENT_RESULT_CODE = "payment_result_code";
    public static final String PAYMENT_STATUS = "payment_status";
    public static final String PAYMENT_REQUEST_STATUS = "payment_request_status";
    public static final String CAPTURE_IS_EXPIRED = "capture_is_expired";
    public static final String REFUND_IS_EXPIRED = "refund_is_expired";
    public static final String PAYMENT_REQUEST_TIME = "payment_request_time";
    public static final String PAYMENT_DELTA_HOURS = "payment_delta_hours";
    public static final String CHARGE_ACCOUNT = "charge_account";
    public static final String DATE_FORMAT_PPM_DISPLAY = "dd/MM/yyyy HH:mm:ss";
    public static final String SUB_ACTIVE_DATE = "subscriber_active_date";
    public static final String SUB_DELTA_ACTIVE_DATE = "sub_delta_active_date";
    public static final String SUB_PREPAID_MAIN_BALANCE = "sub_prepaid_main_balance";


    public static final String DPS_STATUS = "dps_status";
    public static final String DPS_MESSAGE = "dps_message";


    public static final String CPS_PARAM_PACKAGE_NAME = "cps_package_name";
    public static final String CPS_PARAM_SHORT_CODE = "shortcode";



    public enum CPS_CATEGORY{

        CHARGE("000001","CHARGE","Trừ tiền mua nội dung"),
        REFUND("000002","REFUND","Hoàn tiền giao dịch"),
        BOOK_DEBIT("000003","BOOK_DEBIT","Book debit"),
        VIEW_IN("000004","VIEW_IN","View IN"),
        CANCEL("000005","CANCEL","Cancel");

        private String categoryId;
        private String categoryName;
        private String categoryDescription;

        CPS_CATEGORY(String categoryId, String categoryName, String categoryDescription) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.categoryDescription = categoryDescription;
        }

        public String getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(String categoryId) {
            this.categoryId = categoryId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public String getCategoryDescription() {
            return categoryDescription;
        }

        public void setCategoryDescription(String categoryDescription) {
            this.categoryDescription = categoryDescription;
        }
    }

    public enum CPS_SUB_PAY_TYPE{
        POSTPAID("0"),
        PREPAID("1")
        ;


        private String code;

        CPS_SUB_PAY_TYPE(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public static CPS_SUB_PAY_TYPE getByCode(String code){
            for (CPS_SUB_PAY_TYPE d : CPS_SUB_PAY_TYPE.values()) {
                if (d.code.equals(code)) {
                    return d;
                }
            }
            return null;
        }
    }

}
