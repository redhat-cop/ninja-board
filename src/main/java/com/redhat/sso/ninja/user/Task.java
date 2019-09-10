package com.redhat.sso.ninja.user;

public class Task {

  private String id;

  private String uid;

  private String timestamp;

  private String user;

  private String list;

  private String title;

  private String owners;

  private String labels;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getList() {
    return list;
  }

  public void setList(String list) {
    this.list = list;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getOwners() {
    return owners;
  }

  public void setOwners(String owners) {
    this.owners = owners;
  }

  public String getLabels() {
    return labels;
  }

  public void setLabels(String labels) {
    this.labels = labels;
  }

}
