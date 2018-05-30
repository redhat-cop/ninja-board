package com.redhat.sso.ninja;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import java.util.logging.Level;

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
import org.apache.xmlbeans.impl.jam.JAnnotationValue;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JComment;
import org.apache.xmlbeans.impl.jam.JElement;
import org.apache.xmlbeans.impl.jam.JParameter;
import org.apache.xmlbeans.impl.jam.JSourcePosition;
import org.apache.xmlbeans.impl.jam.JamClassLoader;
import org.apache.xmlbeans.impl.jam.mutable.MAnnotation;
import org.apache.xmlbeans.impl.jam.mutable.MComment;
import org.apache.xmlbeans.impl.jam.mutable.MConstructor;
import org.apache.xmlbeans.impl.jam.mutable.MParameter;
import org.apache.xmlbeans.impl.jam.mutable.MSourcePosition;
import org.apache.xmlbeans.impl.jam.visitor.JVisitor;
import org.apache.xmlbeans.impl.jam.visitor.MVisitor;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.google.gdata.util.common.base.Pair;
import com.redhat.sso.ninja.chart.Chart2Json;
import com.redhat.sso.ninja.chart.DataSet2;
import com.redhat.sso.ninja.utils.Json;
import com.redhat.sso.ninja.utils.MapBuilder;

@Path("/")
public class ManagementController {
  private static final Logger log=Logger.getLogger(ManagementController.class);
  /**
   * 
   * config/load
   * users/register
   * users/get?id=?
   * users/list
   * users/delete?id=?
   * leaderboard/{max}
   * scorecards/list
   * @throws IOException 
   * @throws JsonMappingException 
   * @throws JsonGenerationException 
   * 
   */
  
  public static void main(String[] asd) throws JsonGenerationException, JsonMappingException, IOException{
//    System.out.println(new ManagementController().register(null,null,null,"[{\"displayName\": \"Mat Allen\",\"username\": \"mallen\",\"trelloId\":\"mallen2\",\"githubId\":\"matallen\"}]"));
//    System.out.println(new ManagementController().getScorecards().getEntity());
    
    System.out.println(new ManagementController().getScorecardSummary("pfann").getEntity());
//    System.out.println(new ManagementController().toNextLevel("BLUE", 7).toString());
  }
  

  @GET
  @Path("/loglevel/{level}")
  public Response setLogLevel(@Context HttpServletRequest request,@Context HttpServletResponse response,@Context ServletContext servletContext, @PathParam("level") String level) throws JsonGenerationException, JsonMappingException, IOException{
    LogManager.getRootLogger().setLevel(org.apache.log4j.Level.toLevel(level));
    return Response.status(200).entity("{\"status\":\"DONE\", \"Message\":\"Changed Log level to: "+LogManager.getRootLogger().getLevel().toString()+"\"}").build();
  }
  
  
  @GET
  @Path("/config/get")
  public Response configGet(@Context HttpServletRequest request,@Context HttpServletResponse response,@Context ServletContext servletContext) throws JsonGenerationException, JsonMappingException, IOException{
    return Response.status(200).entity(Json.newObjectMapper(true).writeValueAsString(Config.get())).build();
  }
  
  @POST
  @Path("/config/save")
  public Response configSave(@Context HttpServletRequest request,@Context HttpServletResponse response,@Context ServletContext servletContext) throws JsonGenerationException, JsonMappingException, IOException{
    System.out.println("Saving config");
    Config newConfig=Json.newObjectMapper(true).readValue(request.getInputStream(), Config.class);
    
    System.out.println("New Config = "+Json.newObjectMapper(true).writeValueAsString(newConfig));
//    System.out.println("heartbeat.intervalInSeconds="+newConfig.getOptions().get("heartbeat.intervalInSeconds"));
//    System.out.println("Saving...");
    newConfig.save();
    System.out.println("Saved");
    return Response.status(200).entity(Json.newObjectMapper(true).writeValueAsString(Config.get())).build();
  }
  
  
  static LevelsUtil levelsUtil=null;
  public LevelsUtil getLevelsUtil(){
    if (null==levelsUtil) levelsUtil=new LevelsUtil(Config.get().getOptions().get("thresholds"));
    return levelsUtil;
  }
  
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
        
        userInfo.put("level", getLevelsUtil().getBaseLevel().getRight());
        userInfo.put("levelChanged", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));// nextLevel.getRight());
        
        db.getUsers().put(username, userInfo);
        log.debug("Registered user: "+Json.newObjectMapper(true).writeValueAsString(userInfo));
        db.getScoreCards().put(username, new HashMap<String, Integer>());
      }
      
      db.save();
      return Response.status(200).entity("{\"status\":\"DONE\"}").build();
    }catch(IOException e){
      e.printStackTrace();
      return Response.status(500).entity("{\"status\":\"ERROR\",\"message\":\""+e.getMessage()+"\"}").build();  
    }
    
  }
  
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
      db.increment(pool, user, Integer.valueOf(increment)).save();
      db.save();
      return Response.status(200).entity("{\"status\":\"DONE\"}").build();
    }catch(Exception e){
      return Response.status(500).entity("{\"status\":\"ERROR\",\"message\":\""+e.getMessage()+"\"}").build();  
    }
  }
  
  @GET
  @Path("/database/get")
  public Response getDatabase() throws JsonGenerationException, JsonMappingException, IOException{
    return Response.status(200).entity(Json.newObjectMapper(true).writeValueAsString(Database2.get())).build();
  }
  
  
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
  
//  @GET
//  @Path("/scorecard/pie/{user}")
//  public Response getUserPointDistribution(@PathParam("user") String user) throws JsonGenerationException, JsonMappingException, IOException{
//  }
  @GET
  @Path("/scorecard/nextlevel/{user}")
  public Response getUserNextLevel(@PathParam("user") String user) throws JsonGenerationException, JsonMappingException, IOException{
    
//    Database2 db=Database2.getCached();
//    Map<String, String> userInfo=db.getUsers().get(user);
//    String currentLevel=userInfo.get("level");
//    String nextLevel=getUserNextLevel(user);
    
    int currentTotal=getTotalPoints(user);
    int outOf=getPointsToNextLevel(user);
    
    Chart2Json chart=new Chart2Json();
    chart.getLabels().add("Points Earned");
    chart.getLabels().add("Points To Next Level");
    chart.getDatasets().add(new DataSet2());
    chart.getDatasets().get(0).getData().add(currentTotal);
    chart.getDatasets().get(0).getData().add(outOf);
    chart.getDatasets().get(0).setBorderWidth(1);
    chart.getDatasets().get(0).setBackgroundColor(Arrays.asList(new String[]{"rgba(0,0,163,0.5)","rgba(235,235,235,0.5)"}));
    chart.getDatasets().get(0).setBorderColor(Arrays.asList(new String[]{"rgba(0,0,163,0.8)","rgba(235,235,235,0.8)"}));
    
    return Response.status(200)
        .header("Access-Control-Allow-Origin",  "*")
        .header("Content-Type","application/json")
        .header("Cache-Control", "no-store, must-revalidate, no-cache, max-age=0")
        .header("Pragma", "no-cache")
        .entity(Json.newObjectMapper(true).writeValueAsString(chart)).build();
  }
  
  @GET
  @Path("/scorecard/breakdown/{user}")
  public Response getUserBreakdown(@PathParam("user") String user) throws JsonGenerationException, JsonMappingException, IOException{
    Database2 db=Database2.get();
    Map<String, Integer> scorecard=db.getScoreCards().get(user);
    
    String[] colors=new String[]{"rgba(204,0,0,%s)", "rgba(0,65,83,%s)", "rgba(146,212,0,%s)", "rgba(59,0,131,%s)", "rgba(0,122,135,%s)"};
    
    Chart2Json chart=new Chart2Json();
    chart.getDatasets().add(new DataSet2());
    int total=0;
    for(Entry<String, Integer> s:scorecard.entrySet()){
      chart.getLabels().add(s.getKey());
      chart.getDatasets().get(0).getData().add(s.getValue());
      chart.getDatasets().get(0).getBackgroundColor().add(String.format(colors[total],"0.5"));
      chart.getDatasets().get(0).getBorderColor().add(String.format(colors[total],"0.8"));
      total+=1;
    }
    
    return Response.status(200)
        .header("Access-Control-Allow-Origin",  "*")
        .header("Content-Type","application/json")
        .header("Cache-Control", "no-store, must-revalidate, no-cache, max-age=0")
        .header("Pragma", "no-cache")
        .entity(Json.newObjectMapper(true).writeValueAsString(chart)).build();
  }
  
  
  @GET
  @Path("/scorecard/summary/{user}")
  public Response getScorecardSummary(@PathParam("user") String user) throws JsonGenerationException, JsonMappingException, IOException{
    Database2 db=Database2.get();
    
    log.debug("Request made for user ["+user+"]");
    
    Map<String, Integer> scorecard=db.getScoreCards().get(user);
    Map<String, String> userInfo=db.getUsers().get(user);
    
    log.debug(user+" user data for scorecards "+(scorecard!=null?"found":"NOT FOUND!"));
    log.debug(user+" user data for userInfo "+(userInfo!=null?"found":"NOT FOUND!"));
    
    String payload="{\"status\":\"ERROR\",\"message\":\"Unable to find user: "+user+"\", \"displayName\":\"You ("+user+") are not registered\"}";
    
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
    
//    if (scorecard!=null && userInfo!=null){
//      Map<String, Object> data=new HashMap<String, Object>();
//      data.put("userId", user);
//      
//      Map<String, Integer> consolidatedTotals=new HashMap<String, Integer>();
//      Integer total=0;
//      for(Entry<String, Integer> e:scorecard.entrySet()){
//        String consolidatedKey=e.getKey().substring(0, e.getKey().contains(".")?e.getKey().indexOf("."):e.getKey().length());
//        if (!consolidatedTotals.containsKey(consolidatedKey)) consolidatedTotals.put(consolidatedKey, 0);
//        consolidatedTotals.put(consolidatedKey, consolidatedTotals.get(consolidatedKey)+e.getValue());
//        total+=e.getValue();
//      }
//      data.put("total", total);
//      data.putAll(consolidatedTotals);
//      data.putAll(userInfo);
//      payload=Json.newObjectMapper(true).writeValueAsString(data);
//    }
    
    return Response.status(payload.contains("ERROR")?500:200)
        .header("Access-Control-Allow-Origin",  "*")
        .header("Content-Type","application/json")
        .header("Cache-Control", "no-store, must-revalidate, no-cache, max-age=0")
        .header("Pragma", "no-cache")
        .entity(payload).build();
  }

  
  @POST
  @Path("/scorecard/{user}")
  public Response saveScorecard(
      @Context HttpServletRequest request 
      ,@Context HttpServletResponse response
      ,@Context ServletContext servletContext
      ,@PathParam("user") String user) throws JsonGenerationException, JsonMappingException, IOException{
    
    String payload=IOUtils.toString(request.getInputStream());
    System.out.println("Saving "+ payload);
    
//    mjson.Json x=mjson.Json.read(payload);
//    String userId=x.at("userId").asString();
//    String displayName=x.at("displayName").asString();
    
    Database2 db=Database2.get();
    
//    for (mjson.Json y:x.asJsonList()){
//      System.out.println(y.asString());
//    }
    
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
          // ALERT! unknown field
          log.error("UNKNOWN FIELD: "+k+" = "+map.get(k));
        }

        
//        if (k.equals("displayName")){
//          log.debug("Setting 'userInfo.displayName' to "+(String)map.get(k));
//          userInfo.put("displayName", (String)map.get(k));
//        }else if (k.equals("level")){
//          log.debug("Setting 'userInfo.level' to "+(String)map.get(k));
//          userInfo.put("level", (String)map.get(k));
//        }else{
//          log.debug("Setting 'scorecard."+k+"' to "+(String)map.get(k));
////          log.debug("field '"+k+"/"+map.get(k)+"' is of type: "+(map.get(k).getClass().getName()));
//          
////          if (map.get(k) instanceof String){
////            
////          }else if (map.get(k) instanceof Integer){
//            scorecard.put(k, Integer.parseInt((String)map.get(k)));
////          }
//        }
      }
    }
    
    
//    if (displayName!=null) userInfo.put("displayName", displayName);
    
    db.save();
    
    return Response.status(200).entity(Json.newObjectMapper(true).writeValueAsString("OK")).build();
  }
  
  
  private int getTotalPoints(String username){
    Database2 db=Database2.getCached();
    Map<String, Integer> scorecard=db.getScoreCards().get(username);
    int total=0;
    for(Entry<String, Integer> s:scorecard.entrySet()){
      total+=s.getValue();
    }
    return total;
  }
  
  private int getPointsToNextLevel(String username){
    Database2 db=Database2.getCached();
    int total=getTotalPoints(username);
    Map<String, String> userInfo=db.getUsers().get(username);
    Integer pointsToNextLevel=getLevelsUtil().getNextLevel(userInfo.get("level")).getLeft()-total;
    if (pointsToNextLevel<0) pointsToNextLevel=0;
    return pointsToNextLevel;
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
      
      name="<div class='link' title='Edit' onclick='edit2(\""+e.getKey()+"\");' data-toggle='modal' data-target='#exampleModal'>"+name+"</div>";
      
      row.put("name", name);
      int total=0;
      for(Entry<String, Integer> s:e.getValue().entrySet()){
        row.put(s.getKey().replaceAll("\\.", " "), s.getValue());
        total+=s.getValue();
        fields.add(s.getKey().replaceAll("\\.", " "));
      }
      row.put("total", total);
      row.put("level", userInfo.get("level"));
      
      
      
//      Tuple<Integer,String> shouldBeLevel=getLevelsUtil().getLevelGivenPoints(total);
//      if (!shouldBeLevel.equals(userInfo.get("level")))
//        row.put("pointsToNextLevel", "PENDING");
      
      // points to next level
      Integer pointsToNextLevel=getLevelsUtil().getNextLevel(userInfo.get("level")).getLeft()-total;
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
    for(String field:fields)
      columns.add(new MapBuilder<String,String>().put("title",field).put("data", field).build());  
    columns.add(new MapBuilder<String,String>().put("title","Total").put("data", "total").build());
    
    columns.add(new MapBuilder<String,String>().put("title","Ninja Belt").put("data", "level").build());
    columns.add(new MapBuilder<String,String>().put("title","Points to next level").put("data", "pointsToNextLevel").build());
    
    wrapper.put("columns", columns);
    wrapper.put("data", data);
    
    return Response.status(200).entity(Json.newObjectMapper(true).writeValueAsString(wrapper)).build();
  }
  
  @GET
  @Path("/leaderboard/{max}")
  public Response getLeaderboard2(@PathParam("max") Integer max) throws JsonGenerationException, JsonMappingException, IOException{
    Database2 db=Database2.get();
    Map<String, Map<String, Integer>> leaderboard=db.getLeaderboard();
    Map<String, Integer> totals=new HashMap<String, Integer>();
    for(Entry<String, Map<String, Integer>> e:leaderboard.entrySet()){
      Integer t=0;
      for(Entry<String, Integer> e2:e.getValue().entrySet()){
        t+=e2.getValue();
      }
      e.getValue().put("total", t);
      totals.put(e.getKey(), t);
    }
    
    //reorder
    List<Entry<String, Integer>> list=new LinkedList<Map.Entry<String, Integer>>(totals.entrySet());
    Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
      public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
          return (o2.getValue()).compareTo(o1.getValue());
      }
    });
    HashMap<String, Integer> sortedTotals=new LinkedHashMap<String, Integer>();
    for (Entry<String, Integer> e:list) {
      sortedTotals.put(e.getKey(), e.getValue());
    }
    
    Chart2Json c=new Chart2Json();
    c.setDatasets(new ArrayList<DataSet2>());
    int count=0;
    for(Entry<String, Integer> e:sortedTotals.entrySet()){
      Map<String, String> userInfo=db.getUsers().get(e.getKey());
      
      c.getLabels().add(null!=userInfo && userInfo.containsKey("displayName")?userInfo.get("displayName"):e.getKey());
      
      if (c.getDatasets().size()<=0) c.getDatasets().add(new DataSet2());
      c.getDatasets().get(0).getData().add(e.getValue());
      c.getDatasets().get(0).setBorderWidth(1);
      
      // TODO: set this to the color of the belt
      Map<String,Pair<String,String>> colors=new MapBuilder<String,Pair<String,String>>()
          .put("BLUE",  new Pair<String, String>("rgba(0,0,163,0.7)",     "rgba(0,0,163,0.8)"))
          .put("GREY",  new Pair<String, String>("rgba(130,130,130,0.7)", "rgba(130,130,130,0.8)"))
          .put("RED",   new Pair<String, String>("rgba(163,0,0,0.7)",     "rgba(163,0,0,0.8)"))
          .put("BLACK", new Pair<String, String>("rgba(20,20,20,0.7)",    "rgba(20,20,20,0.8)"))
          .build();
      c.getDatasets().get(0).getBackgroundColor().add(colors.get(userInfo.get("level").toUpperCase()).getFirst());
      c.getDatasets().get(0).getBorderColor().add(colors.get(userInfo.get("level").toUpperCase()).getSecond());
      
      count=count+1;
      if (count>=max) break;
    }
    return Response.status(200)
        .header("Access-Control-Allow-Origin",  "*")
        .header("Content-Type","application/json")
        .header("Cache-Control", "no-store, must-revalidate, no-cache, max-age=0")
        .header("Pragma", "no-cache")
        .entity(Json.newObjectMapper(true).writeValueAsString(c)).build();
  }
  
  @GET
  @Path("/script/{name}")
  public Response getScript(@PathParam("name") String scriptName) throws JsonGenerationException, JsonMappingException, IOException{
    String path="scripts/"+scriptName;
    InputStream is=this.getClass().getClassLoader().getResourceAsStream(path);
    
    StringBuilder sb = new StringBuilder();
    String inputLine;
    BufferedReader br=new BufferedReader(new InputStreamReader(is));
    while ((inputLine = br.readLine()) != null){
      sb.append(inputLine);
      sb.append('\n');
    }
    
    return Response.status(200).header("Content-Type", "text/application").entity(sb.toString()).build();
  }
  
}