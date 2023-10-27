package com.telsoft.cbs.module.fortumo.service;

import com.telsoft.cbs.module.cbsrest.client.CBSClient;
import com.telsoft.cbs.module.cbsrest.domain.Attribute;
import com.telsoft.cbs.module.cbsrest.domain.CBCode;
import com.telsoft.cbs.module.cbsrest.domain.RestRequest;
import com.telsoft.cbs.module.cbsrest.domain.RestResponse;
import com.telsoft.cbs.module.fortumo.domain.*;
import com.telsoft.cbs.module.fortumo.hazelcast.HazelcastClientStarter;
import com.telsoft.cbs.module.fortumo.utils.StringInBox;
import com.telsoft.httpservice.server.RestResource;
import com.telsoft.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Path("/")
public class FortumoResource implements RestResource {
	private static Logger logger = Logger.getLogger("FORTUMO-SERVER");

	@Context
	private CBSClient cbsClient;

	@Getter
	private StringInBox stringInBox;

	@Getter
	@Setter
	private String msisdn = "";

	@Getter
	@Setter
	@Context
	private FortumoStarter fortumoStarter;

//    @Getter
//    @Setter
//    private String fortumoLog;

//    @Getter
//    @Setter
//    private String cbsLog;

	private static ResponseResult createResponseResult(RestResponse restResponse) {
		FortumoCode fortumoCode = FortumoCode.convertFromCBCode(restResponse.getCBCode());
		ResponseResult responseResult = new ResponseResult();
		responseResult.setMessage(fortumoCode.getDescription());
		responseResult.setStatus(fortumoCode == FortumoCode.FORTUMO_OK ? ResultStatus.OK : ResultStatus.ERROR);
		responseResult.setReasonCode(fortumoCode.getCode());
		return responseResult;
	}

	@Path("/test")
	@POST
	public String test(@Context HttpServletRequest httpServletRequest) throws FortumoException {
		fortumoStarter.logMonitor("TEST API OK");
		return "OK";
	}

	//3.1 Mobifone to Fortumo
	@Path("/submitmo")
	@POST
	public SubmitMOResponse submitMO(SubmitMORequest request, @Context HttpServletRequest httpServletRequest) {
		String startTime = getTime(new Date());
		SubmitMOResponse response = new SubmitMOResponse();
		RestResponse restResponse = new RestResponse();
		this.msisdn = cbsClient.getMsisdn();
		String fortumoLog = "";
		try {
			String messageId = request.getMessageId();
			String message = request.getMessage();
			String destination = request.getDestination();
			UserAccount account = request.getAccount();

			if (this.msisdn.equals(account.getAccount())) {
				restResponse.setCode(0);
				response.setResult(createResponseResult(restResponse));
			} else {
				restResponse.setCode(105);
				response.setResult(createResponseResult(restResponse));
			}
			fortumoLog = "======FORTUMO COMMUNICATION====== \n"
					+ "| Request Time: " + startTime + "\n"
					+ "| Request: " + request.toString() + "\n"
					+ "| Response Time: " + getTime(new Date()) + "\n"
					+ "| Response: " + response.toString() + "\n"
					+ "| ======END======";
			logger.info(stringInBox.printBox(fortumoLog));
			if (fortumoStarter.isDebug()) {
				fortumoStarter.logMonitor(stringInBox.printBox(fortumoLog));
			}
		} catch (Exception ex) {
			logger.error(stringInBox.printBox(ex.getMessage()));
			restResponse.setCode(7);
			response.setResult(createResponseResult(restResponse));
			fortumoLog = "======FORTUMO COMMUNICATION====== \n"
					+ "| Request Time: " + startTime + "\n"
					+ "| Request: " + request.toString() + "\n"
					+ "| Response Time: " + getTime(new Date()) + "\n"
					+ "| Response: " + response.toString() + "\n"
					+ "| ======END======";
			logger.info(stringInBox.printBox(fortumoLog));
			if (fortumoStarter.isDebug()) {
				fortumoStarter.logMonitor(stringInBox.printBox("Exception:", ex.getMessage(), fortumoLog));
			}
		}
		return response;
	}

	//3.2 Fortumo to Mobifone
	@Path("/auth")
	@POST
	public AuthResponse auth(AuthRequest request, @Context HttpServletRequest httpServletRequest) {
		String startTime = getTime(new Date());
		AuthResponse response = new AuthResponse();
		RestResponse restResponse = new RestResponse();
		StringBuilder fortumoLog = new StringBuilder();

		fortumoLog.append("======FORTUMO COMMUNICATION====== \n"
				+ "| Request Time: " + getTime(new Date()) + "\n"
				+ "| Request: " + request.toString() + "\n");

		StringBuilder cbsLog = new StringBuilder();


		try {
			if (
					(request.getCorrelationId() == null || request.getCorrelationId().isEmpty()
							|| request.getAccount() == null || StringUtils.isEmpty(request.getAccount().getAccount())
							|| request.getPurchaseTime() == null || request.getPurchaseTime().isEmpty()
							|| request.getStoreTransactionId() == null || request.getStoreTransactionId().isEmpty()
							|| request.getPurchaseAmount() == null || Double.parseDouble(request.getPurchaseAmount().getAmount()) < 0.0
							|| request.getProductDescription() == null || request.getProductDescription().isEmpty())
							|| request.getPurchaseAmount().getCurrency() == null
							|| (!StringUtils.isEmpty(request.getMerchantInfo()) && !request.getMerchantInfo().matches("^.{1,255}$"))//cho phep null
							||
							!(request.getCorrelationId().matches("^[a-zA-Z0-9-_]{1,128}$")
									&& request.getPurchaseTime().matches("^[0-9]{14}$")
									&& request.getStoreTransactionId().matches("^[a-zA-Z0-9_]{1,128}$")
									&& request.getAccount().getAccount().matches("^(84)[0-9]{9}$")
									&& request.getPurchaseAmount().getAmount().matches("^[0-9]\\d*$")
									&& request.getProductDescription().matches("^.{1,255}$"))
			) {
				//sai parameter
				restResponse.setCode(CBCode.PARAMETER_ERROR.getCode());
				response.setResult(createResponseResult(restResponse));
			} else if (!request.getPurchaseAmount().getCurrency().matches("^(VND)$")) {
				//sai tien te
				restResponse.setCode(CBCode.INVALID_CURRENCY.getCode());
				response.setResult(createResponseResult(restResponse));
			} else {
				try {
					String correlationId = request.getCorrelationId();
					String storeTransactionId = request.getStoreTransactionId();
					String purchaseTime = request.getPurchaseTime();
					UserAccount account = request.getAccount();
					CurrencyAmount purchaseAmount = request.getPurchaseAmount();
					String productDescription = request.getProductDescription();
					String merchantInfo = request.getMerchantInfo();

					String tempKey = AuthRequest.class.getSimpleName() + "_" + correlationId + "_" + cbsClient.getStoreCode();
					String key = tempKey.toUpperCase();
					RestRequest cbsRequest = null;
					switch (HazelcastClientStarter.hzClient.checkAvailable(key)) {
						case 0:
							HazelcastClientStarter.hzClient.put(key, "");
							cbsRequest = cbsClient.createFortumoAuthRequest(correlationId, purchaseTime, storeTransactionId, account.getAccount(), purchaseAmount.getAmount(), productDescription, merchantInfo);
							cbsLog.append("======CBS CORE COMMUNICATION====== \n"
									+ "| Request Time: " + startTime + "\n"
									+ "| Request: " + cbsRequest.toString() + "\n");

							restResponse = cbsClient.execute(httpServletRequest, cbsRequest);

							cbsLog.append("| Response Time: " + getTime(new Date()) + "\n"
									+ "| Response: " + restResponse.toString() + "\n"
									+ "| ======END======");

							response.setIssuerPaymentId(StringUtil.nvl(restResponse.getParameter("cbs_transaction_id"), "null"));
							response.setResult(createResponseResult(restResponse));
//                                    restResponse.setCode(0);
//                                    response.setIssuerPaymentId(key);
//                                    response.setResult(createResponseResult(restResponse));

							HazelcastClientStarter.hzClient.put(key, response);

							break;
						case 1:
							restResponse.setCode(CBCode.IN_PROGRESS.getCode());
							response.setResult(createResponseResult(restResponse));

							break;
						case 2:
							response = (AuthResponse) HazelcastClientStarter.hzClient.get(key);
							fortumoLog.append("Cached response from Hazelcast Map\n");

							break;
						case 99:
							cbsRequest = cbsClient.createFortumoAuthRequest(correlationId, purchaseTime, storeTransactionId, account.getAccount(), purchaseAmount.getAmount(), productDescription, merchantInfo);
							cbsLog.append("======CBS CORE COMMUNICATION====== \n"
									+ "| Request Time: " + startTime + "\n"
									+ "| Request: " + cbsRequest.toString() + "\n");

							restResponse = cbsClient.execute(httpServletRequest, cbsRequest);

							cbsLog.append("| Response Time: " + getTime(new Date()) + "\n"
									+ "| Response: " + restResponse.toString() + "\n"
									+ "| ======END======");


							response.setIssuerPaymentId(StringUtil.nvl(restResponse.getParameter("cbs_transaction_id"), "null"));
							response.setResult(createResponseResult(restResponse));
//                                    restResponse.setCode(107);
//                                    response.setIssuerPaymentId(key);
//                                    response.setResult(createResponseResult(restResponse));

							if (fortumoStarter.isDebug()) {
								fortumoStarter.logMonitor("Could not connect to any cluster, Request was sent to CBS core...");
								fortumoStarter.logMonitor("Restart Hazelcast Client or Hazelcast Server please...");
							}

							break;
					}
				} catch (Exception ex) {
					logger.error("Error when process request: " + ex.getMessage(), ex);
					restResponse.setCode(CBCode.UNKNOWN.getCode());
					response.setResult(createResponseResult(restResponse));
					cbsLog.append("Exception: " + ex.getMessage() + "\n");
				}

			}
		} catch (Exception ex) {
			logger.error("Error when validate request: " + ex.getMessage(), ex);
			restResponse.setCode(CBCode.PARAMETER_ERROR.getCode());
			response.setResult(createResponseResult(restResponse));
			fortumoLog.append("Exception: " + ex.getMessage() + "\n");
		} finally {
			fortumoLog.append("| Response Time: " + getTime(new Date()) + "\n"
					+ "| Response: " + response.toString() + "\n"
					+ "| ======END======");
			logger.info(stringInBox.printBox(fortumoLog, cbsLog));
			if (fortumoStarter.isDebug()) {
				fortumoStarter.logMonitor(stringInBox.printBox(fortumoLog, cbsLog));
			}
		}
		return response;
	}

	//3.3 Fortumo to Mobifone
	@Path("/charge")
	@POST
	public ChargeResponse charge(ChargeRequest request, @Context HttpServletRequest httpServletRequest) {
		String startTime = getTime(new Date());
		ChargeResponse response = new ChargeResponse();
		RestResponse restResponse = new RestResponse();

		StringBuilder fortumoLog = new StringBuilder();
		fortumoLog.append("======FORTUMO COMMUNICATION====== \n"
				+ "| Request Time: " + startTime + "\n"
				+ "| Request: " + request.toString() + "\n");

		StringBuilder cbsLog = new StringBuilder();
		try {
			if ((request.getAuthCorrelationId() == null || request.getAuthCorrelationId().isEmpty()
//					|| request.getAuthPurchaseTime() == null || request.getAuthPurchaseTime().isEmpty()
					|| StringUtils.isEmpty(request.getStoreTransactionId())
			)
					//|| request.getIssuerPaymentId() == null)
					|| !(request.getAuthCorrelationId().matches("^[a-zA-Z0-9-_]{1,128}$")
//					&& request.getAuthPurchaseTime().matches("^[0-9]{14}$")
					&& request.getStoreTransactionId().matches("^[a-zA-Z0-9_]{1,128}$")
			)
			) {
				restResponse.setCode(CBCode.PARAMETER_ERROR.getCode());
				response.setResult(createResponseResult(restResponse));

			} else {
				try {
					String authCorrelationId = request.getAuthCorrelationId();
					String storeTransactionId = request.getStoreTransactionId();
					String authPurchaseTime = request.getAuthPurchaseTime();
					//String issuerPaymentId = request.getIssuerPaymentId();

					String key = (ChargeRequest.class.getSimpleName() + "_" + authCorrelationId + "_" + cbsClient.getStoreCode()).toUpperCase();
					RestRequest cbsRequest;
					switch (HazelcastClientStarter.hzClient.checkAvailable(key)) {
						case 0:
							HazelcastClientStarter.hzClient.put(key, "");

							cbsRequest = cbsClient.createFortumoChargeRequest(authCorrelationId, authPurchaseTime, storeTransactionId);
							cbsLog.append("======CBS CORE COMMUNICATION====== \n"
									+ "| Request Time: " + startTime + "\n"
									+ "| Request: " + cbsRequest.toString() + "\n");

							restResponse = cbsClient.execute(httpServletRequest, cbsRequest);
							cbsLog.append("| Response Time: " + getTime(new Date()) + "\n"
									+ "| Response: " + restResponse.toString() + "\n"
									+ "| ======END======");

							response.setIssuerChargeId(StringUtil.nvl(restResponse.getParameter("cbs_transaction_id"), "null"));
							response.setResult(createResponseResult(restResponse));

							HazelcastClientStarter.hzClient.put(key, response);

							break;
						case 1:
							restResponse.setCode(123);
							response.setResult(createResponseResult(restResponse));

							break;
						case 2:
							response = (ChargeResponse) HazelcastClientStarter.hzClient.get(key);
							fortumoLog.append("Cached response from Hazelcast Map\n");
							break;
						case 99:

							cbsRequest = cbsClient.createFortumoChargeRequest(authCorrelationId, authPurchaseTime, storeTransactionId);
							cbsLog.append("======CBS CORE COMMUNICATION====== \n"
									+ "| Request Time: " + startTime + "\n"
									+ "| Request: " + cbsRequest.toString() + "\n");

							restResponse = cbsClient.execute(httpServletRequest, cbsRequest);
							cbsLog.append("| Response Time: " + getTime(new Date()) + "\n"
									+ "| Response: " + restResponse.toString() + "\n"
									+ "| ======END======");

							response.setIssuerChargeId(StringUtil.nvl(restResponse.getParameter("cbs_transaction_id"), "null"));
							response.setResult(createResponseResult(restResponse));

							if (fortumoStarter.isDebug()) {
								fortumoStarter.logMonitor("Could not connect to any cluster, Request was sent to CBS core...");
								fortumoStarter.logMonitor("Restart Hazelcast Client or Hazelcast Server please...");
							}
							break;
					}
				} catch (Exception ex) {
					logger.error("Error when process request: " + ex.getMessage(), ex);
					restResponse.setCode(CBCode.UNKNOWN.getCode());
					response.setResult(createResponseResult(restResponse));
					cbsLog.append("Exception: " + ex.getMessage() + "\n");
				}
			}
		} catch (Exception ex) {
			logger.error("Error when validate request: " + ex.getMessage(), ex);
			restResponse.setCode(CBCode.PARAMETER_ERROR.getCode());
			response.setResult(createResponseResult(restResponse));
			fortumoLog.append("Exception: " + ex.getMessage() + "\n");
		} finally {
			fortumoLog.append("| Response Time: " + getTime(new Date()) + "\n"
					+ "| Response: " + response.toString() + "\n"
					+ "| ======END======");
			logger.info(stringInBox.printBox(fortumoLog, cbsLog));
			if (fortumoStarter.isDebug()) {
				fortumoStarter.logMonitor(stringInBox.printBox(fortumoLog, cbsLog));
			}
		}
		return response;
	}

	//3.4 Fortumo to Mobifone
	@Path("/reverse")
	@POST
	public ReverseResponse reverse(ReverseRequest request, @Context HttpServletRequest httpServletRequest) {
		String startTime = getTime(new Date());
		ReverseResponse response = new ReverseResponse();
		RestResponse restResponse = new RestResponse();

		StringBuilder fortumoLog = new StringBuilder();
		fortumoLog.append("======FORTUMO COMMUNICATION====== \n"
				+ "| Request Time: " + startTime + "\n"
				+ "| Request: " + request.toString() + "\n");

		StringBuilder cbsLog = new StringBuilder();
		try {


			if ((request.getAuthCorrelationId() == null || request.getAuthCorrelationId().isEmpty()
//					|| request.getAuthPurchaseTime() == null || request.getAuthPurchaseTime().isEmpty()
					|| StringUtils.isEmpty(request.getStoreTransactionId())
			)
					|| !(request.getAuthCorrelationId().matches("^[a-zA-Z0-9-_]{1,128}$")
					&& request.getStoreTransactionId().matches("^[a-zA-Z0-9_]{1,128}$")
//					&& request.getAuthPurchaseTime().matches("^[0-9]{14}$")
			)
			) {
				restResponse.setCode(CBCode.PARAMETER_ERROR.getCode());
				response.setResult(createResponseResult(restResponse));

			} else {
				try {
					String authCorrelationId = request.getAuthCorrelationId();
					String authPurchaseTime = request.getAuthPurchaseTime();
					String storeTransactionId = request.getStoreTransactionId();
					//String issuerPaymentId = request.getIssuerPaymentId();

					String key = (ReverseRequest.class.getSimpleName() + "_" + authCorrelationId + "_" + cbsClient.getStoreCode()).toUpperCase();
					RestRequest cbsRequest;
					switch (HazelcastClientStarter.hzClient.checkAvailable(key)) {
						case 0:
							HazelcastClientStarter.hzClient.put(key, "");

							cbsRequest = cbsClient.createFortumoReverseRequest(authCorrelationId, authPurchaseTime, storeTransactionId);
							cbsLog.append("======CBS CORE COMMUNICATION====== \n"
									+ "| Request Time: " + startTime + "\n"
									+ "| Request: " + cbsRequest.toString() + "\n");

							restResponse = cbsClient.execute(httpServletRequest, cbsRequest);
							cbsLog.append("| Response Time: " + getTime(new Date()) + "\n"
									+ "| Response: " + restResponse.toString() + "\n"
									+ "| ======END======");

							response.setIssuerReverseId(StringUtil.nvl(restResponse.getParameter("cbs_transaction_id"), "null"));
							response.setResult(createResponseResult(restResponse));

							HazelcastClientStarter.hzClient.put(key, response);

							break;
						case 1:
							restResponse.setCode(CBCode.IN_PROGRESS.getCode());
							response.setResult(createResponseResult(restResponse));

							break;
						case 2:
							response = (ReverseResponse) HazelcastClientStarter.hzClient.get(key);
							fortumoLog.append("Cached response from Hazelcast Map\n");
							break;
						case 99:
							cbsRequest = cbsClient.createFortumoReverseRequest(authCorrelationId, authPurchaseTime, storeTransactionId);
							cbsLog.append("======CBS CORE COMMUNICATION====== \n"
									+ "| Request Time: " + startTime + "\n"
									+ "| Request: " + cbsRequest.toString() + "\n");

							restResponse = cbsClient.execute(httpServletRequest, cbsRequest);
							cbsLog.append("| Response Time: " + getTime(new Date()) + "\n"
									+ "| Response: " + restResponse.toString() + "\n"
									+ "| ======END======");

							response.setIssuerReverseId(StringUtil.nvl(restResponse.getParameter("cbs_transaction_id"), "null"));
							response.setResult(createResponseResult(restResponse));

							if (fortumoStarter.isDebug()) {
								fortumoStarter.logMonitor("Could not connect to any cluster, Request was sent to CBS core...");
								fortumoStarter.logMonitor("Restart Hazelcast Client or Hazelcast Server please...");
							}
							break;
					}
				} catch (Exception ex) {
					logger.error("Error when process request: " + ex.getMessage(), ex);
					restResponse.setCode(CBCode.UNKNOWN.getCode());
					response.setResult(createResponseResult(restResponse));
					cbsLog.append("Exception: " + ex.getMessage() + "\n");
				}
			}
		} catch (Exception ex) {
			logger.error("Error when validate request: " + ex.getMessage(), ex);
			restResponse.setCode(CBCode.PARAMETER_ERROR.getCode());
			response.setResult(createResponseResult(restResponse));
			fortumoLog.append("Exception: " + ex.getMessage() + "\n");
		} finally {
			fortumoLog.append("| Response Time: " + getTime(new Date()) + "\n"
					+ "| Response: " + response.toString() + "\n"
					+ "| ======END======");
			logger.info(stringInBox.printBox(fortumoLog, cbsLog));
			if (fortumoStarter.isDebug()) {
				fortumoStarter.logMonitor(stringInBox.printBox(fortumoLog, cbsLog));
			}
		}
		return response;
	}

	//3.5 Fortumo to Mobifone
	@Path("/refund")
	@POST
	public RefundResponse refund(RefundRequest request, @Context HttpServletRequest httpServletRequest) {
		String startTime = getTime(new Date());
		RefundResponse response = new RefundResponse();
		RestResponse restResponse = new RestResponse();

		StringBuilder fortumoLog = new StringBuilder();
		fortumoLog.append("======FORTUMO COMMUNICATION====== \n"
				+ "| Request Time: " + startTime + "\n"
				+ "| Request: " + request.toString() + "\n");

		StringBuilder cbsLog = new StringBuilder();

		try {
			if ((request.getAuthCorrelationId() == null || request.getAuthCorrelationId().isEmpty()
//					|| request.getAuthPurchaseTime() == null || request.getAuthPurchaseTime().isEmpty()
					|| StringUtils.isEmpty(request.getStoreTransactionId())
			)
					|| !(request.getAuthCorrelationId().matches("^[a-zA-Z0-9-_]{1,128}$")
					&& request.getStoreTransactionId().matches("^[a-zA-Z0-9_]{1,128}$")
//					&& request.getAuthPurchaseTime().matches("^[0-9]{14}$")
			)
			) {
				restResponse.setCode(CBCode.PARAMETER_ERROR.getCode());
				response.setResult(createResponseResult(restResponse));
			} else {
				try {
					String authCorrelationId = request.getAuthCorrelationId();
					String authPurchaseTime = request.getAuthPurchaseTime();
					String storeTransactionId = request.getStoreTransactionId();
					//String issuerPaymentId = request.getIssuerPaymentId();
					String refundReason = request.getRefundReason();

					String key = (RefundRequest.class.getSimpleName() + "_" + authCorrelationId + "_" + cbsClient.getStoreCode()).toUpperCase();
					RestRequest cbsRequest;
					switch (HazelcastClientStarter.hzClient.checkAvailable(key)) {
						case 0:
							HazelcastClientStarter.hzClient.put(key, "");

							cbsRequest = cbsClient.createFortumoRefundRequest(authCorrelationId, authPurchaseTime, storeTransactionId, refundReason);
							cbsLog.append("======CBS CORE COMMUNICATION====== \n"
									+ "| Request Time: " + startTime + "\n"
									+ "| Request: " + cbsRequest.toString() + "\n");

							restResponse = cbsClient.execute(httpServletRequest, cbsRequest);
							cbsLog.append("| Response Time: " + getTime(new Date()) + "\n"
									+ "| Response: " + restResponse.toString() + "\n"
									+ "| ======END======");

							response.setIssuerRefundId(StringUtil.nvl(restResponse.getParameter("cbs_transaction_id"), "null"));
							response.setResult(createResponseResult(restResponse));

							HazelcastClientStarter.hzClient.put(key, response);

							break;
						case 1:
							restResponse.setCode(CBCode.IN_PROGRESS.getCode());
							response.setResult(createResponseResult(restResponse));

							break;
						case 2:
							response = (RefundResponse) HazelcastClientStarter.hzClient.get(key);
							fortumoLog.append("Cached response from Hazelcast Map\n");
							break;
						case 99:
							cbsRequest = cbsClient.createFortumoRefundRequest(authCorrelationId, authPurchaseTime, storeTransactionId, refundReason);
							cbsLog.append("======CBS CORE COMMUNICATION====== \n"
									+ "| Request Time: " + startTime + "\n"
									+ "| Request: " + cbsRequest.toString() + "\n");

							restResponse = cbsClient.execute(httpServletRequest, cbsRequest);
							cbsLog.append("| Response Time: " + getTime(new Date()) + "\n"
									+ "| Response: " + restResponse.toString() + "\n"
									+ "| ======END======");

							response.setIssuerRefundId(StringUtil.nvl(restResponse.getParameter("cbs_transaction_id"), "null"));
							response.setResult(createResponseResult(restResponse));

							if (fortumoStarter.isDebug()) {
								fortumoStarter.logMonitor("Could not connect to any cluster, Request was sent to CBS core...");
								fortumoStarter.logMonitor("Restart Hazelcast Client or Hazelcast Server please...");
							}
							break;
					}
				} catch (Exception ex) {
					logger.error("Error when process request: " + ex.getMessage(), ex);
					restResponse.setCode(CBCode.UNKNOWN.getCode());
					response.setResult(createResponseResult(restResponse));
					cbsLog.append("Exception: " + ex.getMessage() + "\n");
				}
			}
		} catch (Exception ex) {
			logger.error("Error when validate request: " + ex.getMessage(), ex);
			restResponse.setCode(CBCode.PARAMETER_ERROR.getCode());
			response.setResult(createResponseResult(restResponse));
			fortumoLog.append("Exception: " + ex.getMessage() + "\n");
		} finally {
			fortumoLog.append("| Response Time: " + getTime(new Date()) + "\n"
					+ "| Response: " + response.toString() + "\n"
					+ "| ======END======");
			logger.info(stringInBox.printBox(fortumoLog, cbsLog));
			if (fortumoStarter.isDebug()) {
				fortumoStarter.logMonitor(stringInBox.printBox(fortumoLog, cbsLog));
			}
		}
		return response;
	}

	//3.7
	@Path("/refundV2")
	@POST
	public RefundResponseV2 refundV2(RefundRequestV2 request, @Context HttpServletRequest httpServletRequest) {
		String startTime = getTime(new Date());
		RefundResponseV2 response = new RefundResponseV2();
		RestResponse restResponse = new RestResponse();
		String fortumoLog = "";
		String cbsLog = "";
		if ((request.getAuthCorrelationId() == null || request.getAuthCorrelationId().isEmpty()
				|| request.getAuthPurchaseTime() == null || request.getAuthPurchaseTime().isEmpty()
				|| request.getRefundAmount() == null || request.getRefundAmount() < 0)
				||
				!(request.getAuthCorrelationId().matches("^[a-zA-Z0-9-_]{1,128}$")
						&& request.getAuthPurchaseTime().matches("^[0-9]{14}$")
						&& request.getRefundAmount().toString().matches("^.{1,255}$"))
		) {
			restResponse.setCode(2);
			response.setResult(createResponseResult(restResponse));
			fortumoLog = "======FORTUMO COMMUNICATION====== \n"
					+ "| Request Time: " + startTime + "\n"
					+ "| Request: " + request.toString() + "\n"
					+ "| Response Time: " + getTime(new Date()) + "\n"
					+ "| Response: " + response.toString() + "\n"
					+ "| ======END======";
			logger.info(stringInBox.printBox(fortumoLog));
			if (fortumoStarter.isDebug()) {
				fortumoStarter.logMonitor(stringInBox.printBox(fortumoLog));
			}
		} else {
			try {
				String authCorrelationId = request.getAuthCorrelationId();
				String authPurchaseTime = request.getAuthPurchaseTime();
				//String issuerPaymentId = request.getIssuerPaymentId();
				String refundReason = request.getRefundReason();
				double amount = request.getRefundAmount();

				String key = (RefundRequestV2.class.getSimpleName() + "_" + authCorrelationId + "_" + cbsClient.getStoreCode()).toUpperCase();

				switch (HazelcastClientStarter.hzClient.checkAvailable(key)) {
					case 0:
						HazelcastClientStarter.hzClient.put(key, "");

						restResponse = cbsClient.fortumoRefundV2(httpServletRequest, authCorrelationId, authPurchaseTime, refundReason, amount);
						cbsLog = "======CBS CORE COMMUNICATION====== \n"
								+ "| Request Time: " + startTime + "\n"
								+ "| Request: " + request.toString() + "\n"
								+ "| Response Time: " + getTime(new Date()) + "\n"
								+ "| Response: " + restResponse.toString() + "\n"
								+ "| ======END======";

						response.setIssuerRefundId(StringUtil.nvl(restResponse.getParameter("cbs_transaction_id"), "null"));
						response.setResult(createResponseResult(restResponse));

						HazelcastClientStarter.hzClient.put(key, response);
						fortumoLog = "======FORTUMO COMMUNICATION====== \n"
								+ "| Request Time: " + startTime + "\n"
								+ "| Request: " + request.toString() + "\n"
								+ "| Response Time: " + getTime(new Date()) + "\n"
								+ "| Response: " + response.toString() + "\n"
								+ "| ======END======";
						logger.info(stringInBox.printBox(fortumoLog, cbsLog));
						if (fortumoStarter.isDebug()) {
							fortumoStarter.logMonitor(stringInBox.printBox(fortumoLog, cbsLog));
						}
						break;
					case 1:
						restResponse.setCode(123);
						response.setResult(createResponseResult(restResponse));
						fortumoLog = "======FORTUMO COMMUNICATION====== \n"
								+ "| Request Time: " + startTime + "\n"
								+ "| Request: " + request.toString() + "\n"
								+ "| Response Time: " + getTime(new Date()) + "\n"
								+ "| Response: " + response.toString() + "\n"
								+ "| ======END======";
						logger.info(stringInBox.printBox(fortumoLog));
						if (fortumoStarter.isDebug()) {
							fortumoStarter.logMonitor(stringInBox.printBox(fortumoLog));
						}
						break;
					case 2:
						response = (RefundResponseV2) HazelcastClientStarter.hzClient.get(key);
						fortumoLog = "======FORTUMO COMMUNICATION====== \n"
								+ "| Request Time: " + startTime + "\n"
								+ "| Request: " + request.toString() + "\n"
								+ "| Response Time: " + getTime(new Date()) + "\n"
								+ "| Response: " + response.toString() + "\n"
								+ "| ======END======";
						logger.info(stringInBox.printBox(fortumoLog));
						if (fortumoStarter.isDebug()) {
							fortumoStarter.logMonitor(stringInBox.printBox("From Hazelcast Maps" + fortumoLog));
						}
						break;
					case 99:
						restResponse = cbsClient.fortumoRefundV2(httpServletRequest, authCorrelationId, authPurchaseTime, refundReason, amount);
						cbsLog = "======CBS CORE COMMUNICATION====== \n"
								+ "| Request Time: " + startTime + "\n"
								+ "| Request: " + request.toString() + "\n"
								+ "| Response Time: " + getTime(new Date()) + "\n"
								+ "| Response: " + restResponse.toString() + "\n"
								+ "| ======END======";
						response.setIssuerRefundId(StringUtil.nvl(restResponse.getParameter("cbs_transaction_id"), "null"));
						response.setResult(createResponseResult(restResponse));
						fortumoLog = "======FORTUMO COMMUNICATION====== \n"
								+ "| Request Time: " + startTime + "\n"
								+ "| Request: " + request.toString() + "\n"
								+ "| Response Time: " + getTime(new Date()) + "\n"
								+ "| Response: " + response.toString() + "\n"
								+ "| ======END======";
						logger.info(stringInBox.printBox(fortumoLog, cbsLog));
						if (fortumoStarter.isDebug()) {
							fortumoStarter.logMonitor("Could not connect to any cluster, Request was sent to CBS core...");
							fortumoStarter.logMonitor("Restart Hazelcast Client or Hazelcast Server please...");
							fortumoStarter.logMonitor(stringInBox.printBox(fortumoLog, cbsLog));
						}
						break;
				}
			} catch (Exception ex) {
				logger.error(stringInBox.printBox(ex.getMessage()));
				restResponse.setCode(7);
				response.setResult(createResponseResult(restResponse));
				fortumoLog = "======FORTUMO COMMUNICATION====== \n"
						+ "| Request Time: " + startTime + "\n"
						+ "| Request: " + request.toString() + "\n"
						+ "| Response Time: " + getTime(new Date()) + "\n"
						+ "| Response: " + response.toString() + "\n"
						+ "| ======END======";
				logger.info(stringInBox.printBox(fortumoLog));
				if (fortumoStarter.isDebug()) {
					fortumoStarter.logMonitor(stringInBox.printBox("Exception:", ex.getMessage(), fortumoLog));
				}
			}
		}
		return response;
	}

	//3.6 Mobifone to Fortumo
	@Path("/accountchange")
	@POST
	public AccountChangeResponse accountChange(AccountChangeRequest request, @Context HttpServletRequest httpServletRequest) {
		String startTime = getTime(new Date());
		AccountChangeResponse response = new AccountChangeResponse();
		RestResponse restResponse = new RestResponse();
		String fortumoLog = "";
		String cbsLog = "";
		this.msisdn = cbsClient.getMsisdn();
		try {
			String messageId = request.getMessageId();
			UserAccount account = request.getAccount();

			if (this.msisdn.equals(account.getAccount())) {
				restResponse.setCode(0);
				response.setResult(createResponseResult(restResponse));
			} else {
				restResponse = new RestResponse();
				restResponse.setCode(105);
				response.setResult(createResponseResult(restResponse));
			}
			fortumoLog = "======FORTUMO COMMUNICATION====== \n"
					+ "| Request Time: " + startTime + "\n"
					+ "| Request: " + request.toString() + "\n"
					+ "| Response Time: " + getTime(new Date()) + "\n"
					+ "| Response: " + response.toString() + "\n"
					+ "| ======END======";
			logger.info(stringInBox.printBox(fortumoLog));
			if (fortumoStarter.isDebug()) {
				fortumoStarter.logMonitor(stringInBox.printBox(fortumoLog));
			}
		} catch (Exception ex) {
			logger.error(stringInBox.printBox(ex.getMessage()));
			restResponse.setCode(7);
			response.setResult(createResponseResult(restResponse));
			fortumoLog = "======FORTUMO COMMUNICATION====== \n"
					+ "| Request Time: " + startTime + "\n"
					+ "| Request: " + request.toString() + "\n"
					+ "| Response Time: " + getTime(new Date()) + "\n"
					+ "| Response: " + response.toString() + "\n"
					+ "| ======END======";
			logger.info(stringInBox.printBox(fortumoLog));
			if (fortumoStarter.isDebug()) {
				fortumoStarter.logMonitor(stringInBox.printBox("Exception:", ex.getMessage(), fortumoLog));
			}
		}
		return response;
	}

	//3.8
	@Path("/subscriberLookup")
	@POST
	public SubscriberLookupResponse subscriberLookup(SubscriberLookupRequest request, @Context HttpServletRequest httpServletRequest) {
		String startTime = getTime(new Date());
		SubscriberLookupResponse response = new SubscriberLookupResponse();
		RestResponse restResponse = new RestResponse();
		String fortumoLog = "";
		String cbsLog = "";
		if (request.getAccount().getAccount() == null || !request.getAccount().getAccount().matches("^(84)[0-9]{9}$")) {
			restResponse.setCode(2);
			response.setResult(createResponseResult(restResponse));
			fortumoLog = "======FORTUMO COMMUNICATION====== \n"
					+ "| Request Time: " + startTime + "\n"
					+ "| Request: " + request.toString() + "\n"
					+ "| Response Time: " + getTime(new Date()) + "\n"
					+ "| Response: " + response.toString() + "\n"
					+ "| ======END======";
			logger.info(stringInBox.printBox(fortumoLog));
			if (fortumoStarter.isDebug()) {
				fortumoStarter.logMonitor(stringInBox.printBox(fortumoLog));
			}
		} else {
			try {
				String msisdn = request.getAccount().getAccount();
				restResponse = cbsClient.subscriberLookup(httpServletRequest, msisdn, null, "API");
				cbsLog = "======CBS CORE COMMUNICATION====== \n"
						+ "| Request Time: " + startTime + "\n"
						+ "| Request: " + request.toString() + "\n"
						+ "| Response Time: " + getTime(new Date()) + "\n"
						+ "| Response: " + restResponse.toString() + "\n"
						+ "| ======END======";

				response.setResult(createResponseResult(restResponse));
				fortumoLog = "======FORTUMO COMMUNICATION====== \n"
						+ "| Request Time: " + startTime + "\n"
						+ "| Request: " + request.toString() + "\n"
						+ "| Response Time: " + getTime(new Date()) + "\n"
						+ "| Response: " + response.toString() + "\n"
						+ "| ======END======";
				logger.info(stringInBox.printBox(fortumoLog, cbsLog));
				if (fortumoStarter.isDebug()) {
					fortumoStarter.logMonitor(stringInBox.printBox(fortumoLog, cbsLog));
				}
			} catch (Exception ex) {
				logger.error(stringInBox.printBox(ex.getMessage()));
				restResponse.setCode(7);
				response.setResult(createResponseResult(restResponse));
				fortumoLog = "======FORTUMO COMMUNICATION====== \n"
						+ "| Request Time: " + startTime + "\n"
						+ "| Request: " + request.toString() + "\n"
						+ "| Response Time: " + getTime(new Date()) + "\n"
						+ "| Response: " + response.toString() + "\n"
						+ "| ======END======";
				logger.info(stringInBox.printBox(fortumoLog));
				if (fortumoStarter.isDebug()) {
					fortumoStarter.logMonitor(stringInBox.printBox("Exception:", ex.getMessage(), fortumoLog));
				}
			}
		}
		return response;
	}

	//3.9
	@Path("/profile")
	@POST
	public AccountProfileResponse accountProfile(AccountProfileRequest request, @Context HttpServletRequest httpServletRequest) {
		String startTime = getTime(new Date());
		AccountProfileResponse response = new AccountProfileResponse();
		RestResponse restResponse = new RestResponse();
		String fortumoLog = "";
		String cbsLog = "";
		if (request.getAccount().getAccount() == null || !request.getAccount().getAccount().matches("^(84)[0-9]{9}$")) {
			restResponse.setCode(2);
			response.setResult(createResponseResult(restResponse));
			fortumoLog = "======FORTUMO COMMUNICATION====== \n"
					+ "| Request Time: " + startTime + "\n"
					+ "| Request: " + request.toString() + "\n"
					+ "| Response Time: " + getTime(new Date()) + "\n"
					+ "| Response: " + response.toString() + "\n"
					+ "| ======END======";
			logger.info(stringInBox.printBox(fortumoLog));
			if (fortumoStarter.isDebug()) {
				fortumoStarter.logMonitor(stringInBox.printBox(fortumoLog));
			}
		} else {
			try {
				String msisdn = request.getAccount().getAccount();
				restResponse = cbsClient.fortumoGetProfile(httpServletRequest, msisdn, null, "API");
				cbsLog = "======CBS CORE COMMUNICATION====== \n"
						+ "| Request Time: " + startTime + "\n"
						+ "| Request: " + request.toString() + "\n"
						+ "| Response Time: " + getTime(new Date()) + "\n"
						+ "| Response: " + restResponse.toString() + "\n"
						+ "| ======END======";

				if (restResponse.getCode() == 0) {
					List<com.telsoft.cbs.module.fortumo.domain.Attribute> attributes = new ArrayList<>();
					for (Attribute attribute : restResponse.getParameters()) {
						com.telsoft.cbs.module.fortumo.domain.Attribute att = new com.telsoft.cbs.module.fortumo.domain.Attribute(attribute.getKey(), attribute.getValue());
						attributes.add(att);
					}
					response.setAttributes(attributes);
				}
				response.setResult(createResponseResult(restResponse));
				fortumoLog = "======FORTUMO COMMUNICATION====== \n"
						+ "| Request Time: " + startTime + "\n"
						+ "| Request: " + request.toString() + "\n"
						+ "| Response Time: " + getTime(new Date()) + "\n"
						+ "| Response: " + response.toString() + "\n"
						+ "| ======END======";
				logger.info(stringInBox.printBox(fortumoLog, cbsLog));
				if (fortumoStarter.isDebug()) {
					fortumoStarter.logMonitor(stringInBox.printBox(fortumoLog, cbsLog));
				}
			} catch (Exception ex) {
				logger.error(stringInBox.printBox(ex.getMessage()));
				restResponse.setCode(7);
				response.setResult(createResponseResult(restResponse));
				fortumoLog = "======FORTUMO COMMUNICATION====== \n"
						+ "| Request Time: " + startTime + "\n"
						+ "| Request: " + request.toString() + "\n"
						+ "| Response Time: " + getTime(new Date()) + "\n"
						+ "| Response: " + response.toString() + "\n"
						+ "| ======END======";
				logger.info(stringInBox.printBox(fortumoLog));
				if (fortumoStarter.isDebug()) {
					fortumoStarter.logMonitor(stringInBox.printBox("Exception:", ex.getMessage(), fortumoLog));
				}
			}
		}
		return response;
	}

	//3.10
	@Path("/checkEligibility")
	@POST
	public CheckEligibilityResponse checkEligibility(CheckEligibilityRequest request, @Context HttpServletRequest httpServletRequest) {
		String startTime = getTime(new Date());
		CheckEligibilityResponse response = new CheckEligibilityResponse();
		RestResponse restResponse = new RestResponse();
		String fortumoLog = "";
		String cbsLog = "";
		if (request.getAccount().getAccount() == null || !request.getAccount().getAccount().matches("^(84)[0-9]{9}$")) {
			restResponse.setCode(2);
			response.setResult(createResponseResult(restResponse));
			fortumoLog = "======FORTUMO COMMUNICATION====== \n"
					+ "| Request Time: " + startTime + "\n"
					+ "| Request: " + request.toString() + "\n"
					+ "| Response Time: " + getTime(new Date()) + "\n"
					+ "| Response: " + response.toString() + "\n"
					+ "| ======END======";
			logger.info(stringInBox.printBox(fortumoLog));
			if (fortumoStarter.isDebug()) {
				fortumoStarter.logMonitor(stringInBox.printBox(fortumoLog));
			}
		} else {
			try {
				String msisdn = request.getAccount().getAccount();
				restResponse = cbsClient.checkEligibility(httpServletRequest, msisdn, null, "API");
				cbsLog = "======CBS CORE COMMUNICATION====== \n"
						+ "| Request Time: " + startTime + "\n"
						+ "| Request: " + request.toString() + "\n"
						+ "| Response Time: " + getTime(new Date()) + "\n"
						+ "| Response: " + restResponse.toString() + "\n"
						+ "| ======END======";

				response.setResult(createResponseResult(restResponse));
				fortumoLog = "======FORTUMO COMMUNICATION====== \n"
						+ "| Request Time: " + startTime + "\n"
						+ "| Request: " + request.toString() + "\n"
						+ "| Response Time: " + getTime(new Date()) + "\n"
						+ "| Response: " + response.toString() + "\n"
						+ "| ======END======";
				logger.info(stringInBox.printBox(fortumoLog, cbsLog));
				if (fortumoStarter.isDebug()) {
					fortumoStarter.logMonitor(stringInBox.printBox(fortumoLog, cbsLog));
				}
			} catch (Exception ex) {
				logger.error(stringInBox.printBox(ex.getMessage()));
				restResponse.setCode(7);
				response.setResult(createResponseResult(restResponse));
				fortumoLog = "======FORTUMO COMMUNICATION====== \n"
						+ "| Request Time: " + startTime + "\n"
						+ "| Request: " + request.toString() + "\n"
						+ "| Response Time: " + getTime(new Date()) + "\n"
						+ "| Response: " + response.toString() + "\n"
						+ "| ======END======";
				logger.info(stringInBox.printBox(fortumoLog));
				if (fortumoStarter.isDebug()) {
					fortumoStarter.logMonitor(stringInBox.printBox("Exception:", ex.getMessage(), fortumoLog));
				}
			}
		}
		return response;
	}

	//3.11
	@Path("/paymentStatus")
	@POST
	public PaymentStatusResponse paymentStatus(PaymentStatusRequest request, @Context HttpServletRequest httpServletRequest) {
		String startTime = getTime(new Date());
		PaymentStatusResponse response = new PaymentStatusResponse();
		RestResponse restResponse = new RestResponse();
		String fortumoLog = "";
		String cbsLog = "";
		if ((request.getPaymentRequestId() == null || request.getPurchaseTime() == null)
				|| !(request.getPurchaseTime().matches("^[0-9]{14}$") && request.getPaymentRequestId().matches("^[a-zA-Z0-9-_]{1,128}$"))
		) {
			restResponse.setCode(2);
			response.setResult(createResponseResult(restResponse));
			fortumoLog = "======FORTUMO COMMUNICATION====== \n"
					+ "| Request Time: " + startTime + "\n"
					+ "| Request: " + request.toString() + "\n"
					+ "| Response Time: " + getTime(new Date()) + "\n"
					+ "| Response: " + response.toString() + "\n"
					+ "| ======END======";
			logger.info(stringInBox.printBox(fortumoLog));
			if (fortumoStarter.isDebug()) {
				fortumoStarter.logMonitor(stringInBox.printBox(fortumoLog));
			}
		} else {
			try {
				String payment_transaction_id = request.getPaymentRequestId();
				String purchaseTime = request.getPurchaseTime();
				restResponse = cbsClient.fortumoPaymentStatus(httpServletRequest, payment_transaction_id, purchaseTime);
				cbsLog = "======CBS CORE COMMUNICATION====== \n"
						+ "| Request Time: " + startTime + "\n"
						+ "| Request: " + request.toString() + "\n"
						+ "| Response Time: " + getTime(new Date()) + "\n"
						+ "| Response: " + restResponse.toString() + "\n"
						+ "| ======END======";

				response.setResult(createResponseResult(restResponse));
				fortumoLog = "======FORTUMO COMMUNICATION====== \n"
						+ "| Request Time: " + startTime + "\n"
						+ "| Request: " + request.toString() + "\n"
						+ "| Response Time: " + getTime(new Date()) + "\n"
						+ "| Response: " + response.toString() + "\n"
						+ "| ======END======";
				logger.info(stringInBox.printBox(fortumoLog, cbsLog));
				if (fortumoStarter.isDebug()) {
					fortumoStarter.logMonitor(stringInBox.printBox(fortumoLog, cbsLog));
				}
			} catch (Exception ex) {
				logger.error(stringInBox.printBox(ex.getMessage()));
				restResponse.setCode(7);
				response.setResult(createResponseResult(restResponse));
				fortumoLog = "======FORTUMO COMMUNICATION====== \n"
						+ "| Request Time: " + startTime + "\n"
						+ "| Request: " + request.toString() + "\n"
						+ "| Response Time: " + getTime(new Date()) + "\n"
						+ "| Response: " + response.toString() + "\n"
						+ "| ======END======";
				logger.info(stringInBox.printBox(fortumoLog));
				if (fortumoStarter.isDebug()) {
					fortumoStarter.logMonitor(stringInBox.printBox("Exception:", ex.getMessage(), fortumoLog));
				}
			}
		}
		return response;
	}

	//3.12
	@Path("/refundStatus")
	@POST
	public RefundStatusResponse refundStatus(RefundStatusRequest request, @Context HttpServletRequest httpServletRequest) {
		String startTime = getTime(new Date());
		RefundStatusResponse response = new RefundStatusResponse();
		RestResponse restResponse = new RestResponse();
		String fortumoLog = "";
		String cbsLog = "";
		if (request.getRefundRequestId() == null || request.getRefundTime() == null
				|| !(request.getRefundRequestId().matches("^[a-zA-Z0-9-_]{1,128}$") && request.getRefundTime().matches("^[0-9]{14}$"))) {
			restResponse.setCode(2);
			response.setResult(createResponseResult(restResponse));
			fortumoLog = "======FORTUMO COMMUNICATION====== \n"
					+ "| Request Time: " + startTime + "\n"
					+ "| Request: " + request.toString() + "\n"
					+ "| Response Time: " + getTime(new Date()) + "\n"
					+ "| Response: " + response.toString() + "\n"
					+ "| ======END======";
			logger.info(stringInBox.printBox(fortumoLog));
			if (fortumoStarter.isDebug()) {
				fortumoStarter.logMonitor(stringInBox.printBox(fortumoLog));
			}
		} else {
			try {
				String refundRequestId = request.getRefundRequestId();
				String purchaseTime = request.getRefundTime();
				restResponse = cbsClient.fortumoRefundStatus(httpServletRequest, refundRequestId, purchaseTime);
				cbsLog = "======CBS CORE COMMUNICATION====== \n"
						+ "| Request Time: " + startTime + "\n"
						+ "| Request: " + request.toString() + "\n"
						+ "| Response Time: " + getTime(new Date()) + "\n"
						+ "| Response: " + restResponse.toString() + "\n"
						+ "| ======END======";

				response.setResult(createResponseResult(restResponse));
				fortumoLog = "======FORTUMO COMMUNICATION====== \n"
						+ "| Request Time: " + startTime + "\n"
						+ "| Request: " + request.toString() + "\n"
						+ "| Response Time: " + getTime(new Date()) + "\n"
						+ "| Response: " + response.toString() + "\n"
						+ "| ======END======";
				logger.info(stringInBox.printBox(fortumoLog, cbsLog));
				if (fortumoStarter.isDebug()) {
					fortumoStarter.logMonitor(stringInBox.printBox(fortumoLog, cbsLog));
				}
			} catch (Exception ex) {
				logger.error(stringInBox.printBox(ex.getMessage()));
				restResponse.setCode(7);
				response.setResult(createResponseResult(restResponse));
				fortumoLog = "======FORTUMO COMMUNICATION====== \n"
						+ "| Request Time: " + startTime + "\n"
						+ "| Request: " + request.toString() + "\n"
						+ "| Response Time: " + getTime(new Date()) + "\n"
						+ "| Response: " + response.toString() + "\n"
						+ "| ======END======";
				logger.info(stringInBox.printBox(fortumoLog));
				if (fortumoStarter.isDebug()) {
					fortumoStarter.logMonitor(stringInBox.printBox("Exception:", ex.getMessage(), fortumoLog));
				}
			}
		}
		return response;
	}

	//3.13
	@Path("/submitMT")
	@POST
	public SubmitMTResponse submitMT(SubmitMTRequest request, @Context HttpServletRequest httpServletRequest) {
		String startTime = getTime(new Date());
		SubmitMTResponse response = new SubmitMTResponse();
		RestResponse restResponse = new RestResponse();
		String fortumoLog = "";
		String cbsLog = "";
		if (request.getAccount() == null
				|| request.getMessage() == null
				|| request.getMessageId() == null
				|| request.getOriginator() == null
				|| request.getValidity() == null) {
			restResponse.setCode(2);
			response.setResult(createResponseResult(restResponse));
			fortumoLog = "======FORTUMO COMMUNICATION====== \n"
					+ "| Request Time: " + startTime + "\n"
					+ "| Request: " + request.toString() + "\n"
					+ "| Response Time: " + getTime(new Date()) + "\n"
					+ "| Response: " + response.toString() + "\n"
					+ "| ======END======";
			logger.info(stringInBox.printBox(fortumoLog));
			if (fortumoStarter.isDebug()) {
				fortumoStarter.logMonitor(stringInBox.printBox(fortumoLog));
			}
		} else {
			try {
				String messageId = request.getMessageId();
				String message = request.getMessage();
				String originator = request.getOriginator();
				String msisdn = request.getAccount().getAccount();
				Integer validity = request.getValidity();

				restResponse = cbsClient.submitMT(httpServletRequest, msisdn, message, messageId, originator, validity);
				cbsLog = "======CBS CORE COMMUNICATION====== \n"
						+ "| Request Time: " + startTime + "\n"
						+ "| Request: " + request.toString() + "\n"
						+ "| Response Time: " + getTime(new Date()) + "\n"
						+ "| Response: " + restResponse.toString() + "\n"
						+ "| ======END======";

				response.setCorrelatorId(restResponse.getParameter("correlatorId"));
				response.setResult(createResponseResult(restResponse));
				fortumoLog = "======FORTUMO COMMUNICATION====== \n"
						+ "| Request Time: " + startTime + "\n"
						+ "| Request: " + request.toString() + "\n"
						+ "| Response Time: " + getTime(new Date()) + "\n"
						+ "| Response: " + response.toString() + "\n"
						+ "| ======END======";
				logger.info(stringInBox.printBox(fortumoLog, cbsLog));
				if (fortumoStarter.isDebug()) {
					fortumoStarter.logMonitor(stringInBox.printBox(fortumoLog, cbsLog));
				}
			} catch (Exception ex) {
				logger.error(stringInBox.printBox(ex.getMessage()));
				restResponse.setCode(7);
				response.setResult(createResponseResult(restResponse));
				fortumoLog = "======FORTUMO COMMUNICATION====== \n"
						+ "| Request Time: " + startTime + "\n"
						+ "| Request: " + request.toString() + "\n"
						+ "| Response Time: " + getTime(new Date()) + "\n"
						+ "| Response: " + response.toString() + "\n"
						+ "| ======END======";
				logger.info(stringInBox.printBox(fortumoLog));
				if (fortumoStarter.isDebug()) {
					fortumoStarter.logMonitor(stringInBox.printBox("Exception:", ex.getMessage() + fortumoLog));
				}
			}
		}
		return response;
	}

	//region Features are not implemented
	@Path("/generateOtp")
	@POST
	public GenerateOTPResponse generateOtp(GenerateOTPRequest request, @Context HttpServletRequest httpServletRequest) throws FortumoException {
		throw new FortumoException(request, "Feature is not implemented");
	}

	@Path("/validateOtp")
	@POST
	public ValidateOTPResponse validateOtp(ValidateOTPRequest request, @Context HttpServletRequest httpServletRequest) throws FortumoException {
		throw new FortumoException(request, "Feature is not implemented");
	}

	@Path("/getAcr")
	@POST
	public GetACRResponse getAcr(GetACRRequest request, @Context HttpServletRequest httpServletRequest) throws FortumoException {
		throw new FortumoException(request, "Feature is not implemented");
	}

	@Path("/getMsisdn")
	@POST
	public GetMSISDNResponse getMsisdn(GetMSISDNRequest request, @Context HttpServletRequest httpServletRequest) throws FortumoException {
		throw new FortumoException(request, "Feature is not implemented");
	}

	public String getTime(Date date) {
		date = Calendar.getInstance().getTime();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		String strDate = dateFormat.format(date);
		return strDate;
	}
}
