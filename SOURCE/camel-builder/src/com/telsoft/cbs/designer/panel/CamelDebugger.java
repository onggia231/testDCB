package com.telsoft.cbs.designer.panel;

import com.telsoft.cbs.designer.utils.CamelHelper;
import lombok.SneakyThrows;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.spi.Registry;
import org.apache.camel.test.junit4.CamelTestSupport;

import javax.swing.*;

public class CamelDebugger extends CamelTestSupport {
    private final PanelDesign panelDesign;
    private final BeanManager beanManager;
    private final boolean debug;

    public CamelDebugger(PanelDesign panelDesign, BeanManager beanManager, boolean debug) {
        this.panelDesign = panelDesign;
        this.beanManager = beanManager;
        this.debug = debug;
    }

    @Override
    public boolean isUseDebugger() {
        // must enable debugger
        return debug;
    }

    @Override
    protected CamelContext createCamelContext() throws Exception {
        DefaultCamelContext context = CamelHelper.createCamelContext();

        Registry registry = beanManager.createRegistry(context);
        context.setRegistry(registry);
        return context;
    }

    @Override
    protected Registry createCamelRegistry() throws Exception {
        return beanManager.createRegistry(context);
    }

    @Override
    protected void debugBefore(Exchange exchange, Processor processor, ProcessorDefinition<?> definition, String id, String label) {
        SwingUtilities.invokeLater(() -> panelDesign.mark(definition));
    }

    @Override
    @SneakyThrows
    protected void debugAfter(Exchange exchange, Processor processor, ProcessorDefinition<?> definition, String id, String label, long timeTaken) {
        Thread.sleep(1000);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                panelDesign.mark(null);
            }
        });
    }

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        RouteBuilder routeBuilder = new RouteBuilder() {
            @Override
            public void configure() throws Exception {
            }
        };
        routeBuilder.setRouteCollection(panelDesign.getRoutesDefinition());
        return routeBuilder;
    }

    public void start() throws Exception {
        setUp();
    }

    public void stop() throws Exception {
        tearDown();
    }

    public boolean isRunning() {
        return context() != null && context().isStarted();
    }
}
