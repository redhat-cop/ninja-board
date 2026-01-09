package com.redhat.cop.giveback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringSubstitutor;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.services.portfolio.utils.Cache;
import com.redhat.services.portfolio.utils.Json;

public class Config extends com.redhat.cop.giveback.legacy.Config{
  private static final Logger log=LoggerFactory.getLogger(Config.class);
  public boolean verboseDebug=false;
  private static Cache<String,String> propertyCache=new Cache.Builder<String,String>().name("Config.app-properties").onDisk(false).timeout("30s").build();
  private static File STORAGE;
  private static Config _instance;
  
  public Config(){
    STORAGE=new File(getStorageRoot(), "config.json");
  }
  public static Config get(){
    _instance=null; // !!!!?? TODO: Huh? this means it'll always reload, was this a dev change that got accidentally committed?
    if (null==_instance) _instance=new Config().load();
    return _instance;
  }
  public void save(){
    try {
      String data=Json.toJson(this);
      STORAGE.getParentFile().mkdirs();
      log.debug("saving to "+STORAGE.getAbsolutePath()+":\n"+data);
      IOUtils.write(data, new FileOutputStream(STORAGE), "UTF-8");
      _instance=null; // clear cached config so when it's saved it gets reloaded next
    }catch(IOException e) {
      e.printStackTrace();
    }
  }
  private Config load(){
    try{
      return Json.toObject(new FileInputStream(STORAGE), Config.class);
    }catch (IOException e){
      log.error(String.format("Unable to load from file %s", STORAGE.getAbsolutePath()));
    }
    try{
      Config config=Json.toObject(Config.class.getClassLoader().getResourceAsStream("config.json"), Config.class); // load defaults
      config.save();
      return config;
    }catch (IOException e){
      log.error(String.format("Unable to read default originally from src/main/resources/config.json - it's likely there is a json formatting error. Error message = %s",e.getMessage()));
    }
    return null;
  }
  
  private String storageRoot;
  public String getStorageRoot(){
    try{
      storageRoot=ConfigProvider.getConfig().getValue("storage.root", String.class);
    }catch(java.util.NoSuchElementException e){ // because the SmallRyeConfig impl is unable to get properties from application.properties in eclipse
      storageRoot=Paths.get("target").toFile().exists()?"target/ninja-persistence":"persistence";
      log.debug(String.format("storage.root not found in application.properties, exception thrown so going with '%s'", storageRoot));
    }
    return storageRoot;
  }
  
  public String getPropertySilent(String... nameDefault){ // using varargs as optional param logic for a default value
//    boolean save=verboseDebug;
//    verboseDebug=false;
    String result=getProperty(nameDefault);
//    verboseDebug=save;
    return result;
  }
  
  public String getProperty(String... nameDefault){
    if (propertyCache.hasExpired(nameDefault[0])){
      String value=new StringSubstitutor(System.getenv()).replace(getUncachedProperty(nameDefault));
      if (Objects.nonNull(value)) propertyCache.put(nameDefault[0], value);
    }
    return propertyCache.get(nameDefault[0]);
  }
  
  // given 4 params... it'll try the 1st, 2nd and 3rd, but if non are found, then it'll return the 4th as a the default
  public String getUncachedProperty(String... nameDefault){
    if (nameDefault.length==1) return getUncachedPropertyInternal(nameDefault);
    for (int i=0;i<nameDefault.length-1;i++){
      String v=getUncachedPropertyInternal(nameDefault[i], null);
//      System.out.println("looking up ["+nameDefault[i]+"], found ["+v+"]");
      if (null!=v) return v;
    }
    return nameDefault[nameDefault.length-1];
  }
  
  private String getUncachedPropertyInternal(String... nameDefault){ // using varargs as optional param logic for a default value
    String name=nameDefault[0];
    
    String d3fault=nameDefault.length>1?nameDefault[1]:null;
    org.eclipse.microprofile.config.Config microprofileConfig=ConfigProvider.getConfig(); // support for quarkus application.properties file
//    System.out.println("Looking for property '"+name+"', or env property '"+name.replaceAll("\\.", "_")+"'");
    if (null!=System.getProperty(name)){
      if (verboseDebug) log.trace(String.format("Found system property '%s', value = '%s'", name, System.getProperty(name)));
      return System.getProperty(name);
    }else if (null!=System.getenv(name)){
      if (verboseDebug) log.trace(String.format("Found env property '%s', value = '%s'", name, System.getenv(name)));
      return System.getenv(name);
    }else if (options.containsKey(name)){
      if (verboseDebug) log.trace(String.format("Found options property '%s', value = '%s'", name, options.get(name)));
      return options.get(name);
    }else if (null!=System.getenv(name.replaceAll("\\.", "_"))){
      if (verboseDebug) log.trace(String.format("Found modified env property '%s', value = '%s'", name.replace(".", "_"), System.getenv(name.replace(".", "_"))));
      return System.getenv(name.replaceAll("\\.", "_"));
//    }else if (appPropertiesHas(name)){ // suspected bug in quarkus that doesnt read application.properties when running in Eclipse IDE, making it hard to code!
//      return getPropertyFromAppPropertiesWhenDeveloping(name);
    }else if (microprofileConfig.getOptionalValue(name, String.class).isPresent()){
      /* note/warning: this microprofile class does some weird stuff, such as if <param>name</param> is "test.one", and there is an ENV variable "TEST_ONE", it WILL read it when you dont want it to!? hence reading the app.properties ourselves on the lines above */
      return microprofileConfig.getValue(name, String.class);
    }else{
      log.debug("Unable to find '"+name+"' in config, system properties or environment variables, returning default '"+d3fault+"'");
      return d3fault;
    }
  }
  
//  /**
//   * Code written because Quarkus wont read application.properties when running in an IDE for some reason - likely a bug.. this is a workaround
//   */
//  
//  @JsonIgnore private Map<String,String> appProperties=Maps.newHashMap();
//  private boolean appPropertiesHas(String key){
//    return getPropertyFromAppPropertiesWhenDeveloping(key)!=null;
//  }
//  private String getPropertyFromAppPropertiesWhenDeveloping(String key){
//    try{
//      if (appProperties.isEmpty()) {
//        InputStream in=this.getClass().getClassLoader().getResourceAsStream("application.properties");
//        Properties p=new Properties();
//        p.load(in);
//        for(Object k:p.keySet()) appProperties.put((String)k, (String)p.get(k));
//      }
//      return appProperties.get(key);
//    }catch(IOException e){
//      e.printStackTrace();
//      return null;
//    }
//  }
}
