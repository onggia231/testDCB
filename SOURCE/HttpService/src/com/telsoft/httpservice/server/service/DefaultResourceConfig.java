package com.telsoft.httpservice.server.service;

import com.telsoft.httpservice.server.RestStarter;
import com.telsoft.httpservice.server.service.provider.FormattingWriter;
import com.telsoft.httpservice.server.service.provider.UnknownExceptionHandler;
import com.telsoft.httpservice.server.service.provider.UnmarshalExceptionHandler;
import com.telsoft.httpservice.utils.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("resources")
public class DefaultResourceConfig extends ResourceConfig {
    public DefaultResourceConfig(RestStarter starter) {
        final Logger LOGGER = LoggerFactory.getLogger(starter.getLoggerName());
        register(new LoggingFilter(LOGGER, true));
        registerClasses(
                FormattingWriter.class,
                UnknownExceptionHandler.class,
                UnmarshalExceptionHandler.class);
    }
}
