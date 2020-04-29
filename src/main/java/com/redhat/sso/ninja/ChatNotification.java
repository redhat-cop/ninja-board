package com.redhat.sso.ninja;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.redhat.sso.ninja.utils.Http;
import com.redhat.sso.ninja.utils.Http.Response;
import com.redhat.sso.ninja.utils.MapBuilder;

/**
 * Integration with google chat boards, to push notifications of events such as user promotions, script failures etc..
 * @author mallen
 */
public class ChatNotification{
	public enum ChatEvent{onRegistration,onBeltPromotion,onScriptError}
//	public static void main(String[] asd){
//		Config c=Config.get();
//		System.out.println("googlehangoutschat.webhook.template="+c.getOptions().get("googlehangoutschat.webhook.template"));
//		new ChatNotification().send(ChatEvent.onRegistration, "<https://your.site.com/people/fbloggs|Fred Bloggs> promoted to BLUE belt");
//	}
	
	public void send(ChatEvent type, String notificationText){
		Config c=Config.get();
		for(Map<String, String> notification:c.getNotifications()){
			if (!"false".equalsIgnoreCase(notification.get("enabled"))){
				List<String> events=Lists.newArrayList(notification.get("events").split(","));
				if (events.contains(type.name())){
					// send the notification!
					String channel=notification.get("channel");
					String template= c.getOptions().get("googlehangoutschat.webhook.template");
					String googleHangoutsChatPayload=String.format(template, notificationText);
					Response r=Http.post(channel, googleHangoutsChatPayload, new MapBuilder<String, String>().put("Content-Type", "application/json; charset=UTF-8").build());
					System.out.println("Response = "+r.getResponseCode());
				}
			}
		}
	}
}
