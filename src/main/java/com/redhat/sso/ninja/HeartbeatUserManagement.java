package com.redhat.sso.ninja;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.google.common.collect.Lists;
import com.redhat.sso.ninja.ChatNotification.ChatEvent;
import com.redhat.sso.ninja.user.UserService;
import com.redhat.sso.ninja.user.UserService.User;
import com.redhat.sso.ninja.utils.LevelsUtil;

public class HeartbeatUserManagement{
	private static final Logger log = Logger.getLogger(HeartbeatUserManagement.class);
	
  public static Map<String, Map<String, String>> getUsersFromRegistrationSheet(Config config) throws IOException, InterruptedException{
    Map<String, Map<String, String>> result=new HashMap<String, Map<String,String>>();
    
    GoogleDrive3 drive=new GoogleDrive3(3000);
    File file=drive.downloadFile(config.getOptions().get("googlesheets.registration.id"));
    List<Map<String, String>> rows=drive.parseExcelDocument(file, new GoogleDrive3.HeaderRowFinder(){
      public int getHeaderRow(XSSFSheet s){
        return 0;
      }}, new SimpleDateFormat("dd-MM-yyyy"));
    for(Map<String,String> r:rows){
      Map<String, String> userInfo=new HashMap<String, String>();
      for(Entry<String, String> c:r.entrySet()){
        if (c.getKey().toLowerCase().contains("timestamp")){
          userInfo.put("reg", c.getValue());
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
      result.put(userInfo.get("username"), userInfo);
    }
    return result;
  }

  public static boolean addOrUpdateRegisteredUsers(Database2 db, Config cfg){
    Map<String, Map<String, String>> dbUsers=db.getUsers();
    UserService userService=new UserService();
    boolean userServiceDown=false;
    try{
    	
    	GoogleDrive3 drive=new GoogleDrive3();
    	File file=drive.downloadFile(cfg.getOptions().get("googlesheets.registration.id"));
    	List<Map<String, String>> rows=drive.parseExcelDocument(file, new GoogleDrive3.HeaderRowFinder(){
          public int getHeaderRow(XSSFSheet s){
            return 0;
          }}, new SimpleDateFormat("yyyy/MM/dd"));
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
//              log.debug("UserService(LDAP) is DOWN, skipping populating the 'displayName'");
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
          
          db.addEvent("New User", userInfo.get("username"), "");
          
          // Notify everyone on the Ninja chat group of a new registree
          String displayName=userInfo.containsKey("displayName")?userInfo.get("displayName"):userInfo.get("username");
          new ChatNotification().send(ChatEvent.onRegistration, "New User Registered: <https://mojo.redhat.com/people/"+userInfo.get("username")+"|"+displayName+">");
          
          
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
  
  public static String cleanupGithubTrelloId(String input){
    String result=input;
    
    // if the id start with @, then strip it
    if (result.startsWith("@")) result=result.substring(1, result.length());
    
    // if the id still contains an @ then assume it's an email and strip the latter part
    result=result.substring(0, (result.contains("@")?result.indexOf("@"):result.length()) );
    
    if (!"".equalsIgnoreCase(result) && !"na".equalsIgnoreCase(result) && !"n/a".equalsIgnoreCase(result))
      return result;
    return null;
  }
  
  enum FieldMapping{
  	displayName("name"),
  	geo("geo");
  	public String value;
  	private FieldMapping(String k){ this.value=k; }
  }
  public static boolean updateUsersDetailsUsingLDAPInfo(Database2 db){
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
//          ex.printStackTrace();
  			}
  		}
  	}
  	return true;
  	
  }
}
