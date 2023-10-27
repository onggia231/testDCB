package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.*;
import com.telsoft.cbs.utils.CbsLog;
import com.telsoft.cbs.utils.CbsUtils;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.annotations.Component;
import org.apache.commons.dbutils.DbUtils;
import telsoft.gateway.core.log.MessageContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Check Subscriber Transaction Limitation Component
 * <p>
 */

@Component("cbs-check-limitation")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-check-limitation",
        title = "Check Limitations",
        syntax = "cbs-check-limitation:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)

public class CheckTransactionLimitationComponent extends ProcessorComponent {
    public static void rollbackReserved(Connection con, long amount, String sub_id, CBStore store) throws SQLException {
        String sql = "UPDATE CB_SUB_STORE SET RESERVED_CHARGE = RESERVED_CHARGE - ? " +
                " WHERE SUB_ID = ? AND STORE_ID = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setLong(1, amount);
            stmt.setString(2, sub_id);
            stmt.setString(3, store.getStoreId());
            int i = stmt.executeUpdate();
        }
    }

    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        if(CbsUtils.isProcessTimeout(messageContext)){
            throw new CBException(CBCode.PROCESS_TIMEOUT);
        }

        // applicable to CHARGE command

        CBCommand command = (CBCommand) exchange.getProperty(CbsContansts.COMMAND);
        Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
        CbsContansts.CPS_SUB_PAY_TYPE payType = (CbsContansts.CPS_SUB_PAY_TYPE) mapFullRequest.get(CbsContansts.SUB_PAY_TYPE);
        if(payType == null){
            payType = CbsContansts.CPS_SUB_PAY_TYPE.PREPAID;
        }
        if (command == CBCommand.CHARGE) {
            Long amountFullTax = (Long) mapFullRequest.get(CbsContansts.AMOUNT_FULL_TAX);
            CBStore store = (CBStore) messageContext.getProperty(CbsContansts.STORE);
            String sub_id = (String) mapFullRequest.get(CbsContansts.SUB_ID);

            try (Connection con = getManager().getConnection()) {
                //todo test timeout
//                Thread.sleep(10000);
                // update reserved_charge in CB_SUB_STORE, reserved_charge = reserved_charge + amount
                String sql = "UPDATE CB_SUB_STORE SET RESERVED_CHARGE = RESERVED_CHARGE + ? " +
                        " WHERE SUB_ID = ? AND STORE_ID = ?";
                try (PreparedStatement stmt = con.prepareStatement(sql)) {
                    stmt.setLong(1, amountFullTax);
                    stmt.setString(2, sub_id);
                    stmt.setString(3, store.getStoreId());
                    int i = stmt.executeUpdate();
                    if (i == 0) {
                        throw new CBException(CBCode.USER_UNKNOWN);
                    }
                    CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest,CbsContansts.RESERVED_CHARGE_SUCCESS,new Boolean(true));
                }


                // select total charge by last TOTAL + reserved_charge,
                // if this value is greater than limit then issue error and restore last reserved charge value
/*                sql = "SELECT TOTAL_YEAR + RESERVED_CHARGE, TOTAL_MONTH + RESERVED_CHARGE, TOTAL_WEEK + RESERVED_CHARGE," +
                        " TOTAL_DAY + RESERVED_CHARGE, YEARLY_LIMIT, MONTHLY_LIMIT, WEEKLY_LIMIT, DAILY_LIMIT, TRANS_LIMIT " +
                        "FROM CB_SUB_STORE " +
                        "WHERE STORE_ID = ? AND SUB_ID = ? ";
                */

/*                sql = "SELECT css.TOTAL_YEAR + css.RESERVED_CHARGE, css.TOTAL_MONTH + css.RESERVED_CHARGE, css.TOTAL_WEEK + css.RESERVED_CHARGE, css.TOTAL_DAY + css.RESERVED_CHARGE, \n" +
                        "    nvl(css.YEARLY_LIMIT, clp.YEARLY_LIMIT), nvl(css.MONTHLY_LIMIT, clp.monthly_limit), nvl(css.WEEKLY_LIMIT,clp.weekly_limit), \n" +
                        "    nvl(css.DAILY_LIMIT,clp.daily_limit), nvl(css.TRANS_LIMIT,clp.trans_limit),\n" +
                        "    case when css.start_year < trunc(sysdate,'YYYY') then 1 else 0 end over_year, \n" +
                        "    case when css.start_month < trunc(sysdate,'MM') then 1 else 0 end over_month, \n" +
                        "    case when css.start_week < trunc(sysdate,'IW') then 1 else 0 end over_week, \n" +
                        "    case when css.start_day < trunc(sysdate,'DD') then 1 else 0 end over_day" +
                        "FROM CB_SUB_STORE css left join cb_limit_profile clp on (css.limit_profile_id = clp.id)" +
                        "WHERE css.STORE_ID = ? AND css.SUB_ID = ? ";*/

                sql = "SELECT css.TOTAL_YEAR + css.RESERVED_CHARGE, css.TOTAL_MONTH + css.RESERVED_CHARGE, \n" +
                        "css.TOTAL_WEEK + css.RESERVED_CHARGE, css.TOTAL_DAY + css.RESERVED_CHARGE,\n" +
                        "nvl(nvl(css.YEARLY_LIMIT, clp.YEARLY_LIMIT),lp.yearly_limit) yearly_limit, \n" +
                        "nvl(nvl(css.MONTHLY_LIMIT, clp.monthly_limit),lp.monthly_limit) monthly_limit, \n" +
                        "nvl(nvl(css.WEEKLY_LIMIT,clp.weekly_limit),lp.weekly_limit) weekly_limit, \n" +
                        "nvl(nvl(css.DAILY_LIMIT,clp.daily_limit),lp.daily_limit) daily_limit, \n" +
                        "nvl(nvl(css.TRANS_LIMIT,clp.trans_limit),lp.trans_limit) trans_limit,\n" +
                        "case when css.start_year < trunc(sysdate,'YYYY') then 1 else 0 end over_year, \n" +
                        "case when css.start_month < trunc(sysdate,'MM') then 1 else 0 end over_month, \n" +
                        "case when css.start_week < trunc(sysdate,'IW') then 1 else 0 end over_week, \n" +
                        "case when css.start_day < trunc(sysdate,'DD') then 1 else 0 end over_day, \n" +
                        "css.limit_profile_id, css.RESERVED_CHARGE \n" +
                        "FROM CB_SUB_STORE css left join cb_limit_profile clp on (css.limit_profile_id = clp.id), cb_store s, cb_limit_profile lp\n" +
                        "WHERE css.store_id = s.id and lp.id = s." + payType.name() + "_limit_profile\n" +
                        "and css.STORE_ID = ? AND css.SUB_ID = ? ";

                StringBuilder sqlUpdateCycle = new StringBuilder("update CB_SUB_STORE set \n") ;

                ResultSet rs = null;
                PreparedStatement statement = con.prepareStatement(sql);
                try {
                    //Todo Test timeout
//                    Thread.sleep(20000);

                    statement.setString(1, store.getStoreId());
                    statement.setString(2, sub_id);
                    rs = statement.executeQuery();
                    if (rs.next()) {
                        long totalYear = rs.getLong(1);
                        long totalMonth = rs.getLong(2);
                        long totalWeek = rs.getLong(3);
                        long totalDay = rs.getLong(4);
                        long limitYear = rs.getLong(5);
                        long limitMonth = rs.getLong(6);
                        long limitWeek = rs.getLong(7);
                        long limitDay = rs.getLong(8);
                        long limitTransaction = rs.getLong(9);
                        int overYear = rs.getInt("over_year");
                        int overMonth = rs.getInt("over_month");
                        int overWeek = rs.getInt("over_week");
                        int overDay = rs.getInt("over_day");

                        Integer limitProfileId = rs.getInt("limit_profile_id");
                        long reservedCharge = rs.getLong("RESERVED_CHARGE");
                        CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest,CbsContansts.SUB_LIMIT_PROFILE,limitProfileId);
                        //check over cycle
                        boolean overCycle = false;
                        if(overYear == 1){
                            totalYear = reservedCharge;//fixme: Change to reserved_charge mới chính xác
                            sqlUpdateCycle.append("start_year = trunc(sysdate,'YYYY'), total_year = 0,");
                            overCycle = true;
                        }
                        if(overMonth == 1){
                            totalMonth = reservedCharge;
                            sqlUpdateCycle.append("start_month = trunc(sysdate,'MM'), total_month = 0,");
                            overCycle = true;
                        }
                        if(overWeek == 1){
                            totalWeek = reservedCharge;
                            sqlUpdateCycle.append("start_week = trunc(sysdate,'IW'), total_week = 0,");
                            overCycle = true;
                        }
                        if(overDay == 1){
                            totalDay = reservedCharge;
                            sqlUpdateCycle.append("start_day = trunc(sysdate,'DD'), total_day = 0,");
                            overCycle = true;
                        }

                        // check limit
                        if (limitTransaction > 0 && amountFullTax > limitTransaction) {
                            rollbackReserved(con, amountFullTax, sub_id, store);
                            throw new CBException(CBCode.CHARGE_EXCEEDS_TRANSACTION_LIMIT);
                        }

                        if (limitDay > 0 && totalDay > limitDay) {
                            rollbackReserved(con, amountFullTax, sub_id, store);
                            throw new CBException(CBCode.SPEND_LIMIT_REACHED_DAY);
                        }

                        if (limitWeek > 0 && totalWeek > limitWeek) {
                            rollbackReserved(con, amountFullTax, sub_id, store);
                            throw new CBException(CBCode.SPEND_LIMIT_REACHED_WEEK);
                        }

                        if (limitMonth > 0 && totalMonth > limitMonth) {
                            rollbackReserved(con, amountFullTax, sub_id, store);
                            throw new CBException(CBCode.SPEND_LIMIT_REACHED_MONTH);
                        }

                        if (limitYear > 0 && totalYear > limitYear) {
                            rollbackReserved(con, amountFullTax, sub_id, store);
                            throw new CBException(CBCode.SPEND_LIMIT_REACHED_YEAR);
                        }

                        //update cycle
                        if(overCycle){
                            sqlUpdateCycle.deleteCharAt(sqlUpdateCycle.length() - 1);
                            sqlUpdateCycle.append(" where SUB_ID = ? AND STORE_ID = ? ");
                            try (PreparedStatement stmt = con.prepareStatement(sqlUpdateCycle.toString())) {
                                stmt.setString(1, sub_id);
                                stmt.setString(2, store.getStoreId());
                                int i = stmt.executeUpdate();
                            }
                        }

                    } else {
                        rollbackReserved(con, amountFullTax, sub_id, store);
                        throw new CBException(CBCode.USER_UNKNOWN);
                    }
                } finally {
                    DbUtils.closeQuietly(rs);
                    DbUtils.closeQuietly(statement);
                }

                // else the subscriber's limitation value is in allowed range,
                // it is neccessary to clear this reserved at end this transaction
            } catch (CBException e) {
                CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest,CbsContansts.EXCEPTION_MESSAGE,e.getCode().name());
                throw e;
            } catch (Exception e) {
                CbsLog.error(messageContext, "CheckTransactionLimitationComponent", CBCode.INTERNAL_SERVER_ERROR, "", e.getMessage());
                throw new CBException(CBCode.INTERNAL_SERVER_ERROR, e);
            }
        }
    }
}
