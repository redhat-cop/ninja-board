package com.redhat.sso.ninja.user;

import java.util.Map;

public class User{
  private String username;
  private String email;
  private String githubUsername;
  private String trelloUsername;
  private Map<String, Integer> scorecard;
  
  public User(String username, String email, String github, String trello){
    this.username=username;
    this.email=email;
    this.githubUsername=github;
    this.trelloUsername=trello;
  }
  
  public Map<String, Integer> getScorecard(){
    return scorecard;
  }
  public void setScorecard(Map<String, Integer> scorecard){
    this.scorecard=scorecard;
  }
  public String getUsername(){
    return username;
  }
  public void setUsername(String username){
    this.username=username;
  }
  public String getGithubUsername(){
    return githubUsername;
  }
  public void setGithubUsername(String githubUsername){
    this.githubUsername=githubUsername;
  }
  public String getTrelloUsername(){
    return trelloUsername;
  }
  public void setTrelloUsername(String trelloUsername){
    this.trelloUsername=trelloUsername;
  }
  public String getEmail(){
    return email;
  }
  public void setEmail(String email){
    this.email=email;
  }
}
