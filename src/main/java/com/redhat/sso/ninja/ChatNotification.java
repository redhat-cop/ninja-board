package com.redhat.sso.ninja;

import com.redhat.sso.ninja.utils.Http;
import com.redhat.sso.ninja.utils.Http.Response;
import com.redhat.sso.ninja.utils.MapBuilder;

public class ChatNotification{
	
	public static void main(String[] asd){
		Config c=Config.get();
		System.out.println("googlehangoutschat.webhook.notifications.enabled="+c.getOptions().get("googlehangoutschat.webhook.notifications.enabled"));
		System.out.println("googlehangoutschat.webhook.url="+c.getOptions().get("googlehangoutschat.webhook.url"));
		System.out.println("googlehangoutschat.webhook.template="+c.getOptions().get("googlehangoutschat.webhook.template"));
		
		new ChatNotification().send("test - please ignore");
	}
	
	public void send(String notificationText){
		Config c=Config.get();
		boolean enabled= "true".equalsIgnoreCase(c.getOptions().get("googlehangoutschat.webhook.notifications.enabled"));
		if (enabled){
			System.out.println("Sending notification...");
			// https://developers.google.com/hangouts/chat/how-tos/webhooks
			String url=      c.getOptions().get("googlehangoutschat.webhook.url");
			String template= c.getOptions().get("googlehangoutschat.webhook.template");
			String googleHangoutsChatPayload=String.format(template, notificationText);
			
			Response r=Http.post(url, googleHangoutsChatPayload, new MapBuilder<String, String>().put("Content-Type", "application/json; charset=UTF-8").build());
			System.out.println("Response = "+r.getResponseCode());
		}
	}
}
