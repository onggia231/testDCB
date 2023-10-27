package com.telsoft.cbs.module.cbsrest.server;

import com.telsoft.httpservice.server.RestStarter;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * <p>Title: Core Gateway System</p>
 *
 * <p>Description: A part of TELSOFT Gateway System</p>
 *
 * <p>Copyright: Copyright (c) 2009</p>
 *
 * <p>Company: TELSOFT</p>
 *
 * @author Nguyen Cong Khanh
 * @version 1.0
 */

@Slf4j
public class CbsNotificationStarter extends RestStarter {
    @Override
    public ResourceConfig createResourceConfig() {
        ResourceConfig resourceConfig = super.createResourceConfig();
        resourceConfig.register(CbsNotificationResource.class);
        return resourceConfig;
    }
}
