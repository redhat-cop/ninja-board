
package com.redhat.sso.ninja;

import com.redhat.sso.ninja.user.Event;
import com.redhat.sso.ninja.user.Task;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database2Test {

  private Database2 db;

  @Before
  public void before() {
    db = Database2.getInMemory();
  }

  @After
  public void after() {
    Database2.resetInstance();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncrementBlankPoolId() {
    db.increment(null, "", 0, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncrementBlankUserId() {
    db.increment("123", "", 0, null);
  }

  @Test
  public void incrementUnrecognizedUser() {
    db.increment("123", "usernotfound", 1, null);
    List<Event> events = db.findEventsForUser("usernotfound");
    Assert.assertEquals(0, events.size());
  }

  @Test
  public void incrementRecognizedUser() {
    db.addUser("mdobozy");
    db.save();
    db.increment("123", "mdobozy", 1, null);
    List<Event> events = db.findEventsForUser("mdobozy");
    Assert.assertEquals(1, events.size());
    System.out.println(events.get(0));
    db.save();
  }

  @Test(expected = NullPointerException.class)
  public void addInvalidEvent() {
    db.addUser("mdobozy");
    db.addEvent(null, "mdobozy", "123");
  }

  @Test
  public void addValidEvent() {
    db.addUser("mdobozy");
    db.addEvent("type.abc", "mdobozy", "123");
    List<Event> events = db.findEventsForUser("mdobozy");
    Assert.assertEquals(1, events.size());
    Assert.assertEquals("type.abc", events.get(0).getType());
  }

  @Test(expected = NullPointerException.class)
  public void addInvalidTask() {
    db.addTask("123", null);
  }

  @Test(expected = NullPointerException.class)
  public void addInvalidTaskHash() {
    db.addTask(new HashMap<>());
  }

  @Test
  public void addValidTask() {
    Task task = db.addTask("test task", "mdobozy");
    Assert.assertEquals("mdobozy", task.getUser());
    Assert.assertEquals("test task", task.getTitle());
    Assert.assertEquals("todo", task.getList());
    Assert.assertTrue(StringUtils.isNotBlank(task.getUid()));
    Assert.assertTrue(StringUtils.isNotBlank(task.getId()));
    Assert.assertTrue(StringUtils.isNotBlank(task.getTimestamp()));
  }

  @Test(expected = NullPointerException.class)
  public void updateTaskInvalid() {
    db.updateTask(null, new HashMap<>());
  }

  @Test
  public void updateTaskValid() {
    Task task = db.addTask("test task", "mdobozy");
    Map<String, Object> hash = new HashMap<>();
    hash.put("title", "test task mod");
    task = db.updateTask(task.getId(), hash);
    Assert.assertEquals("test task mod", task.getTitle());
  }


}
