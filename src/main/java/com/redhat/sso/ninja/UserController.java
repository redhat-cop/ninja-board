package com.redhat.sso.ninja;

import java.io.IOException;
import java.util.List;

import javax.naming.NamingException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.redhat.sso.ninja.utils.Json;

@Path("/user")
public class UserController {
  
  
  /** for example /api/user/mallen will return mallen's LDAP details 
   * @throws NamingException */
  @GET
  @Path("/{uid}")
  public Response findByUid(@PathParam("uid") String uid) throws JsonGenerationException, JsonMappingException, IOException, NamingException{
    return find("uid", uid);
  }
  
  @GET
  @Path("/{field}/{value}")
  public Response find(@PathParam("field") String field, @PathParam("value") String value) throws JsonGenerationException, JsonMappingException, IOException, NamingException{
    List<UserService.User> result=search(field, value);
    return Response.status(200).entity(Json.newObjectMapper(true).writeValueAsString(result)).build();
  }
  
  public List<UserService.User> search(String field, String value) throws NamingException {
    return new UserService().search(field, value);
  }

}
