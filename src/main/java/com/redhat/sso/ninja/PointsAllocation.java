package com.redhat.sso.ninja;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.redhat.sso.ninja.utils.ParamParser;

public class PointsAllocation{
  private static final Logger log=Logger.getLogger(PointsAllocation.class);
	
  private Database2 db;
  private String scriptName;
  private Map<String, String> mapper;
  
  public PointsAllocation database(Database2 db){
  	this.db=db; return this;
  }
  public PointsAllocation scriptName(String scriptName){
  	this.scriptName=scriptName; return this;
  }
  public PointsAllocation mapper(Map<String, String> mapper){
  	this.mapper=mapper; return this;
  }

	private Pattern paramsPattern=Pattern.compile(".*(\\[.*\\]).*");
	public void allocatePoints2(String line) throws UnsupportedEncodingException{

		String s=line.trim();
//		scriptLog.append(s).append("\n");
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
		
		if (s.startsWith("#")){ // Informational lines only, some may need to be added to event logging as reasons points were not awarded
			
			
			
		}else if (s.contains("/")){ // ignore the line if it doesn't contain a slash
			String[] split=s.split("/");
			
			// take the last section of the script name as the pool id. so "trello" stays as "trello", but "trello.thoughtleadership" becomes "thoughtleadership" where the "trello" part is the source type/context
			String pool=(String)scriptName;
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
				params.put("pool", pool);
				
				if (!db.getPointsDuplicateChecker().contains(actionId+"."+poolUserId)){
					db.getPointsDuplicateChecker().add(actionId+"."+poolUserId);
					
					String userId=mapper.get(poolUserId); // convert the channel (trello, github etc..) to the kerberos username
					
					if (null!=userId){
						db.increment(pool, userId, inc, params);//.save();
					}else{
						log.info("Unable to find '"+poolUserId+"' "+scriptName+" user - not registered? "+Database2.buildLink(params));
						db.addEvent("Lost Points", poolUserId +"("+scriptName+")", scriptName+" user '"+poolUserId+"' was not found - not registered? "+Database2.buildLink(params));
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
	
	
}
