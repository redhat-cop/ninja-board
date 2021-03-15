package com.redhat.sso.ninja;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.apache.log4j.Logger;

import com.redhat.sso.ninja.ChatNotification.ChatEvent;
import com.redhat.sso.ninja.utils.LevelsUtil;
import com.redhat.sso.ninja.utils.Tuple;

public class LevelUp{
	private static final Logger log = Logger.getLogger(LevelUp.class);
	
	public static void main(String[] args){
		Map<String, String> userInfo=Database2.get().getUsers().get("<username>");
		Map<String, Integer> scorecards=Database2.get().getScoreCards().get("<username>");
		scorecards.put("testing", 18);
		
		System.out.println("");
		new LevelUp().levelUpChecks(Database2.get());
		
		System.out.println("");
		scorecards.clear();
		new LevelUp().levelUpChecks(Database2.get());
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
        Tuple<Integer, String> lastLevel=LevelsUtil.get().getLastLevel(userInfo.get("level"));
        
        // Deserve a demotion?
        if (total<=lastLevel.getLeft() && !currentLevel.getRight().equals(lastLevel.getRight())){
        	// Uh oh, the user doesnt have the points to be at the current level, shift them down...
        	log.info("User "+userId+" has been demoted to level "+lastLevel.getRight()+" with a points score of "+total);
          userInfo.put("level", lastLevel.getRight());
          userInfo.put("levelChanged", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));// nextLevel.getRight());
          
          db.addEvent("User Demotion", userInfo.get("username"), "Demoted to "+lastLevel.getRight()+". Score is "+total);
          
          String displayName=userInfo.containsKey("displayName")?userInfo.get("displayName"):userInfo.get("username");
          new ChatNotification().send(ChatEvent.onBeltDemotion, displayName +" demoted to "+lastLevel.getRight());
          count+=1;
        }
        
        // Deserve a promotion?
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
          new ChatNotification().send(ChatEvent.onBeltPromotion, message);
          
          count+=1;
        }
        
      }
    }
    //db.save();
  }
	
}
