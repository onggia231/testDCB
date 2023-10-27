package com.telsoft.cbs.modules.cps_rtec;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.support.DefaultEndpoint;

import java.util.Map;

public class RtecEndpoint extends DefaultEndpoint {
    private final Map<String, Object> parameters;

    public RtecEndpoint(String uri, RtecComponent bokuComponent, Map<String, Object> parameters) {
        super(uri, bokuComponent);
        this.parameters = parameters;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
    }

    @Override
    public Producer createProducer() throws Exception {
        return new RtecProducer(this, parameters);
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
    public boolean isLenientProperties() {
        return true;
    }
}
