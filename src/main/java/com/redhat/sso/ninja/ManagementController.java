package com.redhat.sso.ninja;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.google.common.base.Splitter;
import com.redhat.sso.ninja.Database2.EVENT_FIELDS;
import com.redhat.sso.ninja.chart.ChartJson;
import com.redhat.sso.ninja.chart.DataSet;
import com.redhat.sso.ninja.utils.Http;
import com.redhat.sso.ninja.utils.IOUtils2;
import com.redhat.sso.ninja.utils.Json;
import com.redhat.sso.ninja.utils.LevelsUtil;
import com.redhat.sso.ninja.utils.MapBuilder;

@Path("/")
public class ManagementController {
  private static final Logger log=Logger.getLogger(ManagementController.class);
  
//  public static void main(String[] asd) throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException{
//    System.out.println(java.sql.Date.valueOf(LocalDate.now()));
//    System.out.println(java.sql.Date.valueOf(LocalDate.now().minus(365, ChronoUnit.DAYS)));
//    System.out.println((1000 * 60 * 60 * 24));
//    System.out.println(TimeUnit.DAYS.toMillis(1));
////    System.out.println(new ManagementController().toNextLevel("BLUE", 7).toString());
//    
//    new ManagementController().yearEnd(null,  null,  "FY20");
//  }
  
  public static boolean isLoginEnabled(){
    return "true".equalsIgnoreCase(Config.get().getOptions().get("login.enabled"));
  }
  
  // common response created because post v79'ish of Chrome they introduced a SIGNED_EXCHANGE error without the following headers on every response
  private ResponseBuilder newResponse(int status){
    return Response.status(status)
     .header("Access-Control-Allow-Origin",  "*")
     .header("Content-Type","application/json")
     .header("Cache-Control", "no-store, must-revalidate, no-cache, max-age=0")
     .header("Pragma", "no-cache")
     .header("X-Content-Type-Options", "nosniff");
  }
  
  @POST
  @Path("/yearEnd/{priorYear}")
  public Response yearEnd(@Context HttpServletRequest request,@Context HttpServletResponse response,@PathParam("priorYear") String priorYear) throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException{
    log.info("Year Ending for - "+priorYear+". Note: This will loose some data (such as point buckets) as it archives the current years information");
    Database2 db=Database2.get();
    
    if (db.getScorecardHistory().containsKey(priorYear))
      return newResponse(400).entity("Can't do that - the key '"+priorYear+"' already exists!").build();
    
    // clear outstanding tasks
    db.getTasks().clear();
    
    // cleanup scorecards and backup in to a year dated bucket
    Map<String, String> history=new LinkedHashMap<String, String>();
    Map<String, Integer> totals=new HashMap<String, Integer>();
    for(Entry<String, Map<String, Integer>> e:db.getScoreCards().entrySet()){
      if (null!=e.getValue() && e.getValue().size()>0){
      String belt=db.getUsers().get(e.getKey()).get("level");
      String total=String.valueOf(ChartsController.total(e.getValue()));
      history.put(e.getKey(), belt+"|"+total);
      totals.put(e.getKey(), Integer.valueOf(total));
      }
    }
    
    // reorder by total
    List<Entry<String, Integer>> list=new LinkedList<Map.Entry<String, Integer>>(totals.entrySet());
    Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() { public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
      return (o2.getValue()).compareTo(o1.getValue());
    }});
    HashMap<String, Integer> sortedTotals=new LinkedHashMap<String, Integer>();
    for (Entry<String, Integer> e:list)
      sortedTotals.put(e.getKey(), e.getValue());
    
    Map<String, String> sortedHistory=new LinkedHashMap<String, String>();
    for(Entry<String, Integer> e:sortedTotals.entrySet())
      sortedHistory.put(e.getKey(), history.get(e.getKey()));
    
    // write the history for the 'priorYear'
    db.getScorecardHistory().put(priorYear, sortedHistory);
    
    // clear current points
    db.getScoreCards().clear();
    
    //clear current belt status
    for(Entry<String, Map<String, String>> e:db.getUsers().entrySet()){
      e.getValue().put("level", "ZERO");
      e.getValue().remove("levelChanged");
//      e.getValue().put("levelChanged", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
    }
    
    db.save();
    Database2.resetInstance();
    
    return newResponse(200).entity("OK, it's done!").build();
  }
  
  private String getParameter(HttpServletRequest request, String name, String defaultValue){
    if (null!=request.getParameter(name))
      return request.getParameter(name);
    return defaultValue;
  }
  
  
  // support function api - it checks all users trello ids to see if they exist
  @GET
  @Path("/checkTrelloIDs")
  public Response checkTrelloIds(@Context HttpServletRequest request) throws JsonGenerationException, JsonMappingException, IOException, InterruptedException{
    int max=Integer.valueOf(getParameter(request,"max","-1"));
    
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
    
    return newResponse(200).entity(Json.newObjectMapper(true).writeValueAsString(unknownUsers)).build();
  }
  
  @POST
  @Path("/login")
  public Response login(@Context HttpServletRequest request,@Context HttpServletResponse response) throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException{
    log.info("/login");
    String uri=IOUtils.toString(request.getInputStream());
    final Map<String, String> keyValues=Splitter.on('&').trimResults().withKeyValueSeparator("=").split(uri);
    log.info("Controller::login():: username="+keyValues.get("username") +", password=****");
    
    String jwtToken="";
    
    Map<String, String> userAttemptingLogin=Database2.get().getUsers().get(keyValues.get("username"));
    if (null!=userAttemptingLogin){
      String base64EncodedActualPassword=userAttemptingLogin.get("password");
      String base64EncodedPasswordAttempt=java.util.Base64.getEncoder().encodeToString(keyValues.get("password").getBytes());
      
      if (base64EncodedActualPassword.equals(base64EncodedPasswordAttempt)){
        log.info("Login successful");
        jwtToken="ok";
      }else{
        // incorrect password
      }
      
    }else{
      // user doesnt exist
    }
    
    if ("".equals(jwtToken)){
      log.info("Login failure");
    }
    
    request.getSession().setAttribute("x-access-token", jwtToken);
    return Response.status(302).location(new URI("../index.jsp")).header("x-access-token", jwtToken).build();
  }
  @GET
  @Path("/logout")
  public Response logout(@Context HttpServletRequest request,@Context HttpServletResponse response) throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException{
    log.info("/logout");
    request.getSession().setAttribute("x-access-token", null);
    request.getSession().invalidate();
    return Response.status(302).location(new URI("../index.jsp")).build();
  }
  
  // returns the config file contents - used in admin UI & backup purposes
  @GET
  @Path("/config/get")
  public Response configGet(@Context HttpServletRequest request,@Context HttpServletResponse response,@Context ServletContext servletContext) throws JsonGenerationException, JsonMappingException, IOException{
    return newResponse(200).entity(Json.newObjectMapper(true).writeValueAsString(Config.get())).build();
  }
  
  // saves a new complete config
  @POST
  @Path("/config/save")
  public Response configSave(@Context HttpServletRequest request,@Context HttpServletResponse response,@Context ServletContext servletContext) throws JsonGenerationException, JsonMappingException, IOException{
    log.info("Saving config");
    Config newConfig=Json.newObjectMapper(true).readValue(request.getInputStream(), Config.class);
    
    log.debug("New Config = "+Json.newObjectMapper(true).writeValueAsString(newConfig));
    newConfig.save();
    
    // re-start the heartbeat with a new interval
    //TODO: reset the heartbeat ONLY if the interval changed from what it was before
//    String startTime=(String)Config.get().getOptions().get("heartbeat.startTime");
//    if (null==startTime) startTime="21:00"; // default to 9PM
//    String heartbeatInterval=newConfig.getOptions().get("heartbeat.intervalInSeconds");
//    if (null!=heartbeatInterval && heartbeatInterval.matches("\\d+")){
//      log.info("Re-setting heartbeat with interval: "+heartbeatInterval);
      Heartbeat2.stop();
      Heartbeat2.start(Config.get());
//      Heartbeat2.start(Long.parseLong(heartbeatInterval));
//      Heartbeat2.start(Config.get());
//    }
    
    Database2.maxEventEntries=0;
    Database2.getMaxEventEntries();
    
    Database2.resetInstance();
    Database2.get(); //reload it
    
    log.debug("Config Saved");
    return newResponse(200).entity(Json.newObjectMapper(true).writeValueAsString(Config.get())).build();
  }

  // Runs the scripts immediately - critical feature for supporting the system
  @GET
  @Path("/scripts/runNow")
  public Response runScriptsNow(@Context HttpServletRequest request, @Context HttpServletResponse response, @Context ServletContext servletContext){
    Heartbeat2.runOnceAsync();
    Database2.resetInstance();
    Database2.get(); //reload it
    log.debug("Scripts run started - check logs for results");
    return newResponse(200).entity("RUNNING").build();
  }

  // Pushes the current database graph data to the external cache to be accessible by end users mojo dashboard - critical for support
  @GET
  @Path("/scripts/publishGraphs")
  public Response pushGraphDataOnly(@Context HttpServletRequest request, @Context HttpServletResponse response, @Context ServletContext servletContext){
  	Database2 db=Database2.get();
  	Config cfg=Config.get();
  	new Heartbeat2.HeartbeatRunnable(null).publishGraphsData(db, cfg);
    return newResponse(200).entity("RUNNING").build();
  }
  
  // returns the database content - used in admin UI & backup purposes
  @GET
  @Path("/database/get")
  public Response getDatabase() throws JsonGenerationException, JsonMappingException, IOException{
    return Response.status(200).entity(Json.newObjectMapper(true).writeValueAsString(Database2.get())).build();
  }
  
  // saves/replaces the database content
  @POST
  @Path("/database/save")
  public Response databaseSave(@Context HttpServletRequest request,@Context HttpServletResponse response,@Context ServletContext servletContext) throws JsonGenerationException, JsonMappingException, IOException{
    System.out.println("Saving database");
    Database2 db=Json.newObjectMapper(true).readValue(IOUtils2.toStringAndClose(request.getInputStream()), new TypeReference<Database2>() {});
    
    //System.out.println("New DB = "+Json.newObjectMapper(true).writeValueAsString(db));
    db.save();
    
    Database2.resetInstance();
    Database2.get(); // reload instance in memory
    
    System.out.println("New Database Saved");
    return Response.status(200).entity(Json.newObjectMapper(true).writeValueAsString(Database2.get())).build();
  }

  
  // Admin UI call (to edit the user) - returns the scorecard and userInfo data for be able to display and edit one specific user
  @GET
  @Path("/scorecard/{user}")
  public Response getScorecard(@PathParam("user") String user) throws JsonGenerationException, JsonMappingException, IOException{
    Database2 db=Database2.get();
    
    log.debug("Request made for user ["+user+"]");
    
    Map<String, Integer> scorecard=db.getScoreCards().get(user);
    Map<String, String> userInfo=db.getUsers().get(user);
    
    log.debug(user+" user data for scorecards "+(scorecard!=null?"found":"NOT FOUND!"));
    log.debug(user+" user data for userInfo "+(userInfo!=null?"found":"NOT FOUND!"));
    
    String payload="{\"status\":\"ERROR\",\"message\":\"Unable to find user: "+user+"\", \"displayName\":\"You ("+user+") are not registered\"}";
    if (scorecard!=null && userInfo!=null){
      Map<String, Object> data=new HashMap<String, Object>();
      data.put("userId", user);
      data.putAll(scorecard);
      data.putAll(userInfo);
      payload=Json.newObjectMapper(true).writeValueAsString(data);
    }
    
    return newResponse(payload.contains("ERROR")?500:200).entity(payload).build();
  }
  
  
  // User Dashboard UI call - returns the payload to render a chart displaying the breakdown of how many points came from which pool (trello, github PR, github reviewed PR's etc..)
  @GET
  @Path("/scorecard/breakdown/{user}")
  public Response getUserBreakdown(@PathParam("user") String user) throws JsonGenerationException, JsonMappingException, IOException{
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
    return newResponse(200).entity(Json.newObjectMapper(true).writeValueAsString(chart)).build();
  }
  
  // User Dashboard UI call - returns user scorecard data to display the user dashboard (mojo)
  @GET
  @Path("/scorecard/summary/{user}")
  public Response getScorecardSummary(@PathParam("user") String user) throws JsonGenerationException, JsonMappingException, IOException{
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
    
    return newResponse(200).entity(payload).build();
  }

  // Admin UI call (edit/update user) - updates an existing user with new values & points
  @POST
  @Path("/scorecard/{user}")
  public Response saveScorecard(
      @Context HttpServletRequest request 
      ,@Context HttpServletResponse response
      ,@Context ServletContext servletContext
      ,@PathParam("user") String user) throws JsonGenerationException, JsonMappingException, IOException{
    
    String payload=IOUtils.toString(request.getInputStream());
    log.debug("Saving "+ payload);
    
    Database2 db=Database2.get();
    
    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, Object> map = objectMapper.readValue(payload, new TypeReference<HashMap<String,Object>>(){});
    String username=(String)map.get("userId");
    
    Map<String, String> userInfo=db.getUsers().get(username);
    Map<String, Integer> scorecard=db.getScoreCards().get(username);
    
    for(String k:map.keySet()){
      if (!k.equals("userId")){
        
        if (userInfo.containsKey(k)) {
          log.debug("Setting 'userInfo."+k+"' to "+(String)map.get(k));
          userInfo.put(k, (String)map.get(k));
        }else if (scorecard.containsKey(k)) {
          log.debug("Setting 'scorecard."+k+"' to "+(String)map.get(k));
          scorecard.put(k, Integer.parseInt((String)map.get(k)));
        }else{
          log.debug("Setting 'userInfo."+k+"' to "+(String)map.get(k));
          userInfo.put(k, (String)map.get(k));
          //// ALERT! unknown field
          //log.error("UNKNOWN FIELD: "+k+" = "+map.get(k));
        }
      }
    }
    
    db.save();
    return newResponse(200).entity(Json.newObjectMapper(true).writeValueAsString("OK")).build();
  }
  
  // Admin/Support UI call to display all events, user events or specific types of events
  @GET
  @Path("/events")
  public Response getEvents(@Context HttpServletRequest request) throws JsonGenerationException, JsonMappingException, IOException{
    return newResponse(200).entity(Json.newObjectMapper(true).writeValueAsString(getEvents(request.getParameter("user"), request.getParameter("event")))).build();
  }
  public List<Map<String, String>> getAllEvents() throws JsonGenerationException, JsonMappingException, IOException{
    return getEvents(null, null);
  }
  public List<Map<String, String>> getEvents(String user, String event) throws JsonGenerationException, JsonMappingException, IOException{
    Database2 db=Database2.get();
    List<Map<String, String>> result=new ArrayList<Map<String,String>>();
    
    if (null==user && null==event){
      result=db.getEvents();
    }else{
      for(Map<String, String> e:db.getEvents()){
        if (e.get(EVENT_FIELDS.USER.v).equals(user)) result.add(e);
        if (e.get(EVENT_FIELDS.TYPE.v).equals(event)) result.add(e);
      }
    }
    return result;
  }
  
  // Admin/Support UI call to list all users and their scorecards
  @GET
  @Path("/scorecards")
  public Response getScorecards() throws JsonGenerationException, JsonMappingException, IOException{
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
      Integer pointsToNextLevel=LevelsUtil.get().getNextLevel(userInfo.get("level")).getLeft()-total;
      if (pointsToNextLevel<0) pointsToNextLevel=0;
      row.put("pointsToNextLevel", pointsToNextLevel);
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
    
    return newResponse(200).entity(Json.newObjectMapper(true).writeValueAsString(wrapper)).build();
  }
  
  
}
