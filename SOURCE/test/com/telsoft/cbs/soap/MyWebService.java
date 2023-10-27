package com.telsoft.cbs.soap;

import javax.jws.WebService;

@WebService
public class MyWebService {

    public String hello(String s) {
        return "hi " + s;
    }
}
