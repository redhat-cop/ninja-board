package com.redhat.services.portfolio.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.redhat.cop.giveback.Config;
import com.redhat.services.portfolio.utils.Http2.Response;

import io.vertx.ext.web.handler.HttpException;

/**
 * Integration with google or slack chat boards, to push notifications of events such as user promotions, script failures etc..
 * @author mallen
 * 
	"options": {
    "backupInterval": "1d",
    "auto.delete.older.than":"3 months",
		"notifications.enabled": "false",
    "ssl.certs.trustall": "true",
    "slack.webhook.template": "{\"app\":\"TASK_NAME\", \"text\": \"`TASK_NAME` is *DOWN!*. Response code: RESPONSE_CODE. <https://watcher.apps.int.spoke.prod.us-east-1.aws.paas.redhat.com/watcher|Watcher>\", \"icon_emoji\": \":ghost:\"}",
		"googlechat.webhook.template": "{'text':'TASK_NAME is DOWN! returned RESPONSE_CODE! - <https://watcher.apps.int.spoke.prod.us-east-1.aws.paas.redhat.com/watcher|Watcher>'}"
	},
	"values": {},
	"notifications":[
		{
			"channel":"https://xhooks.slack.com/triggers/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",
			"events": "onHttpFailure,onError",
			"enabled": "true"
		}
	]

or in application.properties:
  notifications.sslcertstrustall
  notifications.google.template={'text':'TASK_NAME is DOWN! returned RESPONSE_CODE! - <https://watcher.apps.int.spoke.prod.us-east-1.aws.paas.redhat.com/tasks|Watcher>'}
  notifications.slack.template={\"app\":\"TASK_NAME\", \"text\": \"`TASK_NAME` is *DOWN!*. Response code: RESPONSE_CODE. <https://watcher.apps.int.spoke.prod.us-east-1.aws.paas.redhat.com/tasks|Watcher>\", \"icon_emoji\": \":ghost:\"}
  notifications=[{"type":"slack","events":"onHttpFailure,onError","channel":"https://xhooks.slack.com/triggers/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","enabled":true}]
	
 */
public class ChatNotification_v2{
	private static final Logger log=LoggerFactory.getLogger(ChatNotification_v2.class);
	public enum ChatEvent{onHttpFailure,onError,onWarning,onBeltPromotion,onBeltDemotion,onScriptError,onRegistration}
	private boolean enabled;
	private boolean sslTrustAll=false;
	private String appName;
	private List<Map<String,String>> notifications;
	private Map<String,String> templates;
	public static ChatNotification_v2 _inst;
	
	public static void main(String[] args) throws JsonProcessingException{
		ChatNotification_v2 test=ChatNotification_v2.get();
		test.enabled=true;
		test.sslTrustAll=true;
		test.appName="Services Portfolio Hub"+" ["+System.getenv("HOSTNAME")+"]";
		test.templates=new MapBuilder<String,String>().put("slack", "{\"app\":\"APP_NAME\", \"text\":\"MESSAGE_TEXT\", \"icon_emoji\": \":ghost:\"}").build();
		test.notifications=Lists.newArrayList(new MapBuilder<String,String>().put("type", "slack").put("events", ChatEvent.onWarning.name()).put("channel", "https://hooks.slack.com/triggers/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").build());
//		test.send(ChatEvent.onWarning, "test message");
		System.out.println(Json.toJson(test.notifications));
	}
	
	private ChatNotification_v2(){}
	public static ChatNotification_v2 get(){if (_inst==null) _inst=initialize(); return _inst;}
	public static ChatNotification_v2 initialize(){
    boolean sslTrustAll="true".equalsIgnoreCase(Config.get().getPropertySilent("notifications.sslcertstrustall", "false"));
    if (sslTrustAll){
      log.debug("Trusting all hostnames & SSL certs");
//    SSLUtilities.trustAllHostnames();
      SSLUtilities.trustAllHttpsCertificates();
    }
    _inst=new ChatNotification_v2();
    _inst.enabled="true".equalsIgnoreCase(Config.get().getPropertySilent("notifications.enabled"));
    if (System.getenv("HOSTNAME").toLowerCase().matches(".*(portfolio|recommender|watcher|assessment).*")){
    	log.warn("Disabling all Notifications for non container deployed apps");
    	_inst.enabled=false;
    }
//    _inst.enabled=_inst.enabled && Lists.newArrayList("portfolio-search","portfolio-hub","recommender","watcher","assessment").contains(System.getenv("HOSTNAME").toLowerCase());
    _inst.sslTrustAll=sslTrustAll;
    _inst.appName=Config.get().getPropertySilent("notifications.app.name")+" ["+System.getenv("HOSTNAME")+"]";
    _inst.templates=Maps.newHashMap();
    _inst.templates.put("slack",  Config.get().getPropertySilent("notifications.slack.template"));
    _inst.templates.put("google", Config.get().getPropertySilent("notifications.googlechat.template"));
    try{
			_inst.notifications=Json.toObject(Config.get().getPropertySilent("notifications"), new TypeReference<List<Map<String,String>>>(){});
		}catch(IOException|IllegalArgumentException e){
//			e.printStackTrace();
			System.out.println("disabling notifications bc 'notification' property in application.properties is empty");
			_inst.enabled=false;
		}
    return _inst;
	}
	
	
	public void send(ChatEvent type, String message){
		send(type, new MapBuilder<String,String>().put("APP_NAME",appName).put("MESSAGE_TEXT",type.name().substring(2)+": "+message).build());
	}
	public void send(ChatEvent type, String appName, String message){
		send(type, new MapBuilder<String,String>().put("APP_NAME",appName).put("MESSAGE_TEXT",message).build());
	}
	public void send(ChatEvent type, String appName, String message, String httpResponse){
		send(type, new MapBuilder<String,String>().put("APP_NAME",appName).put("MESSAGE_TEXT",message).put("RESPONSE_CODE",httpResponse).build());
	}
	
	protected void send(ChatEvent type, Map<String,String> replacements){
//	  System.out.println("send alert called");
		if (enabled){
//		  System.out.println("send alert - notification on");
			for(Map<String, String> notification:notifications){
				List<String> events=Arrays.asList(notification.get("events").split(","));
				if (events.contains(type.name())){ // send the notification!
					String channel=notification.get("channel");
					String alertPayload=getTemplate(templates, channel);
					if (alertPayload!=null){
						for(Entry<String,String> e:replacements.entrySet())
							alertPayload=alertPayload.replaceAll(e.getKey(), e.getValue());	
//						String alertPayload=template.replaceAll("TASK_NAME", name).replaceAll("RESPONSE_CODE", httpResponse).replaceAll("TASK_URL", taskUrl);
						System.out.println(String.format("Sending this; channel=%s, payload=%s", channel, alertPayload));
						try{
							Response r=Http2.post(channel, alertPayload, new MapBuilder<String, String>().put("Content-Type", "application/json; charset=UTF-8").build());
							System.out.println("Sent '"+alertPayload+"' to chat api. response.code=200");
						}catch(HttpException e){
							System.out.println("Sent '"+alertPayload+"' to chat api but hit an error: response.code="+e.getStatusCode());
//						}catch(IOException e){
//							System.out.println("Sent '"+alertPayload+"' to chat api but hit an error: "+ e.getMessage());
						}
					}
				}
			}
		}
	}
	
	private String getTemplate(Map<String,String> templates, String channel){
	  if (channel.toLowerCase().contains("slack")) return templates.get("slack");
		if (channel.toLowerCase().contains("google")/* && channel.contains("chat") */) templates.get("googlechat");
	  return null;
	}
}
