package com.redhat.services.ninja.controller;

import com.redhat.services.ninja.client.UserClient;
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

    @GET
    @Path("/{uid}")
    public RedHatUser findByUid(@PathParam("uid") String uid) {
        return searchById(uid).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @POST
    public User register(User user){
        Optional.ofNullable(user).orElseThrow(() -> new WebApplicationException(Response.Status.BAD_REQUEST));
        RedHatUser redHatUser = searchById(user.getUsername()).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        user.setRegion(redHatUser.getLocation());
        return userClient.create(user);
    }

    @GET
    @Path("/{key}/{value}")
    public List<RedHatUser> find(@PathParam("key") String key, @PathParam("value") String value) {
        return search(key, value);
    }
    
    private Optional<RedHatUser> searchById(String uid){
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