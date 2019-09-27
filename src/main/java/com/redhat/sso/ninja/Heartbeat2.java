package com.redhat.sso.ninja;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.attribute.PosixFilePermission;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mortbay.log.Log;

import com.google.common.collect.Lists;
import com.redhat.sso.ninja.user.UserService;
import com.redhat.sso.ninja.user.UserService.User;
import com.redhat.sso.ninja.utils.DownloadFile;
import com.redhat.sso.ninja.utils.FluentCalendar;
import com.redhat.sso.ninja.utils.Http;
import com.redhat.sso.ninja.utils.LevelsUtil;
import com.redhat.sso.ninja.utils.ParamParser;
import com.redhat.sso.ninja.utils.RegExHelper;
import com.redhat.sso.ninja.utils.Tuple;

public class Heartbeat2 {
  private static final Logger log = LogManager.getLogger(Heartbeat2.class);
  private static Timer t;
  private static Timer tRunOnce;
//  private static final long delay=30000l;

  public static void main(String[] asd){
    try{
    	Heartbeat2.runOnceAsync();
//      new HeartbeatRunnable().levelUpChecks(Database2.get());
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
        Date runTo2=java.sql.Date.valueOf(LocalDate.now());
        Integer daysFromLastRun=(int)((runTo2.getTime() - lastRunDate.getTime()) / (1000 * 60 * 60 * 24))+1;
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
    new HeartbeatRunnable(null).run();
  }
  
  public static void runOnceAsync(){
  	tRunOnce = new Timer("cop-ninja-heartbeat", false);
  	tRunOnce.schedule(new HeartbeatRunnable(tRunOnce), 0);
  }
  
  public static void start(Config config) {
  	boolean heartbeatDisabled="true".equalsIgnoreCase((String)config.getOptions().get("heartbeat.disabled"));
    
  	if (!heartbeatDisabled){
	  	String intervalString=(String)config.getOptions().get("heartbeat.intervalInSeconds");
	    String startTime=(String)config.getOptions().get("heartbeat.startTime");
	    
	    if (null==intervalString) intervalString="60000";
	    int interval=Integer.parseInt(intervalString);
	    
	    if (null==startTime) startTime="21:00"; // default to 9PM
	    
	    long msToStartTime=30000l;
	    try{
	    	msToStartTime=new Heartbeat2().getMillisToNextTime(startTime);
	    }catch(ParseException e){
	    }
	    SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
	    String dateTimeToStart=sdf.format(System.currentTimeMillis()+msToStartTime);
	    
	    System.out.println("Heartbeat (Start or ReStart):");
	    System.out.println("  Status:  "+(heartbeatDisabled?"Disabled":"Enabled"));
	    System.out.println("  StartTime: "+dateTimeToStart +" (in "+msToSensibleString(msToStartTime)+" time)");
	    System.out.println("  Interval:  "+interval +"ms ("+msToSensibleString(interval)+")");
	    
	    Log.info("InitServlet fired - initializing database...");
	    Database2.get();
    
    	Heartbeat2.start(msToStartTime, interval);
  	}
    
  }
  private static void start(long msToStartTime, long intervalInMs) {
    t = new Timer("cop-ninja-heartbeat", false);
    t.scheduleAtFixedRate(new HeartbeatRunnable(null), msToStartTime, intervalInMs);
  }
  private long getMillisToNextTime(String time) throws ParseException{
  	SimpleDateFormat sdf=new SimpleDateFormat("HH:mm");
  	Calendar start=newCalendar(System.currentTimeMillis());
  	Calendar cTime=newCalendar(sdf.parse(time).getTime());
  	start.set(Calendar.HOUR_OF_DAY, cTime.get(Calendar.HOUR_OF_DAY));
  	start.set(Calendar.MINUTE, cTime.get(Calendar.MINUTE));
  	start.set(Calendar.SECOND, cTime.get(Calendar.SECOND));
  	
  	Calendar now=newCalendar(System.currentTimeMillis());
  	
  	int nowHour=now.get(Calendar.HOUR_OF_DAY);
  	int startHour=start.get(Calendar.HOUR_OF_DAY);
  	if (nowHour>=startHour){
  		// too late today, move to tomorrow
  		start.set(Calendar.DAY_OF_MONTH, start.get(Calendar.DAY_OF_MONTH)+1);
  	}
  	return start.getTimeInMillis()-now.getTimeInMillis();
  }
  private static Calendar newCalendar(long millis){
  	Calendar c=Calendar.getInstance();
  	c.setTime(new Date(millis));
  	return c;
  }
  static int[] s=new int[]{1000,60,60,24,365};
  static String[] d=new String[]{"ms","s","m","h","d"};
  public static String msToSensibleString(long ms){
  	double result=ms;
  	String denomination=d[0];
  	for(int i=0;i<=s.length;i++){
  		if (result>=s[i]){
  			result=result/(double)s[i];
  			denomination=d[i+1];
  		}else
  			break;
  	}
  	return new DecimalFormat("##.###").format(result)+denomination;
  }
  

  public static void stop() {
    t.cancel();
  }
  

  static class HeartbeatRunnable extends TimerTask {
    static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private Timer t;
    
    public HeartbeatRunnable(Timer t){
    	this.t=t;
    }
    
    public Map<String, String> getUsersByPool(Database2 db, String key){
//      System.out.println("getting pool by id: "+key);
      Map<String, String> result=new HashMap<String, String>();
      for(Entry<String, Map<String, String>> e:db.getUsers().entrySet()){
        // support a default of your key id when no pool id is found
        if (e.getValue().containsKey(key)){
          result.put(e.getValue().get(key), e.getKey());
        }else{
          result.put(e.getKey(), e.getKey()); // default to user id key
        }
      }
      return result;
    }
    
    private String cleanupGithubTrelloId(String input){
      String result=input;
      
      // if the id start with @, then strip it
      if (result.startsWith("@")) result=result.substring(1, result.length());
      
      // if the id still contains an @ then assume it's an email and strip the latter part
      result=result.substring(0, (result.contains("@")?result.indexOf("@"):result.length()) );
      
      if (!"".equalsIgnoreCase(result) && !"na".equalsIgnoreCase(result) && !"n/a".equalsIgnoreCase(result))
        return result;
      return null;
    }

    public boolean addNewlyRegisteredUsers(Database2 db, Config cfg){
      Map<String, Map<String, String>> dbUsers=db.getUsers();
      UserService userService=new UserService();
      boolean userServiceDown=false;
      try{
        GoogleDrive2 drive=new GoogleDrive2();
        File file=drive.downloadFile(cfg.getOptions().get("googlesheets.registration.id"));
        List<Map<String, String>> rows=drive.parseExcelDocument(file);
        for(Map<String,String> r:rows){
          Map<String, String> userInfo=new HashMap<String, String>();
          for(Entry<String, String> c:r.entrySet()){
            if (c.getKey().toLowerCase().contains("timestamp")){
            }else if (c.getKey().toLowerCase().contains("email")){
              if (c.getValue().contains("@"))
                userInfo.put("username", c.getValue().substring(0, c.getValue().indexOf("@")));
              userInfo.put("email", c.getValue());
            }else if (c.getKey().toLowerCase().contains("trello id")){ // the 'contains' is the text in the google sheet title
            	String trelloId=cleanupGithubTrelloId(c.getValue());
            	if (null!=trelloId) userInfo.put("trelloId", trelloId);
            	
            }else if (c.getKey().toLowerCase().contains("github id")){ // the 'contains' is the text in the google sheet title
            	String githubId=cleanupGithubTrelloId(c.getValue());
            	if (null!=githubId) userInfo.put("githubId", githubId);
              
            }
          }
          
          // populate all the fields we can from LDAP IF THE USER DOESN'T ALREADY EXIST IN THE DATABASE
          if (null!=userInfo.get("username") && !dbUsers.containsKey(userInfo.get("username"))){
            
            // attempt to set the display name if we can get access to RH ldap
          	String ldapEnabled=Config.get().getOptions().get("ldap.enabled");
            userServiceDown=userServiceDown || !"true".equalsIgnoreCase(ldapEnabled); //temporarily set whilst we have no access to LDAP
            try{
              if (!userServiceDown){
                log.debug("UserService(LDAP) is UP, populating the 'displayName'");
                List<User> users=userService.search("uid", userInfo.get("username"));
                if (users.size()>0){
                  userInfo.put("displayName", users.get(0).getName());
                  userInfo.put("geo", users.get(0).getRhatGeo());
                }
              }else{
//                log.debug("UserService(LDAP) is DOWN, skipping populating the 'displayName'");
              }
            }catch(Exception e){
              log.debug("Exception cause flag to say userService is DOWN:");
              e.printStackTrace();
              userServiceDown=true;
            }
            
            userInfo.put("level", LevelsUtil.get().getBaseLevel().getRight());
            userInfo.put("levelChanged", new SimpleDateFormat("yyyy-MM-dd").format(new Date())); // date of registration
            log.info("New User Registered: "+userInfo.get("username") +" ["+userInfo+"]");
            dbUsers.put(userInfo.get("username"), userInfo);
            
            // add the user a zero scorecard
            db.getScoreCards().put(userInfo.get("username"), new HashMap<String, Integer>());
            
            db.addEvent("New User Registered", userInfo.get("username"), "");
            
            // Notify everyone on the Ninja chat group of a new registree
            String displayName=userInfo.containsKey("displayName")?userInfo.get("displayName"):userInfo.get("username");
            new ChatNotification().send("New User Registered: <https://mojo.redhat.com/people/"+userInfo.get("username")+"|"+displayName+">");
//            }// /newUser
            
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
    
    enum FieldMapping{
    	displayName("name"),
    	geo("geo");
    	public String value;
    	private FieldMapping(String k){ this.value=k; }
    }
    private boolean updateUsersDetailsUsingLDAPInfo(Database2 db){
    	UserService userService=new UserService();
    	for(Entry<String, Map<String, String>> e:db.getUsers().entrySet()){
    		
    		String username=e.getKey();
    		
    		Set<String> missingFields=new HashSet<String>(Lists.newArrayList("displayName","geo"));
    		missingFields.removeAll(new HashSet<String>(e.getValue().keySet()));
    		if (missingFields.size()>0){
    			try{
    				List<User> users=userService.search("uid", username);
    				if (users.size()>0){
    					for(String field:missingFields){
    						FieldMapping fm=FieldMapping.valueOf(field);
    						log.info("Updating "+fm.name()+" for '"+username+"' to '"+users.get(0).asMap().get(fm.value)+"'");
    						e.getValue().put(fm.name(), users.get(0).asMap().get(fm.value));
    					}
    					
    				}
    				
    			}catch(Exception ex){
            log.error("Exception caused aborting to update any user info - is ldap accessible?:", ex);
            break;
//            ex.printStackTrace();
    			}
    		}
    	}
    	return true;
    	
    }
    
    @Override
    public void run() {
      log.info("Heartbeat fired");
      
      final Config config=Config.get();
      final Database2 db=Database2.get();
      
      boolean successfullyAccessedRegistrationSheet=addNewlyRegisteredUsers(db, config);
      if (!successfullyAccessedRegistrationSheet) return;
      updateUsersDetailsUsingLDAPInfo(db);
      
      //db.save(); // after the new user registration calls have been made
      
      boolean scriptFailure=false;
      
      Integer daysFromLastRun=30; //default to 30 days
      Date runToDate=java.sql.Date.valueOf(LocalDate.now());
      Date lastRun2=null;
      
      if (((String)config.getValues().get("lastRun2")).startsWith("-")){
        String lastRun=(String)config.getValues().get("lastRun2");
        Matcher m=Pattern.compile("(\\+|\\-)(\\d+)(\\w+)").matcher(lastRun);
        if (m.find()){
          Long numberOfDays=Long.parseLong(m.group(2));
//          TimeUnit unit=TimeUnit.valueOf(units.toUpperCase());
          ChronoUnit unit=ChronoUnit.valueOf(m.group(3).toUpperCase());
          lastRun2=java.sql.Date.valueOf(LocalDate.now().minus(numberOfDays, unit));
        }
        
      }else{ //assume its a date
        try{
          lastRun2=sdf.parse((String)config.getValues().get("lastRun2"));
        }catch (ParseException e){
          e.printStackTrace();
        }
      }
      if (null!=lastRun2){
        daysFromLastRun=(int)((runToDate.getTime() - lastRun2.getTime()) / TimeUnit.DAYS.toMillis(1));
      }
      
      
      File scripts=new File("target/scripts");
      if (!scripts.exists()) scripts.mkdirs();
      
      for(Map<String,Object> script:config.getScripts()){
        final Map<String, String> poolToUserIdMapper=getUsersByPool(db, ((String)script.get("name")).split("\\.")[0].toLowerCase()+"Id");
        long start=System.currentTimeMillis();
        
        
        if (Arrays.asList(new String[]{"java","class","javaclass"}).contains(((String)script.get("type")).toLowerCase())){
//          log.info("Executing script: "+script.get("source"));
          try{
            
            ScriptBase obj=(ScriptBase)Class.forName((String)script.get("source")).newInstance();
            obj.execute((String)script.get("name"), (Map<String,String>)script.get("options"), daysFromLastRun, new PointsAdder(){
              public void addPoints(String user, String pool, Integer increment, Map<String, String> params){
                if (user!=null && pool!=null){
                  try{
                    String userId=poolToUserIdMapper.get(user);
                    if (null!=userId){
                      log.debug("addPoints:: Incrementing Points:: ["+pool+"/"+userId+"] = "+increment);
                      db.increment(pool, userId, increment, params);
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
               || "script".equalsIgnoreCase((String)script.get("type"))
               || "perl".equalsIgnoreCase((String)script.get("type"))){
          try{
            
            String command=(String)script.get("source");
            String version=RegExHelper.extract(command, "/(v.+)/");
            String name=(String)script.get("name");
            File scriptFolder=new File(scripts, name+"/"+version);
            scriptFolder.mkdirs(); // ensure the parent folders exist if they dont already
            
//            URL remoteLocationWithoutParams=new URL(command.contains(" ")?command.substring(0, command.indexOf(" ")):command); // strip script execution params to allow it to be downloaded
//            File localDestination=new File(scriptFolder);//, new File(remoteLocationWithoutParams.getPath()).getName());
            String originalCommand=command.toString();
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
            	Date lastRun=FluentCalendar.get(lastRun2).add(Calendar.DAY_OF_MONTH, -1).build().getTime();
            	command=convertLastRun(command, lastRun);
            }
            
            
            log.info("Script downloaded ("+version+"): "+originalCommand);
            log.info("Script executing: "+command);
            
            Process script_exec=Runtime.getRuntime().exec(command);
            script_exec.waitFor();
            if(script_exec.exitValue() != 0){
              
              BufferedReader stdInput=new BufferedReader(new InputStreamReader(script_exec.getInputStream()));
              StringBuffer sb=new StringBuffer();
              String s;
              while ((s=stdInput.readLine()) != null) sb.append(s).append("\n");
              log.error("Error while executing script (stdout): "+sb.toString());
              
              BufferedReader stdErr=new BufferedReader(new InputStreamReader(script_exec.getErrorStream()));
              sb.setLength(0);
              while ((s=stdErr.readLine()) != null) sb.append(s).append("\n");
              log.error("Error while executing script (stderr): "+sb.toString());
              
              db.addEvent("Script Execution FAILED", "", command+"\nERROR (stderr):\n"+sb.toString());
              
              scriptFailure=true;
              
            }else{
            	allocatePoints(db, script_exec.getInputStream(), script, scriptFolder, poolToUserIdMapper);
            	db.addEvent("Script Execution Succeeded", "", command +" (took "+(System.currentTimeMillis()-start)+"ms)");
            	
            }
            
          }catch (IOException e){
            e.printStackTrace();
          }catch (InterruptedException e){
            e.printStackTrace();
          }catch (ParseException e){
            e.printStackTrace();
          }
        }
        
        log.info("Script ("+(String)script.get("name")+") execution took "+(System.currentTimeMillis()-start)+"ms");
        db.save(); //save after each script execution
        
      } // end of scripts loop
      
      
//      // do any users need levelling up?
      levelUpChecks(db);
      db.save();
      
      if (!((String)config.getValues().get("lastRun2")).startsWith("-")){
        
        if (scriptFailure==false){
          log.debug("Updating the \"lastRun\" date");
          config.getValues().put("lastRun2", sdf.format(runToDate));
          
          // notify the graphs proxy service if configured
          // store summary, breakdown & nextLevel for each user
          String graphsProxyUrl=config.getOptions().get("graphs-proxy");
          if (null==graphsProxyUrl) graphsProxyUrl=System.getenv("GRAPHS_PROXY");
          
          if (!StringUtils.isEmpty(graphsProxyUrl)){
            String url=graphsProxyUrl+"/api/proxy";
            log.warn("graphsProxyUrl is null == "+(null==graphsProxyUrl));
            log.warn("roxy configured at: "+url);
            ChartsController cc=new ChartsController();
            ManagementController mc=new ManagementController();
            for(String user:db.getUsers().keySet()){
              try{
                if (200!=Http.post(url+"/nextLevel_"+user, (String)cc.getUserNextLevel(user).getEntity()).responseCode)
                  log.error("Error pushing 'nextLevel' info for '"+user+"' to roxy");
                if (200!=Http.post(url+"/summary_"+user, (String)mc.getScorecardSummary(user).getEntity()).responseCode)
                  log.error("Error pushing 'summary' info for '"+user+"' to roxy");
                if (200!=Http.post(url+"/breakdown_"+user, (String)mc.getUserBreakdown(user).getEntity()).responseCode)
                  log.error("Error pushing 'breakdown' info for '"+user+"' to roxy");
              }catch (IOException e){
                e.printStackTrace();
              }
              
            }
            
            // add the top 10 to the graphs too so they're available externally
            try{
              if (200!=Http.post(url+"/leaderboard_10", (String)cc.getLeaderboard2(10).getEntity()).responseCode)
                log.error("Error pushing 'leaderboard' info to proxy");
            }catch (IOException e){
              e.printStackTrace();
            }
            
            // add the Ninjas to the graphs too so they're available externally
            try{
              if (200!=Http.post(url+"/ninjas", (String)cc.getNinjas().getEntity()).responseCode)
                log.error("Error pushing 'ninjas' info to proxy");
            }catch (IOException e){
              e.printStackTrace();
            }
            
            
          }else{
            log.warn("not pushing to graphs proxy - url was: "+graphsProxyUrl);
          }
          
        }else{
          log.info("NOT Updating the \"lastRun\" date due to a script failure. It will re-run the same period next time and dupe prevention will keep the data correct");
        }
      }else{
        log.debug("NOT Updating the \"lastRun\" date because it's a rolling date");
      }
      
      config.save();
      
      if (t!=null) t.cancel();
    }
    
    public void levelUpChecks(Database2 db){
    	log.info("Level-up checks...");
      int count=1;
      Map<String, Map<String, String>> users=db.getUsers();
      while (count>0){ // keep checking, some people may need multiple promotions in one go!
        count=0;
        for(Entry<String, Map<String, Integer>> e:db.getScoreCards().entrySet()){
          String userId=e.getKey();
          int total=0;
          for(Entry<String, Integer> s:e.getValue().entrySet()){
            total+=s.getValue();
          }
          Map<String, String> userInfo=users.get(userId);
          
          Tuple<Integer, String> currentLevel=LevelsUtil.get().getLevel(userInfo.get("level"));
          Tuple<Integer, String> nextLevel=LevelsUtil.get().getNextLevel(userInfo.get("level"));
          if (total>=nextLevel.getLeft() && !currentLevel.getRight().equals(nextLevel.getRight())){
            // congrats! the user has been promoted!
            log.info("User "+userId+" has been promoted to level "+nextLevel.getRight()+" with a points score of "+total);
            userInfo.put("level", nextLevel.getRight());
            userInfo.put("levelChanged", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));// nextLevel.getRight());
            
            db.addEvent("User Promotion", userInfo.get("username"), "Promoted to "+nextLevel.getRight()+" level");
            
            String displayName=userInfo.containsKey("displayName")?userInfo.get("displayName"):userInfo.get("username");
            String title=displayName+" promoted to "+nextLevel.getRight()+" belt";
            db.addTask(title, userInfo.get("username"));
            
            // Notify everyone on the Ninja chat group of a new belt promotion
            new ChatNotification().send(title);
            
            count+=1;
          }
          
        }
      }
      db.save();
    }
    
    
    public void allocatePoints(Database2 db, InputStream is, Map<String,Object> script, File scriptFolder, Map<String, String> poolToUserIdMapper) throws NumberFormatException, UnsupportedEncodingException, IOException{
    	BufferedReader stdInput=new BufferedReader(new InputStreamReader(is));
    	Pattern paramsPattern=Pattern.compile(".*(\\[.*\\]).*");
    			
    	StringBuffer scriptLog=new StringBuffer();
    	String s;
    	while ((s=stdInput.readLine()) != null){
    		s=s.trim();
    		scriptLog.append(s).append("\n");
    		log.debug(s);
    		
    		Map<String, String> params=new HashMap<String, String>();
    		// check for params here, extract them if present for use later on
    		if (s.matches(".* \\[.*\\]")){
    			Matcher m=paramsPattern.matcher(s);
    			if (m.find()){
    				String paramsExtract=m.group(1);
    				params.putAll(new ParamParser().splitParams(paramsExtract.replaceAll("\\[", "").replaceAll("\\]", "").trim()));
    				s=s.replaceAll("\\[.*\\]", "").trim();
    			}
    		}
    		
    		if (s.contains("/")){ // ignore the line if it doesn't contain a slash
    			String[] split=s.split("/");
    			
    			// take the last section of the script name as the pool id. so "trello" stays as "trello", but "trello.thoughtleadership" becomes "thoughtleadership" where the "trello" part is the source type/context
    			String pool=(String)script.get("name");
    			String[] splitPool=pool.split("\\.");
    			pool=splitPool[splitPool.length-1];
    			
    			String actionId;
    			String poolUserId;
    			Integer inc;
    			
    			if (split.length==4){   //pool.sub
    				pool=pool+"."+split[0];
    				actionId=split[1];
    				poolUserId=split[2];
  					inc=Integer.valueOf(split[3]);
  					
  					params.put("id", actionId);
    				
    				if (!db.getPointsDuplicateChecker().contains(actionId+"."+poolUserId)){
    					db.getPointsDuplicateChecker().add(actionId+"."+poolUserId);
    					
    					String userId=poolToUserIdMapper.get(poolUserId);
    					
    					if (null!=userId){
    						//                    System.out.println(poolUserId+" mapped to "+userId);
//    						log.info("Incrementing registered user "+poolUserId+" by "+inc);
    						db.increment(pool, userId, inc, params);//.save();
    					}else{
    						log.info("Unable to find '"+poolUserId+"' "+script.get("name")+" user - not registered? "+Database2.buildLink(params));
    						db.addEvent("Lost Points", poolUserId +"("+script.get("name")+")", script.get("name")+" user '"+poolUserId+"' was not found - not registered? "+Database2.buildLink(params));
    					}
    				}else{
    					// it's a duplicate increment for that actionId & user, so ignore it
    					log.warn(actionId+"."+poolUserId+" is a duplicate");
    				}
    				
    			}else{
    				// dont increment because we dont know the structure of the script data
    			}
    			
    			
    		}
    	}
    	
    	scriptFolder.mkdirs();
    	IOUtils.write(scriptLog.toString(), new FileOutputStream(new File(scriptFolder, "last.log")));
    	
    	
    }
    
  }
  
  
  

}
