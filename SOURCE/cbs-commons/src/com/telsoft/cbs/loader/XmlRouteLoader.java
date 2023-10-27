package com.telsoft.cbs.loader;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;

import java.io.*;
import java.util.List;

public class XmlRouteLoader implements RouteLoader {
    @Override
    public RoutesDefinition load(String in) throws Exception {
        return ModelHelper.createModelFromXml(null, in, RoutesDefinition.class);
    }

    @Override
    public String save(RoutesDefinition routesDefinition) throws Exception {
        List<RouteDefinition> routes = routesDefinition.getRoutes();
        if (routes.isEmpty()) {
            return null;
        }

        // use a routes definition to dump the routes
        RoutesDefinition def = new RoutesDefinition();
        def.setRoutes(routes);
        return ModelHelper.dumpModelAsXml(null, def);
    }

    @Override
    public RoutesDefinition load(File file) throws Exception {
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file))) {
            return ModelHelper.loadRoutesDefinition(null, bufferedInputStream);
        }
    }

    @Override
    public void save(RoutesDefinition routesDefinition, File file) throws Exception {
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
            String s = save(routesDefinition);
            out.write(s.getBytes());
        }
    }

    @Override
    public void saveBean(Beans beans, File beanFile) throws Exception {
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(beanFile))) {
            String s = ModelHelper.saveBean(beans);
            out.write(s.getBytes());
        }
    }

    @Override
    public Beans loadBean(File beanFile) throws Exception {
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(beanFile))) {
            return ModelHelper.loadBean(bufferedInputStream);
        }
    }

    @Override
    public void load(DefaultCamelContext context, String in) throws Exception {
        RoutesDefinition routesDefinition = ModelHelper.createModelFromXml(context, in, RoutesDefinition.class);
        context.addRouteDefinitions(routesDefinition.getRoutes());
    }

    @Override
    public void load(DefaultCamelContext context, InputStream in) throws Exception {
        RoutesDefinition routesDefinition = ModelHelper.createModelFromXml(context, in, RoutesDefinition.class);
        context.addRouteDefinitions(routesDefinition.getRoutes());
    }
}
