package com.redhat.sso.ninja;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.redhat.sso.ninja.utils.IOUtils2;
import com.redhat.sso.ninja.utils.Json;

public class Config {
  private static final Logger log = Logger.getLogger(Config.class);
  private static final File STORAGE = new File("target/ninja-persistence", "config.json");
  private static Config instance;
  private List<Map<String, Object>> scripts = null;
  private Map<String, String> options = null;
  private Map<String, Object> values = null;


  public Config() {
  }

  public Config(String json) {
    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX=" + json);
    try {
      Config x = Json.newObjectMapper(true).readValue(json, Config.class);
      this.options = x.options;
      this.scripts = x.scripts;
      this.values = x.values;
      instance = this;
    } catch (IOException e) {
      log.error("Error parsing config.json: ", e);
      throw new IllegalStateException(e);
    }
  }

  // Config options to be able to configure
  // - cycle/zero points every X weeks (or would you want a rolling total?)
  // - multiple pools per user
  // - each pool much have a configurable way of pulling the info (groovy?)
  // - each pool points must have a configurable way of calculating the multiple pool values into a consolidated perception of score
  // - Heartbeat to pull data from last time it was run - must be persistent and survive server restarts


  /**
   * Gets a specific option from the configs
   *
   * @param key The option to retrieve
   * @return The option, or null if it does not exist.
   */
  public String getOption(String key) {
    return get().getOptions().get(key);
  }

  public String getEnvOption(String key) {
    return get().getOptions().get("ENV." + key);
  }

  public Map<String, String> getOptions() {
    if (options == null) options = new HashMap<>();
    return options;
  }

  public List<Map<String, Object>> getScripts() {
    if (scripts == null) scripts = new ArrayList<>();
    return scripts;
  }

  public Map<String, Object> getValues() {
    if (values == null) values = new HashMap<>();
    return values;
  }

//  class MapBuilder<K,V>{
//    Map<K, V> values=new HashMap<K, V>();
//    public MapBuilder<K,V> put(K key, V value){
//      values.put(key, value); return this;
//    }
//    public Map<K, V> build(){
//      return values;
//    }
//  }


  public void save() {
    try {
      if (!Config.STORAGE.getParentFile().exists()) {
        log.info("Config storage folder didn't exist - creating new folder to store config");
        Files.createDirectories(Paths.get(Config.STORAGE.getParentFile().getAbsolutePath()));
      }
      IOUtils2.writeAndClose(Json.newObjectMapper(true).writeValueAsString(instance).getBytes(), new FileOutputStream(Config.STORAGE));
      log.info("Config saved (size=" + Config.STORAGE.length() + ")");
    } catch (IOException e) {
      log.error("Error saving configuration: ", e);
      throw new IllegalStateException(e);
    }
  }

  public static Config get() {
    if (instance == null) {
      try {
        if (!Config.STORAGE.exists()) {
          log.info("Config file doesn't exist, creating default one here: " + STORAGE.getAbsolutePath());
          if (!Config.STORAGE.getParentFile().exists()) {
            Files.createDirectories(Paths.get(Config.STORAGE.getParentFile().getAbsolutePath()));
          }
          // copy the default config over
          InputStream is = Config.class.getClassLoader().getResourceAsStream(STORAGE.getName());
          if (is != null) {
            IOUtils.copy(is, new FileOutputStream(STORAGE));
          }

        }
//          instance=new Config();
//        }else{
        log.info("Config loading (size=" + Config.STORAGE.length() + ")");
        String toLoad = IOUtils2.toStringAndClose(new FileInputStream(Config.STORAGE));
        instance = Json.newObjectMapper(true).readValue(new ByteArrayInputStream(toLoad.getBytes()), Config.class);
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

        /* load the entries from the environment file, if one exists */
        Dotenv env = Dotenv
            .configure()
            .filename("board.env")
            .ignoreIfMissing()
            .load();
        for (DotenvEntry entry : env.entries()) {
          instance.getOptions().put("ENV." + entry.getKey(), entry.getValue());
        }

      } catch (Exception e) {
        log.error("Error instantiating configuration: ", e);
        throw new IllegalStateException(e);
      }
    }
    return instance;
  }

  public void setOptions(Map<String, String> value) {
    this.options = value;
  }

  @JsonIgnore
  public String getNextTaskNum() {
//  	Config cfg=Config.get();

    if (!getValues().containsKey("lastTaskNum")) {
      getValues().put("lastTaskNum", 0);
    }

    int result = 1 + (Integer) getValues().get("lastTaskNum");
    getValues().put("lastTaskNum", result);
    save();

    return String.valueOf(result);
  }

}


