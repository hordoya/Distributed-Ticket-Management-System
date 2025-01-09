package de.uniba.rz.backend;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;

import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)

public class WebTicketApi extends Application {
    public Set<Class<?>> getClasses(){
        final Set<Class<?>> resources = new HashSet<>();
        resources.add(WebTicketResources.class);
        return resources;
    }
}
