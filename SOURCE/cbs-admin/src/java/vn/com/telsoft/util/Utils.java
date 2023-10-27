package vn.com.telsoft.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author TungLM
 */
public class Utils {

    public static String fixIsdn(String input) {
        return input == null ? null : "84" + input.trim().replaceAll("^0|^84(?!84)(?=\\d{8,})|^84(?=84)(?=\\d{8,})", "");
    }

    public static String fixIsdnWithout0and84(String input) {
        return input == null ? null : input.trim().replaceAll("^0|^84(?!84)(?=\\d{8,})|^84(?=84)(?=\\d{8,})", "");
    }

    public static boolean validateIsdn(String in_isdn) {
        if (in_isdn == null || "".equals(in_isdn)) {
            return false;
        }
        Pattern sChar = Pattern.compile("^((8[1-5|8]\\d{7})|(9[1|4]\\d{7})|(12[3|4|5|7|9]\\d{7}))$");
        Matcher msChar = sChar.matcher(in_isdn);
        return msChar.matches();
    }

    public static void main(String[] args) throws Exception {
//        System.out.println(convertToOldSerial("23071400102021"));

        String in_isdn = "84848474222";
        in_isdn = Utils.fixIsdnWithout0and84(in_isdn);
        System.out.println("in_isdn = " + in_isdn);
        System.out.println("validateISDN: " + Utils.validateIsdn(in_isdn));
    }

}
