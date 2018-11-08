package com.redhat.sso.ninja;

import java.util.Map;

public interface PointsAdder{
  public void addPoints(String username, String pool, Integer increment, Map<String, String> params);
//  public void execute(String copName, Integer daysFromLastRun);
}
