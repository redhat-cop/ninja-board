package com.redhat.services.ninja.controller;

import com.redhat.services.ninja.client.EventClient;
import com.redhat.services.ninja.client.ScorecardClient;
import com.redhat.services.ninja.entity.Event;
import com.redhat.services.ninja.entity.Point;
import com.redhat.services.ninja.entity.Scorecard;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Path("/scorecard")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ScorecardResource {

    private static final Comparator<Scorecard> LEADERBOARD_COMPARATOR
            = Comparator.comparingDouble(Scorecard::getTotal).reversed();

    @Inject
    @RestClient
    ScorecardClient scorecardClient;

    @Inject
    @RestClient
    EventClient eventClient;

    @POST
    @Path("/{username}")
    public Scorecard increment(
            @PathParam("username") String username,
            Point point
    ) {
        Scorecard scorecard = get(username);
        int pastScore = scorecard.getTotal();
        scorecard.increment(point.getPool(), point.getValue());
        Scorecard updatedCard = scorecardClient.update(scorecard);

        Event event = Event.Type.POINT_INCREMENT.createEvent(
                username, point.getValue(), pastScore, scorecard.getTotal(), point.getPool(), point.getReference()
        );
        event.setUser(username);

        eventClient.create(event);

        return updatedCard;
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
        List<Scorecard> scorecards = scorecardClient.getAll();
        scorecards.sort(LEADERBOARD_COMPARATOR);
        return scorecards;
    }
}