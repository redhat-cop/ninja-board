package com.redhat.services.ninja.controller;

import com.redhat.services.ninja.service.LdapService;
import com.redhat.services.ninja.user.RedHatUser;

import javax.inject.Inject;
import javax.naming.NamingException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/user")
public class UserResource {

    @Inject
    LdapService ldapService;

    @GET
    @Path("/{uid}")
    @Produces(MediaType.APPLICATION_JSON)
    public RedHatUser findByUid(@PathParam("uid") String uid) {
        return search("uid", uid).stream().findFirst().orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @GET
    @Path("/{key}/{value}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<RedHatUser> find(@PathParam("key") String key, @PathParam("value") String value) {
        return search(key, value);
    }

    public List<RedHatUser> search(String key, String value) {
        try {
            return ldapService.search(key, value);
        } catch (NamingException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}