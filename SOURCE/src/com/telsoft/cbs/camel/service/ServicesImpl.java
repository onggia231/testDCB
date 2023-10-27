package com.telsoft.cbs.camel.service;

import com.telsoft.cbs.camel.service.cache.CacheService;
import com.telsoft.cbs.camel.service.cbs.CbsDbService;
import com.telsoft.cbs.camel.service.configuration.ConfigService;
import telsoft.gateway.commons.DBSynchronizable;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServicesImpl implements Services {
    private Map<Class, Service> serviceList = new HashMap<>();

    @Override
    public Service getService(Class clazz) {
        return serviceList.get(clazz);
    }

    @Override
    public void init() {
        serviceList.put(ConfigService.class, new ConfigService());
        serviceList.put(CacheService.class, new CbsDbService());

        for (Service service : serviceList.values()) {
            service.setServiceManager(this);
        }

        for (Service service : serviceList.values()) {
            service.init();
        }
    }

    @Override
    public void close() {
        List<Service> services = new ArrayList<>(serviceList.values());
        for (int i = services.size() - 1; i >= 0; i--) {
            Service service = services.get(i);
            service.close();
        }
        services.clear();
        serviceList.clear();
        serviceList = null;
    }

    @Override
    public void syncData(Connection connection, int i) throws Exception {
        for (Service service : serviceList.values()) {
            if (service instanceof DBSynchronizable)
                ((DBSynchronizable) service).syncData(connection, i);
        }
    }
}
