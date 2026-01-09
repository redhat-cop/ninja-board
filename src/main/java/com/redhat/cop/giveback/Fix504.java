package com.redhat.cop.giveback;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.redhat.cop.giveback.auth.OAuth;
import com.redhat.services.portfolio.utils.TimeUtils;

public class Fix504{
	private static final Logger log=Logger.getLogger(Fix504.class);
	private static final Date abitraryDateInThePast=new Date(0);
	
	public static void attemptAutoFix_removeSpecificTokenOnly(OAuth oauth, String token) throws JsonProcessingException, FileNotFoundException, IOException{
		log.error("504Fix:: attempting to remove individual refresh token: "+token);
		oauth.removeRefreshToken(token);
	}
	public static void attemptAutoFix_nuclearWipeEntireTokensFile(String response){
		if (response.contains("504 Gateway Time-out")){
			log.error("504fix:: ATTEMPTING auto fix for response: "+response);
			OAuth.resetRefreshTokens();
		}
	}
	public void periodicRemovalOfRefreshTokens(){
		try {
			Config c=Config.get();
			String last=c.getOptions().get("oauth_tokens_clearout_last_date"); //date in the Initialization.sdf format
			if (Objects.isNull(last)) last=Initialization.sdf.format(abitraryDateInThePast);//default
			long lastDateMs=Initialization.sdf.parse(last).getTime();
			long intervalMs=TimeUtils.sensibleStringToMs(c.getProperty("OAUTH_TOKENS_CLEAROUT_INTERVAL", null));
			if (intervalMs<=0) intervalMs=TimeUtils.sensibleStringToMs("14d"); //default
			long nextDateMs=lastDateMs+intervalMs;
			long toNextDateMs=nextDateMs-System.currentTimeMillis(); // if negative, then trigger, if positive then there's still time
			if (toNextDateMs<0){
				log.info(String.format("Fix504-check: clearing tokens file because last clearout was %s, and interval is %s",Initialization.sdf.format(new Date(lastDateMs)), TimeUtils.msToSensibleString(intervalMs)));
				boolean success=OAuth.resetRefreshTokens();
				c.getOptions().put("oauth_tokens_clearout_last_date", Initialization.sdf.format(new Date(System.currentTimeMillis())));
			}else{
				log.info(String.format("Fix504-check: NOT removing token file because it was last cleared out %s, which is %s ago. interval is %s",Initialization.sdf.format(new Date(lastDateMs)),TimeUtils.msToSensibleString(System.currentTimeMillis()-lastDateMs), TimeUtils.msToSensibleString(intervalMs)));
			}
			c.save();
		}catch(Exception e) {
			log.error("Error executing the token clearout: "+e.getMessage());
			e.printStackTrace();
		}
		
	}
}
