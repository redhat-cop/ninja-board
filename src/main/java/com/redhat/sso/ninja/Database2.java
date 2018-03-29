package com.redhat.sso.ninja;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.type.TypeReference;

import com.redhat.sso.ninja.utils.IOUtils2;
import com.redhat.sso.ninja.utils.Json;

public class Database2{
  private static final Logger log=Logger.getLogger(Database2.class);
  private static final String storage="database2.json";
  
  
  // User -> Pool (sub pool separated with a dot) + Score
//  private Map<User, Map<String, Integer>> users;
  
  
  private Map<String, Map<String, Integer>> scorecards;
  private Map<String, Map<String, String>> users;
  
  
  
  // PoolId -> UserId + Score
//  private Map<String, Map<String, Integer>> pools;
//  private Map<String, User> users;
  private String created;
  static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  
  public Database2(){
    created=sdf.format(new Date());
  }
  public String getCreated(){ return created; }
  
  
//  public void setUsers(Map<String,User> value){ this.users=value; }
//  public Map<String,User> getUsers(){
//    if (null==users) users=new HashMap<String, User>();
//      return this.users;
//  }
  
  
  public Database2 increment(String poolId, String userId, Integer increment){
    if (null==poolId || null==userId){
      log.error("Unable to add due to null key [poolId="+poolId+", userId="+userId+"]");
      return this;
    }
    if (users.containsKey(userId)){ // means the user is registered
      if (null==scorecards.get(userId)) scorecards.put(userId, new HashMap<String, Integer>());
      if (null==scorecards.get(userId).get(poolId)) scorecards.get(userId).put(poolId, 0);
      scorecards.get(userId).put(poolId, scorecards.get(userId).get(poolId)+increment);
    }else{
      System.out.println("Unregistered user detected ["+userId+"]");
    }
    
//    if (!pools.containsKey(poolId)) pools.put(poolId, new HashMap<String, Integer>());
//    Map<String, Integer> pool=pools.get(poolId);
//    if (!pool.containsKey(userId)){
//      pool.put(userId, increment);
//    }else{
//      pool.put(userId, pool.get(userId)+increment);
//    }
    return this;
  }
//  public Map<String, Map<String, Integer>> getPools(){
//    if (null==pools) pools=new HashMap<String, Map<String,Integer>>();
//    return pools;
//  }
  public Map<String, Map<String, String>> getUsers(){
    if(null==users) users=new HashMap<String, Map<String, String>>();
    return users;
  }
  public Map<String, Map<String, Integer>> getScoreCards(){
    if (null==scorecards) scorecards=new HashMap<String, Map<String,Integer>>();
    return scorecards;
  }
  
  @JsonIgnore
  private Map<String, Map<String, Integer>> leaderboard=new HashMap<String, Map<String, Integer>>();
  public Map<String, Map<String, Integer>> getLeaderboard(){
//    leaderboard.putAll(users);
    for(Entry<String, Map<String, String>> e:users.entrySet()){
      leaderboard.put(e.getKey(), new HashMap<String, Integer>());
    }
    
    for(Entry<String, Map<String, Integer>> e:scorecards.entrySet()){
//      leaderboard.get(e.getKey()).setScorecard(e.getValue());
      leaderboard.get(e.getKey()).putAll(e.getValue());
    }
    return leaderboard;
  }
  
  public synchronized void save(){
    try{
      IOUtils2.writeAndClose(Json.newObjectMapper(true).writeValueAsBytes(this), new FileOutputStream(new File(storage)));
    }catch (JsonGenerationException e){
      e.printStackTrace();
    }catch (JsonMappingException e){
      e.printStackTrace();
    }catch (FileNotFoundException e){
      e.printStackTrace();
    }catch (IOException e){
      e.printStackTrace();
    }
  }
  
  public static synchronized Database2 load(){
    try{
//      Database db=new Database();
//      Database db=Json.newObjectMapper(true).readValue(IOUtils2.toStringAndClose(new FileInputStream(new File(storage))), new TypeReference<HashMap<String,Map<String,Integer>>>(){});
      Database2 db=Json.newObjectMapper(true).readValue(IOUtils2.toStringAndClose(new FileInputStream(new File(storage))), Database2.class);
      return db;
    }catch (JsonParseException e){
      e.printStackTrace();
    }catch (JsonMappingException e){
      e.printStackTrace();
    }catch (FileNotFoundException e){
      e.printStackTrace();
    }catch (IOException e){
      e.printStackTrace();
    }
    return null;
  }
  
  public static Database2 get(){
    if (!new File(storage).exists())
      new Database2().save();
    return Database2.load();
  }
  
  public static void main(String[] asd){
    Database2.get().increment("pool", "test", 1);
  }

}
