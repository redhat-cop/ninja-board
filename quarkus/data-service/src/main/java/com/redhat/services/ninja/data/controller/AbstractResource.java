package com.redhat.services.ninja.data.controller;

import com.redhat.services.ninja.data.operation.BasicDatabaseOperations;
import com.redhat.services.ninja.data.service.DatabaseEngine;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public abstract class AbstractResource<T, O extends BasicDatabaseOperations<T>> {

    @POST
    public T create(T entity) {
        T createdEntity = getOperations().create(entity);
        save();
        return createdEntity;
    }

    @GET
    public List<T> get() {
        return getOperations().getAll();
    }

    private void save() {
        try {
            getEngine().save();
        } catch (IOException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    public abstract DatabaseEngine getEngine();

    public abstract O getOperations();

}
