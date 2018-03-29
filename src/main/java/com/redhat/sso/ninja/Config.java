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
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.redhat.sso.ninja.utils.IOUtils2;
import com.redhat.sso.ninja.utils.Json;

public class Config {
  private static final Logger log=Logger.getLogger(Config.class);
  public static final File STORAGE=new File("config.json");
  private static Config instance;
  private List<Map<String,Object>> scripts=null;
  private Map<String,String> options=null;
  private Map<String,Object> values=null;
  
  
  public Config(){}
  public Config(String json){
    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX="+json);
    try{
      Config x=Json.newObjectMapper(true).readValue(json, Config.class);
      this.options=x.options;
      this.scripts=x.scripts;
      this.values=x.values;
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
  
  // Config options to be able to configure
  // - cycle/zero points every X weeks (or would you want a rolling total?)
  // - multiple pools per user
  // - each pool much have a configurable way of pulling the info (groovy?)
  // - each pool points must have a configurable way of calculating the multiple pool values into a consolidated perception of score
  // - Heartbeat to pull data from last time it was run - must be persistent and survive server restarts
  
  
  
  
  public Map<String,String> getOptions() {if (options==null) options=new HashMap<String, String>(); return options;}
  public List<Map<String,Object>> getScripts() {if (scripts==null) scripts=new ArrayList<Map<String, Object>>(); return scripts;}
  public Map<String,Object> getValues() {if (values==null) values=new HashMap<String, Object>(); return values;}
  
  class MapBuilder<K,V>{
    Map<K, V> values=new HashMap<K, V>();
    public MapBuilder<K,V> put(K key, V value){
      values.put(key, value); return this;
    }
    public Map<K, V> build(){
      return values;
    }
  }
  
  public static void main(String[] asd){
    Config c=Config.get();
//    c.getOptions().put("sources", "com.redhat.sso.ninja.TrelloSync");
    
    c.getScripts().add(c.new MapBuilder<String, Object>()
        .put("source", "com.redhat.sso.ninja.TrelloSync")
        .put("type", "class")
        .put("options", c.new MapBuilder<String, String>().put("organizationName","redhatcop").build())
        .build());
    c.getScripts().add(c.new MapBuilder<String, Object>().put("source", "/home/mallen/poc/script1.perl").put("type", "perl").build());
    c.getScripts().add(c.new MapBuilder<String, Object>().put("source", "/home/mallen/poc/script2.sh").put("type", "bash").build());
//    c.getScripts().put("scripts", scripts);
    
    
//    Map<String, String> scripts=new HashMap<String, String>();
//    scripts.put("doX", "/home/mallen/poc/script1.perl");
    
    c.getOptions().put("heartbeat.intervalInSeconds", "60000");
    c.getValues().put("lastRun", 1520660463301l);
    c.save();
  }
  
  public void save(){
    try{
      IOUtils2.writeAndClose(Json.newObjectMapper(true).writeValueAsString(instance).getBytes(), new FileOutputStream(Config.STORAGE));
    }catch (IOException e){
      e.printStackTrace();
    }
  }
  
  public static Config get(){
    if (instance==null){
      try{
        log.debug("Looking for config in: "+STORAGE.getAbsolutePath());
        if (!Config.STORAGE.exists()){
          // copy the default config over
          IOUtils.copy(Config.class.getClassLoader().getResourceAsStream(STORAGE.getName()), new FileOutputStream(STORAGE));
          
          // copy the default scripts over to where they can be executed
          IOUtils.copy(Config.class.getClassLoader().getResourceAsStream("scripts/github-stats.py"), new FileOutputStream("github-stats.py"));
          
          Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
          perms.add(PosixFilePermission.OWNER_READ);
          perms.add(PosixFilePermission.OWNER_WRITE);
          perms.add(PosixFilePermission.OWNER_EXECUTE);
          Files.setPosixFilePermissions(new File("github-stats.py").toPath(), perms);
          
        }
//          instance=new Config();
//        }else{
          String toLoad=IOUtils2.toStringAndClose(new FileInputStream(Config.STORAGE));
          instance=Json.newObjectMapper(true).readValue(new ByteArrayInputStream(toLoad.getBytes()), Config.class);
//        }
//        UserController uc=new UserController();
//        GoogleAddressResolution gar=new CachedGoogleAddressResolution(false);
//        boolean changed=false;
//        for(Architect a:instance.getArchitects().values()){
//          if (a.getHome()==null || a.getHome().length()<=0){
//            changed=true;
//            List<User> userList=uc.search("uid", a.getUid());
//            if (userList.size()!=1) continue; // uncertain? dont do anything
//            User user=userList.get(0);
//            a.setName(user.getName());
//            String country=instance.countryCodeToName.get(user.getCountry());
//            if (country==null){
//              System.err.println("Unknown country code ["+user.getCountry()+"]");
//              continue;
//            }
//            Map<String, String> formattedAddress=gar.getFormattedAddress(country);
//            a.setHome(formattedAddress.get("longitude")+","+formattedAddress.get("latitude"));
//          }
//        }
//        if (changed){
//          String str=Json.newObjectMapper(false).writeValueAsString(instance);
//          IOUtils2.writeAndClose(str.getBytes(), new FileOutputStream(new File("config2.json")));
//        }
        
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
}


