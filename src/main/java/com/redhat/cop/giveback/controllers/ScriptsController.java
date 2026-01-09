package com.redhat.cop.giveback.controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.cop.giveback.Database2;
import com.redhat.cop.giveback.Heartbeat2;
import com.redhat.cop.giveback.legacy.Config;
import com.redhat.sso.ninja.utils.Http;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

@Path("/")
public class ScriptsController extends CommonController{
  private static final Logger log=LoggerFactory.getLogger(ScriptsController.class);

  // Runs the scripts immediately - critical feature for supporting the system
  @GET
  @Path("/api/scripts/runNow")
  public Response runScriptsNow(){
    Heartbeat2.runOnceAsync();
    Database2.resetInstance();
    Database2.get(); //reload it
    log.debug("Scripts run started - check logs for results");
    return Http.newResponse(200, "text/html; charset=UTF-8", "RUNNING").build();
  }

  // Pushes the current database graph data to the external cache to be accessible by end users mojo dashboard - critical for support
  @GET
  @Path("/api/scripts/publishGraphs")
  public Response pushGraphDataOnly(){
    Database2 db=Database2.get();
    Config cfg=Config.get();
    new Heartbeat2.HeartbeatRunnable(null).publishGraphsData(db, cfg);
    return Http.newResponse(200, "text/html; charset=UTF-8", "RUNNING").build();
  }
  
  // Pushes graph data for a specific user only (used in conjunction with user details update features so the UI pulls data from the graph proxy)
  public void pushGraphDataFor(String user){
    new Heartbeat2.HeartbeatRunnable(null).publishGraphDataFor(user);
  }
  
  @POST
  @Path("/api/yearEnd/{priorYear}")
  public Response yearEnd(@PathParam("priorYear") String priorYear) throws IOException, URISyntaxException{
    log.info("Year Ending for - "+priorYear+". Note: This will loose some data (such as point buckets) as it archives the current years information");
    Database2 db=Database2.get();
    
    if (db.getScorecardHistory().containsKey(priorYear))
      return Http.newResponse(400, "text/html; charset=UTF-8", "Can't do that - the key '"+priorYear+"' already exists!").build();
    
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
    
    return Http.newResponse(200, "text/html; charset=UTF-8", "OK, it's done!").build();
  }
}
