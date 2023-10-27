package com.telsoft.cbs.camel.service;

import telsoft.gateway.commons.DBSynchronizable;

import java.sql.Connection;

public abstract class ServiceAdapter implements Service, DBSynchronizable {

    private Services serviceManager;

    @Override
    public final void syncData(Connection connection, int i) throws Exception {
        if (hasModified(connection)) {
            doSync(connection);
            resetModifyFlag(connection);
        }
    }

    protected abstract void resetModifyFlag(Connection connection);

    protected abstract boolean hasModified(Connection connection);

    protected abstract void doSync(Connection connection);

    @Override
    public Services getServiceManager() {
        return serviceManager;
    }

    @Override
    public void setServiceManager(Services serviceManager) {
        this.serviceManager = serviceManager;
    }
}
