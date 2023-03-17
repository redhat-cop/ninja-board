package com.redhat.sso.ninja.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.redhat.sso.ninja.utils.Config;
import com.redhat.sso.ninja.utils.IOUtilsCustom;
import com.redhat.sso.ninja.utils.Json;

import io.quarkus.logging.Log;

/**
 * Database
 */
@ApplicationScoped
public class Database {

  // @ConfigProperty(name = "app.database",
  // defaultValue="target/ninja-persistence/database.json")
  private static Database instance = null;
  static String STORAGE = ConfigProvider.getConfig().getValue("app.database", String.class);

  private Map<String, Map<String, Integer>> scorecards;
  private Map<String, Map<String, String>> users;
  private List<Map<String, String>> events;
  private List<Map<String, String>> tasks;
  private String created;
  private String version;
  public static Integer maxEventEntries=0;

  static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  static SimpleDateFormat sdf2=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

  @Inject
  Logger log;

  public static Database get() {
    return get(new File(STORAGE));
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

  public static Database get(File storage) {
    if (instance != null)
      return instance;
    if (!new File(STORAGE).exists()) {
      Log.warn("No database file found, creating new/blank/default one...");
      new Database().save();
    }
    instance = Database.load();

    return instance;
  }

  public synchronized void save() {
    save(new File(STORAGE));
  }

  public void addEvent(String type, String user, String text) {
    Map<String, String> event = new HashMap<String, String>();
    event.put(EVENT_FIELDS.TIMESTAMP.v, sdf2.format(new Date()));
    event.put(EVENT_FIELDS.TYPE.v, type);
    event.put(EVENT_FIELDS.USER.v, user);
    if (text != null && !"".equals(text))
      event.put(EVENT_FIELDS.TEXT.v, text);
    getEvents().add(event);

    // limit the events to a configurable number of entries
    while (getEvents().size() > getMaxEventEntries()) {
      getEvents().remove(0);
    }
  }

  private Set<String> pointsDuplicateChecker = new HashSet<String>();

  public Set<String> getPointsDuplicateChecker() {
    if (null == pointsDuplicateChecker)
      pointsDuplicateChecker = new HashSet<String>();
    return pointsDuplicateChecker;
  }

  public static synchronized Integer getMaxEventEntries(){
  	if (maxEventEntries<=0){
  		String max=Config.get().getOptions().get("events.max");
  		if (null!=max && max.matches("\\d+")){
  			maxEventEntries=Integer.parseInt(max);
  		}
  	}
  	return maxEventEntries;
  }

  private Map<String, Map<String, String>> scorecardHistory = new HashMap<String, Map<String, String>>();

  public Map<String, Map<String, String>> getScorecardHistory() {
    if (null == scorecardHistory)
      scorecardHistory = new HashMap<String, Map<String, String>>();
    return scorecardHistory;
  }

  public static synchronized Database load() {
    try {
      Database db = Json.newObjectMapper(true)
          .readValue(IOUtilsCustom.toStringAndClose(new FileInputStream(new File(STORAGE))), Database.class);
      return db;
    } catch (JsonParseException e) {
      e.printStackTrace();
    } catch (JsonMappingException e) {
      e.printStackTrace();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public synchronized void save(File storeHere) {
    try {
      long s = System.currentTimeMillis();
      if (!storeHere.getParentFile().exists())
        storeHere.getParentFile().mkdirs();
      IOUtilsCustom.writeAndClose(Json.newObjectMapper(true).writeValueAsBytes(this), new FileOutputStream(storeHere));
//      log.info("Database saved (" + (System.currentTimeMillis() - s) + "ms, size=" + storeHere.length() + ")");
    } catch (JsonGenerationException e) {
      e.printStackTrace();
    } catch (JsonMappingException e) {
      e.printStackTrace();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Map<String, Map<String, String>> getUsers() {
    if (null == users)
      users = new HashMap<String, Map<String, String>>();
    return users;
  }

  public Map<String, Map<String, Integer>> getScoreCards() {
    if (null == scorecards)
      scorecards = new HashMap<String, Map<String, Integer>>();
    return scorecards;
  }

  public List<Map<String, String>> getEvents() {
    if (null == events)
      events = new ArrayList<Map<String, String>>();
    return events;
  }

  public enum EVENT_FIELDS {
    TIMESTAMP("timestamp"),
    TYPE("type"),
    USER("user"),
    TEXT("text"),
    POINTS("points"),
    SOURCE("source"),
    POOL("pool"),
    ;

    public String v;

    EVENT_FIELDS(String v) {
      this.v = v;
    }
  }

  public List<Map<String, String>> getTasks() {
    if (null == tasks)
      tasks = new ArrayList<Map<String, String>>();
    return tasks;
  }

  public enum TASK_FIELDS {
    TIMESTAMP("timestamp"),
    USER("user"),

    LIST("list"),
    ID("id"),
    UID("uid"),
    TITLE("title"),
    OWNERS("owners"),
    LABELS("labels");

    public String v;

    TASK_FIELDS(String v) {
      this.v = v;
    }
  }
}