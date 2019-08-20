package com.redhat.sso.ninja;

import com.google.common.base.Joiner;
import com.redhat.sso.ninja.user.Task;
import com.redhat.sso.ninja.utils.Json;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Path("/")
public class TasksController {
  private static final Logger log = Logger.getLogger(TasksController.class);


  @POST
  @Path("/tasks")
  public Response newTask(@Context HttpServletRequest request, @Context HttpServletResponse response) throws IOException {
    log.info("NewTask:: [POST] /tasks");
    String data = IOUtils.toString(request.getInputStream());
    log.debug("data = " + data);
    Database2 db = Database2.get();

    Map<String, String> taskData = new HashMap<>();
    mjson.Json json = mjson.Json.read(data);
    for (Entry<String, Object> e : json.asMap().entrySet()) {
      taskData.put(e.getKey(), (String) e.getValue());
    }
    Task task = db.addTask(taskData);
    db.save();
    return Response.status(200).entity(task.getId()).build();
  }

  @GET
  @Path("/tasks")
  public Response getTasks(@Context HttpServletRequest request, @Context HttpServletResponse response) throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException {
    log.info("GetTasks:: [GET] /tasks");

    Map<String, List<Task>> boards = new HashMap<>();
    List<Task> tasks = Database2.get().getTasks();
    for (Task task : tasks) {
      String list = task.getList();
      log.debug("getTasks():: task key = " + list);
      log.debug("task " + task.getId() + " list name = " + list);

      if (!boards.containsKey(list))
        boards.put(list, new ArrayList<>());
      boards.get(list).add(task);
    }

    return Response.ok().type(MediaType.APPLICATION_JSON).entity(Json.newObjectMapper(true).writeValueAsString(boards)).build();
  }

  @POST
  @Path("/tasks/{taskId}/delete")
  public Response deleteTask(@PathParam("taskId") String taskId, @Context HttpServletRequest request, @Context HttpServletResponse response) throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException {
    log.info("DeleteTask:: [POST] /tasks/" + taskId + "/delete");
    for (Task task : Database2.get().getTasks()) {
      if (taskId.equals(task.getId())) {
        Database2.get().getTasks().remove(task);
        return Response.ok().build();
      }
    }
    return Response.serverError().build();
  }


  @POST
  @Path("/tasks/{taskId}")
  public Response updateTask(@PathParam("taskId") String taskId, @Context HttpServletRequest request, @Context HttpServletResponse response) throws IOException {
    log.info("UpdateTask:: [GET] /tasks/" + taskId);

    String data = IOUtils.toString(request.getInputStream(), "UTF-8");
    log.debug("request data = " + data);
    mjson.Json json = mjson.Json.read(data);

    Map<String, Object> updates = json.asMap();
    String targetListId = null;
    if (updates.containsKey("list")) {
      targetListId = ((String) (updates.get("list"))).replaceAll("_", "");
    }

    if (null != targetListId) {
      log.debug(taskId + ":: Adding to list: " + targetListId);
      updates.put("list", targetListId);
    }

    Database2 db = Database2.get();
    db.updateTask(taskId, updates);
    db.save();

    return Response.ok().build();
  }

  @DELETE
  @Path("/tasks/{taskId}/labels/{label}")
  public Response removeLabel(@PathParam("taskId") String taskId, @PathParam("label") String label, @Context HttpServletRequest request, @Context HttpServletResponse response) throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException {
    log.info("RemoveLabel:: [DELETE] /tasks/" + taskId + "/labels/" + label);
    Database2 db = Database2.get();
    for (Task task : db.getTasks()) {
      if (taskId.equals(task.getId())) {

        if (StringUtils.isBlank(task.getLabels())) {
					task.setLabels("");
				}

        String f = task.getLabels();

        List<String> newList = new ArrayList<>();
        for (String x : f.split(",")) {
          if (!label.equals(x) && !"".equals(x)) {
            newList.add(x);
          }
        }
        task.setLabels(Joiner.on(",").skipNulls().join(newList));

      }
    }
    db.save();
    return Response.ok().build();
  }

  @POST
  @Path("/tasks/{taskId}/labels/{label}")
  public Response addLabel(@PathParam("taskId") String taskId, @PathParam("label") String label, @Context HttpServletRequest request, @Context HttpServletResponse response) throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException {
    log.info("addLabel:: [POST] /tasks/" + taskId + "/labels/" + label);
    Database2 db = Database2.get();
    for (Task task : db.getTasks()) {
      if (taskId.equals(task.getId())) {

        if (StringUtils.isBlank(task.getLabels())) {
					task.setLabels("");
				}

        String f = task.getLabels();
        List<String> split = new ArrayList<>();
        for (String x : f.split(",")) {
					split.add(x);
				}
        split.add(label);
        task.setLabels(Joiner.on(",").join(split));

      }
    }
    db.save();
    return Response.ok().build();
  }

}