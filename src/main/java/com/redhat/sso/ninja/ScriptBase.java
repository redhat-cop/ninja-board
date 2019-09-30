package com.redhat.sso.ninja;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public abstract class ScriptBase{
  public abstract void execute(String name, Map<String,String> options, Integer daysFromLastRun, PointsAdder adder);
  
  public Map<String, String> getUsersBy(String key){
    Map<String, String> result= new HashMap<>();
    for(Entry<String, Map<String, String>> e:Database2.get().getUsers().entrySet()){
      result.put(e.getValue().get(key), e.getKey());
    }
    return result;
  }
}
