package com.redhat.sso.ninja;

import com.redhat.sso.ninja.utils.Http;

public class ChatNotification{
	
	public void send(String notificationText){
		Config c=Config.get();
		boolean enabled= "true".equalsIgnoreCase(c.getOptions().get("googlehangoutschat.webhook.notifications.enabled"));
		if (enabled){
			// https://developers.google.com/hangouts/chat/how-tos/webhooks
			String url=      c.getOptions().get("googlehangoutschat.webhook.url");
			String template= c.getOptions().get("googlehangoutschat.webhook.template");
			String googleHangoutsChatPayload=String.format(template, notificationText);
			Http.post(url, googleHangoutsChatPayload);
		}
	}
}
