package com.redhat.sso.ninja;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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

import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.google.common.base.Splitter;
import com.redhat.sso.ninja.Database2.EVENT_FIELDS;
import com.redhat.sso.ninja.chart.Chart2Json;
import com.redhat.sso.ninja.chart.DataSet2;
import com.redhat.sso.ninja.utils.IOUtils2;
import com.redhat.sso.ninja.utils.Json;
import com.redhat.sso.ninja.utils.LevelsUtil;
import com.redhat.sso.ninja.utils.MapBuilder;

@Path("/")
public class ManagementController {
  private static final Logger log=Logger.getLogger(ManagementController.class);
  
  public static void main(String[] asd) throws JsonGenerationException, JsonMappingException, IOException{
    System.out.println(java.sql.Date.valueOf(LocalDate.now()));
    System.out.println(java.sql.Date.valueOf(LocalDate.now().minus(365, ChronoUnit.DAYS)));
    System.out.println((1000 * 60 * 60 * 24));
    System.out.println(TimeUnit.DAYS.toMillis(1));
//    System.out.println(new ManagementController().toNextLevel("BLUE", 7).toString());
  }
  
  public static boolean isLoginEnabled(){
  	return "true".equalsIgnoreCase(Config.get().getOptions().get("login.enabled"));
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
		
		
//		if ("admin".equals(keyValues.get("username")) && "admin".equals(keyValues.get("password"))){
//			log.info("Login successful");
//			jwtToken="ok";
//		}else
//			log.info("Login failure");
		
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
	
  // This doenst work but would be a nice feature
  @GET
  @Path("/loglevel/{level}")
  public Response setLogLevel(@Context HttpServletRequest request,@Context HttpServletResponse response,@Context ServletContext servletContext, @PathParam("level") String level) throws JsonGenerationException, JsonMappingException, IOException{
    LogManager.getRootLogger().setLevel(org.apache.log4j.Level.toLevel(level));
    return Response.status(200).entity("{\"status\":\"DONE\", \"Message\":\"Changed Log level to: "+LogManager.getRootLogger().getLevel().toString()+"\"}").build();
  }
  
  // returns the config file contents (and yes, I shouldnt put the http method in the url, but that's a fix for later)
  @GET
  @Path("/config/get")
  public Response configGet(@Context HttpServletRequest request,@Context HttpServletResponse response,@Context ServletContext servletContext) throws JsonGenerationException, JsonMappingException, IOException{
    return Response.status(200).entity(Json.newObjectMapper(true).writeValueAsString(Config.get())).build();
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
    
    String maxEvents=newConfig.getOptions().get("events.max");
    if (null!=maxEvents && maxEvents.matches("\\d+")){
      Database2.MAX_EVENT_ENTRIES=Integer.parseInt(maxEvents);
    }
    
    Database2.resetInstance();
    Database2.get(); //reload it
    
    log.debug("Config Saved");
    return Response.status(200).entity(Json.newObjectMapper(true).writeValueAsString(Config.get())).build();
  }

  @GET
  @Path("/scripts/runNow")
  public Response runScriptsNow(@Context HttpServletRequest request, @Context HttpServletResponse response, @Context ServletContext servletContext){
  	Heartbeat2.runOnceAsync();
  	Database2.resetInstance();
  	Database2.get(); //reload it
    log.debug("Scripts run started - check logs for results");
    return Response.status(200).entity("RUNNING").build();
  }
  
  // manually (via rest) to register new users via a rest/json payload
  @POST
  @Path("/users/register")
  public Response register(
      @Context HttpServletRequest request 
      ,@Context HttpServletResponse response
      ,@Context ServletContext servletContext
      ,String raw
      ){
    try{
      log.debug("/register called");
//      String raw=IOUtils.toString(request.getInputStream());
      mjson.Json x=mjson.Json.read(raw);
      
      Database2 db=Database2.get();
      for (mjson.Json user:x.asJsonList()){
        String username=user.at("username").asString();
        
        if (db.getUsers().containsKey(username))
          db.getUsers().remove(username); // remove so we can overwrite the user details
        
        Map<String, String> userInfo=new HashMap<String, String>();
        for(Entry<String, Object> e:user.asMap().entrySet())
          userInfo.put(e.getKey(), (String)e.getValue());
        
        userInfo.put("level", LevelsUtil.get().getBaseLevel().getRight());
        userInfo.put("levelChanged", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));// nextLevel.getRight());
        
        db.getUsers().put(username, userInfo);
        log.debug("New User Registered (via API): "+Json.newObjectMapper(true).writeValueAsString(userInfo));
        db.getScoreCards().put(username, new HashMap<String, Integer>());
        
        db.addEvent("New User Registered (via API)", username, "");
      }
      
      db.save();
      return Response.status(200).entity("{\"status\":\"DONE\"}").build();
    }catch(IOException e){
      e.printStackTrace();
      return Response.status(500).entity("{\"status\":\"ERROR\",\"message\":\""+e.getMessage()+"\"}").build();  
    }
    
  }
  
  // manually (via rest) to increment the points for a specific user and pool id
  @GET
  @Path("/points/{user}/{pool}/{increment}")
  public Response incrementPool(
      @Context HttpServletRequest request 
      ,@Context HttpServletResponse response
      ,@Context ServletContext servletContext
      ,@PathParam("user") String user
      ,@PathParam("pool") String pool
      ,@PathParam("increment") String increment
      ){
    try{
      Database2 db=Database2.get();
      db.increment(pool, user, Integer.valueOf(increment), null).save();
      db.save();
      return Response.status(200).entity("{\"status\":\"DONE\"}").build();
    }catch(Exception e){
      return Response.status(500).entity("{\"status\":\"ERROR\",\"message\":\""+e.getMessage()+"\"}").build();  
    }
  }
  
  // returns the database content
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

  
  // UI call (edit user) - returns the scorecard and userInfo data for be able to display and edit one specific user
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
    
    return Response.status(payload.contains("ERROR")?500:200)
        .header("Access-Control-Allow-Origin",  "*")
        .header("Content-Type","application/json")
        .header("Cache-Control", "no-store, must-revalidate, no-cache, max-age=0")
        .header("Pragma", "no-cache")
        .entity(payload).build();
  }
  
  
  // UI call (user dashboard) - returns the payload to render a chart displaying the breakdown of how many points came from which pool (trello, github PR, github reviewed PR's etc..)
  @GET
  @Path("/scorecard/breakdown/{user}")
  public Response getUserBreakdown(@PathParam("user") String user) throws JsonGenerationException, JsonMappingException, IOException{
    Database2 db=Database2.get();
    Map<String, Integer> scorecard=db.getScoreCards().get(user);
    
    Chart2Json chart=new Chart2Json();
    chart.getDatasets().add(new DataSet2());
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
    return Response.status(200)
        .header("Access-Control-Allow-Origin",  "*")
        .header("Content-Type","application/json")
        .header("Cache-Control", "no-store, must-revalidate, no-cache, max-age=0")
        .header("Pragma", "no-cache")
        .entity(Json.newObjectMapper(true).writeValueAsString(chart)).build();
  }
  
  // UI call (user dashboard) - returns user scorecard data to display the user dashboard
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
    
    return Response.status(payload.contains("ERROR")?500:200)
        .header("Access-Control-Allow-Origin",  "*")
        .header("Content-Type","application/json")
        .header("Cache-Control", "no-store, must-revalidate, no-cache, max-age=0")
        .header("Pragma", "no-cache")
        .entity(payload).build();
  }

  // UI call (edit/update user) - updates an existing user with new values & points
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
    return Response.status(200).entity(Json.newObjectMapper(true).writeValueAsString("OK")).build();
  }
  
  @GET
  @Path("/events")
  public Response getEvents(@Context HttpServletRequest request) throws JsonGenerationException, JsonMappingException, IOException{
  	return Response.status(200).entity(Json.newObjectMapper(true).writeValueAsString(getEvents(request.getParameter("user"), request.getParameter("event")))).build();
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
  
  
  @GET
  @Path("/scorecards")
  public Response getScorecards() throws JsonGenerationException, JsonMappingException, IOException{
    Database2 db=Database2.get();
    List<Map<String, Object>> data=new ArrayList<Map<String,Object>>();
    
    Set<String> fields=new HashSet<String>();
    
    for(Entry<String, Map<String, Integer>> e:db.getScoreCards().entrySet()){
      Map<String, Object> row=new HashMap<String, Object>();
      Map<String,String> userInfo=db.getUsers().get(e.getKey());
      row.put("id", e.getKey());
      
      String name=userInfo.containsKey("displayName")?userInfo.get("displayName"):e.getKey();
      
      row.put("name", name);
      int total=0;
      for(Entry<String, Integer> s:e.getValue().entrySet()){
        row.put(s.getKey().replaceAll("\\.", " "), s.getValue());
        total+=s.getValue();
        fields.add(s.getKey().replaceAll("\\.", " "));
      }
      row.put("total", total);
      row.put("level", userInfo.get("level"));
      
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
    
    return Response.status(200).entity(Json.newObjectMapper(true).writeValueAsString(wrapper)).build();
  }
  
  
}