package com.gigaspaces;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.id.Uuid;
import net.jini.id.UuidFactory;
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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
    private boolean fireRandomEvents;
    private ScheduledExecutorService scheduledExecutorService;
    private List<ServiceItem> randomItems;
    private final Random random = new Random();
    private final AtomicInteger count = new AtomicInteger(0);

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
        JSONObject jsonItems = new JSONObject();
        try {
            res.put("items", jsonItems);
            res.put("fireRandomEvents", fireRandomEvents);
        } catch (Exception e) {
            logger.error(e.toString(), e);
        }
        ServiceItem[] items = cache.lookup(null, Integer.MAX_VALUE);
        for (ServiceItem item : items) {
            try {
                jsonItems.put(item.serviceID.toString(), createJSON(item));
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
                logger.debug("broadcastMessage {} data {}" , event, event.getData());
                broadcaster.broadcast(event);
            } catch (Throwable ignored) {
                logger.error("Invoking of broadcastMessage() failed due the [{}], type={}, value:{}", ignored.toString(), type, value, ignored);
                ignored.printStackTrace();
            }
        }
    }

    @POST
    @Path("toggleFiringRandomEvent")
    public synchronized void toggleFiringRandomEvent(){
        fireRandomEvents = !fireRandomEvents;
        JSONObject value = new JSONObject();
        try {
            value.put("GENERATE_RANDOM_EVENTS", fireRandomEvents);
        }catch(Exception e){
            logger.error(e.toString(), e);
        }
        broadcastMessage("GENERATE_RANDOM_EVENTS", value);

        if(fireRandomEvents){
            randomItems = new ArrayList<>();
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.scheduleAtFixedRate((Runnable) this::doFireRundomEvent,100,  100, TimeUnit.MILLISECONDS);
        }else{
            if(scheduledExecutorService != null){
                scheduledExecutorService.shutdownNow();
            }
            scheduledExecutorService = null;
            for (ServiceItem randomItem : randomItems) {
                serviceRemoved(new ServiceDiscoveryEvent(this, randomItem, randomItem));
            }
            randomItems.clear();
        }
    }

    private void doFireRundomEvent() {
        if(1000 < randomItems.size()){
            if(random.nextBoolean()){
                doDeleteRandomItem();
            }else{
                doUpdateRandomItem();
            }
        }else if(randomItems.size() == 0) {
            doCreateRandomItem();
        }else{
            if(random.nextBoolean()){
                doCreateRandomItem();
            }else{
                doUpdateRandomItem();
            }
        }
    }

    private void doUpdateRandomItem() {
        int index = random.nextInt(randomItems.size());
        ServiceItem item = randomItems.get(index);
        ((Name)item.attributeSets[0]).name = "updatedName_" + count.incrementAndGet();
        serviceRemoved(new ServiceDiscoveryEvent(this, item, item));
    }

    private void doDeleteRandomItem() {
        int index = random.nextInt(randomItems.size());
        ServiceItem item = randomItems.remove(index);
        serviceRemoved(new ServiceDiscoveryEvent(this, item, item));
    }

    private void doCreateRandomItem() {
        Uuid uuid = UuidFactory.generate();
        ServiceID serviceID = new ServiceID(uuid.getMostSignificantBits(),
                uuid.getLeastSignificantBits());
        String name = "service_" + count.incrementAndGet();
        ServiceItem item = new ServiceItem(serviceID, name, new Entry[]{new Name(name)});
        randomItems.add(item);
        serviceAdded(new ServiceDiscoveryEvent(this, item, item));
    }

    private JSONObject createJSON(ServiceItem item) throws Exception {
        JSONObject res = new JSONObject();
        res.put("id", item.serviceID.toString());
        String cls = item.getService().getClass().getCanonicalName();
        res.put("cls", cls);
        JSONObject attributes = new JSONObject();
        res.put("attributes", attributes);
        for (Entry entry : item.attributeSets) {
            if (entry instanceof Name) {
                res.put("name",  ((Name) entry).name);
            }else{
                for (Field field : entry.getClass().getFields()) {
                    if(Modifier.isPublic(field.getModifiers())){
                        if(Entry.class.isAssignableFrom(field.getType())){
                            JSONObject jsob = new JSONObject();
                            fill(jsob, (Entry)field.get(entry));
                            attributes.put(field.getName(), jsob);
                        }else {
                            attributes.put(field.getName(), field.get(entry));
                        }
                    }
                }
                attributes.put(entry.getClass().getCanonicalName(), entry.toString());
            }
        }
        return res;
    }

    private void fill(JSONObject jsob, Entry entry) throws JSONException, IllegalAccessException {
        jsob.put("_type", entry.getClass().getCanonicalName());
        for (Field field : entry.getClass().getFields()) {
            if(Modifier.isPublic(field.getModifiers())){
                if(Entry.class.isAssignableFrom(field.getType())){
                    JSONObject rec = new JSONObject();
                    fill(rec, (Entry)field.get(entry));
                    jsob.put(field.getName(), rec);
                }else {
                    jsob.put(field.getName(), field.get(entry));
                }
            }
        }
    }


    @Override
    public void serviceAdded(ServiceDiscoveryEvent event) {
        logger.debug("serviceAdded {}", event);
        try {
            broadcastMessage("CREATED", createJSON(event.getPostEventServiceItem()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void serviceRemoved(ServiceDiscoveryEvent event) {
        logger.debug("serviceRemoved {}", event);
        try {
            broadcastMessage("REMOVED", createJSON(event.getPreEventServiceItem()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void serviceChanged(ServiceDiscoveryEvent event) {
        logger.debug("serviceChanged {}", event);
        try {
            broadcastMessage("UPDATED", createJSON(event.getPostEventServiceItem()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
