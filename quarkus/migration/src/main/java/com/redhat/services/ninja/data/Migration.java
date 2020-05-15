package com.redhat.services.ninja.data;

import com.redhat.services.ninja.entity.Event;
import com.redhat.services.ninja.entity.Scorecard;
import com.redhat.services.ninja.entity.User;

import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Migration {
    public static void main(String... args) throws IOException {
        InputStream jsonInputStream = Files.newInputStream(Path.of("old-ninja-db.json"));
        JsonObject oldDatabase = Json.createReader(jsonInputStream).readObject();

        Migration migration = new Migration();

        migration.users = oldDatabase.getJsonObject("users")
                .values().stream()
                .map(JsonObject.class::cast)
                .map(jo -> {
                    User user = new User();

                    user.setLevel(jo.getString("level"));
                    user.setDisplayName(jo.getString("displayName", ""));
                    user.setGithubUsername(jo.getString("githubId", ""));
                    user.setLevelChanged(jo.getString("levelChanged"));
                    user.setEmail(jo.getString("email"));
                    user.setTrelloUsername(jo.getString("trelloId", ""));
                    user.setUsername(jo.getString("username"));
                    user.setRegion(jo.getString("geo", ""));

                    return user;
                }).collect(Collectors.toSet());

        migration.createdOn = LocalDateTime.parse(oldDatabase.getString("created"));

        migration.scorecards = oldDatabase.getJsonObject("scoreCards").entrySet().stream()
                .map(e -> {
                    JsonObject jo = (JsonObject) e.getValue();

                    Map<String, Integer> scores = jo.entrySet().stream().collect(Collectors.toMap(
                            Map.Entry::getKey,
                            score -> ((JsonNumber) score.getValue()).intValueExact()
                    ));

                    Scorecard scorecard = new Scorecard();
                    scorecard.setUsername(e.getKey());
                    scorecard.setPointMap(scores);
                    return scorecard;
                }).collect(Collectors.toSet());

        migration.events = oldDatabase.getJsonArray("events").stream()
                .map(JsonValue::asJsonObject)
                .map(jo -> {
                    Event event = new Event();
                    
                    event.setDescription(jo.getString("text"));
                    event.setType(jo.getString("type"));
                    event.setUser(jo.getString("user"));
                    event.setTimestamp(LocalDateTime.parse(jo.getString("timestamp")));
                    
                    return event;
                }).collect(Collectors.toList());

        JsonbConfig config = new JsonbConfig().withFormatting(true).withPropertyOrderStrategy(PropertyOrderStrategy.ANY);
        Jsonb jsonb = JsonbBuilder.newBuilder().withConfig(config).build();

        Path ninjaPath = Paths.get("new-ninja-db.json");
        String content = jsonb.toJson(migration);
        Files.writeString(ninjaPath, content);
    }

    private LocalDateTime createdOn = LocalDateTime.now();
    private Set<User> users;
    private Set<Scorecard> scorecards;
    private List<Event> events;

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public Set<Scorecard> getScorecards() {
        return scorecards;
    }

    public void setScorecards(Set<Scorecard> scorecards) {
        this.scorecards = scorecards;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }
}
