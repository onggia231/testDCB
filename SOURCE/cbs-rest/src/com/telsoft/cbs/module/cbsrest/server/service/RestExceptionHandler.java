package com.telsoft.cbs.module.cbsrest.server.service;

import com.telsoft.cbs.module.cbsrest.domain.CBCode;
import com.telsoft.cbs.module.cbsrest.domain.RestResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RestExceptionHandler implements ExceptionMapper<RestException> {
    @Override
    public Response toResponse(RestException e) {
        RestResponse response = e.getRequest().createResponse();

        if (response != null) {
            response.setDescription(e.getMessage() == null ? CBCode.getDescription(e.getErrorCode()) : e.getMessage());
            response.setCode(e.getErrorCode().getCode());
            return Response.status(Response.Status.OK).entity(response).build();
        } else {
            if (e.getRequest() == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Mobifone Carrier Billing").build();
            } else
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Mobifone Carrier Billing").build();
        }
    }
}
