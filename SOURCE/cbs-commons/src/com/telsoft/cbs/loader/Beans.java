package com.telsoft.cbs.loader;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;

public class Beans {
    @JsonProperty("bean")
    private Collection<Bean> bean;

    public Collection<Bean> getBeans() {
        return this.bean;
    }

    public void setBeans(Collection<Bean> bean) {
        this.bean = bean;
    }
}
