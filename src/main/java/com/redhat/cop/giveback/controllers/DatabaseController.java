package com.redhat.cop.giveback.controllers;

import java.io.IOException;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.redhat.cop.giveback.Database2;
import com.redhat.services.portfolio.utils.Json;
import com.redhat.sso.ninja.utils.Http;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/")
public class DatabaseController extends CommonController{
  private static final Logger log=LoggerFactory.getLogger(DatabaseController.class);

  // returns the database content - used in admin UI & backup purposes
  @GET
  @Path("/api/database/get")
  public Response getDatabase() throws IOException{
    return Response.status(200).entity(Json.newObjectMapper(true).writeValueAsString(Database2.get())).build();
  }
  
  @PUT @Path("/api/database/save") // Used to save the raw config json displayed on the config page
  public Response databaseSave2(String json) throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException{
    log.info("Saving database");
//    Database2 db=Json.newObjectMapper(true).readValue(IOUtils2.toStringAndClose(is), new TypeReference<Database2>() {});
    Database2 db=Json.toObject(json, new TypeReference<Database2>() {});
    db.save();
    Database2.resetInstance();
    Database2.get(); // reload instance in memory

//    Config config3=Json.toObject(json, Config.class);
//    config3.save();
//    SearchController2.updateCaches();
    return Http.newResponse(200, "application/json; charset=UTF-8", Json.toJson(Database2.get())).build();
  }
  
//  // saves/replaces the database content
//  @POST
//  @Path("/api/database/save")
//  @Consumes(MediaType.APPLICATION_OCTET_STREAM)
//  public Response databaseSave(InputStream is) throws IOException{
//    System.out.println("Saving database");
//    Database2 db=Json.newObjectMapper(true).readValue(IOUtils2.toStringAndClose(is), new TypeReference<Database2>() {});
//    
//    //System.out.println("New DB = "+Json.newObjectMapper(true).writeValueAsString(db));
//    db.save();
//    
//    Database2.resetInstance();
//    Database2.get(); // reload instance in memory
//    
//    System.out.println("New Database Saved");
//    return Response.status(200).entity(Json.newObjectMapper(true).writeValueAsString(Database2.get())).build();
//  }
  
}
