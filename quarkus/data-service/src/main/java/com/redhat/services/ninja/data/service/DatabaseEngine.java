package com.redhat.services.ninja.data.service;

import com.redhat.services.ninja.data.*;
import com.redhat.services.ninja.data.operation.BasicDatabaseOperations;
import com.redhat.services.ninja.data.operation.IdentifiableDatabaseOperations;
import com.redhat.services.ninja.entity.Event;
import com.redhat.services.ninja.entity.Scorecard;
import com.redhat.services.ninja.entity.User;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.config.PropertyOrderStrategy;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
public class DatabaseEngine {
    static private Logger LOGGER = Logger.getLogger(DatabaseEngine.class.getSimpleName());
    @ConfigProperty(name = "database.file", defaultValue = "database.json")
    String databaseLocation;
    @ConfigProperty(name = "events.max", defaultValue = "5000")
    int maxEvents;

    private Map<String, User> users;
    private Map<String, Scorecard> scorecards;
    private Queue<Event> events;
    private Jsonb jsonb;
    private IdentifiableDatabaseOperationsImpl<String, User> userOperations;
    private IdentifiableDatabaseOperationsImpl<String, Scorecard> scorecardOperations;
    private BasicDatabaseOperationsImpl<Event> eventOperations;

    @PostConstruct
    void init() throws IOException {
        JsonbConfig config = new JsonbConfig().withFormatting(true).withPropertyOrderStrategy(PropertyOrderStrategy.ANY);
        jsonb = JsonbBuilder.newBuilder().withConfig(config).build();

        Path databasePath = Paths.get(databaseLocation);

        if(!Files.exists(databasePath)){
            LOGGER.log(Level.INFO, "Database file does not yet exist : " + databaseLocation + ". Creating new database.");
            writeFile(new Database());
        }

        InputStream databaseFile = Files.newInputStream(databasePath);

        Database database = jsonb.fromJson(databaseFile, Database.class);
        users = database.getUsers().parallelStream().collect(Collectors.toMap(User::getUsername, Function.identity()));
        scorecards = database.getScorecards().parallelStream().collect(Collectors.toMap(Scorecard::getUsername, Function.identity()));
        events = new LinkedBlockingQueue<>(maxEvents);
        userOperations = new IdentifiableDatabaseOperationsImpl<>(users);
        scorecardOperations = new IdentifiableDatabaseOperationsImpl<>(scorecards);
        eventOperations = new BasicDatabaseOperationsImpl<>(events){
            @Override
            public Event create(Event entity) {
                if (events.size() >= maxEvents) {
                    events.poll();
                }

                return super.create(entity);
            }
        };
        database.getEvents().forEach(eventOperations::create);
    }

    synchronized public void save() throws IOException {
        Database database = new Database();

        database.setUsers(Set.copyOf(users.values()));
        database.setScorecards(Set.copyOf(scorecards.values()));
        database.setEvents(List.copyOf(events));

        writeFile(database);
    }
    
    synchronized private void writeFile(Database database) throws IOException {
        String content = jsonb.toJson(database);

        Files.writeString(Paths.get(databaseLocation), content);
    }

    public BasicDatabaseOperations<Event> getEventOperations() {
        return eventOperations;
    }

    public IdentifiableDatabaseOperations<String, Scorecard> getScorecardOperations() {
        return scorecardOperations;
    }

    public com.redhat.services.ninja.data.operation.IdentifiableDatabaseOperations<String, User> getUserOperations() {
        return userOperations;
    }
}