package com.redhat.services.ninja.controller;

import com.redhat.services.ninja.client.ScorecardClient;
import com.redhat.services.ninja.entity.Scorecard;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Path("/scorecard")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ScorecardResource {
    @Inject
    @RestClient
    ScorecardClient scorecardClient;
    

    @POST
    @Path("/{username}/{category}")
    @Consumes(MediaType.TEXT_PLAIN)
    public Scorecard increment(
            @PathParam("username") String username,
            @PathParam("category") String category,
            Integer incrementedBy
    ) {
        Scorecard scorecard = get(username);

        scorecard.increment(category, incrementedBy);

        return scorecardClient.update(scorecard);
    }

    @GET
    @Path("/{username}")
    public Scorecard get(@PathParam("username") String username) {
        return Optional.ofNullable(scorecardClient.get(username))
                .orElseThrow(() -> new WebApplicationException(
                        "Scorecard for " + username + " NOT FOUND.",
                        Response.Status.NOT_FOUND
                ));
    }

    @GET
    public List<Scorecard> getAll() {
        return scorecardClient.getAll();
    }
}