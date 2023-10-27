package com.telsoft.cbs.camel.component;

import com.telsoft.cbs.camel.CbsContansts;
import com.telsoft.cbs.camel.service.Service;
import com.telsoft.cbs.camel.service.Services;
import com.telsoft.cbs.domain.CBException;
import com.telsoft.cbs.domain.CBRequest;
import lombok.Getter;
import lombok.Setter;
import org.apache.camel.*;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.support.DefaultComponent;
import org.apache.camel.support.ProcessorEndpoint;
import org.apache.camel.util.ObjectHelper;
import telsoft.gateway.core.GatewayManager;
import telsoft.gateway.core.log.MessageContext;

import java.util.Map;

public abstract class ProcessorComponent extends DefaultComponent {
    private final String uriString;
    @Getter
    private transient Services services;

    @Getter
    private GatewayManager manager;

    public ProcessorComponent() {
        UriEndpoint uriEndpoint = this.getClass().getAnnotation(UriEndpoint.class);
        if (uriEndpoint != null) {
            this.uriString = uriEndpoint.scheme();
        } else
            this.uriString = "";
    }

    public <T extends Service> T getService(Class<?> clazz) {
        return (T) getServices().getService(clazz);
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        services = (Services) this.getCamelContext().getRegistry().lookupByName(CbsContansts.SERVICES);
        manager = (GatewayManager) this.getCamelContext().getRegistry().lookupByName(CbsContansts.MANAGER);

        ObjectHelper.notNull(services, CbsContansts.SERVICES);
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        CBProcessor processor = createProcessor(parameters);
        return new CBProcessorEndpoint(createUriString(), this, processor);
    }

    protected CBProcessor createProcessor(Map<String, Object> parameters) {
        CBProcessor processor = new CBProcessor(this);
        processor.setParameters(parameters);
        return processor;
    }

    public abstract void process(CBRequest request, Exchange exchange, Map<String, Object> parameters, MessageContext messageContext) throws CBException;

    protected final String createUriString() {
        return uriString;
    }

    private class CBProcessor implements Processor {
        private final ProcessorComponent component;
        @Setter
        @Getter
        private Map<String, Object> parameters;

        public CBProcessor(ProcessorComponent component) {
            this.component = component;
        }

        @Override
        public final void process(Exchange exchange) throws Exception {
            MessageContext messageContext = (MessageContext) exchange.getProperty(CbsContansts.CB_MSG_CONTEXT);

            Object o = exchange.getProperty(CbsContansts.CB_REQUEST);
            if (o instanceof CBRequest)
                process((CBRequest) o, exchange, messageContext);
            else
                process(null, exchange, messageContext);
        }

        public void process(CBRequest request, Exchange exchange, MessageContext messageContext) throws CBException {
            component.process(request, exchange, getParameters(), messageContext);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    private class CBProcessorEndpoint extends ProcessorEndpoint {
        private final ProcessorComponent cbComponent;

        public CBProcessorEndpoint(String uri, ProcessorComponent cbComponent, CBProcessor processor) {
            super(uri, cbComponent, processor);
            this.cbComponent = cbComponent;
        }

        @Override
        public boolean isLenientProperties() {
            return true;
        }

        @Override
        public PollingConsumer createPollingConsumer() throws Exception {
            throw new Exception("Cannot create consumer for this component type");
        }

        @Override
        public Consumer createConsumer(Processor processor) throws Exception {
            throw new Exception("Cannot create consumer for this component type");
        }

        @Override
        protected String createEndpointUri() {
            return this.cbComponent.createUriString();
        }
    }
}
