package com.redhat.services.ninja.client;

import com.redhat.services.ninja.entity.Scorecard;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("data/scorecard")
@RegisterRestClient
@ApplicationScoped
public interface ScorecardClient {
    @GET
    @Path("{identifier}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Scorecard get(@PathParam("identifier") String username);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    List<Scorecard> getAll();

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Scorecard create(Scorecard scorecard);

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Scorecard update(Scorecard scorecard);
}
