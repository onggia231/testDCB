package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.*;
import com.telsoft.cbs.utils.CbsLog;
import com.telsoft.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.annotations.Component;
import org.apache.commons.lang3.time.DateUtils;
import telsoft.gateway.core.log.MessageContext;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

/**
 * The Check Input Component
 * <p>
 */

@Component("cbs-check-input")
@UriEndpoint(
		firstVersion = "1.0.0",
		scheme = "cbs-check-input",
		title = "Check Input Parameters",
		syntax = "cbs-check-input:",
		label = "cbs,endpoint",
		producerOnly = true,
		generateConfigurer = false
)
@Slf4j
public class CheckInputComponent extends ProcessorComponent {
	@UriParam(name = "min-isdn-length", description = "Min length of isdn number", displayName = "Isdn Min Length")
	int min_isdn_length;

	@UriParam(name = "max-isdn-length", description = "Max length of isdn number", displayName = "Isdn Max Length")
	int max_isdn_length;

	@UriParam(name = "PurchaseTimeCheckLimit", description = "Enable or Disable Check limit Difference between current time and Purchase Time ", displayName = "CheckLimitPurchaseTime")
	Boolean bPurchaseTimeCheckLimit;

	@UriParam(name = "PurchaseTimeCheckMaxDeltaHour", description = "Max Hour Check Purchase Time", displayName = "Max Hour Check Purchase Time")
	int iPurchaseTimeCheckMaxDeltaHour;

	@Override
	public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
		min_isdn_length = Integer.parseInt(StringUtil.nvl((String) parameters.get("min_isdn_length"), "0"));
		max_isdn_length = Integer.parseInt(StringUtil.nvl((String) parameters.get("max_isdn_length"), "0"));
		bPurchaseTimeCheckLimit = Boolean.valueOf(StringUtil.nvl((String) parameters.get("PurchaseTimeCheckLimit"), "false"));
		iPurchaseTimeCheckMaxDeltaHour = Integer.parseInt(StringUtil.nvl((String) parameters.get("PurchaseTimeCheckMaxDeltaHour"), "0"));


		CBCommand command = (CBCommand) exchange.getProperty(CbsContansts.COMMAND);
		switch (command) {
			case CHARGE: {
//                checkClientRequestId(request, exchange, parameters, messageContext);
//                checkMsisdn(request, exchange, parameters, messageContext);
				checkStore(request, exchange, parameters, messageContext);
				checkAmount(request, exchange, parameters, messageContext);
//                checkClientTransId(request, exchange, parameters, messageContext);
                checkContent(request, exchange, parameters, messageContext);
				checkPurchaseTime(request, exchange, parameters, messageContext, true);
				break;
			}
            case CAPTURE: {
                checkStore(request, exchange, parameters, messageContext);
                checkPurchaseTime(request, exchange, parameters, messageContext, false);
                break;
            }
            case REVERSE: {
                checkStore(request, exchange, parameters, messageContext);
                checkPurchaseTime(request, exchange, parameters, messageContext, false);
                break;
            }
            case REFUND: {
                checkStore(request, exchange, parameters, messageContext);
                checkPurchaseTime(request, exchange, parameters, messageContext, false);
                break;
            }
			case GET_PROFILE: {
//                checkMsisdn(request, exchange, parameters, messageContext);
//                checkStore(request, exchange, parameters, messageContext);
				break;
			}
			case PAYMENT_STATUS: {
//                checkStore(request, exchange, parameters, messageContext);
//                checkPaymentRequestId(request, exchange, parameters, messageContext);
				break;
			}
			case REGISTER_PROFILE: {
//                checkClientTransId(request, exchange, parameters, messageContext);
//                checkMsisdn(request, exchange, parameters, messageContext);
//                checkStore(request, exchange, parameters, messageContext);
				break;
			}
			case SUBMIT_MT: {
//                checkClientTransId(request, exchange, parameters, messageContext);
//                checkMsisdn(request, exchange, parameters, messageContext);
//                checkStore(request, exchange, parameters, messageContext);
				break;
			}
			case UNREGISTER_PROFILE: {
//                checkClientTransId(request, exchange, parameters, messageContext);
//                checkMsisdn(request, exchange, parameters, messageContext);
//                checkStore(request, exchange, parameters, messageContext);
				break;
			}
		}
	}

	/**
	 * @param request
	 * @param exchange
	 * @param parameters
	 * @param messageContext
	 * @throws CBException
	 */
	private void checkStore(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
		CBStore store = (CBStore) messageContext.getProperty(CbsContansts.STORE);
		if (store == null) {
			CbsLog.error(messageContext, "CheckInputComponent.checkStore", CBCode.STORE_UNKNOWN, "store_code", messageContext.getProperty(CbsContansts.STORE_CODE));
			throw new CBException(CBCode.STORE_UNKNOWN);
		}
	}

	/**
	 * @param request
	 * @param exchange
	 * @param parameters
	 * @param messageContext
	 * @throws CBException
	 */
	private void checkClientTransId(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
		String client_transaction_id = messageContext.getOrigin();

		if (client_transaction_id == null) {
			CbsLog.error(messageContext, "CheckInputComponent.checkClientTransId", CBCode.PARAMETER_ERROR, "client_transaction_id", messageContext.getOrigin());
			throw new CBException(CBCode.PARAMETER_ERROR);
		}
	}

	/**
	 * @param request
	 * @param exchange
	 * @param parameters
	 * @param messageContext
	 * @throws CBException
	 */
	private void checkClientRequestId(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
		Object requestId = messageContext.getProperty(CbsContansts.REQUEST_ID);

		if (requestId == null) {
			CbsLog.error(messageContext, "CheckInputComponent.checkClientRequestId", CBCode.PARAMETER_ERROR, "requestId", requestId);
			throw new CBException(CBCode.PARAMETER_ERROR);
		}
	}

	/**
	 * @param request
	 * @param exchange
	 * @param parameters
	 * @param messageContext
	 * @throws CBException
	 */
	private void checkPaymentRequestId(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
		Object requestId = messageContext.getProperty(CbsContansts.PAYMENT_REQUEST_ID);

		if (requestId == null) {
			CbsLog.error(messageContext, "CheckInputComponent.checkPaymentRequestId", CBCode.PARAMETER_ERROR, "requestId", requestId);
			throw new CBException(CBCode.PARAMETER_ERROR);
		}
	}

	/**
	 * @param request
	 * @param exchange
	 * @param parameters
	 * @param messageContext
	 * @throws CBException
	 */
	private void checkPaymentTransId(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
		String payment_transaction_id = (String) messageContext.getProperty(CbsContansts.PAYMENT_TRANSACTION_ID);

		if (payment_transaction_id == null) {
			CbsLog.error(messageContext, "CheckInputComponent.checkPaymentTransId", CBCode.PARAMETER_ERROR, "payment_transaction_id", payment_transaction_id);
			throw new CBException(CBCode.PARAMETER_ERROR);
		}

		if (messageContext.getIsdn() == null ||
				(messageContext.getProperty(CbsContansts.PAYMENT_COMMAND) == null) ||
				(messageContext.getProperty(CbsContansts.PAYMENT_DATE) == null)) {
			CbsLog.error(messageContext, "CheckInputComponent.checkPaymentTransId", CBCode.PARAMETER_ERROR, "Cannot decode payment_transaction_id", null);
			throw new CBException(CBCode.PARAMETER_ERROR);
		}
	}


	/**
	 * @param request
	 * @param exchange
	 * @param parameters
	 * @param messageContext
	 * @throws CBException
	 */
	private void checkAmount(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
		Object amount = messageContext.getProperty(MessageContext.AMOUNT);

		if (amount == null) {
			CbsLog.error(messageContext, "CheckInputComponent.checkAmount", CBCode.PARAMETER_ERROR, "amount", amount);
			throw new CBException(CBCode.PARAMETER_ERROR);
		}
	}

	/**
	 * @param request
	 * @param exchange
	 * @param parameters
	 * @param messageContext
	 * @throws CBException
	 */
	private void checkContent(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
		Object content = messageContext.getProperty(CbsContansts.CONTENT_DESCRIPTION);

		if (content == null) {
			CbsLog.error(messageContext, "CheckInputComponent.checkContent", CBCode.PARAMETER_ERROR, "content", content);
			throw new CBException(CBCode.PARAMETER_ERROR);
		}
	}

	/**
	 * @param request
	 * @param exchange
	 * @param parameters
	 * @param messageContext
	 * @throws CBException
	 */
	private void checkMsisdn(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
		int isdnMinLength = Integer.parseInt((String) parameters.get("min-isdn-length"));
		int isdnMaxLength = Integer.parseInt((String) parameters.get("max-isdn-length"));

		String msisdn = messageContext.getIsdn();

		// missing msisdn parameter
		if (msisdn == null) {
			CbsLog.error(messageContext, "CheckInputComponent.checkMsisdn", CBCode.PARAMETER_ERROR, "msisdn", msisdn);
			throw new CBException(CBCode.PARAMETER_ERROR);
		}

		// check length
		if (msisdn.length() < isdnMinLength || msisdn.length() > isdnMaxLength) {
			CbsLog.error(messageContext, "CheckInputComponent.checkMsisdn", CBCode.USER_UNKNOWN, "msisdn", msisdn);
			throw new CBException(CBCode.USER_UNKNOWN);
		}

		// check subscriber number is in mobifone prefix ranges
/*        CacheService cacheService = this.getService(CacheService.class);
        if (cacheService != null) {
            Map<String, String> isdnPrefixes = cacheService.getMap(CbsContansts.ISDN_LIST);
        }*/

		// if subscriber number is not in mobifone prefix ranges, subscriber number must be in Mobifone MNP subscriber list.
	}

	private void checkPurchaseTime(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext, boolean requiredPurchaseTime) throws CBException {
		String strPurchaseTime = request.get(CbsContansts.PURCHASE_TIME);
		if (strPurchaseTime == null || strPurchaseTime.isEmpty()) {
			if (requiredPurchaseTime) {
				CbsLog.error(messageContext, "CheckInputComponent.checkPurchaseTime", CBCode.PARAMETER_ERROR, "strPurchaseTime", strPurchaseTime);
				throw new CBException(CBCode.PARAMETER_ERROR);
			} else {
				return;
			}
		}
		// parse date
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(CbsContansts.PURCHASE_TIME_FORMAT);
			sdf.setLenient(false);
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			Date purchaseTime = sdf.parse(strPurchaseTime);

			//compare date
			Date curentDate = new Date();
			if (purchaseTime.after(curentDate)) {
				throw new CBException(CBCode.PARAMETER_ERROR);
			}
			if (bPurchaseTimeCheckLimit && purchaseTime.before(DateUtils.addHours(curentDate, -iPurchaseTimeCheckMaxDeltaHour))) {
				throw new CBException(CBCode.PARAMETER_ERROR);
			}

		} catch (Exception ex) {
			CbsLog.error(messageContext, "CheckInputComponent.checkPurchaseTime", CBCode.PARAMETER_ERROR, "strPurchaseTime", strPurchaseTime);
			throw new CBException(CBCode.PARAMETER_ERROR);
		}

	}
}
