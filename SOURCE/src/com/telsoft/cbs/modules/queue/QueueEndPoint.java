package com.telsoft.cbs.modules.queue;

import lombok.Getter;
import org.apache.camel.BeanInject;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.support.DefaultEndpoint;
import telsoft.gateway.core.GatewayManager;

import java.util.Map;

@UriEndpoint(
        firstVersion = "1.0.0",
        scheme = "queue",
        title = "Put message in queue to execute on external systems",
        syntax = "queue:",
        label = "cbs,endpoint",
        producerOnly = true,
        generateConfigurer = false
)
public class QueueEndPoint extends DefaultEndpoint {
    @Getter
    private Map<String, Object> parameters;

    @UriParam(name = "id", description = "Server id", displayName = "Server ID")
    private ServerId serverId;

    @BeanInject("manager")
    @Getter
    private GatewayManager gatewayManager;

    public QueueEndPoint() {
    }

    public void setParameters(Map<String, Object> parameters) throws Exception {
        this.parameters = parameters;
        serverId = ServerId.valueOf((String) parameters.get("id"));
        if (serverId == null) {
            throw new Exception("Illegal parameter");
        }
    }

    @Override
    public Producer createProducer() throws Exception {
        return new QueueProducer(this, serverId);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        throw new Exception("Not implements");
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    protected String createEndpointUri() {
        return "queue";
    }

    @Override
    public boolean isLenientProperties() {
        return true;
    }
}
