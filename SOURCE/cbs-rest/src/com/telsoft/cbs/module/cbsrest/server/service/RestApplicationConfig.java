package com.telsoft.cbs.module.cbsrest.server.service;

import com.telsoft.cbs.module.cbsrest.server.CbsNotificationStarter;
import com.telsoft.httpservice.server.service.provider.FormattingWriter;
import com.telsoft.httpservice.server.service.provider.UnknownExceptionHandler;
import com.telsoft.httpservice.server.service.provider.UnmarshalExceptionHandler;
import com.telsoft.httpservice.utils.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("resources")
public class RestApplicationConfig extends ResourceConfig {
    public RestApplicationConfig(CbsNotificationStarter starter) {
        final Logger LOGGER = LoggerFactory.getLogger("REST-CBS");
        register(new LoggingFilter(LOGGER, true));
        registerClasses(
                RestExceptionHandler.class,
                FormattingWriter.class,
                UnknownExceptionHandler.class,
                UnmarshalExceptionHandler.class);
    }
}
