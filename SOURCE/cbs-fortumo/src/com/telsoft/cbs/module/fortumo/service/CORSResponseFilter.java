package com.telsoft.cbs.module.fortumo.service;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.List;

public class CORSResponseFilter implements ContainerResponseFilter {

    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {

        MultivaluedMap<String, Object> headers = responseContext.getHeaders();

        List<String> originList = requestContext.getHeaders().get("Origin");
        if (originList != null && originList.size() >= 1)
            headers.add("Access-Control-Allow-Origin", requestContext.getHeaders().get("Origin").get(0));
        else
            headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
        headers.add("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-Codingpedia,Set-Cookie");
        headers.add("Access-Control-Allow-Credentials", "true");
    }
}