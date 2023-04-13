package com.redhat.sso.ninja.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.sso.ninja.database.Database;
import com.redhat.sso.ninja.model.User;
import com.redhat.sso.ninja.utils.Json;
import com.redhat.sso.ninja.utils.LevelsUtil;
import com.redhat.sso.ninja.utils.MapBuilder;

@Path("/api/scorecards")
public class ScoreCards {

    @Path("/{user}")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getScoreCardById(@PathParam("user") String user) {
        try {
            Database db = Database.get();

            Map<String, Integer> scorecard = db.getScoreCards().get(user);
            Map<String, String> userInfo = db.getUsers().get(user);

            String payload = "{\"status\":\"ERROR\",\"message\":\"Unable to find user: " + user
                    + "\", \"displayName\":\"You (" + user + ") are not registered\"}";

            Map<String, Object> data = new HashMap<String, Object>();
            data.put("userId", user);
            if (null != scorecard)
                data.putAll(scorecard);
            if (null != userInfo)
                data.putAll(userInfo);

            payload = Json.newObjectMapper(true).writeValueAsString(data);
            return Response.ok().entity(payload).build();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Response.status(500).build();
        }
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    public Response listScoreCards() {
        Database db = Database.get();

        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

        Set<String> fields = new HashSet<String>();

        for (Entry<String, Map<String, String>> u : db.getUsers().entrySet()) {
            String username = u.getKey();
            Map<String, String> userInfo = u.getValue();
            Map<String, Integer> scorecard = db.getScoreCards().get(u.getKey());
            Map<String, Object> row = new HashMap<String, Object>();
            row.put("id", username);
            row.put("name", userInfo.containsKey("displayName") ? userInfo.get("displayName") : username);
            int total = 0;
            if (null != scorecard) {
                for (Entry<String, Integer> s : scorecard.entrySet()) {
                    row.put(s.getKey().replaceAll("\\.", " "), s.getValue());
                    total += s.getValue();
                    fields.add(s.getKey().replaceAll("\\.", " "));
                }
                row.put("total", total);
                row.put("level", userInfo.get("level"));
            } else {
                row.put("total", 0);
                row.put("level", "ZERO");
            }
            // points to next level
            if (null == userInfo.get("level") || null == LevelsUtil.get().getNextLevel(userInfo.get("level"))) {
                // log.error("Invalid level for user "+row.get("name")+" :
                // "+Json.newObjectMapper(true).writeValueAsString(userInfo));
                row.put("pointsToNextLevel", 0);
            } else {
                Integer pointsToNextLevel = LevelsUtil.get().getNextLevel(userInfo.get("level")).getLeft() - total;
                if (pointsToNextLevel < 0)
                    pointsToNextLevel = 0;
                row.put("pointsToNextLevel", pointsToNextLevel);
            }

            data.add(row);
        }

        // fill in the missing points fields with zero's
        for (Map<String, Object> e : data) {
            for (String field : fields) {
                if (!e.containsKey(field)) {
                    e.put(field, 0);
                }
            }
        }

        Map<String, Object> wrapper = new HashMap<String, Object>();
        List<Map<String, String>> columns = new ArrayList<Map<String, String>>();
        columns.add(new MapBuilder<String, String>().put("title", "Name").put("data", "name").build());
        columns.add(new MapBuilder<String, String>().put("title", "Total").put("data", "total").build());

        columns.add(new MapBuilder<String, String>().put("title", "Ninja Belt").put("data", "level").build());
        columns.add(new MapBuilder<String, String>().put("title", "Points to next level")
                .put("data", "pointsToNextLevel").build());

        for (String field : fields)
            columns.add(new MapBuilder<String, String>().put("title", field).put("data", field).build());

        wrapper.put("columns", columns);
        wrapper.put("data", data);

        return Response.ok().entity(wrapper).build();
    }

    @POST
    @Path("/{user}")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response saveScorecard(@PathParam("user") String user, String payload) {

        Database db = Database.get();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map;
        try {
            map = objectMapper.readValue(payload, new TypeReference<HashMap<String, Object>>() {
            });

            String username = (String) map.get("userId");

            Map<String, String> userInfo = db.getUsers().get(username);
            Map<String, Integer> scorecard = db.getScoreCards().get(username);

            for (String k : map.keySet()) {
                if (!k.equals("userId")) {

                    if (userInfo.containsKey(k)) {
                        if (!userInfo.get(k).equals(map.get(k))) { // if it's changed then...
                            db.addEvent("User Update", user,
                                    k + " changed from " + userInfo.get(k) + " to " + (String) map.get(k));
                            userInfo.put(k, (String) map.get(k));
                        }
                    } else if (scorecard.containsKey(k)) {
                        if (!scorecard.get(k).equals(map.get(k))) { // if it's changed then...
                            db.addEvent("User Update", user,
                                    k + " changed from " + scorecard.get(k) + " to " + (String) map.get(k));
                            scorecard.put(k, Integer.parseInt((String) map.get(k)));
                        }
                    } else {
                        if (!userInfo.get(k).equals(map.get(k))) { // if it's changed then...
                            db.addEvent("User Update", user, k + " set as " + (String) map.get(k));
                            userInfo.put(k, (String) map.get(k));
                        }
                        //// ALERT! unknown field
                        // log.error("UNKNOWN FIELD: "+k+" = "+map.get(k));
                    }

                }
            }

            db.save();
            return Response.status(201).build();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Response.status(500).build();
        }
    }
}
