package com.telsoft.cbs.utils;

public class IsdnUtils {
    public static String correctISDN(String isdn) {
        if (isdn == null) {
            return null;
        }
        if (isdn.startsWith("0") && isdn.length() > 9) {
            isdn = isdn.substring(1);
        } else if (isdn.startsWith("84") && isdn.length() > 10) {
            isdn = isdn.substring(2);
        }
        return isdn;
    }
}
