package com.redhat.cop.giveback.legacy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.redhat.cop.giveback.Database2;
import com.redhat.services.portfolio.utils.Json;
import com.redhat.sso.ninja.utils.Http;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;


/**
 * MJA trying to make this controller obsolete by splitting it out into more understandable code
 */
@Path("/")
public class ManagementController {
  private static final Logger log=Logger.getLogger(ManagementController.class);
  
  public static boolean isLoginEnabled(){
    return "true".equalsIgnoreCase(Config.get().getOptions().get("login.enabled"));
  }
  

  
  // support function api - it checks all users trello ids to see if they exist
  @GET
  @Path("/checkTrelloIDs")
  @Deprecated public Response checkTrelloIds(@QueryParam("max") String pMax) throws IOException, InterruptedException{
    int max=Integer.valueOf(pMax!=null?pMax:"-1");
    
    Map<String,String> unknownUsers=new HashMap<String,String>();
    Database2 db=Database2.get();
    int count=0;
    for(Entry<String, Map<String, String>> e:db.getUsers().entrySet()){
      count+=1;
      String trelloId=e.getValue().get("trelloId");
      if (null==trelloId || "null".equals(trelloId.trim().toLowerCase())|| "".equals(trelloId.trim().toLowerCase())){
        unknownUsers.put(e.getKey(), "Not Registered?");
        continue;
      }
//      System.out.println("Checking trello user: "+trelloId);
      String trelloCheckUrl="https://api.trello.com/1/members/"+trelloId;
      int rc=Http.get(trelloCheckUrl).responseCode;
      
      long wait=1000/(100/10); //(no more than 100 requests per 10 seconds, or 10 per second)
      
      if (200==rc){
        // user exists
      }else if(404==rc){
        // user doesnt exist
        unknownUsers.put(e.getKey(), trelloId);
      }else if(429==rc){ // Too Many Requests
        int waitInSeconds=10;
        System.out.println("Waiting a while ("+waitInSeconds+"s) due to 'Too Many Requests'! HTTP 429 received");
        Thread.sleep(waitInSeconds*1000);
//        break;
      }
      Thread.sleep(wait+100/*ms*/);
      
      if (max>0 && count>=max) break;
    }
    
    return Http.newResponse(200, "text/html; charset=UTF-8", Json.newObjectMapper(true).writeValueAsString(unknownUsers)).build();
  }
  
//  // returns the config file contents - used in admin UI & backup purposes
//  @GET
//  @Path("/config/get")
//  public Response configGet() throws JsonProcessingException {
//    return Http.newResponse(200, "text/html; charset=UTF-8", Json.toJson(Config.get())).build();
//  }
  
//  // saves a new complete config
//  @POST
//  @Path("/config/save")
//  @Consumes(MediaType.APPLICATION_OCTET_STREAM) 
//  public Response configSave(InputStream is) throws StreamReadException, DatabindException, IOException {
//    log.info("Saving config");
//    Config newConfig=Json.newObjectMapper(true).readValue(is, Config.class);
//    
//    log.debug("New Config = "+Json.toJson(newConfig));
//    newConfig.save();
//    
//    // re-start the heartbeat with a new interval
//    //TODO: reset the heartbeat ONLY if the interval changed from what it was before
////    String startTime=(String)Config.get().getOptions().get("heartbeat.startTime");
////    if (null==startTime) startTime="21:00"; // default to 9PM
////    String heartbeatInterval=newConfig.getOptions().get("heartbeat.intervalInSeconds");
////    if (null!=heartbeatInterval && heartbeatInterval.matches("\\d+")){
////      log.info("Re-setting heartbeat with interval: "+heartbeatInterval);
//      Heartbeat2.stop();
//      Heartbeat2.start(Config.get());
////      Heartbeat2.start(Long.parseLong(heartbeatInterval));
////      Heartbeat2.start(Config.get());
////    }
//    
//    Database2.maxEventEntries=0;
//    Database2.getMaxEventEntries();
//    
//    Database2.resetInstance();
//    Database2.get(); //reload it
//    
//    log.debug("Config Saved");
//    return Http.newResponse(200, "text/html; charset=UTF-8", Json.newObjectMapper(true).writeValueAsString(Config.get())).build();
//  }

  

}

