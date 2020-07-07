package com.redhat.services.ninja.controller;

import com.redhat.services.ninja.client.EventClient;
import com.redhat.services.ninja.entity.Event;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/event")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EventResource {

    @Inject
    @RestClient
    EventClient eventClient;

    @GET
    public List<Event> getAll() {
        return eventClient.getAll();
    }
}