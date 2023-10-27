package com.telsoft.cbs.loader;

import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RoutesDefinition;

import java.io.File;
import java.io.InputStream;

public interface RouteLoader {
    RoutesDefinition load(String in) throws Exception;

    String save(RoutesDefinition routesDefinition) throws Exception;

    RoutesDefinition load(File file) throws Exception;

    void save(RoutesDefinition routesDefinition, File file) throws Exception;

    void saveBean(Beans beans, File beanFile) throws Exception;

    Beans loadBean(File beanFile) throws Exception;

    void load(DefaultCamelContext context, String in) throws Exception;

    void load(DefaultCamelContext context, InputStream in) throws Exception;
}
