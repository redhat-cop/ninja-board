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

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.type.TypeReference;

import com.redhat.sso.ninja.utils.IOUtils2;
import com.redhat.sso.ninja.utils.Json;

public class XDatabase{
  private static final String storage="database.json";
  // PoolId -> UserId + Score
  private Map<String, Map<String, Integer>> pools;
  private Map<String, User> users;
  private String created;
  static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  
  public void setUsers(Map<String,User> value){ this.users=value; }
  public Map<String,User> getUsers(){
    if (null==users) users=new HashMap<String, User>();
      return this.users;
  }
  
  public String getCreated(){
    return created;
  }
  
  public XDatabase(){
    created=sdf.format(new Date());
  }
  public XDatabase increment(String poolId, String userId, Integer increment){
    if (!pools.containsKey(poolId)) pools.put(poolId, new HashMap<String, Integer>());
    Map<String, Integer> pool=pools.get(poolId);
    if (!pool.containsKey(userId)){
      pool.put(userId, increment);
    }else{
      pool.put(userId, pool.get(userId)+increment);
    }
    return this;
  }
  public Map<String, Map<String, Integer>> getPools(){
    if (null==pools) pools=new HashMap<String, Map<String,Integer>>();
    return pools;
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
  
  public static synchronized XDatabase load(){
    try{
//      Database db=new Database();
//      Database db=Json.newObjectMapper(true).readValue(IOUtils2.toStringAndClose(new FileInputStream(new File(storage))), new TypeReference<HashMap<String,Map<String,Integer>>>(){});
      XDatabase db=Json.newObjectMapper(true).readValue(IOUtils2.toStringAndClose(new FileInputStream(new File(storage))), XDatabase.class);
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
  
  public static XDatabase get(){
    if (!new File(storage).exists())
      new XDatabase().save();
    
    return XDatabase.load();
  }
  
  public static void main(String[] asd){
    XDatabase.get().increment("pool", "test", 1);
  }

}
