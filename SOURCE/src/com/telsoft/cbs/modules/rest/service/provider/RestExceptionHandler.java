package com.telsoft.cbs.modules.rest.service.provider;

import com.telsoft.cbs.domain.CBCode;
import com.telsoft.cbs.modules.rest.RestReceptionist;
import com.telsoft.cbs.modules.rest.domain.Request;
import com.telsoft.cbs.modules.rest.service.RestException;
import com.telsoft.cbs.utils.CbsUtils;
import com.telsoft.cbs.utils.JsonObjectUtils;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RestExceptionHandler implements ExceptionMapper<RestException> {

    private final RestReceptionist receptionist;

    public RestExceptionHandler(RestReceptionist receptionist) {
        this.receptionist = receptionist;
    }

    @Override
    public Response toResponse(RestException e) {
        com.telsoft.cbs.modules.rest.domain.Response response = Request.createResponse(e.getRequest());

        if (response != null) {
            response.setDescription(e.getMessage() == null ? CBCode.getDescription(e.getErrorCode()) : e.getMessage());
            response.setCode(e.getErrorCode().getCode());
            CbsUtils.logMonitorDebug(this.receptionist.getGateway(), "Response message: HTTP(" + Response.Status.OK.getStatusCode() + "-" + Response.Status.OK.toString() + "): ", response);
            return Response.status(Response.Status.OK).entity(response).build();
        } else {
            if (e.getRequest() == null) {
                CbsUtils.logMonitorDebug(this.receptionist.getGateway(), "Response message: HTTP(" + Response.Status.BAD_REQUEST.getStatusCode() + "-" + Response.Status.BAD_REQUEST.toString() + ") ", "Mobifone Carrier Billing");
                return Response.status(Response.Status.BAD_REQUEST).entity("Mobifone Carrier Billing").build();
            } else {
                CbsUtils.logMonitorDebug(this.receptionist.getGateway(), "Response message: HTTP(" + Response.Status.INTERNAL_SERVER_ERROR.getStatusCode() + "-" + Response.Status.INTERNAL_SERVER_ERROR.toString() + ") ", "Mobifone Carrier Billing");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Mobifone Carrier Billing").build();
            }
        }
    }
}
