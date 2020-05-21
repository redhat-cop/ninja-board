package com.redhat.services.ninja.client;

import com.redhat.services.ninja.entity.User;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("data/user")
@RegisterRestClient
@ApplicationScoped
public interface UserClient {
    @GET
    @Path("{identifier}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    User get(@PathParam("identifier") String username);
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    User create(User user);
}
