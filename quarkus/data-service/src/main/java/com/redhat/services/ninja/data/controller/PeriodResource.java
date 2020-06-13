package com.redhat.services.ninja.data.controller;

import com.redhat.services.ninja.data.operation.IdentifiableDatabaseOperations;
import com.redhat.services.ninja.data.service.DatabaseEngine;
import com.redhat.services.ninja.entity.Period;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/data/period")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PeriodResource extends AbstractIdentifiableResource<String, Period, IdentifiableDatabaseOperations<String, Period>> {
    @Inject
    DatabaseEngine engine;

    @Override
    public DatabaseEngine getEngine() {
        return engine;
    }

    @Override
    public IdentifiableDatabaseOperations<String, Period> getOperations() {
        return engine.getPeriodOperations();
    }
}
