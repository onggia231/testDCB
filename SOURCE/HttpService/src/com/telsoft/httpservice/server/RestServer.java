package com.telsoft.httpservice.server;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class RestServer {
    private final Logger log = LoggerFactory.getLogger(RestServer.class);
    private final ResourceConfig config;
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

    private int miTcpMaxIdleTime;
    private int miTcpAcceptors;
    private int miMaxAcceptQueueSize;
    private int miMaxThreads;
    private int miMinThreads;

    RestServer(ResourceConfig config) {
        this.config = config;
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
        QueuedThreadPool threadPool = new QueuedThreadPool(miMaxThreads, miMinThreads);
        mServletServer = new Server(threadPool);
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
        http.setAcceptQueueSize(miMaxAcceptQueueSize);
        mServletServer.setConnectors(new Connector[]{http});

        ContextHandlerCollection collectionHandler = new ContextHandlerCollection();


        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        handler.setContextPath(getContextPath());

        ServletHolder servletHolder = new ServletHolder();

        servletHolder.setServlet(new ServletContainer(config));

        handler.addServlet(servletHolder, getServletContext());

        collectionHandler.addHandler(handler);

        mServletServer.setHandler(collectionHandler);
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

}
