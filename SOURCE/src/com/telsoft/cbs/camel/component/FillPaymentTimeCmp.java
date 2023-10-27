package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.CBCode;
import com.telsoft.cbs.domain.CBException;
import com.telsoft.cbs.domain.CBRequest;
import com.telsoft.cbs.utils.CbsLog;
import com.telsoft.cbs.utils.CbsUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.annotations.Component;
import org.apache.commons.lang3.StringUtils;
import telsoft.gateway.core.log.MessageContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Fill paymentTime from mapping table
 * <p>
 */

@Slf4j
@Component("cbs-fill-payment-time")
@UriEndpoint(
		firstVersion = "1.0.0",
		scheme = "cbs-fill-payment-time",
		title = "Get paymentTime by storeTransactionID",
		syntax = "cbs-fill-payment-time:",
		label = "cbs,endpoint",
		producerOnly = true,
		generateConfigurer = false
)

public class FillPaymentTimeCmp extends ProcessorComponent {

	@UriParam(name = "mappingTableName", displayName = "MappingTableName", description = "Mapping Table Name")
	String mappingTableName;

	public static String escapeTableNameSql(String str) {
		if (str == null) {
			return null;
		}
		return StringUtils.replace(str, "'", "''");
	}

	@Override
	public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {

		Date request_date = (Date) messageContext.getProperty(CbsContansts.PAYMENT_DATE);
		if(request_date == null) {

			String storeTransactionId = (String) messageContext.getProperty(CbsContansts.STORE_TRANSACTION_ID);
			String mappingTableName = escapeTableNameSql((String) parameters.get("mappingTableName"));
			String sql = "select request_time from " + mappingTableName + " where store_transaction_id = ?";

			Date paymentTime = null;
			try (Connection con = getManager().getConnection()) {
				try (PreparedStatement stmt = con.prepareStatement(sql)) {
					stmt.setString(1, storeTransactionId);
					try(ResultSet rs = stmt.executeQuery()){
						if(rs.next()){
							java.sql.Date requestTime = rs.getDate("request_time");
							if(requestTime != null) {
								paymentTime = new Date(requestTime.getTime());

								messageContext.setProperty(CbsContansts.PAYMENT_DATE, paymentTime);
								Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
								CbsUtils.putValueIntoMapCheckNullValue(mapFullRequest,CbsContansts.PAYMENT_DATE,paymentTime);//Payment_date

							}
						}
					}

				}
			} catch (Exception e) {
				CbsLog.error(messageContext, "FillPaymentTimeCmp", CBCode.INTERNAL_SERVER_ERROR, "", e.getMessage());
				throw new CBException(CBCode.INTERNAL_SERVER_ERROR, e);
			}
			if(paymentTime == null){
				throw new CBException(CBCode.TRANSACTION_NOT_FOUND);
			}

		}


	}
}
