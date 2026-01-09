package com.redhat.cop.giveback.controllers;

import static com.redhat.services.portfolio.utils.Resources.getTemplateAsString;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.redhat.cop.giveback.Database2;
import com.redhat.cop.giveback.Heartbeat2;
import com.redhat.cop.giveback.legacy.Config;
import com.redhat.services.portfolio.utils.Json;
import com.redhat.sso.ninja.utils.Http;

import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/")
public abstract class CommonController{

  @GET @Path("/config") // display the config editor page
  public Response configPage(@CookieParam("RHServicesPortfolio_search_token") String token) throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException{
    return Http.newResponse(200, "text/html; charset=UTF-8", buildPageTemplate("/config.html")).build();
  }

  @GET @Path("/_/config") // Used to get the raw config json to display on the config page
  public Response configLoad() throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException{
    return Http.newResponse(200, "application/json; charset=UTF-8", Json.toJson(Config.get())).build();
  }

  @PUT @Path("/_/config") // Used to save the raw config json displayed on the config page
  public Response configSave(String jsonString) throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException{
    Config config3=Json.toObject(jsonString, Config.class);
    System.out.println("NewConfig = "+Json.toJson(config3));
    Config.instance=config3;
    config3.save();
    Config.instance=null;
    
    Database2.maxEventEntries=0;
    Database2.getMaxEventEntries();
    Database2.resetInstance();
    Database2.get();
    
    Heartbeat2.stop();
    Heartbeat2.start(Config.get());
    return Http.newResponse(200, "application/json; charset=UTF-8", null).build();
  }

  protected String buildPageTemplate(String pagePath) throws FileNotFoundException, IOException{
    String prefix=this.getClass().getSimpleName().toLowerCase().matches(".*(admin|auth|config|database|common|events|scorecard).*")?"admin-":"";
//    System.out.println("prefix = "+prefix);
//    System.out.println("pagePath = "+pagePath);
//    System.out.println("clazz = "+this.getClass().getSimpleName());
    
    
    
    String parent="";
    if (pagePath.substring(1).contains("/")) parent=pagePath.substring(0, pagePath.substring(1).indexOf("/")+2);
//    parent=(StringUtils.isNotBlank(prefix)?"/"+parent:"");
    return getTemplateAsString(pagePath).replace("<!--HEADER_TEMPLATE-->", getTemplateAsString(parent+prefix+"header.html")).replace("<!--NAV_TEMPLATE-->",getTemplateAsString(parent+prefix+"nav.html"));
  }

}
