package com.telsoft.cbs.modules.selfcare.ulti;

import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VasgateUlti {
    public static String formatISDN(String strISDN) {
        String strReturn = "";
        if (strISDN.startsWith("84") || strISDN.startsWith("04")) {
            String tmp = strISDN.substring(2);
            if (tmp.startsWith("0"))
                strReturn = tmp.substring(1);
            else
                strReturn = tmp;
        } else
            strReturn = strISDN;
        return strReturn;
    }


    public static String fixPhoneNumber(String strPhone) {
        if (strPhone == null) {
            return null;
        } else if (strPhone.startsWith("84")) {
            return strPhone;
        } else if (strPhone.startsWith("084")) {
            return strPhone.replaceFirst("084", "84");
        } else if (strPhone.startsWith("0")) {
            return strPhone.replaceFirst("0", "84");
        } else {
            return "84" + strPhone;
        }
    }

    public static String convertPhone(String isdn) {
        return standardPhoneNumber(isdn);
    }

    private static final LinkedHashMap<String, String> mhmRegexPhone;
    static
    {
        mhmRegexPhone = new LinkedHashMap<String, String>();
        mhmRegexPhone.put("^\\+(84)[0-9]{9,10}$", "+84");
        mhmRegexPhone.put("^(84)[0-9]{9,10}$", "84");
        mhmRegexPhone.put("^(84)[0-9]{7,8}$", "");
        mhmRegexPhone.put("^(?!84)0[0-9]{9,10}$", "0");
        mhmRegexPhone.put("^(0084)[0-9]{9,10}$", "0084");
//        mhmRegexPhone.put("^(?!84)[0-9]{9,10}$", "");
        mhmRegexPhone.put("^(?!84)[^0][0-9]{8,9}$", "");
        //ADD mnp Mobifone
        mhmRegexPhone.put("^(001)[0-9]{9,10}$", "001");
        mhmRegexPhone.put("^(84001)[0-9]{9,10}$", "84001");
        mhmRegexPhone.put("^(\\+84001)[0-9]{9,10}$", "+84001");
        mhmRegexPhone.put("^(0084001)[0-9]{9,10}$", "0084001");
    }

    public static String standardPhoneNumber(String input){
        String strReturn = "";
    /*        String regex = "^\\+(84)[0-9]{9,10}$";
            String regex1 = "^(84)[0-9]{9,10}$";
            String regex2 = "^(84)[0-9]{7,8}$";
            String regex3 = "^(?!84)0[0-9]{9,10}$";
            String regex5 = "^(?!84)[0-9]{9,10}$";
            String regex4 = "^(0084)[0-9]{9,10}$";
            HashMap<String, String> hmRegexPhone = new HashMap<>();
            hmRegexPhone.put(regex, "+84");
            hmRegexPhone.put(regex1, "84");
            hmRegexPhone.put(regex2, "");
            hmRegexPhone.put(regex3, "0");
            hmRegexPhone.put(regex4, "0084");
            hmRegexPhone.put(regex5, "");*/
        for (String strRegex : mhmRegexPhone.keySet()) {
            Pattern pattern = Pattern.compile(strRegex);
            Matcher matcher = pattern.matcher(input);
            if (matcher.matches()) {
                input = input.substring(mhmRegexPhone.get(strRegex).length());
                strReturn = "84" + input;
                break;
            }
        }
        if (strReturn.equals("")) {
            strReturn = input;
        }
        return strReturn;
    }

    public static void main(String[] arg){
        String strSample = "84002915000015\n" +
                "+84002915000015\n" +
                "002915000015\n" +
                "0084002915000015\n" +
                "\n" +
                "840021242020615\n" +
                "+840021242020615\n" +
                "0021242020615\n" +
                "00840021242020615\n" +
                "\n" +
                "0084915000015\n" +
                "+84915000015\n" +
                "915000015\n" +
                "84979080988\n" +
                "842020615\n" +
                "0979080988\n" +
                "01242020615\n" +
                "33441223\n" +
                "1242020615";
//        strSample = "84948077782";
        String [] strarrSample = strSample.split("\n");
        for(String strnumber:strarrSample){
            System.out.println(strnumber +" : " + standardPhoneNumber(strnumber) );
        }
    }
}
