package com.telsoft.cbs.camel;

import com.telsoft.cbs.camel.service.Services;
import com.telsoft.cbs.camel.service.ServicesImpl;
import com.telsoft.cbs.loader.RouteLoader;
import com.telsoft.cbs.loader.XmlRouteLoader;
import com.telsoft.cbs.utils.CBDispatcherManager;
import com.telsoft.thread.ParameterType;
import com.telsoft.util.AppException;
import com.telsoft.util.StringUtil;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.engine.DefaultInjector;
import org.apache.camel.spi.Registry;
import org.apache.camel.support.SimpleRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import telsoft.gateway.commons.DBSynchronizable;
import telsoft.gateway.commons.GWUtil;
import telsoft.gateway.core.component.GWDataLayer;
import telsoft.gateway.core.dsp.Dispatcher;
import telsoft.gateway.core.log.MessageContext;
import telsoft.gateway.core.queue.QueueOUT;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class CamelDispatcher extends CBDispatcherManager {
    public static final String LOCK = "$LOCK";
    private static final Logger log = LoggerFactory.getLogger(CamelDispatcher.class);
    private DefaultCamelContext context;
    private List<Thread> receiptThreads = new ArrayList<Thread>();
    private int miReceiptThreadNumber;
    private String routeFiles;
    private Services services;
    private AtomicBoolean done = new AtomicBoolean(true);

    @Override
    protected Dispatcher prepareDispatcher() throws Exception {
        return new CamelDispatcherSplitThread(this);
    }

    @Override
    public Vector getParameterDefinition() {
        Vector vtReturn = new Vector();
        vtReturn.add(createParameter("RouteFiles", "", ParameterType.PARAM_TEXTBOX_MAX, "500", "", "", "Routes"));
        vtReturn.add(createParameter("ReceiptThreadNumber", "", ParameterType.PARAM_TEXTBOX_MASK, "99990", "", "", "Processing"));

        vtReturn.addAll(super.getParameterDefinition());
        return vtReturn;
    }

    @Override
    public void fillParameter() throws AppException {
        super.fillParameter();
        routeFiles = loadString("RouteFiles");
        miReceiptThreadNumber = loadUnsignedInteger("ReceiptThreadNumber");
    }

    @Override
    protected void fillDispatcherParameter(Dispatcher thr) throws Exception {
        super.fillDispatcherParameter(thr);
        CamelDispatcherSplitThread camelThread = (CamelDispatcherSplitThread) thr;
        camelThread.setContext(context);
    }

    @Override
    public void threadDelay(long iSecond) throws InterruptedException {
        if (services != null) {
            GWDataLayer dataLayer = null;
            try {
                dataLayer = new GWDataLayer(getGatewayManager(), getGatewayManager().getCachedGroup());
                dataLayer.init();
                services.syncData(dataLayer.getConnection(), DBSynchronizable.SYNC_FORCE);
            } catch (Exception e) {
                logMonitor("Sync error" + GWUtil.decodeException(e));
            } finally {
                if (dataLayer != null)
                    dataLayer.free();
            }
        }
        super.threadDelay(iSecond);
    }

    @Override
    protected void beforeSession() throws Exception {
        super.beforeSession();

        services = new ServicesImpl();
        services.init();

//        BeanManager beanManager = new BeanManager();

        context = new DefaultCamelContext();
        Registry registry = new SimpleRegistry();
        registry.bind(CbsContansts.SERVICES, services);
        registry.bind(CbsContansts.MANAGER, this.getGatewayManager());
        registry.bind("thread", this);

        context.setRegistry(registry);

        context.setInjector(new DefaultInjector(context));
        context.setAutoStartup(true);
        context.setMessageHistory(true);
        context.disableJMX();

        RouteLoader loader = new XmlRouteLoader();

        List<String> files = StringUtil.toStringVector(routeFiles, ",");
        for (String f : files) {
            try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(f))) {
                loader.load(context, bufferedInputStream);
            }
        }
        context.start();
        startDetachResponse();
    }

    @Override
    protected void afterSession() throws Exception {
        done.set(true);
        receiptThreads = null;

        if (context != null) {
            context.stop();
            context = null;
        }

        services.close();
        services = null;
        super.afterSession();
    }


    /**
     * @throws Exception
     */
    protected void startDetachResponse() throws Exception {
        logMonitor("Start init detach response threads");

        if (receiptThreads == null) {
            receiptThreads = new ArrayList<>();
        }

        int interval;
        try {
            interval = loadUnsignedInteger("IdleInterval");
        } catch (Exception ex) {
            interval = 10;
        }

        final int idleInterval = interval;
        done.set(false);
        //create receipt thread
        for (int i = 0; i < miReceiptThreadNumber; i++) {
            String threadName = "Receipt Thread " + (i + 1);
            receiptThreads.add(new Thread(threadName) {
                public void run() {
                    logMonitor(getName() + " start");
                    QueueOUT queueOUT = getGatewayManager().getQueueOUT();
                    while (!done.get()) {
                        try {
                            MessageContext ctx = queueOUT.detach("CAMEL");

                            if (ctx != null) {
                                CountDownLatch lockObject = (CountDownLatch) ctx.getProperty(LOCK);
                                ctx.setProcessed(true);
                                lockObject.countDown();
                            } else {
                                Thread.sleep(idleInterval);
                            }
                        } catch (Exception ex) {
                            log.error("Exception when process out queue", ex);
                        }
                    }
                    logMonitor(getName() + " stop");
                }
            });
        }

        //start receipt thread
        for (Thread thread : receiptThreads) {
            thread.start();
        }
    }
}
