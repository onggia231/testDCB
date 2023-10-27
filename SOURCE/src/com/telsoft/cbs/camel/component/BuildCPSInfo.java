package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.CBCode;
import com.telsoft.cbs.domain.CBException;
import com.telsoft.cbs.domain.CBRequest;
import com.telsoft.cbs.domain.CBStore;
import com.telsoft.cbs.utils.CbsLog;
import com.telsoft.cbs.utils.CbsUtils;
import com.telsoft.cbs.utils.DbUtils;
import com.telsoft.util.StringUtil;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.annotations.Component;
import org.apache.commons.lang3.StringUtils;
import telsoft.gateway.core.log.MessageContext;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Build cps information for internal reconciliation
 * <p>
 */

@Component("cbs-build-cps-info")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-build-cps-info",
        title = "Build cps information for internal reconciliation",
        syntax = "cbs-build-cps-info:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)

public class BuildCPSInfo extends ProcessorComponent {

    @UriParam(name = "maxLengthCorrelationId", displayName = "Max Length of CorrelationId", description = "Max Length of CorrelationId")
    int maxLengthCorrelationId;


    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        try {
            int maxLengthCorrelationId = Integer.parseInt(StringUtil.nvl((String) parameters.get("maxLengthCorrelationId"), "50"));

            Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
            String storeTransactionID = (String) mapFullRequest.get(CbsContansts.STORE_TRANSACTION_ID);
            String storeRequestTime = (new SimpleDateFormat(CbsContansts.DATE_FORMAT_JAVA_SECOND)).format(mapFullRequest.get(CbsContansts.STORE_START_TIME));
            long amount = messageContext.getAmount();

            //Content ID (Sequence)
            if (mapFullRequest.get(CbsContansts.CPS_CONTENTID) == null || StringUtils.isEmpty((String) mapFullRequest.get(CbsContansts.CPS_CONTENTID))) {
                try (Connection connection = getManager().getConnection()) {
                    long lcontentId = DbUtils.getSequenceValue(connection, CbsContansts.CPS_TRANSACTION_SEQUENCE);
                    String seq = CbsUtils.formatLengthString(Long.toString(lcontentId), 10, true, "0");
                    CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.CPS_CONTENTID, seq);
                } catch (Exception e) {
//                throw new CBException(CBCode.INTERNAL_SERVER_ERROR);
                    throw e;
                }
            }

            //CPS_B_ISDN
            String contentType = StringUtil.nvl((String) mapFullRequest.get(CbsContansts.CPS_B_ISDN), "");


            //Amount full tax
            //Get tax config from store
            CBStore store = (CBStore) messageContext.getProperty(CbsContansts.STORE);
            int taxRate = 0;
            try {
                taxRate = Integer.parseInt(StringUtil.nvl(store.getAttributes().getProperty(CbsContansts.TAX_CONFIG_KEY), "0"));
            } catch (Exception ignored) {

            }
            Long amountFullTax = amount;
            String tax = "0";
            if (taxRate != 0) {
                try {
                    double dAmount = amount; //String.valueOf(Math.round(Double.parseDouble(amount)))
                    amountFullTax = Math.round(dAmount + dAmount * taxRate / 100.0);
                    tax = String.valueOf(Math.round(dAmount * taxRate / 100.0));
                } catch (Exception ignored) {

                }
            }
            exchange.setProperty(CbsContansts.AMOUNT_FULL_TAX, amountFullTax);
            CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.AMOUNT_FULL_TAX, amountFullTax);
            exchange.setProperty(CbsContansts.TAX, tax);
            CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.TAX, tax);

            //Extra info
            String extra_info = CbsUtils.formatLengthString(storeTransactionID, maxLengthCorrelationId, false, "") +
                    "-" + storeRequestTime +
                    "-" + amountFullTax +
                    "-" + CbsUtils.formatLengthString(contentType, 10, true, "0");
            CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.CPS_EXTRA_INFO, extra_info);

            //CPS short_code
            exchange.setProperty(CbsContansts.CPS_SHORTCODE, StringUtil.nvl(store.getAttributes().getProperty(CbsContansts.CPS_PARAM_SHORT_CODE), ""));
            CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.CPS_SHORTCODE, StringUtil.nvl(store.getAttributes().getProperty(CbsContansts.CPS_PARAM_SHORT_CODE), ""));

            //CPS CPS_SPID
            exchange.setProperty(CbsContansts.CPS_SPID, StringUtil.nvl(store.getAttributes().getProperty("sp"), ""));
            CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.CPS_SPID, StringUtil.nvl(store.getAttributes().getProperty("sp"), ""));

            //CPS short_code
            exchange.setProperty(CbsContansts.CPS_CPID, StringUtil.nvl(store.getAttributes().getProperty("cp"), ""));
            CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.CPS_CPID, StringUtil.nvl(store.getAttributes().getProperty("cp"), ""));

            //charge account
            CbsContansts.CPS_SUB_PAY_TYPE payType = (CbsContansts.CPS_SUB_PAY_TYPE) mapFullRequest.get(CbsContansts.SUB_PAY_TYPE);
            if (CbsContansts.CPS_SUB_PAY_TYPE.PREPAID == payType) {
                exchange.setProperty(CbsContansts.CHARGE_ACCOUNT, StringUtil.nvl(store.getAttributes().getProperty("charge_account_prepaid"), ""));
                CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.CHARGE_ACCOUNT, StringUtil.nvl(store.getAttributes().getProperty("charge_account_prepaid"), ""));
            } else if (CbsContansts.CPS_SUB_PAY_TYPE.POSTPAID == payType) {
                exchange.setProperty(CbsContansts.CHARGE_ACCOUNT, StringUtil.nvl(store.getAttributes().getProperty("charge_account_postpaid"), ""));
                CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest, CbsContansts.CHARGE_ACCOUNT, StringUtil.nvl(store.getAttributes().getProperty("charge_account_postpaid"), ""));
            }

        }catch (CBException e) {
            throw e;
        } catch (Exception ex){
            CbsLog.error(messageContext, "BuildCPSInfo", CBCode.INTERNAL_SERVER_ERROR, "", ex.getMessage());
            throw new CBException(CBCode.INTERNAL_SERVER_ERROR, ex);
        }
    }






}
