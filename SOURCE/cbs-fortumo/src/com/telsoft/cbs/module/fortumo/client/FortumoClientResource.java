package com.telsoft.cbs.module.fortumo.client;

import com.telsoft.cbs.module.cbsrest.client.CBSClient;
import com.telsoft.cbs.module.cbsrest.domain.Attribute;
import com.telsoft.cbs.module.cbsrest.domain.CBCode;
import com.telsoft.cbs.module.cbsrest.domain.RestRequest;
import com.telsoft.cbs.module.cbsrest.domain.RestResponse;
import com.telsoft.cbs.module.fortumo.domain.*;
import com.telsoft.cbs.module.fortumo.service.FortumoException;
import com.telsoft.cbs.module.fortumo.utils.StringInBox;
import com.telsoft.httpservice.server.RestResource;
import com.telsoft.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Path("/")
public class FortumoClientResource implements RestResource {
    private static Logger logger = Logger.getLogger("FORTUMO-CLIENT");

    @Context
    private FortumoClient client;

    @Context
    private CBSClient cbsClient;

    @Getter
    private StringInBox stringInBox;

    @Getter
    @Setter
    @Context
    private FortumoClientStarter fortumoClientStarter;

//    @Getter
//    @Setter
//    private String cbsLog;

//    @Getter
//    @Setter
//    private String fortumoLog;

    @Path("/fortumoclient")
    @POST
    public RestResponse process(RestRequest request, @Context HttpServletRequest httpServletRequest) throws Exception {
        RestResponse response = new RestResponse();
        StringBuilder cbsLog = new StringBuilder();
        String startTime = getTime(new Date());
        try {
            cbsLog.append("======CBS CORE COMMUNICATION====== \n"
                    + "Request Time: " + startTime + "\n"
                    + "Request: " + StringUtil.nvl(request.toString(), "null") + "\n");

            if (request.getName() == null) {
                response.getParameters().add(new Attribute("status", "ERROR"));
                response.setCode(2);
                response.setDescription(CBCode.getDescription(response.getCode()));
                return response;
            } else {
                switch (request.getName()) {
                    case SUBMIT_MO:
                        response = this.submitMOBMF(request, httpServletRequest);
                        break;
                    case ACCOUNT_CHANGE:
                        response = this.accountChange(request, httpServletRequest);
                        break;
                    default:
                        response.getParameters().add(new Attribute("status", "ERROR"));
                        response.setCode(7);
                        response.setDescription("Feature is not implemented");
                        break;
                }

                cbsLog.append("Response Time: " + getTime(new Date()) + "\n"
                        + "Response: " + StringUtil.nvl(response.toString(), "null") + "\n"
                        + "======END======");
                logger.info((cbsLog));
                if (fortumoClientStarter.isDebug()) {
                    fortumoClientStarter.logMonitor(cbsLog.toString());
                }
                return response;
            }
        } catch (Exception ex) {
            cbsLog.append("Error when process: " + ex.getMessage() + "\n" + "======END======");
            logger.info((cbsLog));
            fortumoClientStarter.logMonitor(cbsLog.toString());
            throw ex;
        }
    }

    public RestResponse submitMOBMF(RestRequest request, @Context HttpServletRequest httpServletRequest) {
        RestResponse response = new RestResponse();
        String startTime = getTime(new Date());
        if (request.getTransaction_id() == null || request.getTransaction_id().isEmpty()
                || request.getIsdn() == null || request.getIsdn().isEmpty()
                || request.getParameter("short_code") == null || request.getParameter("short_code").isEmpty()
                || request.getParameter("content") == null || request.getParameter("content").isEmpty()
        ) {
            response.getParameters().add(new Attribute("status", "ERROR"));
            response.setCode(2);
            response.setDescription(CBCode.getDescription(response.getCode()));
        } else {
            if (request.getTransaction_id().matches("^[a-zA-Z0-9-_]{1,128}$")
                    && request.getIsdn().matches("^^(84)[0-9]{9}$")
                    && request.getParameter("short_code").matches("^[a-zA-Z0-9]{1,20}$")
                    && request.getParameter("content").matches("^.{1,160}$")
            ) {
                StringBuilder fortumoLog = new StringBuilder();
                try {
                    String messageId = request.getTransaction_id();
                    String message = request.getParameter("content");
                    String destination = request.getParameter("short_code");
                    UserAccount account = new UserAccount();
                    account.setAccount(request.getIsdn());
                    account.setRefType(RefType.MSISDN);

                    SubmitMORequest submitMORequest = new SubmitMORequest();
                    submitMORequest.setAccount(account);
                    submitMORequest.setMessageId(messageId);
                    submitMORequest.setMessage(message);
                    submitMORequest.setDestination(destination);

                    fortumoLog.append("======FORTUMO COMMUNICATION======\n"
                            + "Request Time: " + startTime + "\n"
                            + "Request: " + StringUtil.nvl(submitMORequest.toString(), "null") + "\n");
                    SubmitMOResponse submitMOResponse = client.post(fortumoClientStarter.getSubmitMOApi(), submitMORequest, SubmitMOResponse.class);

                    fortumoLog.append("Response Time: " + getTime(new Date()) + "\n"
                            + "Response: " + StringUtil.nvl(submitMOResponse.toString(), "null") + "\n"
                            + "======END======");
                    response.getParameters().add(new Attribute("status", submitMOResponse.getResult().getStatus().toString()));
                    response.setCode(submitMOResponse.getResult().getReasonCode());
                    response.setDescription(CBCode.getDescription(submitMOResponse.getResult().getReasonCode()));
                    logger.info(fortumoLog);
                    if (fortumoClientStarter.isDebug()) {
                        fortumoClientStarter.logMonitor(fortumoLog.toString());
                    }
                } catch (Exception ex) {
                    fortumoLog.append("Call API Failed: " + ex.getMessage() + "\n"
                            + "======END======");
                    logger.error(fortumoLog, ex);
                    fortumoClientStarter.logMonitor(fortumoLog.toString());
                    response.getParameters().add(new Attribute("status", "ERROR"));
                    response.setCode(CBCode.EXTERNAL_RESOURCE_ERROR.getCode());
                    response.setDescription(ex.getMessage());
                }
            } else {
                response.getParameters().add(new Attribute("status", "ERROR"));
                response.setCode(CBCode.PARAMETER_ERROR.getCode());
                response.setDescription(CBCode.getDescription(response.getCode()));
            }
        }
        return response;
    }

    public RestResponse accountChange(RestRequest request, @Context HttpServletRequest httpServletRequest) {
        RestResponse response = new RestResponse();
        String startTime = getTime(new Date());
        if (request.getTransaction_id() == null || request.getTransaction_id().isEmpty()
                || request.getIsdn() == null || request.getIsdn().isEmpty()
        ) {
            response.getParameters().add(new Attribute("status", "ERROR"));
            response.setCode(2);
            response.setDescription(CBCode.getDescription(response.getCode()));
        } else {
            if (request.getTransaction_id().matches("^[a-zA-Z0-9-_]{1,128}$")
                    && request.getIsdn().matches("^(84)[0-9]{9}$")
            ) {
                StringBuilder fortumoLog = new StringBuilder();
                try {
                    String messageId = request.getTransaction_id();
                    UserAccount account = new UserAccount();
                    account.setAccount(request.getIsdn());
                    account.setRefType(RefType.MSISDN);

                    AccountChangeRequest accountChangeRequest = new AccountChangeRequest();
                    accountChangeRequest.setAccount(account);
                    accountChangeRequest.setMessageId(messageId);

                    fortumoLog.append("======FORTUMO COMMUNICATION======\n"
                            + "Request Time: " + startTime + "\n"
                            + "Request: " + StringUtil.nvl(accountChangeRequest.toString(), "null") + "\n");
                    AccountChangeResponse accountChangeResponse = client.post(fortumoClientStarter.getAccChangeApi(), accountChangeRequest, AccountChangeResponse.class);

                    fortumoLog.append("Response Time: " + getTime(new Date()) + "\n"
                            + "Response: " + StringUtil.nvl(accountChangeResponse.toString(), "null") + "\n"
                            + "======END======");

                    response.getParameters().add(new Attribute("status", accountChangeResponse.getResult().getStatus().toString()));
                    response.setCode(accountChangeResponse.getResult().getReasonCode());
                    response.setDescription(CBCode.getDescription(accountChangeResponse.getResult().getReasonCode()));

                    logger.info(fortumoLog);
                    if (fortumoClientStarter.isDebug()) {
                        fortumoClientStarter.logMonitor(fortumoLog.toString());
                    }
                } catch (Exception ex) {
                    fortumoLog.append("Call API Failed: " + ex.getMessage() + "\n"
                            + "======END======");
                    logger.error(fortumoLog, ex);
                    fortumoClientStarter.logMonitor(fortumoLog.toString());
                    response.getParameters().add(new Attribute("status", "ERROR"));
                    response.setCode(CBCode.EXTERNAL_RESOURCE_ERROR.getCode());
                    response.setDescription(ex.getMessage());
                }
            } else {
                response.getParameters().add(new Attribute("status", "ERROR"));
                response.setCode(CBCode.PARAMETER_ERROR.getCode());
                response.setDescription(CBCode.getDescription(response.getCode()));
            }
        }
        return response;
    }

    public String getTime(Date date) {
        date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String strDate = dateFormat.format(date);
        return strDate;
    }
}
