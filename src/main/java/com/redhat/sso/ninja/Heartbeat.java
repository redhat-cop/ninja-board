package com.redhat.sso.ninja;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class Heartbeat {
  private static final Logger log = Logger.getLogger(Heartbeat.class);
  private static Timer t;

  public static void main(String[] asd){
    try{
      
      Calendar lastRunC=Calendar.getInstance();
      lastRunC.setTime(new Date());
      lastRunC.set(Calendar.DAY_OF_MONTH, 1);
      lastRunC.set(Calendar.HOUR, 0);
      lastRunC.set(Calendar.MINUTE, 0);
      lastRunC.set(Calendar.SECOND, 1);
      
      System.out.println("LAST RUN: "+new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(lastRunC.getTime()));
      System.out.println(Heartbeat.convertLastRun("perl ${user.home}/Work/poc/sso-tools/cop-ninja/github-stats.py -s ${LAST_RUN:yyyy-MM-dd}", lastRunC.getTime()));
      System.out.println(Heartbeat.convertLastRun("sh ${user.home}/Work/poc/sso-tools/cop-ninja/trello.sh -s ${DAYS_FROM_LAST_RUN}", lastRunC.getTime()));
      
      lastRunC.set(Calendar.DAY_OF_MONTH, 21);
      System.out.println("TODAY?: "+new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(lastRunC.getTime()));
      
      
//        Heartbeat.start(60000l);
//        Thread.sleep(300000l);
    }catch(Exception e){
      e.printStackTrace();
    }
  }
  
  public static String convertLastRun(String command, Date lastRunDate) throws ParseException {
    Matcher m=Pattern.compile("(\\$\\{([^}]+)\\})").matcher(command);
    StringBuffer sb=new StringBuffer();
    while (m.find()){
      String toReplace=m.group(2);
      if (toReplace.contains("LAST_RUN:")){
        SimpleDateFormat sdf=new SimpleDateFormat(toReplace.split(":")[1].replaceAll("}", "")); // nasty replaceall when I just want to trim the last char
        m.appendReplacement(sb, sdf.format(lastRunDate));
        
      }else if (toReplace.contains("DAYS_FROM_LAST_RUN")){
        Calendar runTo=Calendar.getInstance();
        runTo.setTime(new Date());
        runTo.set(Calendar.HOUR, 0);
        runTo.set(Calendar.MINUTE, 0);
        runTo.set(Calendar.SECOND, 0);
        Integer daysFromLastRun=(int)((runTo.getTime().getTime() - lastRunDate.getTime()) / (1000 * 60 * 60 * 24))+1;
        m.appendReplacement(sb, String.valueOf(daysFromLastRun));
      }else{
        // is it a system property?
        if (null!=System.getProperty(toReplace)){
          m.appendReplacement(sb, System.getProperty(toReplace));
        }else{
          m.appendReplacement(sb, "?????");
        }
      }
    }
    m.appendTail(sb);
    return sb.toString();
  }
  
  public static void runOnce(){
    new HeartbeatRunnable().run();
  }
  
  public static void start(long intervalInMs) {
    t = new Timer("cop-ninja-heartbeat", false);
    t.scheduleAtFixedRate(new HeartbeatRunnable(), 30000l, intervalInMs);
  }

  public static void stop() {}
  

  static class HeartbeatRunnable extends TimerTask {
    static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    
    public Map<String, String> getUsersByPool(Database2 db, String key){
      System.out.println("getting pool by id: "+key);
      Map<String, String> result=new HashMap<String, String>();
      for(Entry<String, Map<String, String>> e:db.getUsers().entrySet()){
        result.put(e.getValue().get(key), e.getKey());
      }
      return result;
    }

    @Override
    public void run() {
      log.info("Heartbeat fired");
      
      Config config=Config.get();
      Integer daysFromLastRun=30; //default to 30 days
      
      Date now=new Date();
      Calendar c=Calendar.getInstance();
      c.setTime(now);
      c.set(Calendar.HOUR, 0);
      c.set(Calendar.MINUTE, 0);
      c.set(Calendar.SECOND, 0);
      Date runToDate=c.getTime();
      Date lastRun2=null;
      try{
        lastRun2=sdf.parse((String)config.getValues().get("lastRun2"));
      }catch (ParseException e){
        e.printStackTrace();
      }
      if (null!=lastRun2){
        daysFromLastRun=(int)((runToDate.getTime() - lastRun2.getTime()) / (1000 * 60 * 60 * 24));
      }
      
      final Database2 db=Database2.get();
      
      for(Map<String,Object> script:config.getScripts()){
        final Map<String, String> poolToUserIdMapper=getUsersByPool(db, ((String)script.get("name")).split("\\.")[0]+"Id");
        if (Arrays.asList(new String[]{"java","class","javaclass"}).contains(((String)script.get("type")).toLowerCase())){
          log.debug("Executing script: "+script.get("source"));
          try{
            
            ScriptBase obj=(ScriptBase)Class.forName((String)script.get("source")).newInstance();
            obj.execute((String)script.get("name"), (Map<String,String>)script.get("options"), daysFromLastRun, new PointsAdder(){
              public void addPoints(String user, String pool, Integer increment){
                if (user!=null && pool!=null){
                  try{
                    String userId=poolToUserIdMapper.get(user);
                    if (null!=userId){
                      log.debug("addPoints:: Incrementing Points:: ["+pool+"/"+userId+"] = "+increment);
                      db.increment(pool, userId, increment);
                    }//else //its most likely an unregistered user
                      
                  }catch(Exception e){
                    e.printStackTrace();
                  }
                }
              }
            });
          }catch(Exception sinkSinceTheresNothingWeCanDo){
            sinkSinceTheresNothingWeCanDo.printStackTrace();
          }
        }else if ("sh".equalsIgnoreCase((String)script.get("type"))
               || "bash".equalsIgnoreCase((String)script.get("type"))
               || "perl".equalsIgnoreCase((String)script.get("type"))){
          try{
            
            String command=(String)script.get("source");
//            ${LAST_RUN:yyyy-MM-dd}
            if (command.contains("${LAST_RUN") || command.contains("${DAYS_FROM_LAST_RUN")){
              command=convertLastRun(command, lastRun2);
            }
            
            log.debug("Executing script: "+command);
            
            Process script_exec=Runtime.getRuntime().exec(command);
            script_exec.waitFor();
            if(script_exec.exitValue() != 0){
              log.error("Error while executing script");
            }else{
              BufferedReader stdInput=new BufferedReader(new InputStreamReader(script_exec.getInputStream()));
              
              String s;
              while ((s=stdInput.readLine()) != null){
                System.out.println(s.trim());
                if (s.contains("/")){ // ignore the line if it doesn't contain a slash
                  String[] split=s.split("/");
                  String pool=(String)script.get("name");
                  String poolUserId;
                  Integer inc;
                  if (split.length>=2){
                    pool=pool+"."+split[0];
                    poolUserId=split[1];
                    inc=Integer.valueOf(split[2]);
                  }else{
                    poolUserId=split[0];
                    inc=Integer.valueOf(split[1]);
                  }
                  
//                  System.out.println("looking up user ["+poolUserId+"]");
                  
                  String userId=poolToUserIdMapper.get(poolUserId);
                  
                  if (null!=userId){
//                    System.out.println(poolUserId+" mapped to "+userId);
                  }else{
                    log.debug(poolUserId+" did NOT map to any registered user");
                  }
                  
                  db.increment(pool, userId, inc).save();
                }
              }
            }
            
          }catch (IOException e){
            e.printStackTrace();
          }catch (InterruptedException e){
            e.printStackTrace();
          }catch (ParseException e){
            e.printStackTrace();
          }
        }
          
      }
      db.save();
      
      config.getValues().put("lastRun2", sdf.format(runToDate));
      config.save();
    }      
  }

}
