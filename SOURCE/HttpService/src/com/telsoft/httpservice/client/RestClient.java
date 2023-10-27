package com.telsoft.httpservice.client;

import com.telsoft.httpservice.utils.LoggingFilter;
import lombok.Getter;
import lombok.Setter;
import org.glassfish.jersey.SslConfigurator;
import org.glassfish.jersey.client.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URL;

public class RestClient {
    private Client client;

    @Getter
    private String url;

    @Getter
    @Setter
    private String mediaType = MediaType.APPLICATION_XML;

    @Setter
    @Getter
    private String keyStorePath;
    @Setter
    @Getter
    private String keyStorePassword;

    @Setter
    @Getter
    private String trustStorePath;
    @Setter
    @Getter
    private String trustStorePassword;

    public RestClient(String url) {
        this.url = url;
    }

    public String getLoggerName() {
        return "REST-CLIENT";

    }

    public void start() throws MalformedURLException {
        final Logger LOGGER = LoggerFactory.getLogger(getLoggerName());

        URL url = new URL(this.getUrl());
        String scheme = url.getProtocol();
        SSLContext scl = null;
        if ("https".equals(scheme)) {
            scl = SslConfigurator.newInstance()
                    .trustStoreFile(trustStorePath)
                    .trustStorePassword(trustStorePassword)
                    .keyStoreFile(keyStorePath)
                    .keyStorePassword(keyStorePassword)
                    .createSSLContext();
        }

        ClientBuilder builder = ClientBuilder.newBuilder();
        builder.withConfig(new ClientConfig().register(new LoggingFilter(LOGGER, true)));
        if (scl != null) {
            builder.sslContext(scl);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });
        }
        client = builder.build();
    }

    public void stop() {
        if (client != null) {
            client.close();
            client = null;
        }

    }

    public <REQ, RES> RES execute(String method, String xForwardFor, String path, REQ restRequest, Class<RES> resClass) {
        Invocation.Builder invocationBuilder;
        WebTarget target = client.target(path);
        invocationBuilder = target.request(getMediaType());
        if (xForwardFor != null) {
            invocationBuilder.header("X-Forward-For", xForwardFor);
        }

        Response response;
        if ("POST".equalsIgnoreCase(method) && restRequest != null) {
            response = invocationBuilder.method(method, Entity.entity(restRequest, getMediaType()));
        } else {
            response = invocationBuilder.method(method);
        }

        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
        }

        return response.readEntity(resClass);
    }

    public <REQ, RES> RES post(String xForwardFor, String path, REQ restRequest, Class<RES> resClass) {
        return execute("POST", xForwardFor, url + path, restRequest, resClass);
    }

    public <RES> RES get(String xForwardFor, String path, Class<RES> resClass) {
        return execute("GET", xForwardFor, url + path, null, resClass);
    }

    public <RES> RES delete(String xForwardFor, String path, Class<RES> resClass) {
        return execute("DELETE", xForwardFor, url + path, null, resClass);
    }

    public <REQ, RES> RES postA(String xForwardFor, String path, REQ restRequest, Class<RES> resClass) {
        return execute("POST", xForwardFor, path, restRequest, resClass);
    }

    public <RES> RES getA(String xForwardFor, String path, Class<RES> resClass) {
        return execute("GET", xForwardFor, path, null, resClass);
    }

    public <RES> RES deleteA(String xForwardFor, String path, Class<RES> resClass) {
        return execute("DELETE", xForwardFor, path, null, resClass);
    }

    public <REQ, RES> RES post(String path, REQ restRequest, Class<RES> resClass) {
        return execute("POST", null, url + path, restRequest, resClass);
    }

    public <RES> RES get(String path, Class<RES> resClass) {
        return execute("GET", null, url + path, null, resClass);
    }

    public <RES> RES delete(String path, Class<RES> resClass) {
        return execute("DELETE", null, url + path, null, resClass);
    }

    public <REQ, RES> RES postA(String path, REQ restRequest, Class<RES> resClass) {
        return execute("POST", null, path, restRequest, resClass);
    }

    public <RES> RES getA(String path, Class<RES> resClass) {
        return execute("GET", null, path, null, resClass);
    }

    public <RES> RES deleteA(String path, Class<RES> resClass) {
        return execute("DELETE", null, path, null, resClass);
    }
}
