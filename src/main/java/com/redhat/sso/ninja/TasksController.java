package com.redhat.sso.ninja;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.redhat.sso.ninja.Database2.TASK_FIELDS;
import com.redhat.sso.ninja.utils.Json;

@Path("/")
public class TasksController {
  private static final Logger log=LogManager.getLogger(TasksController.class);
  

  
  
	@POST
	@Path("/tasks")
	public Response newTask(@Context HttpServletRequest request,@Context HttpServletResponse response) throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException{
		log.info("NewTask:: [POST] /tasks");
		String data=IOUtils.toString(request.getInputStream());
		log.debug("data = "+data);
		Database2 db=Database2.get();
		
		Map<String, String> task=new HashMap<String, String>();
		mjson.Json json=mjson.Json.read(data);
		for(Entry<String, Object> e:json.asMap().entrySet()){
			task.put(e.getKey(), (String)e.getValue());
		}
		task.put("list", "todo"); // start every task on the todo list
		
		task.put(TASK_FIELDS.UID.v, UUID.randomUUID().toString());
		task.put(TASK_FIELDS.ID.v, Config.get().getNextTaskNum());
		db.getTasks().add(task);
		
		db.save();
		
		return Response.status(200).entity(task.get(TASK_FIELDS.ID.v)).build();
	}
	
	@GET
	@Path("/tasks")
	public Response getTasks(@Context HttpServletRequest request,@Context HttpServletResponse response) throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException{
		log.info("GetTasks:: [GET] /tasks");
		
		Map<String, List<Map<String, String>>> boards=new HashMap<String, List<Map<String,String>>>();
		List<Map<String, String>> tasks=Database2.get().getTasks();
		for(Map<String, String> task:tasks){
			String list=task.get(TASK_FIELDS.LIST.v);
			log.debug("getTasks():: task key = "+list);
			log.debug("task "+task.get("id")+" list name = "+list);
			
			if (!boards.containsKey(list))
				boards.put(list, new ArrayList<Map<String,String>>());
			boards.get(list).add(task);
		}
		
		return Response.ok().type(MediaType.APPLICATION_JSON).entity(Json.newObjectMapper(true).writeValueAsString(boards)).build();
	}

	@POST
	@Path("/tasks/{taskId}/delete")
	public Response deleteTask(@PathParam("taskId") String taskId, @Context HttpServletRequest request,@Context HttpServletResponse response) throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException{
		log.info("DeleteTask:: [POST] /tasks/"+taskId+"/delete");
		for(Map<String, String> task:Database2.get().getTasks()){
			if (taskId.equals(task.get(TASK_FIELDS.ID.v))){
				Database2.get().getTasks().remove(task);
				return Response.ok().build();
			}
		}
		return Response.serverError().build();
	}
	
	
	@POST
	@Path("/tasks/{taskId}")
	public Response updateTask(@PathParam("taskId") String taskId, @Context HttpServletRequest request,@Context HttpServletResponse response) throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException{
		log.info("UpdateTask:: [GET] /tasks/"+taskId);
		
		String data=IOUtils.toString(request.getInputStream(), "UTF-8");
		log.debug("request data = "+data);
		mjson.Json json=mjson.Json.read(data);
		
		Map<String, Object> updates=json.asMap();
		String targetListId=null;
		if (updates.containsKey("list")){
			targetListId=((String)(updates.get("list"))).replaceAll("_","");
		}
		
		Database2 db=Database2.get();
		for(Map<String, String> task:db.getTasks()){
			if (taskId.equals(task.get(TASK_FIELDS.ID.v))){
				
				log.debug(taskId+":: Found - updating properties...");
				for(Entry<String, Object> e:json.asMap().entrySet()){
					if (!e.getKey().equals("list")){// so we dont process the list twice, since it isnt a property but a location where the task resides
						log.debug(taskId+":: saving key="+e.getKey()+", value="+e.getValue());
						task.put(e.getKey(), (String)e.getValue());
					}
				}
				
				if (null!=targetListId){
					log.debug(taskId+":: Adding to list: "+targetListId);
					task.put(TASK_FIELDS.LIST.v, targetListId);
				}
				
			}
		}
		
		db.save();
		
		return Response.ok().build();
	}

	@DELETE
	@Path("/tasks/{taskId}/labels/{label}")
	public Response removeLabel(@PathParam("taskId") String taskId, @PathParam("label") String label, @Context HttpServletRequest request,@Context HttpServletResponse response) throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException{
		log.info("RemoveLabel:: [DELETE] /tasks/"+taskId+"/labels/"+label);
		Database2 db=Database2.get();
		for(Map<String, String> task:db.getTasks()){
			if (taskId.equals(task.get(TASK_FIELDS.ID.v))){
				
				if (!task.containsKey(TASK_FIELDS.LABELS.v))
					task.put(TASK_FIELDS.LABELS.v, "");
				
				String f=task.get(TASK_FIELDS.LABELS.v);
				
				List<String> newList=new ArrayList<String>();
				for(String x:f.split(",")){
					if (!label.equals(x) && !"".equals(x)){
						newList.add(x);
					}
				}
				task.put(TASK_FIELDS.LABELS.v, Joiner.on(",").skipNulls().join(newList));
				
			}
		}
		db.save();
		return Response.ok().build();
	}
	
	@POST
	@Path("/tasks/{taskId}/labels/{label}")
	public Response addLabel(@PathParam("taskId") String taskId, @PathParam("label") String label, @Context HttpServletRequest request,@Context HttpServletResponse response) throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException{
		log.info("addLabel:: [POST] /tasks/"+taskId+"/labels/"+label);
		Database2 db=Database2.get();
		for(Map<String, String> task:db.getTasks()){
			if (taskId.equals(task.get(TASK_FIELDS.ID.v))){
				
				if (!task.containsKey(TASK_FIELDS.LABELS.v))
					task.put(TASK_FIELDS.LABELS.v, "");
				
				String f=task.get(TASK_FIELDS.LABELS.v);
				List<String> split=new ArrayList<String>();
				for(String x:f.split(","))
					split.add(x);
				split.add(label);
				task.put(TASK_FIELDS.LABELS.v, Joiner.on(",").join(split));
				
			}
		}
		db.save();
		return Response.ok().build();
	}
	
}