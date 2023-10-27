//package com.telsoft.cbs;
//
//import org.osgi.framework.BundleActivator;
//import org.osgi.framework.BundleContext;
//import telsoft.gateway.core.GatewayConfiguration;
//
//public class CBAppOsgi implements BundleActivator {
//    private CBApp app;
//
//    @Override
//    public void start(BundleContext context) throws Exception {
//        GatewayConfiguration config = new GatewayConfiguration();
//        app = new CBApp(config);
//        app.initSystem();
//        app.start();
//    }
//
//    @Override
//    public void stop(BundleContext context) throws Exception {
//        if (app != null) {
//            app.close();
//        }
//    }
//}
