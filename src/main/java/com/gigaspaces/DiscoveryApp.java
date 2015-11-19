package com.gigaspaces;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.linking.DeclarativeLinkingFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.sse.SseFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ApplicationPath;

/**
 * Created by Barak Bar Orion
 * on 11/19/15.
 *
 * @since 11.0
 */
@ApplicationPath("/")
public class DiscoveryApp extends ResourceConfig {
    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(DiscoveryApp.class);


    public DiscoveryApp() {
        super(DiscoveryAppResource.class,
//                BroadcasterResource.class,
                RolesAllowedDynamicFeature.class,
                MultiPartFeature.class, DeclarativeLinkingFeature.class, LoggingFilter.class, SseFeature.class);
//        property(ServerProperties.TRACING, "ALL");
    }
}
