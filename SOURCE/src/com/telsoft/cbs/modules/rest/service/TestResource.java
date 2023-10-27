package com.telsoft.cbs.modules.rest.service;

import com.telsoft.cbs.modules.rest.RestReceptionist;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

@Path("/")
@Produces({"application/xml"})
@Consumes({"application/xml"})
public class TestResource {
    @Context
    private RestReceptionist receptionist;

    @Path("/test")
    @GET
    public String test(@Context HttpServletRequest httpRequest) {
        return "OK";
    }
}
