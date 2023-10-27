package com.telsoft.cbs.utils;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.CBCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import telsoft.gateway.core.gw.Gateway;
import telsoft.gateway.core.log.MessageContext;
import telsoft.gateway.core.log.ServerCommand;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CbsUtils {

    public static String formatLengthString(String content, int lengthFormat, boolean fillValue, String padChar) {
        if(lengthFormat <= 0){
            return content;
        }
        if (content.length() > lengthFormat) {
            return content.substring(content.length() - lengthFormat);
        } else {
            if (fillValue) {
                return StringUtils.leftPad(content, lengthFormat, padChar);
            } else {
                return content;
            }
        }
    }


    public static Object putValueIntoMapCheckNullValue(Map map, Object key, Object value){
        if(map == null || key == null || value == null){
            return null;
        }else{
            return map.put(key,value);
        }
    }

    public static long compareTwoTimeStampsByHours(java.sql.Timestamp currentTime, java.sql.Timestamp oldTime)
    {
        long milliseconds1 = oldTime.getTime();
        long milliseconds2 = currentTime.getTime();

        long diff = milliseconds2 - milliseconds1;
        long diffSeconds = diff / 1000;
        long diffMinutes = diff / (60 * 1000);
        long diffHours = diff / (60 * 60 * 1000);
        long diffDays = diff / (24 * 60 * 60 * 1000);

        return diffHours;
    }

    public static long compareTwoDateByDays(Date currentTime, Date oldTime)
    {
        long milliseconds1 = oldTime.getTime();
        long milliseconds2 = currentTime.getTime();

        long diff = milliseconds2 - milliseconds1;
        long diffSeconds = diff / 1000;
        long diffMinutes = diff / (60 * 1000);
        long diffHours = diff / (60 * 60 * 1000);
        long diffDays = diff / (24 * 60 * 60 * 1000);

        return diffDays;
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

    public static boolean isProcessTimeout(MessageContext messageContext){

        Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
        if(mapFullRequest.get(CbsContansts.FLOW_STATUS) != null || mapFullRequest.get(CbsContansts.FLOW_STATUS) == CBCode.PROCESS_TIMEOUT) {
            return true;
        }
        return false;
    }

    public static void setMessageServerCommand(ServerCommand sc, String strContent, Exception ex){
        if (ex != null) {
            sc.setAttribute(CbsContansts.DPS_MESSAGE, strContent + ex.getMessage());
            sc.setAttribute(CbsContansts.DPS_STATUS, "ERROR");
        } else if (StringUtils.isEmpty(strContent)) {
            sc.setAttribute(CbsContansts.DPS_STATUS, "ERROR");
        } else {
            sc.setAttribute(CbsContansts.DPS_MESSAGE, strContent);
            sc.setAttribute(CbsContansts.DPS_STATUS, "SUCCESS");
        }
    }

    public static void logMonitorDebug(Gateway gw, String start, Object o) {
        if (gw.isDebug()) {
            gw.logMonitor(start + JsonObjectUtils.jsonifyNoThrow(o));
        }
    }

    /*public static void main(String[] args) {
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

    }*/

    //    public static void main(String[] arg) {
//        System.out.println(formatLengthString("123456789012345678901234567890", 20, false, ""));
//        System.out.println(formatLengthString("123456789012345678901234567890", 12, false, ""));
//        System.out.println(formatLengthString("123456789012345678901234567890", 17, true, "@"));
//        System.out.println(formatLengthString("1", 10, true, "0"));
//        System.out.println(formatLengthString("2", 10, true, "0"));
//    }

}
