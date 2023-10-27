package com.telsoft.cbs.camel.service;

public interface Service {
    void init();

    void close();

    Services getServiceManager();

    void setServiceManager(Services serviceManager);
}
