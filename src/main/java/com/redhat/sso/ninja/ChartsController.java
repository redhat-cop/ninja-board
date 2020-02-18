package com.redhat.sso.ninja;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gdata.util.common.base.Pair;
import com.redhat.sso.ninja.chart.Chart2Json;
import com.redhat.sso.ninja.chart.DataSet2;
import com.redhat.sso.ninja.utils.Http;
import com.redhat.sso.ninja.utils.Json;
import com.redhat.sso.ninja.utils.LevelsUtil;
import com.redhat.sso.ninja.utils.MapBuilder;

@Path("/")
public class ChartsController{
	
  @GET
  @Path("/ninjas")
  public Response getNinjas() throws JsonGenerationException, JsonMappingException, IOException{
  	return Http.newResponse(200).entity(Json.newObjectMapper(true).writeValueAsString(getParticipants(null))).build();
  }
  
  @GET
  @Path("/leaderboard/{max}")
  public Response getLeaderboard2(@PathParam("max") Integer max) throws JsonGenerationException, JsonMappingException, IOException{
  	return Http.newResponse(200).entity(Json.newObjectMapper(true).writeValueAsString(getParticipants(max))).build();
  }
  
  static Integer total(Map<String,Integer> points){
  	int t=0;
  	for(Entry<String, Integer> e:points.entrySet()){
  		t+=e.getValue();
  	}
  	return t;
  }
  public Chart2Json getParticipants(Integer max) throws JsonGenerationException, JsonMappingException, IOException{
    Database2 db=Database2.get();
    Map<String, Map<String, Integer>> leaderboard=db.getLeaderboard();
    Map<String, Integer> totals=new HashMap<String, Integer>();
    for(Entry<String, Map<String, Integer>> e:leaderboard.entrySet()){
      totals.put(e.getKey(), total(e.getValue()));
    }
    
    // identify past years for historical badges
    Set<String> historyYears=db.getScorecardHistory().keySet();
    List<String> historyYearsList=new ArrayList<String>(historyYears);
    Collections.sort(historyYearsList);
    historyYears=new LinkedHashSet<String>(historyYearsList);
    
    
    //reorder
    List<Entry<String, Integer>> list=new LinkedList<Map.Entry<String, Integer>>(totals.entrySet());
    Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() { public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
      return (o2.getValue()).compareTo(o1.getValue());
    }});
    HashMap<String, Integer> sortedTotals=new LinkedHashMap<String, Integer>();
    for (Entry<String, Integer> e:list)
      sortedTotals.put(e.getKey(), e.getValue());
    
    // Build Chart data structure
    Chart2Json c=new Chart2Json();
    c.setDatasets(new ArrayList<DataSet2>());
    int count=0;
    for(Entry<String, Integer> e:sortedTotals.entrySet()){
      Map<String, String> userInfo=db.getUsers().get(e.getKey());
      
      if (null==max && userInfo.get("level").equalsIgnoreCase("zero")) continue; // all ninjas with belts. break should work if ordered correctly
      
      c.getLabels().add(null!=userInfo && userInfo.containsKey("displayName")?userInfo.get("displayName"):e.getKey());
      
      String geo=userInfo.containsKey("geo")?userInfo.get("geo"):"Unknown";
      String level=userInfo.get("level");
      if (level==null) level="none";
      c.getCustom1().add(e.getKey()+"|"+level.toLowerCase()+"|"+geo); // users rh username, belt & geo
      
      List<String> pastYearBadges=Lists.newArrayList();
      for(String year:historyYears){
      	String pastYearHistory=db.getScorecardHistory().get(year).get(e.getKey());
      	if (null!=pastYearHistory){
      		String belt=pastYearHistory.split("\\|")[0];
      		String total=pastYearHistory.split("\\|")[1];
      		pastYearBadges.add(String.format("%s|%s|%s",year,belt,total));
      	}
      }
      c.getCustom2().add(Joiner.on(",").join(pastYearBadges));
      
      if (c.getDatasets().size()<=0) c.getDatasets().add(new DataSet2());
      c.getDatasets().get(0).getData().add(e.getValue());
      c.getDatasets().get(0).setBorderWidth(1);
      
      // TODO: set this to the color of the belt. should be on the ui side, not server
      Map<String,Pair<String,String>> colors=new MapBuilder<String,Pair<String,String>>()
          .put("BLUE",  new Pair<String, String>("rgba(0,0,163,0.7)",     "rgba(0,0,163,0.8)"))
          .put("GREY",  new Pair<String, String>("rgba(130,130,130,0.7)", "rgba(130,130,130,0.8)"))
          .put("RED",   new Pair<String, String>("rgba(163,0,0,0.7)",     "rgba(163,0,0,0.8)"))
          .put("BLACK", new Pair<String, String>("rgba(20,20,20,0.7)",    "rgba(20,20,20,0.8)"))
          .put("ZERO",  new Pair<String, String>("rgba(255,255,255,0.7)", "rgba(255,255,255,0.8)"))
          // ZERO???
          .build();
      c.getDatasets().get(0).getBackgroundColor().add(colors.get(userInfo.get("level").toUpperCase()).getFirst());
      c.getDatasets().get(0).getBorderColor().add(colors.get(userInfo.get("level").toUpperCase()).getSecond());
      
      count=count+1;
      if (null!=max && count>=max) break; // hard maximum suppled as param
    }
    
    return c;
  }
  
  // UI call (user dashboard) - returns the payload to render a chart displaying the current points and points to the next level
  @GET
  @Path("/scorecard/nextlevel/{user}")
  public Response getUserNextLevel(@PathParam("user") String user) throws JsonGenerationException, JsonMappingException, IOException{
    
    Database2 db=Database2.get();
    boolean userExists=db.getScoreCards().containsKey(user);
    
    Chart2Json chart=new Chart2Json();
    chart.getLabels().add("Earned");
    chart.getLabels().add("To Next Level");
    chart.getDatasets().add(new DataSet2());
    chart.getDatasets().get(0).setBorderWidth(1);
    
    if (userExists){
      int currentTotal=getTotalPoints(user);
      int outOf=getPointsToNextLevel(user);
      chart.getDatasets().get(0).getData().add(currentTotal);
      chart.getDatasets().get(0).getData().add(outOf);
    }else{
      chart.getDatasets().get(0).getData().add(0);
      chart.getDatasets().get(0).getData().add(Integer.parseInt(Config.get().getOptions().get("thresholds").split(":")[0])); // blue level threshold
    }
    
    return Http.newResponse(200).entity(Json.newObjectMapper(true).writeValueAsString(chart)).build();
  }
  
  private int getTotalPoints(String username){
    Database2 db=Database2.get();
    Map<String, Integer> scorecard=db.getScoreCards().get(username);
    int total=0;
    for(Entry<String, Integer> s:scorecard.entrySet()){
      total+=s.getValue();
    }
    return total;
  }
  
  private int getPointsToNextLevel(String username){
    Database2 db=Database2.get();
    int total=getTotalPoints(username);
    Map<String, String> userInfo=db.getUsers().get(username);
    Integer pointsToNextLevel=LevelsUtil.get().getNextLevel(userInfo.get("level")).getLeft()-total;
    if (pointsToNextLevel<0) pointsToNextLevel=0;
    return pointsToNextLevel;
  }

}
