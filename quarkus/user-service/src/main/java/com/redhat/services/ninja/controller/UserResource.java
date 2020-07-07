package com.redhat.services.ninja.controller;

import com.redhat.services.ninja.client.EventClient;
import com.redhat.services.ninja.client.ScorecardClient;
import com.redhat.services.ninja.client.UserClient;
import com.redhat.services.ninja.entity.Event;
import com.redhat.services.ninja.entity.Scorecard;
import com.redhat.services.ninja.entity.User;
import com.redhat.services.ninja.service.LdapService;
import com.redhat.services.ninja.user.RedHatUser;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.naming.NamingException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Path("/user")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    LdapService ldapService;

    @Inject
    @RestClient
    UserClient userClient;

    @Inject
    @RestClient
    ScorecardClient scorecardClient;

    @Inject
    @RestClient
    EventClient eventClient;

    @GET
    @Path("/{uid}")
    public RedHatUser findByUid(@PathParam("uid") String uid) {
        return searchById(uid).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @POST
    public Response register(User user) {
        Optional.ofNullable(user).orElseThrow(() -> new WebApplicationException(Response.Status.BAD_REQUEST));
        RedHatUser redHatUser = searchById(user.getUsername()).orElseThrow(() -> {
            Event event = Event.Type.FAILED_LDAP_REGISTRATION.createEvent(user.getUsername());
            event.setUser(user.getUsername());
            eventClient.create(event);
            return new WebApplicationException(Response.Status.NOT_FOUND);
        });

        user.setRegion(redHatUser.getLocation());
        User createdUser = userClient.create(user);
        Scorecard scorecard = new Scorecard();
        scorecard.setUsername(createdUser.getUsername());
        scorecardClient.create(scorecard);

        Event event = Event.Type.SUCCESSFUL_REGISTRATION.createEvent(user.getUsername());
        event.setUser(createdUser.getUsername());

        eventClient.create(event);

        return Response.status(Response.Status.CREATED).entity(createdUser).build();
    }

    @GET
    @Path("/{key}/{value}")
    public List<RedHatUser> find(@PathParam("key") String key, @PathParam("value") String value) {
        return search(key, value);
    }

    private Optional<RedHatUser> searchById(String uid) {
        return search("uid", uid).stream().findFirst();
    }

    private List<RedHatUser> search(String key, String value) {
        try {
            return ldapService.search(key, value);
        } catch (NamingException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}