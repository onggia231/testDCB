package com.telsoft.cbs.camel;

import com.telsoft.cbs.camel.service.Services;
import com.telsoft.cbs.domain.CBException;
import com.telsoft.cbs.domain.CBRequest;
import lombok.Getter;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;

public abstract class CBProducer extends DefaultProducer {
    @Getter
    private Services services;

    public CBProducer(Endpoint endpoint) {
        super(endpoint);
    }

    @Override
    public final void process(Exchange exchange) throws Exception {
        if (services == null) {
            CamelContext context = exchange.getContext();
            services = (Services) context.getRegistry().lookupByName("services");
        }

        Object o = exchange.getIn().getHeader(CbsContansts.CB_REQUEST);
        if (o instanceof CBRequest)
            process((CBRequest) o, exchange);
        else
            process(null, exchange);

        if (exchange.hasOut()) {
            exchange.getMessage().setHeaders(exchange.getIn().getHeaders());
        }
    }

    public abstract void process(CBRequest request, Exchange exchange) throws CBException;
}
