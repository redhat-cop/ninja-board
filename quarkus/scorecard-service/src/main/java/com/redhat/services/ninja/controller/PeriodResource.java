package com.redhat.services.ninja.controller;

import com.redhat.services.ninja.client.PeriodClient;
import com.redhat.services.ninja.client.ScorecardClient;
import com.redhat.services.ninja.entity.Period;
import com.redhat.services.ninja.entity.Record;
import com.redhat.services.ninja.entity.Scorecard;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/history")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PeriodResource {
    @Inject
    @RestClient
    PeriodClient periodClient;

    @Inject
    @RestClient
    ScorecardClient scorecardClient;

    @POST
    @Path("/{periodName}")
    @Consumes(MediaType.TEXT_PLAIN)
    public Period create(@PathParam("periodName") String periodName) {
        Optional.ofNullable(periodClient.get(periodName)).ifPresent(period -> {
                    throw new WebApplicationException("Period already exist", 409);
                }
        );

        List<Scorecard> scorecards = scorecardClient.getAll();

        Map<String, Record> records = scorecards.stream()
                .collect(
                        Collectors.toMap(
                                Scorecard::getIdentifier,
                                scorecard -> new Record(scorecard.getLevel(), scorecard.getTotal())));

        Period period = new Period(periodName, records);

        periodClient.create(period);

        scorecards.stream().map(Scorecard::getIdentifier).forEach(scorecardClient::delete);

        scorecards.stream().map(Scorecard::getIdentifier).map(Scorecard::new).forEach(scorecardClient::create);

        return periodClient.create(period);
    }

    @GET
    @Path("/{identifier}")
    public Period get(@PathParam("identifier") String identifier) {
        return Optional.ofNullable(periodClient.get(identifier))
                .orElseThrow(() -> new WebApplicationException(
                        "Period for " + identifier + " NOT FOUND.",
                        Response.Status.NOT_FOUND
                ));
    }
}