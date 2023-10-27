package com.telsoft.cbs.module.fortumo.service;

import com.telsoft.cbs.module.fortumo.domain.FortumoCode;
import com.telsoft.cbs.module.fortumo.domain.ResultStatus;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class FortumoExceptionHandler implements ExceptionMapper<FortumoException> {
    @Override
    public Response toResponse(FortumoException e) {
        com.telsoft.cbs.module.fortumo.domain.Response response = com.telsoft.cbs.module.fortumo.domain.Response.createResponse(e.getRequest());

        if (response != null) {
            response.getResult().setMessage(e.getMessage() == null ? FortumoCode.getDescription(e.getErrorCode()) : e.getMessage());
            response.getResult().setReasonCode(e.getErrorCode().getCode());
            response.getResult().setStatus(ResultStatus.ERROR);
            return Response.status(Response.Status.OK).entity(response).build();
        } else {
            if (e.getRequest() == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Mobifone Carrier Billing").build();
            } else
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Mobifone Carrier Billing").build();
        }
    }
}
