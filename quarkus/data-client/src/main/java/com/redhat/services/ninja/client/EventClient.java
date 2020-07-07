package com.redhat.services.ninja.client;

import com.redhat.services.ninja.entity.Event;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("data/event")
@RegisterRestClient
@ApplicationScoped
public interface EventClient {
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Event create(Event entity);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<Event> getAll();
}
