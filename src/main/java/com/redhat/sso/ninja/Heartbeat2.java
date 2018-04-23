package com.redhat.sso.ninja;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.redhat.sso.ninja.UserService.User;
import com.redhat.sso.ninja.utils.DownloadFile;
import com.redhat.sso.ninja.utils.FilePermissions;
import com.redhat.sso.ninja.utils.Tuple;

public class Heartbeat2 {
  private static final Logger log = Logger.getLogger(Heartbeat2.class);
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
      System.out.println(Heartbeat2.convertLastRun("perl ${user.home}/Work/poc/sso-tools/cop-ninja/github-stats.py -s ${LAST_RUN:yyyy-MM-dd}", lastRunC.getTime()));
      System.out.println(Heartbeat2.convertLastRun("sh ${user.home}/Work/poc/sso-tools/cop-ninja/trello.sh -s ${DAYS_FROM_LAST_RUN}", lastRunC.getTime()));
      
      lastRunC.set(Calendar.DAY_OF_MONTH, 21);
      System.out.println("TODAY?: "+new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(lastRunC.getTime()));
      
      Heartbeat2.runOnce();
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
    t.scheduleAtFixedRate(new HeartbeatRunnable(), 20000l, intervalInMs);
  }

  public static void stop() {}
  

  static class HeartbeatRunnable extends TimerTask {
    static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    
    public Map<String, String> getUsersByPool(Database2 db, String key){
//      System.out.println("getting pool by id: "+key);
      Map<String, String> result=new HashMap<String, String>();
      for(Entry<String, Map<String, String>> e:db.getUsers().entrySet()){
        result.put(e.getValue().get(key), e.getKey());
      }
      return result;
    }

    public boolean addNewlyRegisteredUsers(Map<String, Map<String, String>> dbUsers){
      UserService userService=new UserService();
      boolean userServiceDown=false;
      try{
        GoogleDrive2 drive=new GoogleDrive2();
        File file=drive.downloadFile("1E91hT_ZpySyvhnANxqZ7hcBSM2EEd9TqfQF-cavB8hQ");
        List<Map<String, String>> rows=drive.parseExcelDocument(file);
        for(Map<String,String> r:rows){
          Map<String, String> userInfo=new HashMap<String, String>();
          for(Entry<String, String> c:r.entrySet()){
            if (c.getKey().toLowerCase().contains("timestamp")){
            }else if (c.getKey().toLowerCase().contains("email")){
//              System.out.println("xxx = "+c.getValue());
              if (c.getValue().contains("@"))
                userInfo.put("username", c.getValue().substring(0, c.getValue().indexOf("@")));
              userInfo.put("email", c.getValue());
            }else if (c.getKey().toLowerCase().contains("trello id")){ // the 'contains' is the text in the google sheet title
              userInfo.put("trelloId", c.getValue());
            }else if (c.getKey().toLowerCase().contains("github id")){ // the 'contains' is the text in the google sheet title
              userInfo.put("githubId", c.getValue());
            }
          }
          
//          System.out.println("DBUsers.containsKey('"+userInfo.get("username")+"') = "+dbUsers.containsKey(userInfo.get("username")));
          
          if (null!=userInfo.get("username") && !dbUsers.containsKey(userInfo.get("username"))){
            
            // attempt to set the display name if we can get access to RH ldap
            try{
              if (!userServiceDown){
                log.debug("UserService(LDAP) is UP, populating the 'displayName'");
                List<User> users=userService.search("uid", userInfo.get("username"));
                if (users.size()>0)
                  userInfo.put("displayName", users.get(0).getName());
              }else{
//                log.debug("UserService(LDAP) is DOWN, skipping populating the 'displayName'");
              }
            }catch(Exception e){
              log.debug("Exception cause flag to say userService is DOWN:");
              e.printStackTrace();
              userServiceDown=true;
            }

            userInfo.put("level", new ManagementController().getLevelsUtil().getBaseLevel().getRight());
            userInfo.put("levelChanged", new SimpleDateFormat("yyyy-MM-dd").format(new Date())); // date of registration
            log.info("Adding Newly Registered User: "+userInfo.get("username") +" ["+userInfo+"]");
            dbUsers.put(userInfo.get("username"), userInfo);
          }else if (dbUsers.containsKey(userInfo.get("username"))){
            log.debug("User already registered: "+userInfo.get("username"));
          }
          
        }
      }catch(Exception e){
        e.printStackTrace();
        return false;
      }
      return true;
    }
    
    @Override
    public void run() {
      log.info("Heartbeat fired");
      
      final Config config=Config.get();
      final Database2 db=Database2.get();
      
      boolean successfullyAccessedRegistrationSheet=addNewlyRegisteredUsers(db.getUsers());
      if (!successfullyAccessedRegistrationSheet) return;
      
      db.save();
      
      
      
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
      
      
      
      File scripts=new File("scripts");
      if (!scripts.exists()) scripts.mkdirs();
      
      for(Map<String,Object> script:config.getScripts()){
        final Map<String, String> poolToUserIdMapper=getUsersByPool(db, ((String)script.get("name")).split("\\.")[0].toLowerCase()+"Id");
        if (Arrays.asList(new String[]{"java","class","javaclass"}).contains(((String)script.get("type")).toLowerCase())){
          log.info("Executing script: "+script.get("source"));
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
               || "python".equalsIgnoreCase((String)script.get("type"))
               || "perl".equalsIgnoreCase((String)script.get("type"))){
          try{
            
            String command=(String)script.get("source");
            String name=(String)script.get("name");
            File scriptFolder=new File(scripts, name);
            scriptFolder.mkdirs(); // ensure the parent folders exist if they dont already
            
//            URL remoteLocationWithoutParams=new URL(command.contains(" ")?command.substring(0, command.indexOf(" ")):command); // strip script execution params to allow it to be downloaded
//            File localDestination=new File(scriptFolder);//, new File(remoteLocationWithoutParams.getPath()).getName());
            
            command=new DownloadFile().get(command, scriptFolder, 
                PosixFilePermission.OWNER_READ, 
                PosixFilePermission.OWNER_WRITE, 
                PosixFilePermission.OWNER_EXECUTE,
                PosixFilePermission.GROUP_READ, 
                PosixFilePermission.GROUP_WRITE, 
                PosixFilePermission.GROUP_EXECUTE);
            
//            ${LAST_RUN:yyyy-MM-dd}
//            System.setProperty("server", "http://localhost:8082/community-ninja-board");
            
            if (command.contains("${LAST_RUN") || command.contains("${DAYS_FROM_LAST_RUN")){
              command=convertLastRun(command, lastRun2);
            }
            
            log.info("Executing script: "+command);
            
            Process script_exec=Runtime.getRuntime().exec(command);
            script_exec.waitFor();
            if(script_exec.exitValue() != 0){
              log.error("Error while executing script");
            }else{
              BufferedReader stdInput=new BufferedReader(new InputStreamReader(script_exec.getInputStream()));
              
              StringBuffer scriptLog=new StringBuffer();
              String s;
              while ((s=stdInput.readLine()) != null){
                scriptLog.append(s.trim()).append("\n");
                log.debug(s.trim());
                if (s.contains("/")){ // ignore the line if it doesn't contain a slash
                  String[] split=s.split("/");
                  String pool=(String)script.get("name");
                  String actionId;
                  String poolUserId;
                  Integer inc;
                  if (split.length==4){   //pool.sub
                    pool=pool+"."+split[0];
                    actionId=split[1];
                    poolUserId=split[2];
                    inc=Integer.valueOf(split[3]);
                    
                    if (!db.getPointsDuplicateChecker().contains(actionId+"."+poolUserId)){
                      db.getPointsDuplicateChecker().add(actionId+"."+poolUserId);
                      
                      String userId=poolToUserIdMapper.get(poolUserId);
                      
                      if (null!=userId){
  //                    System.out.println(poolUserId+" mapped to "+userId);
                        log.debug("Incrementing registered user "+poolUserId+" by "+inc);
                        db.increment(pool, userId, inc).save();
                      }else{
  //                    log.debug(poolUserId+" did NOT map to any registered user");
                      }
                    }else{
                      // it's a duplicate incremenent for that actionId & user, so ignore it
                      log.debug(actionId+"."+poolUserId+" is a duplicate");
                    }
                    
                  }else{
                    // dont increment because we dont know the structure of the script data
                  }
                  
                  
                }
              }
              IOUtils.write(scriptLog.toString(), new FileOutputStream(new File(scriptFolder, "last.log")));
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
      
      // do any users need leveling up?
      Map<String, Map<String, String>> users=db.getUsers();
      for(Entry<String, Map<String, Integer>> e:db.getScoreCards().entrySet()){
        String userId=e.getKey();
        int total=0;
        for(Entry<String, Integer> s:e.getValue().entrySet()){
          total+=s.getValue();
        }
        Map<String, String> userInfo=users.get(userId);
        
        Tuple<Integer, String> currentLevel=new ManagementController().getLevelsUtil().getLevel(userInfo.get("level"));
        Tuple<Integer, String> nextLevel=new ManagementController().getLevelsUtil().getNextLevel(userInfo.get("level"));
        if (total>=nextLevel.getLeft() && !currentLevel.getRight().equals(nextLevel.getRight())){
          // congrats! the user has been promoted!
          log.info("User "+userId+" has been promoted to level "+nextLevel.getRight()+" with a points score of "+total);
          userInfo.put("level", nextLevel.getRight());
          userInfo.put("levelChanged", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));// nextLevel.getRight());
          
        }
        
      }
      
      
      log.info("Saving database...");
      db.save();
      
      log.debug("Updating the \"lastRun\" date");
      config.getValues().put("lastRun2", sdf.format(runToDate));
      config.save();
    }      
  }

}
