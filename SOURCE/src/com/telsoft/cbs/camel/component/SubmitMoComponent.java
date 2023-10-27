package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.domain.*;
import com.telsoft.cbs.module.cbsrest.domain.Attribute;
import com.telsoft.cbs.module.cbsrest.domain.CBCommand;
import com.telsoft.cbs.module.cbsrest.domain.RestRequest;
import com.telsoft.cbs.module.cbsrest.domain.RestResponse;
import com.telsoft.cbs.utils.CbsLog;
import com.telsoft.cbs.utils.DbUtils;
import com.telsoft.database.Database;
import com.telsoft.util.StringUtil;
import org.apache.axis.utils.StringUtils;
import org.apache.camel.Exchange;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.annotations.Component;
import org.bouncycastle.jcajce.provider.symmetric.AES;
import telsoft.gateway.core.log.MessageContext;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.telsoft.cbs.modules.selfcare.ulti.VasgateUlti.convertPhone;

/**
 * Submit Mo
 * <p>
 */

@Component("cbs-submit-mo")
@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "cbs-submit-mo",
        title = "Submit Mo",
        syntax = "cbs-submit-mo:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)

public class SubmitMoComponent extends ProcessorComponent {

    @UriParam(name = "channelType", displayName = "ChannelType", description = "SYS, API, SMS, WAP, WEB, APP")
    CHANNEL_TYPE channelType;

    @UriParam(name = "listCode", displayName = "ListCode", description = "list_name in cb_list, separate by comma")
    String listCode;

    @UriParam(name = "dateFormat", displayName = "DateFormat", description = "Format for date field")
    String dateFormat;

    @UriParam(name = "attribute", displayName = "Store attribute", description = "url to call api")
    String attribute;


    @UriParam(name = "xForward", displayName = "xForward", description = "xForward")
    String xForward;

    @Override
    public void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException {
        CHANNEL_TYPE channelType = CHANNEL_TYPE.valueOf((String) parameters.get("channelType"));
        String listCode = (String) parameters.get("listCode");
        String dateFormat = (String) parameters.get("dateFormat");
        String xForward = (String) parameters.get("xForward");
        String attribute = (String) parameters.get("attribute");

        Map mapFullRequest = (ConcurrentHashMap) messageContext.getProperty(CbsContansts.MAP_FULL_REQUEST);
        Long subId = 0L;
        Long storeId = 0L;
        String subType = null;
        ResultSet resultSet = null;
        String storeCode = (String) messageContext.getProperty(CbsContansts.STORE_CODE);
        String isdn = convertPhone((String) exchange.getProperty(CbsContansts.MSISDN));
        String transactionId = (String) exchange.getProperty(CbsContansts.TRANSACTION_ID);
        String shortCode = (String) messageContext.getProperty(CbsContansts.SHORT_CODE);
        String content = (String) exchange.getProperty(CbsContansts.CONTENT_DESCRIPTION);
        CbsContansts.CPS_SUB_PAY_TYPE payType = (CbsContansts.CPS_SUB_PAY_TYPE) mapFullRequest.get(CbsContansts.SUB_PAY_TYPE);

        CBStore store = (CBStore) messageContext.getProperty(CbsContansts.STORE);
        String url = store.getAttributes().getProperty(attribute);
        if (url == null) {
            throw new CBException(CBCode.STORE_ATTRIBUTE_ERROR);
        }
//        String payType="PREPAID";
//        if (!sendRequestFortumo(transactionId, content, isdn, shortCode, url, "10.10.10.1", storeCode)) {
//            throw new CBException(CBCode.AUTHENTICATION_ERROR);
//        }
//        if (isdn == null)
        if (isdn != null) {
            if (payType == null) {
                //lỗi ko lấy đc thông tin thuê bao
	            throw new CBException(CBCode.USER_UNKNOWN);
            }


	        try (Connection connection = getManager().getConnection()) {
		        try {
			        connection.setAutoCommit(false);
			        String sql = "select id,sub_type from cb_subscriber where isdn =? and status=?";
			        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
				        stmt.setString(1, isdn);
				        stmt.setInt(2, 1);
				        resultSet = stmt.executeQuery();
				        if (resultSet.next()) {
					        subId = resultSet.getLong("id");
					        subType = StringUtil.nvl(resultSet.getString("sub_type"), "");
				        }
			        } finally {
				        Database.closeObject(resultSet);
			        }

			        sql = "select id from cb_store where store_code = ?";
			        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
				        stmt.setString(1, storeCode);
				        resultSet = stmt.executeQuery();
				        if (resultSet.next()) {
					        storeId = resultSet.getLong("id");
				        }
			        } finally {
				        Database.closeObject(resultSet);
			        }

			        if (storeId == 0L)
				        throw new CBException(CBCode.STORE_UNKNOWN);
			        if (subId == 0L) {
				        subId = DbUtils.getSequenceValue(connection, "SEQ_SUBSCRIBER");
				        sql = "insert into cb_subscriber(id,isdn,reg_date,sub_type,status,vasgate_status) values (?,?,sysdate,?,?,?)";
				        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
					        stmt.setLong(1, subId);
					        stmt.setString(2, isdn);
					        stmt.setObject(3, payType.name());
					        stmt.setInt(4, 1);
					        stmt.setInt(5, 4);
					        stmt.executeUpdate();

					        InsertSubStore(subId, storeId, connection);
				        } catch (Exception e) {
					        throw e;
				        }

			        } else { //tim thay cb_subscriber
				        //update neu doi loai thue bao prepaid <-> postpaid
				        if (!subType.equalsIgnoreCase(payType.name())) {
					        removeMSISDNOutOfBlacklist(listCode, isdn, connection);
					        sql = "update cb_subscriber set sub_type=?,vasgate_status=? where id=?";
					        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
						        stmt.setString(1, payType.name());
						        stmt.setInt(2, 4);
						        stmt.setLong(3, subId);
						        stmt.executeUpdate();
					        } catch (Exception e) {
						        throw e;
					        }
				        }
				        // tim xem thue bao da dang ky store chua
				        sql = "select * from cb_sub_store where sub_id=? and store_id=?";
				        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
					        stmt.setLong(1, subId);
					        stmt.setLong(2, storeId);
					        resultSet = stmt.executeQuery();
					        if (resultSet.next()) {//neu co roi va doi loai thue bao thi update lai han muc tieu dung
						        if (!subType.equalsIgnoreCase(payType.name())) {
							        UpdateSubStore(subId, storeId, connection);
						        }
					        } else {//neu chua dang ky thi dang ky moi
						        InsertSubStore(subId, storeId, connection);
					        }
				        } catch (Exception e) {
					        throw e;
				        } finally {
					        Database.closeObject(resultSet);
				        }

			        }

			        //call fortumo api
			        sendRequestFortumo(transactionId, content, isdn, shortCode, url, xForward, storeCode);
			        connection.commit();
		        } catch (Exception ex) {
			        connection.rollback();
			        throw ex;
		        } finally {
			        connection.setAutoCommit(true);
		        }
	        } catch (CBException e) {
		        log.error("Submit MO failed", e);
		        CbsLog.error(messageContext, "SubmitMoComponent", e.getCode(), "Submit MO failed", e.getMessage());
		        throw e;
	        } catch (Exception e) {
		        log.error("Submit MO failed", e);
		        CbsLog.error(messageContext, "SubmitMoComponent", CBCode.INTERNAL_SERVER_ERROR, "Submit MO failed", e.getMessage());
		        throw new CBException(CBCode.INTERNAL_SERVER_ERROR, e);
	        }
        }
    }

	private void removeMSISDNOutOfBlacklist(String listCode, String isdn, Connection connection) throws SQLException {
    	if(StringUtils.isEmpty(listCode) || StringUtils.isEmpty(isdn)){
    		return;
	    }
		String sql;
		//xoa blacklist
		String listCodeSQL = "('" + listCode.replaceAll(",", "','") + "')";
		sql = "delete from cb_item_isdn where list_id in (select list_id from cb_list where list_name in ?) and isdn=?";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, listCodeSQL);
			stmt.setString(2, isdn);
			stmt.executeUpdate();
		} catch (Exception e) {
			throw e;
		}
	}

	private void InsertSubStore(Long subId, Long storeId, Connection connection) throws SQLException {
		String sql = "insert into cb_sub_store" +
				"(sub_id,store_id,reg_date," + //1 2
				"total_year,total_month,total_week,total_day," + //  34 5 6
				"start_year,start_month,start_week,start_day," +
				"reserved_charge) values" + //7
				"(?,?,sysdate," +
                "?,?,?,?," +
                "trunc(sysdate,'y'),trunc(sysdate,'mm'),trunc(sysdate,'iw'),trunc(sysdate,'dd')," +
                "?) ";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, subId);
            stmt.setLong(2, storeId);
            stmt.setInt(3, 0);
            stmt.setInt(4, 0);
            stmt.setInt(5, 0);
            stmt.setInt(6, 0);
            stmt.setInt(7, 0);
            stmt.executeUpdate();
        }
    }

    private void UpdateSubStore(Long subId, Long storeId, Connection connection) throws SQLException {
        String sql = "update cb_sub_store set " +
                "yearly_limit = ?,monthly_limit = ?, " +
                "weekly_limit = ?, " +
                "daily_limit = ?,trans_limit = ?, " +
                "limit_profile_id=? " +
                "where sub_id =? and store_id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, null);
            stmt.setObject(2, null);
            stmt.setObject(3, null);
            stmt.setObject(4, null);
            stmt.setObject(5, null);
            stmt.setObject(6, null);
            stmt.setLong(7, subId);
            stmt.setLong(8, storeId);
            stmt.executeUpdate();
        }
    }

    private String sendRequestFortumo(String transactionId, String content, String msisdn, String shortCode, String url, String xForward, String storeCode) throws CBException {

        RestRequest request = new RestRequest();

        request.setName(CBCommand.SUBMIT_MO);
        request.setStore_code(storeCode);
        request.setTransaction_id(transactionId);
        request.setIsdn(msisdn);
        request.getParameters().add(new Attribute("short_code", shortCode));
        request.getParameters().add(new Attribute("content", content));


	    Client client = null;
        try {
	        client = ClientBuilder.newClient();
            Invocation.Builder invocationBuilder;
//            WebTarget target = client.target("http://10.11.10.143:8005/fortumoclient/fortumoclient");
            WebTarget target = client.target(url);
            invocationBuilder = target.request(MediaType.APPLICATION_XML);
            if (xForward != null) {
                invocationBuilder.header("X-Forward-For", xForward);
            }

            Response response;
            if ("POST".equalsIgnoreCase("POST") && request != null) {
                response = invocationBuilder.method("POST", Entity.entity(request, MediaType.APPLICATION_XML));
            } else {
                response = invocationBuilder.method("POST");
            }

	        if (response.getStatus() != 200) {
		        throw new CBException(CBCode.EXTERNAL_RESOURCE_ERROR);
	        }

	        RestResponse responseMO = response.readEntity(RestResponse.class);
	        if (responseMO.getCode() == 0) {
		        return responseMO.getDescription();
	        } else {
		        throw new CBException(CBCode.valueOfCode(responseMO.getCode(), CBCode.EXTERNAL_RESOURCE_ERROR));
	        }
        } catch (Exception e) {
	        log.error("Error when sendRequestFortumo: ", e);
	        throw e;
        } finally {
	        if (client != null)
		        client.close();
        }
    }
}
