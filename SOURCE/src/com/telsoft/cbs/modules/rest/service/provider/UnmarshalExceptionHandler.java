package com.telsoft.cbs.modules.rest.service.provider;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.UnmarshalException;

@Provider
public class UnmarshalExceptionHandler implements ExceptionMapper<UnmarshalException> {
    @Override
    public Response toResponse(UnmarshalException e) {
        return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
    }
}
