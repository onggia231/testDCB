package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.CBException;
import com.telsoft.cbs.domain.CBRequest;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.annotations.Component;
import telsoft.gateway.core.log.MessageContext;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Clear total charged for reserve and refund
 * <p>
 */

@Component("cbs-clear-total-charged")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-clear-total-charged",
        title = "Clear total charged for reserve and refund",
        syntax = "cbs-clear-total-charged:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)

public class ClearTotalChargedForRefund extends ProcessorComponent  {
    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        String sql = "select \n" +
                "case when css.start_year < trunc(sysdate,'YYYY') then 1 else 0 end over_year, \n" +
                "case when css.start_month < trunc(sysdate,'MM') then 1 else 0 end over_month, \n" +
                "case when css.start_week < trunc(sysdate,'IW') then 1 else 0 end over_week, \n" +
                "case when css.start_day < trunc(sysdate,'DD') then 1 else 0 end over_day\n" +
                "FROM CB_SUB_STORE css \n" +
                "where css.STORE_ID = 34 AND css.SUB_ID = 190562";

        Long amountFullTax = (Long) exchange.getProperty(CbsContansts.AMOUNT_FULL_TAX);
        Date currentDate = new Date();
        Date firstDateOfDayCurrent = getFirstDateOf(currentDate,Calendar.HOUR_OF_DAY);
        Date firstDateOfWeekCurrent = getFirstDateOf(currentDate,Calendar.DAY_OF_WEEK);
        Date firstDateOfMonthCurrent = getFirstDateOf(currentDate,Calendar.DAY_OF_MONTH);
        Date firstDateOfYearCurrent = getFirstDateOf(currentDate,Calendar.DAY_OF_YEAR);

        Date paymentDate = (Date) exchange.getProperty(CbsContansts.PAYMENT_REQUEST_TIME);
        Date firstDateOfDay = getFirstDateOf(paymentDate,Calendar.HOUR_OF_DAY);
        Date firstDateOfWeek = getFirstDateOf(paymentDate,Calendar.DAY_OF_WEEK);
        Date firstDateOfMonth = getFirstDateOf(paymentDate,Calendar.DAY_OF_MONTH);
        Date firstDateOfYear = getFirstDateOf(paymentDate,Calendar.DAY_OF_YEAR);

        if(paymentDate != null){
            if(firstDateOfDayCurrent.compareTo(firstDateOfDay) == 0){

            }
        }
    }



    public static Date getFirstDateOf(Date date, int of){
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
//        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        if(of == Calendar.DAY_OF_WEEK){
            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        }else {
            cal.set(of, cal.getActualMinimum(of));
        }
        return cal.getTime();
    }

    public static void main(String[] args) {
        Date currentDate = new Date();
        Date firstDateOfDayCurrent = getFirstDateOf(currentDate,Calendar.HOUR_OF_DAY);
        Date firstDateOfWeekCurrent = getFirstDateOf(currentDate,Calendar.DAY_OF_WEEK);
        Date firstDateOfMonthCurrent = getFirstDateOf(currentDate,Calendar.DAY_OF_MONTH);
        Date firstDateOfYearCurrent = getFirstDateOf(currentDate,Calendar.DAY_OF_YEAR);

        try {
            Date anotherDate = new SimpleDateFormat(CbsContansts.DATE_FORMAT_JAVA_SECOND).parse("20201223050632");
            Date firstDateOfDay = getFirstDateOf(anotherDate,Calendar.HOUR_OF_DAY);
            Date firstDateOfWeek = getFirstDateOf(anotherDate,Calendar.DAY_OF_WEEK);
            Date firstDateOfMonth = getFirstDateOf(anotherDate,Calendar.DAY_OF_MONTH);
            Date firstDateOfYear = getFirstDateOf(anotherDate,Calendar.DAY_OF_YEAR);
            String a = "";
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}
