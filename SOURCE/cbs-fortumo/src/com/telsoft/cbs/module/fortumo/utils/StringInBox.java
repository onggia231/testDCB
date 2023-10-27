package com.telsoft.cbs.module.fortumo.utils;


import org.apache.commons.lang3.StringUtils;

public class StringInBox {
    public StringInBox() {
    }

    private static int getMaxLength(String... strings) {
        int len = Integer.MIN_VALUE;
        for (String str : strings) {
            len = Math.max(str.length(), len);
        }
        return len;
    }

    private static String padString(String str, int len) {
        StringBuilder sb = new StringBuilder(str);
        return sb.append(fill(' ', len - str.length())).toString();
    }

    private static String fill(char ch, int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }

//    public static String printBox(String... strings) {
//        int maxBoxWidth = getMaxLength(strings);
//        String startStr = null;
//        String finalStr = "| Log";
//        String line = "+" + fill('-', 10) + "+";
//        for (String str : strings) {
//            startStr = "\n| " + padString(str, maxBoxWidth);
//            finalStr += startStr;
//        }
//        return "\n" + line + "\n" + finalStr + "\n" + line + "";
//    }

    public static String printBox(String... strings) {
        StringBuilder finalString = new StringBuilder();
        finalString.append("\n+" + fill('-', 10) + "+");

        for (String str : strings) {
            if (!StringUtils.isEmpty(str)) {
                finalString.append("\n").append(str);
            }
        }
        finalString.append("\n+" + fill('-', 10) + "+");
        return finalString.toString();
    }

    public static String printBox(StringBuilder... strings) {
        StringBuilder finalString = new StringBuilder();
        finalString.append("\n+" + fill('-', 10) + "+");

        for (StringBuilder str : strings) {
            if (str != null && str.length() > 0) {
                finalString.append("\n").append(str);
            }
        }
        finalString.append("\n+" + fill('-', 10) + "+");
        return finalString.toString();
    }
}
