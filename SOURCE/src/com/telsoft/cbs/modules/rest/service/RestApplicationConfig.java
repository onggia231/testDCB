package com.telsoft.cbs.modules.rest.service;

import com.telsoft.cbs.modules.rest.RestReceptionist;
import com.telsoft.cbs.modules.rest.service.provider.RestExceptionHandler;
import com.telsoft.cbs.modules.rest.service.provider.FormattingWriter;
import com.telsoft.cbs.modules.rest.service.provider.UnknownExceptionHandler;
import com.telsoft.cbs.modules.rest.service.provider.UnmarshalExceptionHandler;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("resources")
public class RestApplicationConfig extends ResourceConfig {
    public RestApplicationConfig(RestReceptionist receptionist) {
//        register(CORSResponseFilter.class);
        register(new RestApplicationBinder(receptionist));
        register(RestResource.class);
        register(TestResource.class);
        register(new RestExceptionHandler(receptionist));
        register(new FormattingWriter());
        register(new UnknownExceptionHandler());
        register(new UnmarshalExceptionHandler());
    }
}
