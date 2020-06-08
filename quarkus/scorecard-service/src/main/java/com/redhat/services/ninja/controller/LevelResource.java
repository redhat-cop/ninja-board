package com.redhat.services.ninja.controller;

import com.redhat.services.ninja.client.LevelClient;
import com.redhat.services.ninja.entity.Level;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/level")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LevelResource {
    @Inject
    @RestClient
    LevelClient levelClient;

    @GET
    public List<Level> getAll() {
        return levelClient.getAll();
    }
}
