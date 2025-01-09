package de.uniba.rz.backend;

import com.sun.net.httpserver.HttpServer;
import jakarta.ws.rs.core.UriBuilder;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

public class WebServiceRemoteAccess extends RemoteAccess{
    private String baseURL;
    private URI baseUri;
    private ResourceConfig config;
    private HttpServer server;
    public static TicketStore ticketStore;

    public WebServiceRemoteAccess(String baseURL) {
        this.baseURL = baseURL;
    }

    @Override
    public void prepareStartup(TicketStore ticketStore) {
        WebServiceRemoteAccess.ticketStore = ticketStore;


        baseUri =  UriBuilder.fromUri(baseURL).build();

        config = ResourceConfig.forApplicationClass(WebTicketApi.class);

    }

    @Override
    public void shutdown() {
        System.out.println("Stopping server");
        server.stop(1);
    }

    @Override
    public void run() {
        System.out.println("baseuri: " + baseUri.getPath());
        server = JdkHttpServerFactory.createHttpServer(baseUri, config);

        System.out.println("Server ready to serve your JAX-RS requests at..." + baseURL);
    }
}
