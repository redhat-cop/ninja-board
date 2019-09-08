package com.redhat.sso.ninja;

import com.google.gdata.util.common.base.Pair;
import com.redhat.sso.ninja.chart.Chart2Json;
import com.redhat.sso.ninja.chart.DataSet2;
import com.redhat.sso.ninja.utils.Json;
import com.redhat.sso.ninja.utils.LevelsUtil;
import com.redhat.sso.ninja.utils.MapBuilder;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

@Path("/")
public class ChartsController {

  @GET
  @Path("/ninjas")
  public Response getNinjas() throws IOException {
    return buildResponse(getParticipants(null));
  }

  @GET
  @Path("/leaderboard/{max}")
  public Response getLeaderboard2(@PathParam("max") Integer max) throws IOException {
    return buildResponse(getParticipants(max));
  }

  private Chart2Json getParticipants(Integer max) {
    Database2 db = Database2.get();
    Map<String, Map<String, Integer>> leaderBoard = db.getLeaderboard();
    Map<String, Integer> totals = new HashMap<>();
    for (Entry<String, Map<String, Integer>> e : leaderBoard.entrySet()) {
      Integer t = 0;
      for (Entry<String, Integer> e2 : e.getValue().entrySet()) {
        t += e2.getValue();
      }
      e.getValue().put("total", t);
      totals.put(e.getKey(), t);
    }

    //reorder
    List<Entry<String, Integer>> list = new LinkedList<>(totals.entrySet());
    list.sort(Comparator.comparing(Entry::getValue));
    HashMap<String, Integer> sortedTotals = new LinkedHashMap<>();
    for (Entry<String, Integer> e : list) {
      sortedTotals.put(e.getKey(), e.getValue());
    }

    Chart2Json c = new Chart2Json();
    c.setDatasets(new ArrayList<>());
    int count = 0;
    for (Entry<String, Integer> e : sortedTotals.entrySet()) {

      Map<String, String> userInfo = db.getUsers().get(e.getKey());
      Validate.notNull(userInfo);

      if (null == max && userInfo.get("level").equalsIgnoreCase("zero")) {
        break; // all ninjas with belts
      }

      c.getLabels().add(userInfo.containsKey("displayName") ? userInfo.get("displayName") : e.getKey());

      String geo = userInfo.getOrDefault("geo", "Unknown");
      String level = userInfo.get("level");
      if (level == null) level = "none";
      c.getCustom1().add(e.getKey() + "|" + level.toLowerCase() + "|" + geo); // users rh username, belt & geo

      if (c.getDatasets().size() <= 0) c.getDatasets().add(new DataSet2());
      c.getDatasets().get(0).getData().add(e.getValue());
      c.getDatasets().get(0).setBorderWidth(1);

      // TODO: set this to the color of the belt. should be on the ui side, not server
      Map<String, Pair<String, String>> colors = new MapBuilder<String, Pair<String, String>>()
          .put("BLUE", new Pair<>("rgba(0,0,163,0.7)", "rgba(0,0,163,0.8)"))
          .put("GREY", new Pair<>("rgba(130,130,130,0.7)", "rgba(130,130,130,0.8)"))
          .put("RED", new Pair<>("rgba(163,0,0,0.7)", "rgba(163,0,0,0.8)"))
          .put("BLACK", new Pair<>("rgba(20,20,20,0.7)", "rgba(20,20,20,0.8)"))
          .put("ZERO", new Pair<>("rgba(255,255,255,0.7)", "rgba(255,255,255,0.8)"))
          // ZERO???
          .build();
      c.getDatasets().get(0).getBackgroundColor().add(colors.get(userInfo.get("level").toUpperCase()).getFirst());
      c.getDatasets().get(0).getBorderColor().add(colors.get(userInfo.get("level").toUpperCase()).getSecond());

      count = count + 1;
      if (null != max && count >= max) {
        break; // hard maximum supplied as param
      }
    }

    return c;
  }

  // UI call (user dashboard) - returns the payload to render a chart displaying the current points and points to the next level
  @GET
  @Path("/scorecard/nextlevel/{user}")
  public Response getUserNextLevel(@PathParam("user") String user) throws IOException {

    Database2 db = Database2.get();
    boolean userExists = db.getScoreCards().containsKey(user);

    Chart2Json chart = new Chart2Json();
    chart.getLabels().add("Earned");
    chart.getLabels().add("To Next Level");
    chart.getDatasets().add(new DataSet2());
    chart.getDatasets().get(0).setBorderWidth(1);

    if (userExists) {
      int currentTotal = getTotalPoints(user);
      int outOf = getPointsToNextLevel(user);
      chart.getDatasets().get(0).getData().add(currentTotal);
      chart.getDatasets().get(0).getData().add(outOf);
    } else {
      chart.getDatasets().get(0).getData().add(0);
      chart.getDatasets().get(0).getData().add(Integer.parseInt(Config.get().getOptions().get("thresholds").split(":")[0])); // blue level threshold
    }
    return buildResponse(chart);
  }

  private Response buildResponse(Object value) throws IOException {
    return Response.status(200)
        .header("Access-Control-Allow-Origin", "*")
        .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
        .header(HttpHeaders.CACHE_CONTROL, "no-store, must-revalidate, no-cache, max-age=0")
        .header(HttpHeaders.PRAGMA, "no-cache")
        .entity(Json.newObjectMapper(true).writeValueAsString(value))
        .build();
  }

  private int getTotalPoints(String username) {
    Database2 db = Database2.get();
    Map<String, Integer> scorecard = db.getScoreCards().get(username);
    int total = 0;
    for (Entry<String, Integer> s : scorecard.entrySet()) {
      total += s.getValue();
    }
    return total;
  }

  private int getPointsToNextLevel(String username) {
    Database2 db = Database2.get();
    int total = getTotalPoints(username);
    Map<String, String> userInfo = db.getUsers().get(username);
    int pointsToNextLevel = LevelsUtil.get().getNextLevel(userInfo.get("level")).getLeft() - total;
    if (pointsToNextLevel < 0) pointsToNextLevel = 0;
    return pointsToNextLevel;
  }

}
