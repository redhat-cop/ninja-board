package com.redhat.services.ninja.data.controller;

import com.redhat.services.ninja.entity.Event;
import com.redhat.services.ninja.data.operation.BasicDatabaseOperations;
import com.redhat.services.ninja.data.service.DatabaseEngine;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/data/event")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EventResource extends AbstractResource<Event, BasicDatabaseOperations<Event>>{
    @Inject
    DatabaseEngine engine;

    @Override
    public DatabaseEngine getEngine() {
        return engine;
    }

    @Override
    public BasicDatabaseOperations<Event> getOperations() {
        return engine.getEventOperations();
    }
}
