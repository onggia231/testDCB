package com.telsoft.cbs.module.fortumo.client;

import com.telsoft.cbs.module.fortumo.domain.*;
import lombok.Getter;
import lombok.Setter;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.filter.LoggingFilter;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@Getter
@Setter
public class RestXmlHttpClient {
    String apiUrl;

    public RestXmlHttpClient() {}

    public <T> T callAPI(String url, String path, Object requestEntity, Class<T> responseEntity) throws IOException {
        Client client = ClientBuilder.newClient( new ClientConfig().register( LoggingFilter.class ) );
        WebTarget webTarget = client.target(url).path(path);

        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_XML);
        Response response = invocationBuilder.post(Entity.entity(requestEntity, MediaType.APPLICATION_XML));

//        System.out.println(response.getStatus());
//        System.out.println(response.readEntity(AccountChangeResponse.class));

        return response.readEntity(responseEntity);
    }
    
    public static <T> T convertXMLStringToObject(Class<T> e, String xmlString) throws Exception {
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(e);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            return (T) jaxbUnmarshaller.unmarshal(new StringReader(xmlString));
        }
        catch (Exception ex) {
            throw ex;
        }
    }

    public static void main(String[] args) throws IOException {
        AccountChangeRequest accountChangeRequest = new AccountChangeRequest();
        UserAccount userAccount = new UserAccount();
        userAccount.setRefType(RefType.MSISDN);
        userAccount.setAccount("84904123456");
        accountChangeRequest.setAccount(userAccount);
        accountChangeRequest.setMessageId("1234567890123456");

        //System.out.println(callAPI("http://localhost:8887/mobifone/", "accountchange", accountChangeRequest, AccountChangeResponse.class));

//        Client client = ClientBuilder.newClient( new ClientConfig().register( LoggingFilter.class ) );
//        WebTarget webTarget = client.target("http://localhost:8887/mobifone").path("accountchange");
//
//        AccountChangeRequest accountChangeRequest = new AccountChangeRequest();
//        UserAccount userAccount = new UserAccount();
//        userAccount.setRefType(RefType.MSISDN);
//        userAccount.setAccount("84904123456");
//        accountChangeRequest.setAccount(userAccount);
//        accountChangeRequest.setMessageId("1234567890123456");
//
//        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_XML);
//        Response response = invocationBuilder.post(Entity.entity(accountChangeRequest, MediaType.APPLICATION_XML));
//
//        System.out.println(response.getStatus());
//        System.out.println(response.readEntity(AccountChangeResponse.class));

//        String request = "<AccountChangeRequest>\n" +
//                "<messageId>1234567890123456</messageId>\n" +
//                "<account refType=\"MSISDN\">84904123456</account>\n" +
//                "</AccountChangeRequest>";
//
//        URL url = new URL("http://localhost:8887/mobifone/refund");
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//
//        // Set timeout as per needs
//        connection.setConnectTimeout(20000);
//        connection.setReadTimeout(20000);
//
//        // Set DoOutput to true if you want to use URLConnection for output.
//        // Default is false
//        connection.setDoOutput(true);
//
//        connection.setUseCaches(true);
//        connection.setRequestMethod("POST");
//
//        // Set Headers
//        connection.setRequestProperty("Accept", "application/xml");
//        connection.setRequestProperty("Content-Type", "application/xml");
//
//        // Write XML
//        OutputStream outputStream = connection.getOutputStream();
//        byte[] b = request.getBytes("UTF-8");
//        outputStream.write(b);
//        outputStream.flush();
//        outputStream.close();
//
//        // Read XML
//        InputStream inputStream = connection.getInputStream();
//        byte[] res = new byte[2048];
//        int i = 0;
//        StringBuilder response = new StringBuilder();
//        while ((i = inputStream.read(res)) != -1) {
//            response.append(new String(res, 0, i));
//        }
//        inputStream.close();
//
//        try {
//            AccountChangeResponse o = convertXMLStringToObject(AccountChangeResponse.class, response.toString());
//            System.out.println(o);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
