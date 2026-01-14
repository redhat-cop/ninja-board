package com.redhat.cop.giveback.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.redhat.cop.giveback.Database2;
import com.redhat.cop.giveback.chart.ChartJson;
import com.redhat.cop.giveback.chart.DataSet;
import com.redhat.services.portfolio.utils.ChatNotification_v2;
import com.redhat.services.portfolio.utils.IOUtils2;
import com.redhat.services.portfolio.utils.ChatNotification_v2.ChatEvent;
import com.redhat.services.portfolio.utils.Json;
import com.redhat.services.portfolio.utils.MapBuilder;
import com.redhat.sso.ninja.utils.Http;
import com.redhat.sso.ninja.utils.LevelsUtil;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
public class ScorecardController extends CommonController{
  private static final Logger log=LoggerFactory.getLogger(ScorecardController.class);


  // Admin/Support UI call to list all users and their scorecards
  @GET
  @Path("/api/scorecards")
  public Response getScorecards() throws JsonMappingException, IOException{
    Database2 db=Database2.get();
    List<Map<String, Object>> data=new ArrayList<Map<String,Object>>();
    
    Set<String> fields=new HashSet<String>();
    
    for(Entry<String, Map<String, String>> u:db.getUsers().entrySet()){
      String username=u.getKey();
      Map<String,String> userInfo=u.getValue();
      Map<String, Integer> scorecard=db.getScoreCards().get(u.getKey());
      
      Map<String, Object> row=new HashMap<String, Object>();
      row.put("id", username);
      row.put("name", userInfo.containsKey("displayName")?userInfo.get("displayName"):username);
      int total=0;
      if (null!=scorecard){
        for(Entry<String, Integer> s:scorecard.entrySet()){
          row.put(s.getKey().replaceAll("\\.", " "), s.getValue());
          total+=s.getValue();
          fields.add(s.getKey().replaceAll("\\.", " "));
        }
        row.put("total", total);
        row.put("level", userInfo.get("level"));
      }else{
        row.put("total", 0);
        row.put("level", "ZERO");
      }
      
      // points to next level
      if (null==userInfo.get("level") || null==LevelsUtil.get().getNextLevel(userInfo.get("level"))){
        log.error("Invalid level for user "+row.get("name")+" : "+Json.newObjectMapper(true).writeValueAsString(userInfo));
        row.put("pointsToNextLevel", 0);
      }else{
        Integer pointsToNextLevel=LevelsUtil.get().getNextLevel(userInfo.get("level")).getLeft()-total;
        if (pointsToNextLevel<0) pointsToNextLevel=0;
        row.put("pointsToNextLevel", pointsToNextLevel);
      }
      
      data.add(row);
    }
    
    // fill in the missing points fields with zero's
    for(Map<String, Object> e:data){
      for (String field:fields){
        if (!e.containsKey(field)){
          e.put(field, 0);
        }
      }
    }
    
    Map<String,Object> wrapper=new HashMap<String, Object>();
    List<Map<String,String>> columns=new ArrayList<Map<String, String>>();
//    columns.add(Config.get().new MapBuilder<String,String>().put("title","ID").put("data", "id").build());
    columns.add(new MapBuilder<String,String>().put("title","Name").put("data", "name").build());
    columns.add(new MapBuilder<String,String>().put("title","Total").put("data", "total").build());
    
    columns.add(new MapBuilder<String,String>().put("title","Ninja Belt").put("data", "level").build());
    columns.add(new MapBuilder<String,String>().put("title","Points to next level").put("data", "pointsToNextLevel").build());
    
    for(String field:fields)
      columns.add(new MapBuilder<String,String>().put("title",field).put("data", field).build());  
    
    wrapper.put("columns", columns);
    wrapper.put("data", data);
    
    return Http.newResponse(200, "text/html; charset=UTF-8", Json.newObjectMapper(true).writeValueAsString(wrapper)).build();
  }
  
  
  // Admin UI call (to edit the user) - returns the scorecard and userInfo data for be able to display and edit one specific user
  @GET
  @Path("/api/scorecard/{user}")
  public Response getScorecard(@PathParam("user") String user) throws JsonMappingException, IOException{
    Database2 db=Database2.get();
    
    log.debug("Request made for user ["+user+"]");
    
    Map<String, Integer> scorecard=db.getScoreCards().get(user);
    Map<String, String> userInfo=db.getUsers().get(user);
    
    log.debug(user+" user data for scorecards "+(scorecard!=null?"found":"NOT FOUND!"));
    log.debug(user+" user data for userInfo "+(userInfo!=null?"found":"NOT FOUND!"));
    
    String payload="{\"status\":\"ERROR\",\"message\":\"Unable to find user: "+user+"\", \"displayName\":\"You ("+user+") are not registered\"}";
    
    Map<String, Object> data=new HashMap<String, Object>();
    data.put("userId", user);
    if (null!=scorecard)
      data.putAll(scorecard);
    if (null!=userInfo)
      data.putAll(userInfo);
    payload=Json.newObjectMapper(true).writeValueAsString(data);
    
    return Http.newResponse(payload.contains("ERROR")?500:200, "text/html; charset=UTF-8", payload).build();
  }
  
  // User Dashboard UI call - returns the payload to render a chart displaying the breakdown of how many points came from which pool (trello, github PR, github reviewed PR's etc..)
  @GET
  @Path("/api/scorecard/breakdown/{user}")
  public Response getUserBreakdown(@PathParam("user") String user) throws JsonMappingException, IOException{
    Database2 db=Database2.get();
    Map<String, Integer> scorecard=db.getScoreCards().get(user);
    
    ChartJson chart=new ChartJson();
    chart.getDatasets().add(new DataSet());
    chart.getDatasets().get(0).setBorderWidth(1);
    if (null!=scorecard){
      for(Entry<String, Integer> s:scorecard.entrySet()){
        chart.getLabels().add(s.getKey());
        chart.getDatasets().get(0).getData().add(s.getValue());
      }
    }else{
      chart.getLabels().add("No Points");
      chart.getDatasets().get(0).getData().add(0);
    }
    return Http.newResponse(200, "text/html; charset=UTF-8", Json.newObjectMapper(true).writeValueAsString(chart)).build();
  }
  
  // User Dashboard UI call - returns user scorecard data to display the user dashboard (mojo)
  @GET
  @Path("/api/scorecard/summary/{user}")
  public Response getScorecardSummary(@PathParam("user") String user) throws JsonMappingException, IOException{
    Database2 db=Database2.get();
    
    log.debug("Request made for user ["+user+"]");
    
    Map<String, Integer> scorecard=db.getScoreCards().get(user);
    Map<String, String> userInfo=db.getUsers().get(user);
    
    log.debug(user+" user data for scorecards "+(scorecard!=null?"found":"NOT FOUND!"));
    log.debug(user+" user data for userInfo "+(userInfo!=null?"found":"NOT FOUND!"));
    
    String payload="{\"status\":\"ERROR\",\"message\":\"Unable to find user: "+user+"\", \"displayName\":\""+user+" not registered\"}";
    
    if (userInfo!=null){
      Map<String, Object> data=new HashMap<String, Object>();
      data.put("userId", user);
      
      Map<String, Integer> consolidatedTotals=new HashMap<String, Integer>();
      Integer total=0;
      if (scorecard!=null){
        for(Entry<String, Integer> e:scorecard.entrySet()){
          String consolidatedKey=e.getKey().substring(0, e.getKey().contains(".")?e.getKey().indexOf("."):e.getKey().length());
          if (!consolidatedTotals.containsKey(consolidatedKey)) consolidatedTotals.put(consolidatedKey, 0);
          consolidatedTotals.put(consolidatedKey, consolidatedTotals.get(consolidatedKey)+e.getValue());
          total+=e.getValue();
        }
      }
      data.put("total", total);
      data.putAll(consolidatedTotals);
      data.putAll(userInfo);
      payload=Json.newObjectMapper(true).writeValueAsString(data);
    }
    
    return Http.newResponse(200, "text/html; charset=UTF-8", payload).build();
  }

  // called from /admin/scorecards - updates an existing user with new values & points
  @PUT
  @Path("/api/scorecard/{user}")
  public Response saveScorecard(String payload, @PathParam("user") String user) throws JsonMappingException, IOException{
    log.debug("Saving "+ payload);
    Database2 db=Database2.get();
    
    Map<String, Object> map = Json.toObject(payload, new TypeReference<HashMap<String,Object>>(){});
    String username=(String)map.get("userId");
    
    Map<String, String> userInfo=db.getUsers().get(username);
    Map<String, Integer> scorecard=db.getScoreCards().get(username);
    
    for(String k:map.keySet()){
      if (!k.equals("userId")){

        if (userInfo.containsKey(k)) {
          if (!userInfo.get(k).equals(map.get(k))){ // if it's changed then...
            log.debug("Setting 'userInfo."+k+"' to "+(String)map.get(k));
            db.addEvent("User Update", user, k+" changed from "+userInfo.get(k)+" to "+(String)map.get(k));
            userInfo.put(k, (String)map.get(k));
          }
        }else if (scorecard.containsKey(k)){
          if (!scorecard.get(k).equals(map.get(k))){ // if it's changed then...
            log.debug("Setting 'scorecard."+k+"' to "+(String)map.get(k));
            db.addEvent("User Update", user, k+" changed from "+scorecard.get(k)+" to "+(String)map.get(k));
            scorecard.put(k, Integer.parseInt((String)map.get(k)));
          }
        }else{
          if (!userInfo.get(k).equals(map.get(k))){ // if it's changed then...
            log.debug("Setting 'userInfo."+k+"' to "+(String)map.get(k));
            db.addEvent("User Update", user, k+" set as "+(String)map.get(k));
            userInfo.put(k, (String)map.get(k));
          }
          //// ALERT! unknown field
          //log.error("UNKNOWN FIELD: "+k+" = "+map.get(k));
        }
        
      }
    }
    
    db.save();
    return Http.newResponse(200, "text/html; charset=UTF-8", Json.newObjectMapper(true).writeValueAsString("OK")).build();
  }
  
  

  // Admin UI to be able to update a single user field
  
  @PUT
  @Path("/api/users/{user}")
  @Consumes(MediaType.APPLICATION_OCTET_STREAM) 
  public Response updateUserProperty(InputStream is, @PathParam("user") String user) throws  IOException{
    
    Map<String,String> values=Json.toObject(IOUtils2.toStringAndClose(is), new TypeReference<Map<String,String>>() {});
    Database2 db=Database2.get();
    Map<String, String> userInfo=db.getUsers().get(user);
    if (null==userInfo) throw new JsonMappingException("User info for '"+user+"' not found");
    for (Entry<String, String> e:values.entrySet()){
      
      String existingValue=userInfo.get(e.getKey());
      if ("displayName".equals(e.getKey()) || e.getKey().endsWith("Id")){ // ensure we only update info fields, not points or levels
        
        if (null==existingValue){ // no existing, value so just add it
          userInfo.put(e.getKey(), e.getValue());
          db.addEvent("User Update", user, e.getKey()+" added as "+e.getValue());
        }else{ // value exists, so update it (if it's changed)
          if (!existingValue.equals(e.getValue())){
            userInfo.put(e.getKey(), e.getValue());
            // Add an event entry so we know what was changed and when
            db.addEvent("User Update", user, e.getKey()+" changed from "+existingValue+" to "+e.getValue());
          }
        }
        
      }else{
        // Field NOT ALLOWED - potential fraudulent activity
        if (e.getKey().contains("level")){
          log.warn("Suspicious Activity: User ["+user+"] attempting to update their level from ["+existingValue+"] to ["+e.getValue()+"]");
          ChatNotification_v2.get().send(ChatEvent.onWarning, "Suspicious Activity: User "+user+" has attempted to update their level via the API from "+existingValue+" to "+e.getValue()+"");
        }
      }
      
    }
    
    // push the new data to the graphs proxy, so when the page refreshes it loads the new values from the proxy
    new ScriptsController().pushGraphDataFor(user);
    
    db.save();
    
    return Response.status(200).build();
  }
}
