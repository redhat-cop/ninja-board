package com.redhat.sso.ninja.controllers;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.google.common.base.Joiner;
import com.redhat.sso.ninja.Database2;
import com.redhat.sso.ninja.Database2.EVENT_FIELDS;
import com.redhat.sso.ninja.utils.FluentCalendar;
import com.redhat.sso.ninja.utils.Json;
import com.redhat.sso.ninja.utils.MapBuilder;

@Path("/")
public class EventsController{
  private static final Logger log=Logger.getLogger(EventsController.class);
  
  
  // Admin/Support UI call to display all events, user events or specific types of events
  @GET
  @Path("/v2/events")
  public Response getEventsV2(@Context HttpServletRequest request) throws JsonGenerationException, JsonMappingException, IOException{
  	Map<String, String> filters=new MapBuilder<String, String>(true)
		.put("user", request.getParameter("user"))
		.put("events", request.getParameter("events"))
		.put("daysOld", request.getParameter("daysOld"))
		.put("asCSV", request.getParameter("asCSV"))
		.build();
    return Response.status(200)
        .header("Access-Control-Allow-Origin",  "*")
        .header("Content-Type","text/html")
        .header("Cache-Control", "no-store, must-revalidate, no-cache, max-age=0")
        .header("Pragma", "no-cache")
        .header("X-Content-Type-Options", "nosniff").entity(getEventsV2(filters)).build();
  }
  public String getEventsV2(Map<String,String> filters) throws JsonGenerationException, JsonMappingException, IOException{
  	boolean asCSV="true".equalsIgnoreCase(filters.get("asCSV")); // ideally i'd use the Accept headers, but it's being called from an =Import function from a spreadsheet which is easier to set params from
  	return asCSV?jsonToCSV(getFilteredEvents2(filters)):Json.newObjectMapper(true).writeValueAsString(getFilteredEvents2(filters));
  }
  
	public String jsonToCSV(List<Map<String, String>> events) throws JsonGenerationException, JsonMappingException, IOException{
  	// go through the events in the time window and extract/build the points values
  	Map<String, Map<String, String>> userMap=Database2.get().getUsers();
  	
  	Pattern regex1=Pattern.compile("(\\d+) point.* added to (.+) \\((.+)\\)");
  	Pattern regex2=Pattern.compile("(\\d+) point.* added to (.+)");
  	
  	List<String> result=new ArrayList<>();
  	result.add("Timestamp,User,Email,Type,Points,Pool,Source");
  	for (Map<String, String> event:events){
  		String ts=event.get(EVENT_FIELDS.TIMESTAMP.v);
  		String user=event.get(EVENT_FIELDS.USER.v);
  		String email=userMap.get(user).get("email"); // lookup email from database user details
  		String type=event.get(EVENT_FIELDS.TYPE.v);
  		String text=event.get(EVENT_FIELDS.TEXT.v);
  		String points=event.get(EVENT_FIELDS.POINTS.v);
  		String pool=event.get(EVENT_FIELDS.POOL.v);
  		String source=event.get(EVENT_FIELDS.SOURCE.v);
  		
  		if (null!=source && !"".equals(source)){ // if it's an event with a source, pool & points
  			result.add(Joiner.on(",").join(ts, user, email, type, points, pool, source));
  		}else{ // if it's the older "text" formats
  			Matcher m1=regex1.matcher(text); // text format with links
  			Matcher m2=regex2.matcher(text); // text format without links
//  			String points=null,pool=null,url=null;
  			if (m1.find()){ // points, pool & url
  				points=m1.group(1);
  				pool=m1.group(2);
  				source=m1.group(3);
  				result.add(Joiner.on(",").join(ts, user, type, points, pool, source));
  			}else if (m2.find()){ // no url
  				points=m2.group(1);
  				pool=m2.group(2);
  				result.add(Joiner.on(",").join(ts, user, type, points, pool, ""));
  			}else{ // exception case where we cant parse the text
  				result.add(Joiner.on(",").join(null!=ts?ts:"null", null!=user?user:"null", null!=type?type:"null", "","",null!=text?text:"null"));
  			}
  		}
  		
  		
  	}
  	
  	String r="";
  	for(String l:result){
  		r+=l+"\n";
  	}
  	
  	return r;
  }
  

  // Admin/Support UI call to display all events, user events or specific types of events
  @GET
  @Path("/events")
  public Response getEventsV1(@Context HttpServletRequest request) throws JsonGenerationException, JsonMappingException, IOException{
  	Map<String, String> filters=new MapBuilder<String, String>(true)
  			.put("user", request.getParameter("user"))
  			.put("events", request.getParameter("events"))
  			.put("daysOld", request.getParameter("daysOld"))
  			.build();
    return NewResponse.status(200).entity(Json.newObjectMapper(true).writeValueAsString(getFilteredEvents1(filters))).build();
  }
  private List<Map<String, String>> getFilteredEvents1(Map<String,String> filters) throws JsonGenerationException, JsonMappingException, IOException{
//  	System.out.println("getFilteredEvents1");
    Database2 db=Database2.get();
    List<Map<String, String>> result=new ArrayList<Map<String,String>>();
    if (filters.size()<=0){
      result.addAll(db.getEvents());
    }else{
    	for(Map<String, String> e:db.getEvents()){
    		HashMap<String,String> v=new HashMap<>(e);
    		if (null!=v.get(EVENT_FIELDS.USER.v) && null!=filters.get("user") && v.get(EVENT_FIELDS.USER.v).matches(filters.get("user"))) result.add(v);
    		if (null!=v.get(EVENT_FIELDS.TYPE.v) && null!=filters.get("event") && v.get(EVENT_FIELDS.TYPE.v).matches(filters.get("event"))) result.add(v);
    	}
    }
    
    // Enrich the event results without affecting the stored data
  	for(Map<String, String> v:result){
  		// add a generated "text" field if none exists - this is because on the events UI there is not enough space for all the separated fields
  		if ("Points Increment".equals(v.get(EVENT_FIELDS.TYPE.v)) && !v.containsKey(EVENT_FIELDS.TEXT.v)){
  			Integer points=Integer.parseInt(v.get(EVENT_FIELDS.POINTS.v));
  			v.put(EVENT_FIELDS.TEXT.v, points+" point"+(points<=1?"":"s")+" added to "+v.get(EVENT_FIELDS.POOL.v)+" "+convertSourceToShowdown(v.get(EVENT_FIELDS.SOURCE.v)));
  		}
  		
  		if ("New User".equals(v.get(EVENT_FIELDS.TYPE.v))){
  			v.put(EVENT_FIELDS.TEXT.v, v.get(EVENT_FIELDS.USER.v)+" registered");
  		}
  		
  	}
  	
  	return result;
  }
  
  // convert link format to showdown markdown?
  private String convertSourceToShowdown(String source){
		Pattern p=Pattern.compile(".+\\((.+)\\)");
		Matcher m=p.matcher(source);
		if (m.find()){
			String link=m.group(1);
			return "<"+link+">";
		}
		return source;
  }
  
//  // Admin/Support UI call to display all events, user events or specific types of events
//  @GET
//  @Path("/v2/events")
//  public Response getEventsV2(@Context HttpServletRequest request) throws JsonGenerationException, JsonMappingException, IOException{
//  	Map<String, String> filters=new MapBuilder<String, String>(true)
//  			.put("user", request.getParameter("user"))
//  			.put("events", request.getParameter("events"))
//  			.put("daysOld", request.getParameter("daysOld"))
//  			.build();
//    return NewResponse.status(200).entity(Json.newObjectMapper(true).writeValueAsString(getFilteredEvents2(filters))).build();
//  }
  public List<Map<String, String>> getAllEvents() throws JsonGenerationException, JsonMappingException, IOException{
    return getFilteredEvents2(new MapBuilder<String,String>().build());
  }
  private List<Map<String, String>> getFilteredEvents2(Map<String,String> filters) throws JsonGenerationException, JsonMappingException, IOException{
    Database2 db=Database2.get();
    List<Map<String, String>> result=new ArrayList<Map<String,String>>();
    
    if (filters.size()<=0){
      result=db.getEvents();
    }else{
    	
    	Date filterDate=null;
    	if (filters.containsKey("daysOld")){
    		FluentCalendar date=FluentCalendar.now();
    		date.add(Calendar.DAY_OF_MONTH, -1*Integer.parseInt(filters.get("daysOld")));
    		filterDate=date.build().getTime();
    	}
    	
      for(Map<String, String> e:db.getEvents()){
      	boolean include=!filters.containsKey("daysOld");
      	try{
					Date date=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(e.get(EVENT_FIELDS.TIMESTAMP.v));
					if (null!=filterDate && date.after(filterDate)) include=true;
				}catch (ParseException e1){
					e1.printStackTrace();
				}
      	
      	if (include){
      		
      		if (filters.containsKey("user") && !e.get(EVENT_FIELDS.USER.v).matches(filters.get("user")))
      			include=false;
      		
      		// include if it contains ANY events listed in the filter
      		if (include && filters.containsKey("events")){
      			String[] eventsSplit=filters.get("events").split(",");
      			boolean matchesAtLeastOne=false;
      			for(String event:eventsSplit){
      				matchesAtLeastOne=matchesAtLeastOne || e.get(EVENT_FIELDS.TYPE.v).matches(event);
      			}
      			if (!matchesAtLeastOne){
      				include=false;
      			}
      		}
      		
      		if (include) result.add(new HashMap<>(e));
      	}
      }
    }
    
    // Enrich the event results without affecting the stored data
  	for(Map<String, String> v:result){
  		// add a generated "text" field if none exists - this is because on the events UI there is not enough space for all the separated fields
  		if ("Points Increment".equals(v.get(EVENT_FIELDS.TYPE.v)) && !v.containsKey(EVENT_FIELDS.TEXT.v)){
//  			System.out.println("adding a text field");
//    		if (!v.containsKey(EVENT_FIELDS.TEXT.v)){// && v.containsKey(EVENT_FIELDS.POINTS.v)){
  			Integer points=Integer.parseInt(v.get(EVENT_FIELDS.POINTS.v));
  			v.put(EVENT_FIELDS.TEXT.v, points+" point"+(points<=1?"":"s")+" added to "+v.get(EVENT_FIELDS.POOL.v)+" "+convertSourceToShowdown(v.get(EVENT_FIELDS.SOURCE.v)));
  		}
  		
  		if ("New User".equals(v.get(EVENT_FIELDS.TYPE.v))){
  			v.put(EVENT_FIELDS.TEXT.v, v.get(EVENT_FIELDS.USER.v)+" registered");
  		}
  		
  	}
    return result;
  }
}
