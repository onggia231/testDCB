package com.telsoft.httpservice.soap.server;

import com.sun.net.httpserver.HttpContext;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.spi.HttpSpiContextHandler;
import org.eclipse.jetty.http.spi.JettyHttpContext;
import org.eclipse.jetty.http.spi.JettyHttpServer;
import org.eclipse.jetty.http.spi.JettyHttpServerProvider;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;

/**
 * <p>Title: Core Gateway System</p>
 *
 * <p>Description: A part of TELSOFT Gateway System</p>
 *
 * <p>Copyright: Copyright (c) 2009</p>
 *
 * <p>Company: TELSOFT</p>
 *
 * @author Nguyen Cong Khanh
 * @version 1.0
 */
@Setter
@Getter
public class SoapServer {
    private final Logger log = LoggerFactory.getLogger(com.telsoft.httpservice.soap.server.SoapServer.class);
//    private final ResourceConfig config;
    private int port;
    private Server mServletServer;
    private boolean https;
    private String servletContext;
    private String contextPath;
    private String keyStorePath;
    private String keyStorePassword;
    private String keyManagerPassword;
    private String trustStorePath;
    private String trustStorePassword;
    private Object webService;

/*    SoapServer(ResourceConfig config) {
        this.config = config;
    }*/

    SoapServer(Object webService) {
        this.webService = webService;
    }

    /**
     *
     */
    void stop() {
        try {
            mServletServer.stop();
        } catch (Exception e) {
            log.error("Cannot stop servlet server", e);
        }
    }

    /**
     * @throws Exception
     */
    public void start() throws Exception {
        mServletServer = new Server();
        ServerConnector http;

        if (!https) {
            http = new ServerConnector(mServletServer);
            http.setPort(port);
        } else {
            SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
            sslContextFactory.setKeyStorePath(keyStorePath);
            sslContextFactory.setKeyStorePassword(keyStorePassword);
            sslContextFactory.setKeyManagerPassword(keyManagerPassword);
            sslContextFactory.setNeedClientAuth(true);
//            sslContextFactory.setCertAlias("client");

            sslContextFactory.setTrustStorePath(trustStorePath);
            sslContextFactory.setTrustStorePassword(trustStorePassword);
            sslContextFactory.setWantClientAuth(true);

            HttpConfiguration https_config = new HttpConfiguration();
            https_config.setSecureScheme("https");
            https_config.setSecurePort(port);
            https_config.setOutputBufferSize(32768);

            SecureRequestCustomizer src = new SecureRequestCustomizer();
            src.setStsMaxAge(2000);
            src.setStsIncludeSubDomains(true);
            https_config.addCustomizer(src);
            http = new ServerConnector(mServletServer,
                    new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                    new HttpConnectionFactory(https_config));
            http.setPort(port);
        }
        mServletServer.setConnectors(new Connector[]{http});

        ContextHandlerCollection collectionHandler = new ContextHandlerCollection();
        mServletServer.setHandler(collectionHandler);

        HttpContext context = buildNew(mServletServer, getContextPath());
        Endpoint endpoint = Endpoint.create(webService);
        endpoint.publish(context);

        // Start the server
        mServletServer.start();


    }

    boolean isRunning() {
        return mServletServer != null && mServletServer.isRunning();
    }

    public void setKeyStore(String keyStorePath, String keyStorePassword, String keyManagerPassword, String trustStorePath, String trustStorePassword) {
        this.keyStorePath = keyStorePath;
        this.keyStorePassword = keyStorePassword;
        this.keyManagerPassword = keyManagerPassword;
        this.trustStorePath = trustStorePath;
        this.trustStorePassword = trustStorePassword;
    }


    public HttpContext buildOrig(Server server, String contextString) throws Exception {
        JettyHttpServerProvider.setServer(server);
        return new JettyHttpServerProvider().createHttpServer(new InetSocketAddress(port), 5).createContext(contextString);
    }

    public static HttpContext buildNew(Server server, String contextString) {
        JettyHttpServer jettyHttpServer = new JettyHttpServer(server, true);
        JettyHttpContext ctx = (JettyHttpContext) jettyHttpServer.createContext(contextString);
        try {
            Method method = JettyHttpContext.class.getDeclaredMethod("getJettyContextHandler");
            method.setAccessible(true);
            HttpSpiContextHandler contextHandler = (HttpSpiContextHandler) method.invoke(ctx);
            contextHandler.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ctx;
    }


}
