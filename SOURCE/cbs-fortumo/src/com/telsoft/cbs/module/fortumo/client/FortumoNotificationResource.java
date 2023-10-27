package com.telsoft.cbs.module.fortumo.client;

import com.telsoft.cbs.module.fortumo.domain.*;
import com.telsoft.cbs.module.cbsrest.domain.CBCode;
import com.telsoft.cbs.module.cbsrest.domain.CBCommand;
import com.telsoft.cbs.module.cbsrest.domain.RestRequest;
import com.telsoft.cbs.module.cbsrest.domain.RestResponse;
import com.telsoft.cbs.module.cbsrest.server.CbsNotificationResource;
import com.telsoft.cbs.module.cbsrest.server.service.RestException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

public class FortumoNotificationResource extends CbsNotificationResource {
    @Context
    private FortumoClient client;

    @Override
    protected RestResponse execute(RestRequest request, HttpServletRequest httpRequest) throws RestException {
        CBCommand command = request.getName();
        switch (command) {
            case SUBMIT_DN:
                return submitDN(request, httpRequest);
            case SUBMIT_MO:
                return submitMO(request, httpRequest);
            case ACCOUNT_STATUS_CHANGE_NOTIF:
                return notification(request, httpRequest);
        }
        throw new RestException(request, CBCode.UNKNOWN_COMMAND);
    }

    public RestResponse submitDN(RestRequest request, HttpServletRequest httpServletRequest) throws RestException {
        String messageId = request.getParameter("message-id");
        String correlatorId = request.getParameter("correlator-id");
        String status = request.getParameter("status");
        SubmitDNRequest submitDNRequest = new SubmitDNRequest();
        submitDNRequest.setCorrelationId(correlatorId);
        submitDNRequest.setStatus(status);
        submitDNRequest.setMessageId(messageId);
        SubmitDNResponse submitDNResponse = client.post("/fortumo/submitDN", submitDNRequest, SubmitDNResponse.class);

        RestResponse restResponse = request.createResponse();
        int code = submitDNResponse.getResult().getReasonCode();
        switch (code) {
            case 0:
                restResponse.setCode(CBCode.OK.getCode());
                break;
            case 2:
                restResponse.setCode(CBCode.PARAMETER_ERROR.getCode());
                break;
            case 3:
                restResponse.setCode(CBCode.AUTHENTICATION_ERROR.getCode());
                break;
            case 4:
                restResponse.setCode(CBCode.INTERNAL_SERVER_ERROR.getCode());
                break;
            case 7:
                restResponse.setCode(CBCode.UNKNOWN.getCode());
                break;
            default:
                restResponse.setCode(CBCode.UNKNOWN.getCode());
                break;
        }
        restResponse.setDescription(submitDNResponse.getResult().getMessage());
        return restResponse;
    }

    public RestResponse submitMO(RestRequest request, HttpServletRequest httpServletRequest) throws RestException {
        String messageId = request.getParameter("message-id");
        String isdn = request.getIsdn();
        String destination = request.getParameter("destination");
        String message = request.getParameter("message");
        String country = request.getParameter("country");
        String network = request.getParameter("network");

        SubmitMORequest submitMORequest = new SubmitMORequest();
        submitMORequest.setMessageId(messageId);
        submitMORequest.setMessage(message);
        submitMORequest.setDestination(destination);
        submitMORequest.setCountry(country);
        submitMORequest.setNetwork(network);
        submitMORequest.setAccount(new UserAccount(isdn, RefType.MSISDN));

        SubmitMOResponse submitMOResponse = client.post("/fortumo/submitMO", submitMORequest, SubmitMOResponse.class);

        RestResponse restResponse = request.createResponse();
        int code = submitMOResponse.getResult().getReasonCode();
        switch (code) {
            case 0:
                restResponse.setCode(CBCode.OK.getCode());
                break;
            case 2:
                restResponse.setCode(CBCode.PARAMETER_ERROR.getCode());
                break;
            case 3:
                restResponse.setCode(CBCode.AUTHENTICATION_ERROR.getCode());
                break;
            case 4:
                restResponse.setCode(CBCode.INTERNAL_SERVER_ERROR.getCode());
                break;
            case 7:
                restResponse.setCode(CBCode.UNKNOWN.getCode());
                break;
            default:
                restResponse.setCode(CBCode.UNKNOWN.getCode());
                break;
        }
        restResponse.setDescription(submitMOResponse.getResult().getMessage());
        return restResponse;
    }

    public RestResponse notification(RestRequest request, HttpServletRequest httpServletRequest) throws RestException {
        String isdn = request.getIsdn();
        try {
            String response = client.delete("/api/notification/user-account/" + isdn, String.class);
            RestResponse restResponse = request.createResponse();
            restResponse.setCode(CBCode.OK.getCode());
            restResponse.setDescription(response);
            return restResponse;
        } catch (Exception ex) {
            RestResponse restResponse = request.createResponse();
            restResponse.setCode(CBCode.UNKNOWN.getCode());
            restResponse.setDescription(ex.getMessage());
            return restResponse;
        }
    }
}
