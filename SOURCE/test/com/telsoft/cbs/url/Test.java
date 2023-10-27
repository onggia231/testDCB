package com.telsoft.cbs.url;

import com.telsoft.cbs.camel.CbsContansts;
import org.apache.camel.util.URISupport;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;

public class Test {
    public static void main(String[] args) throws UnsupportedEncodingException, URISyntaxException {
//        String filename = "VASGATE_20210305020344.txt";
//        String date = filename.substring(8, 22);
//        System.out.println(date);
//        URI uri = new URI(URISupport.normalizeUri("http"));
//        uri = new URI(URISupport.normalizeUri("http:?test"));
//        uri = new URI(URISupport.normalizeUri("http://test"));

/*        String a = "GOOGLE - Book 1 hey";
        String b = "APPLE - Book 3 hey";

        String regex = "^GOOGLE - [\\s\\S]+";
        if(a.matches(regex)){
            System.out.println("Matched: " + a);
        }
        if(b.matches(regex)){
            System.out.println("Matched: " + b);
        }*/
/*        double dAmount = 545;
        int taxRate = 10;
        System.out.println(Math.round(dAmount + dAmount * taxRate / 100.0));
        System.out.println(Math.round(dAmount + dAmount * taxRate / 100));
        Runtime runtime = Runtime.getRuntime();*/
//        for (int i = 0; i < 100; i++) {
//            System.out.println(ThreadLocalRandom.current().nextInt(1, 10 + 1));
//        }

        SimpleDateFormat sdf = new SimpleDateFormat(CbsContansts.PURCHASE_TIME_FORMAT);
        Date currentDate = new Date();
        System.out.println(currentDate);
        System.out.println(sdf.format(currentDate));

        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        System.out.println(sdf.format(currentDate));

        sdf.setTimeZone(TimeZone.getTimeZone("GMT+17"));
        System.out.println(sdf.format(currentDate));


    }
}
