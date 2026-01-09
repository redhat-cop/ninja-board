package com.redhat.cop.giveback.controllers;

import static java.lang.String.format;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.redhat.sso.ninja.utils.Http;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

@Path("/")
public class AdminController extends CommonController{
  private static final Logger log=LoggerFactory.getLogger(AdminController.class);
  private static final List<String> knownAdminUris=Lists.newArrayList("admin","scorecards","leaderboard","database","tasks","events","config");

  @GET @Path("/admin")
  public Response admin() throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException{
    return Http.newResponse(200, "text/html; charset=UTF-8", buildPageTemplate("/admin-scorecards.html")).build();
  }

  @GET @Path("/admin/{page}")
  public Response loadAdminPage(@PathParam("page") String page) throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException{
    Preconditions.checkArgument(knownAdminUris.contains(page), format("Admin page unknown [%s] - please check config in AdminController",page));
    return Http.newResponse(200, "text/html; charset=UTF-8", buildPageTemplate("/admin-"+page+".html")).build();
  }
}
