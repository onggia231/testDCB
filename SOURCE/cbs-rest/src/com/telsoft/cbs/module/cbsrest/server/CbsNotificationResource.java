package com.telsoft.cbs.module.cbsrest.server;

import com.telsoft.cbs.module.cbsrest.domain.RestRequest;
import com.telsoft.cbs.module.cbsrest.domain.RestResponse;
import com.telsoft.cbs.module.cbsrest.server.service.RestException;
import com.telsoft.httpservice.server.RestResource;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

@Path("/notification")
public class CbsNotificationResource implements RestResource {
    protected RestResponse execute(RestRequest request, HttpServletRequest httpRequest) throws RestException {
        return new RestResponse();
    }

    @Path("/notify")
    @POST
    public final RestResponse notify(RestRequest request, @Context HttpServletRequest httpRequest) throws RestException {
        return execute(request, httpRequest);
    }
}
