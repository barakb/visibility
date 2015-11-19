package com.gigaspaces;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by Barak Bar Orion
 * on 11/19/15.
 *
 * @since 11.0
 */
public class HttpServer {
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
    private static final String DEFAULT_WEB_FOLDER_PATH = "./web";

    public static void main(String[] args) {
        Server server = new Server(8080);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        DefaultServlet defaultServlet = new DefaultServlet();
        ServletHolder holderPwd = new ServletHolder("default", defaultServlet);
        File webDir = new File(DEFAULT_WEB_FOLDER_PATH);
        if (!webDir.exists()) {
            logger.info("File {} not found", webDir.getAbsolutePath());
            System.exit(1);
        }

        logger.info("Using {} to serve static content", webDir.getAbsolutePath());
        holderPwd.setInitParameter("resourceBase", webDir.getAbsolutePath());
        holderPwd.setInitOrder(2);
        context.addServlet(holderPwd, "/*");

        ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/api/*");
        jerseyServlet.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");
        jerseyServlet.setInitParameter("javax.ws.rs.Application", "com.gigaspaces.DiscoveryApp");
        jerseyServlet.setInitParameter("com.sun.jersey.spi.container.ContainerRequestFilters", "com.sun.jersey.api.container.filter.LoggingFilter");
        jerseyServlet.setInitParameter("com.sun.jersey.spi.container.ContainerResponseFilters", "com.sun.jersey.api.container.filter.LoggingFilter");
        jerseyServlet.setInitParameter("com.sun.jersey.config.feature.Trace", "true");

        jerseyServlet.setInitOrder(0);
        try {
            server.setHandler(context);
            server.start();
            for (Connector connector : server.getConnectors()) {
                logger.info("connector: {}, protocols {}, transport {}", connector.getName(), connector.getProtocols(), connector.getTransport());
            }
            logger.info("server started!");
            server.join();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            server.destroy();
        }
    }

}
