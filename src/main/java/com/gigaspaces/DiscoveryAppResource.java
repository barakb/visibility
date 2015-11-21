package com.gigaspaces;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.lookup.LookupCache;
import net.jini.lookup.ServiceDiscoveryEvent;
import net.jini.lookup.ServiceDiscoveryListener;
import net.jini.lookup.entry.Name;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseBroadcaster;
import org.glassfish.jersey.media.sse.SseFeature;
import org.glassfish.jersey.server.ChunkedOutput;
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
import javax.ws.rs.core.Response;

/**
 * Created by Barak Bar Orion
 * on 11/19/15.
 *
 * @since 11.0
 */
@Singleton
@Path("discovery")
@PermitAll
public class DiscoveryAppResource implements ServiceDiscoveryListener {
    private static final Logger logger = LoggerFactory.getLogger(DiscoveryAppResource.class);
    private final LookupCache cache;
    private final SseBroadcaster broadcaster;


    public DiscoveryAppResource(@Context ServletContext servletContext) throws Exception {
        Discovery discovery = new Discovery();
        cache = discovery.go(this);
        this.broadcaster = new SseBroadcaster(){
            @Override
            public void onException(ChunkedOutput<OutboundEvent> chunkedOutput, Exception exception) {
                logger.error(exception.toString(), exception);
                remove(chunkedOutput);
            }

            @Override
            public void onClose(ChunkedOutput<OutboundEvent> chunkedOutput) {
                remove(chunkedOutput);
                logger.info("Closing {}", chunkedOutput);
            }
        };
    }


    @GET
    @Path("all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        JSONObject res = new JSONObject();
        ServiceItem[] items = cache.lookup(null, Integer.MAX_VALUE);
        for (ServiceItem item : items) {
            try {
                res.put(item.serviceID.toString(), createJSON(item));
            } catch (Exception e) {
                logger.error(e.toString(), e);
            }
        }
        return Response.ok(res.toString()).build();
    }

    @GET
    @Path("subscribe")
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    public EventOutput listenToBroadcast() {
        final EventOutput eventOutput = new EventOutput();
        this.broadcaster.add(eventOutput);
        return eventOutput;
    }

    private void broadcastMessage(String type, JSONObject value) {
        if (value != null) {
            try {
                OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
                OutboundEvent event = eventBuilder.name(type)
                        .mediaType(MediaType.APPLICATION_JSON_TYPE)
//                        .mediaType(MediaType.TEXT_PLAIN_TYPE)
                        .data(String.class, value.toString())
                        .build();
                logger.debug("broadcastMessage {}" , event);
                broadcaster.broadcast(event);
            } catch (Throwable ignored) {
                logger.error("Invoking of broadcastMessage() failed due the [{}], type={}, value:{}", ignored.toString(), type, value, ignored);
                ignored.printStackTrace();
            }
        }
    }

    private JSONObject createJSON(ServiceItem item) throws JSONException {
        JSONObject res = new JSONObject();
        res.put("id", item.serviceID.toString());
        String cls = item.getService().getClass().getCanonicalName();
        res.put("cls", cls);
        for (Entry entry : item.attributeSets) {
            if (entry instanceof Name) {
                res.put("name",  ((Name) entry).name);
            }else{
                res.put(entry.getClass().getCanonicalName(), entry.toString());
            }
        }
        return res;
    }


    @Override
    public void serviceAdded(ServiceDiscoveryEvent event) {
        logger.debug("serviceAdded {}", event);
        try {
            broadcastMessage("CREATED", createJSON(event.getPostEventServiceItem()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void serviceRemoved(ServiceDiscoveryEvent event) {
        logger.debug("serviceRemoved {}", event);
        try {
            broadcastMessage("REMOVED", createJSON(event.getPreEventServiceItem()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void serviceChanged(ServiceDiscoveryEvent event) {
        logger.debug("serviceChanged {}", event);
        try {
            broadcastMessage("UPDATED", createJSON(event.getPostEventServiceItem()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
