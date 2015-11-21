package com.gigaspaces;

import com.gigaspaces.config.lrmi.nio.NIOConfiguration;
import com.gigaspaces.lrmi.GenericExporter;
import net.jini.config.PlainConfiguration;
import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.discovery.DiscoveryEvent;
import net.jini.discovery.DiscoveryListener;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.lookup.LookupCache;
import net.jini.lookup.ServiceDiscoveryEvent;
import net.jini.lookup.ServiceDiscoveryListener;
import net.jini.lookup.ServiceDiscoveryManager;
import net.jini.lookup.entry.Name;
import org.openspaces.pu.container.servicegrid.PUServiceBeanProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Barak Bar Orion
 * on 11/19/15.
 *
 * @since 11.0
 */
public class Discovery implements DiscoveryListener, ServiceDiscoveryListener {
    private static final Logger logger = LoggerFactory.getLogger(Discovery.class);
    private LookupDiscoveryManager ldm;
    private ServiceDiscoveryManager sdm;
    private LookupCache cache;

    public static void main(String[] args) throws Exception {
        logger.debug("main !");
        Discovery discovery = new Discovery();
        discovery.go(null);
        logger.info("Done !");
        Thread.sleep(Long.MAX_VALUE);
    }

    public Discovery() {
    }

    public LookupCache go(ServiceDiscoveryListener listener) throws Exception {
        System.setProperty(net.jini.discovery.Constants.HOST_ADDRESS, "127.0.0.1");
//        System.setProperty(SystemProperties.ENABLE_DYNAMIC_LOCATORS, "true");
        GenericExporter genericExporter = new GenericExporter(NIOConfiguration.create());
        PlainConfiguration config = new PlainConfiguration();
        config.setEntry("net.jini.lookup.ServiceDiscoveryManager", "eventListenerExporter", genericExporter);
        ldm = new LookupDiscoveryManager(new String[]{"visibility"}, new LookupLocator[]{}, this, config);
        sdm = new ServiceDiscoveryManager(ldm, null, config);
        cache = sdm.createLookupCache(null, null, listener == null ? this : listener);
        return cache;
    }

    public LookupCache getCache() {
        return cache;
    }

    @Override
    public void discovered(DiscoveryEvent e) {
        logger.debug("discovered {}", e);
    }

    @Override
    public void discarded(DiscoveryEvent e) {
        logger.debug("discarded {}", e);
    }

    @Override
    public void serviceAdded(ServiceDiscoveryEvent event) {
        logger.debug("serviceAdded {}", event);

//        ServiceItem serviceItem = event.getPostEventServiceItem();
//        String cls = serviceItem.getService().getClass().getCanonicalName();
//        for (Entry entry : serviceItem.attributeSets) {
//            if (entry instanceof Name) {
//                logger.debug("service added name: {}, class: {}", ((Name) entry).name, cls);
//            }
//        }
//
//        if (serviceItem.getService() instanceof PUServiceBeanProxy) {
//            for (Entry entry : serviceItem.attributeSets) {
//                logger.debug("service attribute: {}", entry);
//            }
//        }
    }

    @Override
    public void serviceRemoved(ServiceDiscoveryEvent event) {
        logger.debug("service remove {}", event);
    }

    @Override
    public void serviceChanged(ServiceDiscoveryEvent event) {
        logger.debug("service changed {}", event);
    }
}
