package com.telsoft.cbs.utils;

/**
 * Created by Viz on 28/10/2016.
 */

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVUtils {

    private static final char DEFAULT_SEPARATOR = ',';
    private static final char DEFAULT_QUOTE = '"';
    private static final String DEFAULT_NEWLINE = "\r\n";

    public static void writeLine(FileWriter w, List<String> values, boolean forceQuote) throws IOException {
        writeLine(w, values, DEFAULT_SEPARATOR, DEFAULT_QUOTE, forceQuote, DEFAULT_NEWLINE);
    }

    public static void writeLine(FileWriter w, List<String> values, char separators, boolean forceQuote) throws IOException {
        writeLine(w, values, separators, DEFAULT_QUOTE, forceQuote, DEFAULT_NEWLINE);
    }

    public static void writeLine(FileWriter w, List<String> values, char separators, boolean forceQuote, char cQuote) throws IOException {
        writeLine(w, values, separators, cQuote, forceQuote, DEFAULT_NEWLINE);
    }


    //https://tools.ietf.org/html/rfc4180
    private static String followCVSformat(String value) {

        String result = value;
        if (result.contains("\"")) {
            result = result.replace("\"", "\"\"");
        }
        if (result.indexOf("\n") >= 0) {
            result = result.replace("\n", " ");
        }
        if (result.indexOf("\t") >= 0) {
            result = result.replace("\t", " ");
        }
        if (result.indexOf("\r") >= 0) {
            result = result.replace("\r", " ");
        }
        return result;

    }

    public static void writeLine(FileWriter os, List<String> values, char separators, char customQuote, boolean forceQuote, String strNewLine) throws IOException {

        boolean first = true;

        //default customQuote is empty

        if (separators == ' ') {
            separators = DEFAULT_SEPARATOR;
        }
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            if (!first) {
                sb.append(separators);
            }
            if (!forceQuote && !value.contains("\"") && !value.contains(String.valueOf(customQuote)) && !value.contains(String.valueOf("\n"))) {
                sb.append(followCVSformat(value));
            } else {
                sb.append(customQuote).append(followCVSformat(value)).append(customQuote);
            }

            first = false;
        }
        sb.append(strNewLine);
        os.write(sb.toString());
        os.flush();
    }


}