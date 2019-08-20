package com.redhat.sso.ninja;

import com.redhat.sso.ninja.user.Event;
import com.redhat.sso.ninja.user.Task;
import com.redhat.sso.ninja.utils.IOUtils2;
import com.redhat.sso.ninja.utils.Json;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class Database2 {

  private static final Logger log = Logger.getLogger(Database2.class);
  private static final String STORAGE = "target/ninja-persistence/database2.json";
  static final File STORAGE_AS_FILE = new File(STORAGE);
  static Integer MAX_EVENT_ENTRIES = 1000;
  public static boolean systemUpdating = false;

  // User -> Pool (sub pool separated with a dot) + Score
  private Map<String, Map<String, Integer>> scorecards = new HashMap<>();
  private Map<String, Map<String, String>> users = new HashMap<>();
  private List<Event> events = new ArrayList<>();
  private List<Task> tasks = new ArrayList<>();
  private Set<String> pointsDuplicateChecker = new HashSet<>();

  private enum Mode { PERSISTENT, IN_MEMORY };

  private Mode mode = Mode.PERSISTENT;

  @JsonIgnore
  private Map<String, Map<String, Integer>> leaderboard = new HashMap<>();

  private static Database2 instance = null;

  // PoolId -> UserId + Score
  private String created;
  private String version;

  private static DateTimeFormatter sdf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
  private static DateTimeFormatter sdf2 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  private Database2() {
    created = sdf.format(LocalDateTime.now());
  }

  public String getCreated() {
    return created;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }


  public Database2 increment(String poolId, String userId, Integer increment, Map<String, String> params) {
    if (StringUtils.isBlank(poolId) || StringUtils.isBlank(userId)) {
      log.error("Either pool id or user id is null.");
      throw new IllegalArgumentException("Unable to add due to null key [poolId=" + poolId + ", userId=" + userId + "]");
    }
    if (users.containsKey(userId)) { // means the user is registered
      scorecards.computeIfAbsent(userId, (k) -> new HashMap<>());
      Map<String, Integer> scorecard = scorecards.get(userId);
      scorecard.computeIfAbsent(poolId, (k) -> 0);
      log.info("Incrementing points: user=" + userId + ", poolId=" + poolId + ", increment/points=" + increment + " + params=" + params);
      scorecard.put(poolId, scorecards.get(userId).get(poolId) + increment);

      if (params != null && params.get("id") != null && params.get("id").startsWith("TR") && null != params.get("linkId")) { // its a trello point
        addEvent("Points Increment", userId, increment + " point" + (increment <= 1 ? "" : "s") + " added to " + poolId + " ([Trello card: " + params.get("linkId") + "|" + params.get("linkId") + "])");
      } else { // it's a point from any other source
        addEvent("Points Increment", userId, increment + " point" + (increment <= 1 ? "" : "s") + " added to " + poolId);
      }


    } else {
      log.warn("Attempting to increment for unregistered user: " + userId);
    }

    return this;
  }

  /**
   * Adds a user to the database. If the specified user already exists, this is a noop.
   * @param userId The ID of the user to add.
   * @return The user info hash.
   */
  public Map<String, String> addUser(String userId) {
    return users.putIfAbsent(userId, new HashMap<>());
  }

  public Map<String, Map<String, String>> getUsers() {
    return users;
  }

  public Map<String, Map<String, Integer>> getScoreCards() {
    return scorecards;
  }

  public List<Event> getEvents() {
    return events;
  }

  public List<Event> findEventsForUser(String userId) {
    if (StringUtils.isBlank(userId)) {
      return Collections.emptyList();
    }
    return events
        .stream()
        .filter(e -> e.getUser().equals(userId))
        .collect(Collectors.toList());
  }

  public List<Task> getTasks() {
    return tasks;
  }

  public void addEvent(String type, String user, String text) {
    Validate.notNull(type, user);
    Event event = new Event();
    event.setTimestamp(sdf2.format(LocalDateTime.now()));
    event.setType(type);
    event.setUser(user);
    event.setText(text);
    getEvents().add(event);

    // limit the events to 100 entries
    while (getEvents().size() > MAX_EVENT_ENTRIES) {
      getEvents().remove(0);
    }
  }

  // user is the target user: ie. fbloggs
  public Task addTask(String taskText, String user) {
    Validate.notNull(user);
    Task task = new Task();
    task.setTimestamp(sdf2.format(LocalDateTime.now()));
    task.setUid(UUID.randomUUID().toString());
    task.setId(Config.get().getNextTaskNum());
    task.setTitle(taskText);
    task.setUser(user);
    task.setList("todo");
    tasks.add(task);
    return task;
  }

  public Task addTask(Map<String, String> taskData) {
    Validate.notNull(taskData);
    Validate.notNull(taskData.get("user"));
    Task task = new Task();
    task.setTimestamp(sdf2.format(LocalDateTime.now()));
    task.setUid(UUID.randomUUID().toString());
    task.setId(Config.get().getNextTaskNum());
    task.setTitle(taskData.get("title"));
    task.setUser(taskData.get("user"));
    task.setList("todo");
    task.setOwners(taskData.get("owners"));
    task.setLabels(taskData.get("labels"));
    tasks.add(task);
    return task;
  }

  public Task updateTask(String id, Map<String, Object> properties) {
    Validate.notNull(id);
    Optional<Task> task = tasks.stream().filter(t -> t.getId().equals(id)).findFirst();
    if (task.isPresent()) {
      Task t = task.get();
      if (properties.containsKey("list")) {
        t.setList((String) properties.get("list"));
      }
      if (properties.containsKey("title")) {
        t.setTitle((String) properties.get("title"));
      }
      if (properties.containsKey("user")) {
        t.setUser((String) properties.get("user"));
      }
      if (properties.containsKey("timestamp")) {
        t.setTimestamp((String) properties.get("timestamp"));
      }
      if (properties.containsKey("owners")) {
        t.setOwners((String) properties.get("owners"));
      }
      if (properties.containsKey("labels")) {
        t.setLabels((String) properties.get("labels"));
      }

      return t;
    } else {
      log.error("Cannot find task with id " + id);
      throw new IllegalArgumentException("Cannot find task with id " + id);
    }
  }


  public Set<String> getPointsDuplicateChecker() {
    if (null == pointsDuplicateChecker) pointsDuplicateChecker = new HashSet<>();
    return pointsDuplicateChecker;
  }

  public Map<String, Map<String, Integer>> getLeaderboard() {
//    leaderboard.putAll(users);
    for (Entry<String, Map<String, String>> e : users.entrySet()) {
      leaderboard.put(e.getKey(), new HashMap<>());
    }

    for (Entry<String, Map<String, Integer>> e : scorecards.entrySet()) {
//      leaderboard.get(e.getKey()).setScorecard(e.getValue());
      leaderboard.get(e.getKey()).putAll(e.getValue());
    }
    return leaderboard;
  }

  public synchronized void save() {
    if (mode == Mode.PERSISTENT) {
      try {
        File sf = new File(STORAGE);
        long s = System.currentTimeMillis();
        if (!sf.getParentFile().exists()) {
          Files.createDirectories(Paths.get(sf.getParentFile().getAbsolutePath()));
        }
        IOUtils2.writeAndClose(Json.newObjectMapper(true).writeValueAsBytes(this), new FileOutputStream(sf));
        log.info("Database saved (" + (System.currentTimeMillis() - s) + "ms, size=" + sf.length() + ")");
      } catch (IOException e) {
        log.error("Error saving database: ", e);
        throw new IllegalStateException(e);
      }
    } else {
      log.debug("Mode is in-memory; db will not persist.");
    }
  }

  private static synchronized Database2 load() {
    try {
//      Database db=new Database();
//      Database db=Json.newObjectMapper(true).readValue(IOUtils2.toStringAndClose(new FileInputStream(new File(storage))), new TypeReference<HashMap<String,Map<String,Integer>>>(){});
      log.info("Database loading (size=" + new File(STORAGE).length() + ")");
      return Json.newObjectMapper(true).readValue(IOUtils2.toStringAndClose(new FileInputStream(new File(STORAGE))), Database2.class);
    } catch (IOException e) {
      log.error("Error loading database: ", e);
      throw new IllegalStateException(e);
    }
  }

  public static Database2 get() {
    if (!Files.exists(Paths.get(STORAGE))) {
      log.warn("No database file found, creating new/blank/default one...");
      new Database2().save();
    }
    if (null == instance) {
      instance = Database2.load();
      log.info("Loading/Replaced 'instance' of database in memory");
    }
//    instance=Database2.load();
    return instance;
  }

  static Database2 getInMemory() {
    instance = new Database2();
    instance.mode = Mode.IN_MEMORY;
    return instance;
  }

  public static void resetInstance() {
    instance = null;
  }

  public static void main(String[] asd) {
    Database2.get().increment("pool", "test", 1, null);
  }


}
