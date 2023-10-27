package com.telsoft.cbs.modules.rest.service;

import com.telsoft.cbs.modules.rest.RestReceptionist;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class RestApplicationBinder extends AbstractBinder {

    private final RestReceptionist receptionist;

    public RestApplicationBinder(RestReceptionist receptionist) {
        this.receptionist = receptionist;
    }

    @Override
    protected void configure() {
        bind(receptionist).to(RestReceptionist.class);
    }
}
