package com.redhat.services.ninja.data.controller;

import com.redhat.services.ninja.data.operation.IdentifiableDatabaseOperations;
import com.redhat.services.ninja.entity.ErrorResponse;
import com.redhat.services.ninja.entity.Identifiable;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public abstract class AbstractIdentifiableResource<I, T extends Identifiable<I>, O extends IdentifiableDatabaseOperations<I, T>> extends AbstractResource<T, O> {

    @GET
    @Path("{identifier}")
    public T get(@PathParam("identifier") I identifier) {
        return getOperations().get(identifier).orElseThrow(() -> {
                    String message = "Entity not found: " + identifier.toString();
                    Response response = Response.status(Response.Status.NOT_FOUND)
                            .entity(
                                    new ErrorResponse(message)
                            ).type(MediaType.APPLICATION_JSON_TYPE)
                            .build();

                    return new WebApplicationException(message, response);
                }
        );
    }

    @PUT
    public T update(T object) {
        return create(object);
    }

    @DELETE
    @Path("{identifier}")
    public T delete(@PathParam("identifier") I identifier) {
        return getOperations().delete(identifier);
    }
}
