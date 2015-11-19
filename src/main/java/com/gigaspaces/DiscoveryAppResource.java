package com.gigaspaces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * Created by Barak Bar Orion
 * on 11/19/15.
 *
 * @since 11.0
 */
@Singleton
@Path("discovery")
@PermitAll
public class DiscoveryAppResource {
    private static final Logger logger = LoggerFactory.getLogger(DiscoveryAppResource.class);

    public DiscoveryAppResource(@Context ServletContext servletContext) {

    }


    @GET
    @Path("all")
    @Produces(MediaType.TEXT_PLAIN)
    public String updateSha() {
        return String.valueOf("foo");
    }
}
