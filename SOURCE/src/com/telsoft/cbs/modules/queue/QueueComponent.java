package com.telsoft.cbs.modules.queue;

import org.apache.camel.Endpoint;
import org.apache.camel.spi.annotations.Component;
import org.apache.camel.support.DefaultComponent;

import java.util.Map;

@Component("queue")
public class QueueComponent extends DefaultComponent {

    public QueueComponent() {
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        QueueEndPoint queueEndPoint = getCamelContext().getInjector().newInstance(QueueEndPoint.class);
        queueEndPoint.setParameters(parameters);
        return queueEndPoint;
    }
}
