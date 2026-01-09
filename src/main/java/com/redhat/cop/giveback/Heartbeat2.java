package com.redhat.cop.giveback;

import static com.redhat.services.portfolio.utils.TimeUtils.msToSensibleString;
import static java.nio.file.attribute.PosixFilePermission.GROUP_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.GROUP_READ;
import static java.nio.file.attribute.PosixFilePermission.GROUP_WRITE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.redhat.cop.giveback.controllers.ChartsController;
import com.redhat.cop.giveback.controllers.EventsController;
import com.redhat.cop.giveback.controllers.ScorecardController;
import com.redhat.cop.giveback.legacy.Config;
import com.redhat.cop.giveback.points.PointsAllocation;
import com.redhat.cop.giveback.points.ScriptRunner;
import com.redhat.cop.giveback.points.ScriptRunner.ProcessResult;
import com.redhat.services.portfolio.utils.ChatNotification_v2;
import com.redhat.services.portfolio.utils.ChatNotification_v2.ChatEvent;
import com.redhat.services.portfolio.utils.FluentCalendar;
import com.redhat.services.portfolio.utils.MapBuilder;
import com.redhat.services.portfolio.utils.RegExHelper;
import com.redhat.services.portfolio.utils.TimeUtils;
import com.redhat.sso.ninja.utils.DownloadFile;
import com.redhat.sso.ninja.utils.Http;
import com.redhat.sso.ninja.utils.LevelsUtil;
import com.redhat.sso.ninja.utils.Tuple;

public class Heartbeat2 {
  private static final Logger log = Logger.getLogger(Heartbeat2.class);
  private static Timer t;
  private static Timer tRunOnce;

  
  public static void main(String[] asd){
    try{
//      Heartbeat2.runOnceAsync();
      
      Database2 db=Database2.get(new File("target/database-test1.json"));
      String testScript="=== Statistics for GitHub Organization 'redhat-cop' ====\n" + 
          "\n" + 
          "== General PR's ==\n" + 
          "\n" + 
          "== Reviewed PR's ==\n" + 
          "\n" + 
          "Reviewed Pull Requests/GH1234567/fredbloggs/1 [org=redhat-cop, board=testing, linkId=1234]\n" + 
          "\n" + 
          "== Closed Issues ==\n" + 
          "Closed Issues/GH392748392/someoneelse/1 [org=redhat-cop, board=cert-utils-operator, linkId=35]\n"+
          "";
      Map<String,Object> script=new MapBuilder<String,Object>()
          .put("name", "Github")
//          .put("source", "https://raw.githubusercontent.com/redhat-cop/ninja-points/v1.5/trello-stats.py -s ${LAST_RUN:yyyy-MM-dd} -o redhatcop")
          .put("source", "https://raw.githubusercontent.com/redhat-cop/ninja-points/v1.16/gitlab-stats.py -s ${LAST_RUN:yyyy-MM-dd} -o customer-success -m ^(.*consulting-delivery-guides|.*consulting-engagement-reports/cer-template)$")
          .put("type", "python")
          .build();
      Config.get().getScripts().clear();
      Config.get().getScripts().add(script);
      File scriptFolder=new File("target/test");
      HeartbeatRunnable hbr=new HeartbeatRunnable(null);
      Map<String, String> poolToUserIdMapper=hbr.getUsersByPool(db, ((String)script.get("name")).split("\\.")[0].toLowerCase()+"Id");
      
      hbr.runPointsScripts(Config.get(), db);
//      hbr.allocatePoints(db, (InputStream)new ByteArrayInputStream(testScript.getBytes()), script, scriptFolder, poolToUserIdMapper);
      
//      new HeartbeatRunnable().levelUpChecks(Database2.get());
    }catch(Exception e){
      e.printStackTrace();
    }
  }
  
  // convert the last run value "2022-09-06T00:00:00" and inject it into a string that looks like this "https://raw.githubusercontent.com/redhat-cop/ninja-points/v1.16/gitlab-stats.py -s ${LAST_RUN:yyyy-MM-dd} -o customer-success"
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
  	  long interval=TimeUtils.sensibleStringToMs(config.getOptions().get("heartbeat.interval"));
//	  	String intervalString=(String)config.getOptions().get("heartbeat.intervalInSeconds");
	    String startTime=(String)config.getOptions().get("heartbeat.startTime");
	    
//	    if (null==intervalString) intervalString="60000";
//	    int interval=Integer.parseInt(intervalString);
	    
	    if (interval<=0) interval=TimeUtils.sensibleStringToMs("1d");
	    
      if (null!=startTime){
        if (startTime.contains("secondsFromNow")){
          Matcher m=Pattern.compile("secondsFromNow\\((\\d+)\\)").matcher(startTime);
          if (m.find())
            startTime=Utils.secondsFromNow(Integer.parseInt(m.group(1)));
        }
        if (startTime.matches("\\d\\d:\\d\\d")){
          // do nothing, it must be an exact time of day, ie. 21:00
        }
      }
      
//      if (null==startTime) startTime=Utils.secondsFromNow(30); // default to 30 seconds from now
      if (null==startTime) startTime="21:00"; // default to 9PM
      
	    long msToStartTime=30000l;
    	msToStartTime=TimeUtils.getMillisToNextTime(startTime);
	    SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
	    String dateTimeToStart=sdf.format(System.currentTimeMillis()+msToStartTime);
	    
	    System.out.println("Heartbeat (Start or ReStart):");
	    System.out.println("  Status:  "+(heartbeatDisabled?"Disabled":"Enabled"));
	    System.out.println("  StartTime: "+dateTimeToStart +" (in "+msToSensibleString(msToStartTime)+" time)");
	    System.out.println("  Interval:  "+interval +"ms ("+msToSensibleString(interval)+")");
	    
	    log.info("InitServlet fired - initializing database...");
	    Database2.get();
    
    	Heartbeat2.start(msToStartTime, interval);
  	}
    
  }
  private static void start(long msToStartTime, long intervalInMs) {
    t = new Timer("cop-ninja-heartbeat", false);
    t.scheduleAtFixedRate(new HeartbeatRunnable(null), msToStartTime, intervalInMs);
  }
  
  public static void stop() {
    t.cancel();
  }

  public static class HeartbeatRunnable extends TimerTask {
    private Timer t;
    
    public HeartbeatRunnable(Timer t){
    	this.t=t;
    }
    
    public Map<String, String> getUsersByPool(Database2 db, String key){
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
    
    
    @Override
    public void run() {
      log.info("Heartbeat fired");
      
      final Date now=FluentCalendar.now().build().getTime();
      final Config config=Config.get();
      final Database2 db=Database2.get();
      
      boolean successfullyAccessedRegistrationSheet=HeartbeatUserManagement.addOrUpdateRegisteredUsers(db, config);
      if (!successfullyAccessedRegistrationSheet) return;
      HeartbeatUserManagement.updateUsersDetailsUsingLDAPInfo(db);
      
    	boolean allScriptsWorked=runPointsScripts(config, db);
    	
    	levelUpChecks(db); // aka. do any users need belt promotions/leveling-up?
    	db.save();

      publishGraphsData(db, config);

    	if (allScriptsWorked){
	      log.info("Updating the \"lastRun\" date");
	      config.getValues().put("lastRun2", Initialization.sdf.format(now));
	    }else{
	      log.info("NOT Updating the \"lastRun\" date due to a script failure. It will re-run the same period next time and dupe prevention will keep the data correct");
	    }
		  config.save();
		  if (t!=null) t.cancel();
    }
    
    /**
     * 
     * @return false if ANY scripts failed to execute
     */
    private boolean runPointsScripts(Config config, Database2 db){
    	boolean result=true;
    	try{
	    	Date lastRun2=Initialization.sdf.parse((String)config.getValues().get("lastRun2"));
	    	
	      File scripts=new File("target/scripts");
	      if (!scripts.exists()) scripts.mkdirs();
	      for(Map<String,Object> script:config.getScripts()){
	        String command=(String)script.get("source");
	        String name=(String)script.get("name");
	        long start=System.currentTimeMillis();
	      	try{
		        final Map<String, String> poolToUserIdMapper=getUsersByPool(db, ((String)script.get("name")).split("\\.")[0].toLowerCase()+"Id");
		        
		        if (Lists.newArrayList("sh","bash","python","script","perl").contains(((String)script.get("type")).toLowerCase())){
		          String version=RegExHelper.extract(command, "/(v.+?)/");
		          File scriptFolder=new File(scripts, name+"/"+version);
		          scriptFolder.mkdirs(); // ensure the parent folders exist if they dont already
		
		          log.info("Script downloading: "+command.toString());
		          command=new DownloadFile().get(command, scriptFolder, OWNER_READ, OWNER_WRITE,OWNER_EXECUTE, GROUP_READ, GROUP_WRITE, GROUP_EXECUTE);
		          
		          if (command.contains("${LAST_RUN") || command.contains("${DAYS_FROM_LAST_RUN")){
		          	Date lastRun=FluentCalendar.get(lastRun2).add(Calendar.DAY_OF_MONTH, -1).build().getTime();
		          	command=convertLastRun(command, lastRun); // that convertLastRun method is awful! - Mat rewrite it!
		          }
		          
		          log.info("Script executing : "+command);
		          PointsAllocation allocate=new PointsAllocation().database(db).mapper(poolToUserIdMapper).scriptName((String)script.get("name"));
		          
		          ProcessResult process=new ScriptRunner().run(scriptFolder, command);
		          if (0==process.exitValue()){
		          	for(String line:process.lines())
		          		allocate.allocatePoints2(line);
		          	db.addEvent("Script Execution Succeeded", "", command +" (took "+(System.currentTimeMillis()-start)+"ms)");
		          	log.info("Script succeeded");
		          }else{ // error
		          	db.addEvent("Script Execution FAILED", "", command+"\nERROR (stderr):\n"+ Joiner.on("\n").join(process.lines()));
		          	ChatNotification_v2.get().send(ChatEvent.onScriptError, name+" script failure occurred. Please investigate");
//		          	new ChatNotification().send(ChatEvent.onScriptError, name+" script failure occurred. Please investigate");
		          	result=false;
		          	log.error("Script failed - (stderr):\n"+Joiner.on("\n").join(process.lines()));
		          }
		        }
		        log.info("Script ("+(String)script.get("name")+") execution took "+(System.currentTimeMillis()-start)+"ms");
		        
	      	}catch(Exception e){
	        	db.addEvent("Script Execution FAILED", "", command+"\nERROR (e.message):\n"+ e.getMessage());
	        	ChatNotification_v2.get().send(ChatEvent.onScriptError, name+" script failure occurred. Please investigate");
//	        	new ChatNotification().send(ChatEvent.onScriptError, name+" script failure occurred. Please investigate");
	        	result=false;
          	log.error("Script failed - "+e.getMessage());
	      	}
	      	db.save(); //save after each script execution, including if it failed because otherwise we lose the event log failure
	        
	      }
    	}catch(ParseException e){
    	  ChatNotification_v2.get().send(ChatEvent.onScriptError, "Catastrophic failure running all scripts - please investigate");
//    		new ChatNotification().send(ChatEvent.onScriptError, "Catastrophic failure running all scripts - please investigate");
    		result=false;
    	}
    	return result;
    }
    
    
    // Just push the user info, no graphs since the graphs
    public void publishGraphDataFor(String user){
      String graphsProxyUrl=Config.get().getOptions().get("graphs-proxy");
      if (null==graphsProxyUrl) graphsProxyUrl=System.getenv("GRAPHS_PROXY");
      
      if (!StringUtils.isEmpty(graphsProxyUrl)){
        String url=graphsProxyUrl+"/api/proxy";
        ChartsController cc=new ChartsController();
        ScorecardController mc=new ScorecardController();
        Http.loggingEnabled=false;
        try{
          if (200!=Http.post(url+"/nextLevel_"+user, (String)cc.getUserNextLevel(user).getEntity()).responseCode){
            log.error("Error pushing 'nextLevel' info for '"+user+"' to graphsProxy");
          }
          if (200!=Http.post(url+"/summary_"+user, (String)mc.getScorecardSummary(user).getEntity()).responseCode){
            log.error("Error pushing 'summary' info for '"+user+"' to graphsProxy");
          }
          if (200!=Http.post(url+"/breakdown_"+user, (String)mc.getUserBreakdown(user).getEntity()).responseCode){
            log.error("Error pushing 'breakdown' info for '"+user+"' to graphsProxy");
          }
        }catch (IOException e){
          e.printStackTrace();
        }
      }
    }
    
    public void publishGraphsData(Database2 db, Config config){
      String graphsProxyUrl=config.getOptions().get("graphs-proxy");
      if (null==graphsProxyUrl) graphsProxyUrl=System.getenv("GRAPHS_PROXY");
      
      if (!StringUtils.isEmpty(graphsProxyUrl)){
        String url=graphsProxyUrl+"/api/proxy";
        log.warn("graphsProxyUrl is null == "+(null==graphsProxyUrl));
        log.warn("graphsProxy configured at: "+url);
        ChartsController cc=new ChartsController();
        ScorecardController mc=new ScorecardController();
        EventsController ec=new EventsController();
        int errorCount=0;
        for(String user:db.getUsers().keySet()){
          if (errorCount>=20){
            log.error("Aborting pushing data to graphs because it failed 20+ times");
            break;
          }
          try{
            if (200!=Http.post(url+"/nextLevel_"+user, (String)cc.getUserNextLevel(user).getEntity()).responseCode){
              log.error("Error pushing 'nextLevel' info for '"+user+"' to graphsProxy");
              errorCount+=1;
            }
            if (200!=Http.post(url+"/summary_"+user, (String)mc.getScorecardSummary(user).getEntity()).responseCode){
              log.error("Error pushing 'summary' info for '"+user+"' to graphsProxy");
              errorCount+=1;
            }
            if (200!=Http.post(url+"/breakdown_"+user, (String)mc.getUserBreakdown(user).getEntity()).responseCode){
              log.error("Error pushing 'breakdown' info for '"+user+"' to graphsProxy");
              errorCount+=1;
            }
          }catch (IOException e){
            e.printStackTrace();
            errorCount+=1;
          }
        }
        
        // add the top 10 to the graphs too so they're available externally
        try{
          if (200!=Http.post(url+"/leaderboard_10", (String)cc.getLeaderboard(10).getEntity()).responseCode)
            log.error("Error pushing 'leaderboard' info to graphsProxy");
        }catch (IOException e){
          e.printStackTrace();
        }
        
        // add the Ninjas to the graphs too so they're available externally
        try{
          if (200!=Http.post(url+"/ninjas", (String)cc.getNinjas().getEntity()).responseCode)
            log.error("Error pushing 'ninjas' info to graphsProxy");
        }catch (IOException e){
          e.printStackTrace();
        }
        
        // add the Events to the graphs so they're available externally
        try{
        	String events=(String)ec.getEventsV2(new MapBuilder<String,String>()
        			.put("events","User Promotion,Points Increment")
      				.put("daysOld", "180")
      				.put("asCSV", "true")
      				.build());
          if (200!=Http.post(url+"/events180", events).responseCode)
            log.error("Error pushing 'events180' info to graphsProxy");
        }catch (IOException e){ e.printStackTrace(); }
        
      }else{
        log.warn("not pushing to graphs proxy - url was: "+graphsProxyUrl);
      }
    }
    
    
    // MJA TODO: I think this was being replaced by the code in LevelUp.java but was never completed/migrated
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
            
            db.addEvent("User Promotion", userInfo.get("username"), "Promoted to "+nextLevel.getRight());
            
            String displayName=userInfo.containsKey("displayName")?userInfo.get("displayName"):userInfo.get("username");
            String message=displayName +" promoted to "+nextLevel.getRight();
            db.addTask(message, userInfo.get("username"));
            
            // Notify everyone on the Ninja chat group of a new belt promotion
            ChatNotification_v2.get().send(ChatEvent.onBeltPromotion, message);
            
            count+=1;
          }
          
        }
      }
      db.save();
    }
    
    
  }
  
  static class Utils{
    private static String secondsFromNow(int seconds){
      Calendar c=newCalendar(System.currentTimeMillis());
      c.add(Calendar.SECOND, seconds);
      SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss");
      return sdf.format(c.getTime());
    }
    static private long getMillisToNextTime(String time) throws ParseException{
      SimpleDateFormat sdf=new SimpleDateFormat(time.split(":").length <= 2?"HH:mm":"HH:mm:ss");
      Calendar start=newCalendar(System.currentTimeMillis());
      Calendar cTime=newCalendar(sdf.parse(time).getTime());
      start.set(Calendar.HOUR_OF_DAY, cTime.get(Calendar.HOUR_OF_DAY));
      start.set(Calendar.MINUTE, cTime.get(Calendar.MINUTE));
      start.set(Calendar.SECOND, cTime.get(Calendar.SECOND));
      
      Calendar now=newCalendar(System.currentTimeMillis());
      
      if (now.getTimeInMillis() >= start.getTimeInMillis()){
        // too late today, move to tomorrow
        start.set(Calendar.DAY_OF_MONTH, start.get(Calendar.DAY_OF_MONTH) + 1);
      }
      return start.getTimeInMillis() - now.getTimeInMillis();
    }
    private static Calendar newCalendar(long millis){
      Calendar c=Calendar.getInstance();
      c.setTime(new Date(millis));
      return c;
    }
    
    static int[] s=new int[]{1000, 60, 60, 24, 365};
    static String[] d=new String[]{"ms", "s", "m", "h", "d", "y"};
    public static String msToSensibleString(long ms){
      double result=ms;
      String denomination=d[0];
      for (int i=0; i <= s.length; i++){
        if (result >= s[i]){
          result=result / (double)s[i];
          denomination=d[i + 1];
        }else
          break;
      }
      return new DecimalFormat("##.###").format(result) + denomination;
    }
  }
  

}
