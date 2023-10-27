package com.telsoft.cbs.camel.service;

import telsoft.gateway.commons.DBSynchronizable;

public interface Services extends DBSynchronizable {
    <T extends Service> T getService(Class clazz);

    void init();

    void close();

}
