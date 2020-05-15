package com.redhat.services.ninja.data.controller;

import com.redhat.services.ninja.entity.Identifiable;
import com.redhat.services.ninja.data.operation.IdentifiableDatabaseOperations;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public abstract class AbstractIdentifiableResource<I, T extends Identifiable<I>, O extends IdentifiableDatabaseOperations<I, T>> extends AbstractResource<T, O> {

    @GET
    @Path("{identifier}")
    public T get(@PathParam("identifier") I identifier) {
        return getOperations().get(identifier);
    }

    @PUT
    public T update(T object) {
        return create(object);
    }

    @DELETE
    @Path("/{identifier}")
    public T delete(@PathParam("identifier") I identifier) {
            return getOperations().delete(identifier);
    }
}
