package com.redhat.sso.ninja;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.JsonMappingException;

import com.redhat.sso.ninja.utils.IOUtils2;
import com.redhat.sso.ninja.utils.Json;
import com.redhat.sso.ninja.utils.MapBuilder;

public class Config {
  private static final Logger log=Logger.getLogger(Config.class);
  public static final File STORAGE=new File("target/ninja-persistence", "config.json");
  private static Config instance;
  private List<Map<String,Object>> scripts=null;
  private Map<String,String> options=null;
  private Map<String,Object> values=null;
  private List<Map<String,String>> notifications=null;
  
  public Config(){}
  public Config(String json){
    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX="+json);
    try{
      Config x=Json.newObjectMapper(true).readValue(json, Config.class);
      this.options=x.options;
      this.scripts=x.scripts;
      this.values=x.values;
      this.notifications=x.notifications;
      instance=this;
    }catch (JsonParseException e){
      // TODO Auto-generated catch block
      e.printStackTrace();
    }catch (JsonMappingException e){
      // TODO Auto-generated catch block
      e.printStackTrace();
    }catch (IOException e){
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  
  public Map<String,String> getOptions() {if (options==null) options=new HashMap<String, String>(); return options;}
  public List<Map<String,Object>> getScripts() {if (scripts==null) scripts=new ArrayList<Map<String, Object>>(); return scripts;}
  public Map<String,Object> getValues() {if (values==null) values=new HashMap<String, Object>(); return values;}
  public List<Map<String,String>> getNotifications() {if (notifications==null) notifications=new ArrayList<Map<String, String>>(); return notifications;}
  
//  class MapBuilder<K,V>{
//    Map<K, V> values=new HashMap<K, V>();
//    public MapBuilder<K,V> put(K key, V value){
//      values.put(key, value); return this;
//    }
//    public Map<K, V> build(){
//      return values;
//    }
//  }
  
  public void reload(){
  	instance=null;
  	get();
  }
  public void save(){
    try{
      if (!Config.STORAGE.getParentFile().exists()){
      	log.info("Config storage folder didn't exist - creating new folder to store config");
      	Config.STORAGE.getParentFile().mkdirs();
      }
      IOUtils2.writeAndClose(Json.newObjectMapper(true).writeValueAsString(instance).getBytes(), new FileOutputStream(Config.STORAGE));
      log.info("Config saved (size="+Config.STORAGE.length()+")");
    }catch (IOException e){
      e.printStackTrace();
    }
  }
  
  public static Config get(){
    if (instance==null){
      try{
        if (!Config.STORAGE.exists()){
        	log.info("Config file doesn't exist, creating default one here: "+STORAGE.getAbsolutePath());
          if (!Config.STORAGE.getParentFile().exists()) Config.STORAGE.getParentFile().mkdirs();
          // copy the default config over
          IOUtils.copy(Config.class.getClassLoader().getResourceAsStream(STORAGE.getName()), new FileOutputStream(STORAGE));
        }
        log.info("Config loading (location="+Config.STORAGE.getAbsolutePath()+", size="+Config.STORAGE.length()+")");
        String toLoad=IOUtils2.toStringAndClose(new FileInputStream(Config.STORAGE));
        instance=Json.newObjectMapper(true).readValue(new ByteArrayInputStream(toLoad.getBytes()), Config.class);
        
      }catch(Exception e){
        e.printStackTrace();
        instance=new Config();
      }
    }
    return instance;
  }
  
  public void setOptions(Map<String,String> value) {
    this.options=value;
  }
  
  @JsonIgnore
  public String getNextTaskNum(){
  	if (!getValues().containsKey("lastTaskNum")){
  		getValues().put("lastTaskNum", 0);
  	}
  	
  	int result=1+(Integer)getValues().get("lastTaskNum");
  	getValues().put("lastTaskNum", result);
  	save();
  	
  	return String.valueOf(result);
  }
}


