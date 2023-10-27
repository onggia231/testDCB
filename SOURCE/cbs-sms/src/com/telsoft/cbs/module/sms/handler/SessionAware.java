package com.telsoft.cbs.module.sms.handler;

public interface SessionAware {
    void stop();

    void start() throws Exception;
}
