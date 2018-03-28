package com.redhat.sso.ninja;

public interface PointsAdder{
  public void addPoints(String username, String pool, Integer increment);
//  public void execute(String copName, Integer daysFromLastRun);
}
