package com.redhat.services.ninja.data;

import com.redhat.services.ninja.entity.*;

import javax.json.*;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.config.PropertyOrderStrategy;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Migration {
    public static void main(String... args) throws IOException {
        Database database = new Database();
        InputStream jsonInputStream = Files.newInputStream(Path.of("old-ninja-db.json"));
        JsonObject oldDatabase = Json.createReader(jsonInputStream).readObject();

        var users = oldDatabase.getJsonObject("users")
                .values().stream()
                .map(JsonObject.class::cast)
                .map(jo -> {
                    User user = new User();

                    user.setDisplayName(jo.getString("displayName", ""));
                    user.setGithubUsername(jo.getString("githubId", ""));
                    user.setEmail(jo.getString("email"));
                    user.setTrelloUsername(jo.getString("trelloId", ""));
                    user.setUsername(jo.getString("username"));
                    user.setRegion(jo.getString("geo", ""));

                    return user;
                }).collect(Collectors.toSet());
        
        database.setUsers(users);

        var createdOn = LocalDateTime.parse(oldDatabase.getString("created"));
        database.setCreatedOn(createdOn);

        var scorecards = oldDatabase.getJsonObject("scoreCards").entrySet().stream()
                .map(e -> {
                    JsonObject jo = (JsonObject) e.getValue();

                    Map<String, Integer> scores = jo.entrySet().stream().collect(Collectors.toMap(
                            Map.Entry::getKey,
                            score -> ((JsonNumber) score.getValue()).intValueExact()
                    ));

                    Scorecard scorecard = new Scorecard();
                    scorecard.setUsername(e.getKey());
                    scorecard.setDetails(scores);
                    scorecard.computeLevel(database.getLevels());
                    return scorecard;
                }).collect(Collectors.toSet());
        
        database.setScorecards(scorecards);

        var events = oldDatabase.getJsonArray("events").stream()
                .map(JsonValue::asJsonObject)
                .map(jo -> {
                    Event event = new Event();
                    
                    event.setDescription(jo.getString("text"));
                    event.setType(jo.getString("type"));
                    event.setUser(jo.getString("user"));
                    event.setTimestamp(LocalDateTime.parse(jo.getString("timestamp")));
                    
                    return event;
                }).collect(Collectors.toList());
        
        database.setEvents(events);

        Set<Period> history = oldDatabase.getJsonObject("scorecardHistory").entrySet().stream()
                .map(entry -> {
                    Map<String, Record> records = entry.getValue().asJsonObject().entrySet().stream()
                            .map(record ->
                                    {
                                        String recordDetail = ((JsonString) record.getValue()).getString();
                                        String[] splitDetail = recordDetail.split("\\|");

                                        return Map.entry(
                                                record.getKey(),
                                                new Record(splitDetail[0], Integer.parseInt(splitDetail[1]))
                                        );
                                    }
                            ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                    return new Period(entry.getKey(), records);
                }).collect(Collectors.toSet());
        
        database.setHistory(new TreeSet<>(history));

        JsonbConfig config = new JsonbConfig().withFormatting(true).withPropertyOrderStrategy(PropertyOrderStrategy.ANY);
        Jsonb jsonb = JsonbBuilder.newBuilder().withConfig(config).build();

        Path ninjaPath = Paths.get("new-ninja-db.json");
        String content = jsonb.toJson(database);
        Files.writeString(ninjaPath, content);
    }
}
