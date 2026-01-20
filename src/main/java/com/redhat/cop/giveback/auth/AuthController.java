package com.redhat.cop.giveback.auth;

import static com.redhat.cop.giveback.auth.AuthFilter2.shorten;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.apache.commons.codec.binary.Base64;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.redhat.cop.giveback.Fix504;
import com.redhat.cop.giveback.Initialization;
import com.redhat.cop.giveback.controllers.CommonController;
import com.redhat.services.portfolio.utils.Cache;
import com.redhat.services.portfolio.utils.ChatNotification_v2;
import com.redhat.services.portfolio.utils.ChatNotification_v2.ChatEvent;
import com.redhat.services.portfolio.utils.CookieBuilder;
import com.redhat.services.portfolio.utils.CookieBuilder.SameSite;
import com.redhat.services.portfolio.utils.Http;
import com.redhat.services.portfolio.utils.Json;
import com.redhat.services.portfolio.utils.MapBuilder;

@Path("/")
public class AuthController extends CommonController{
	private static final Logger log=LoggerFactory.getLogger(AuthController.class);
	private final SimpleDateFormat sdf=new SimpleDateFormat(Initialization.DATE_FORMAT);
	
	@Context
	private UriInfo uri;
	
	@GET
	@Path("/signin")
	public Response signin(@QueryParam("returnUrl") String returnUrl) throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException{
		if (returnUrl==null) return Response.status(500).build();
		returnUrl=new String(Base64.decodeBase64(returnUrl));
		// todo: probably should add some validation to returnUrl here - 2024/12/18 has something that looked like hacking in the logs. should be characters and som symbols, nothing unprintable
		log.info("[GET]/signin - redirecting to google signin [returnUrl="+returnUrl+"]");
		if (null==returnUrl) returnUrl=OAuth.get(uri).getReturnUrl(uri, "/search"); // default post sign-in activity
		return Response.status(302).location(new URI(OAuth.get(uri).generateAuthUrl(OAuth.get(uri).redirectUri, returnUrl))).build();
	}
	
	@GET
	@Path("/signout")
	public Response signout(@QueryParam("returnUrl") String returnUrl) throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException{
		log.info("[GET]/signout - invalidating cookies [returnUrl="+returnUrl+"]");
		return Response.status(302)
			.header("Set-Cookie", new CookieBuilder().name("rh_services_portfolio_access_token") .value("DELETED").maxAge(-1).path("/").secure().sameSite(SameSite.None).build())
			.header("Set-Cookie", new CookieBuilder().name("rh_services_portfolio_refresh_token").value("DELETED").maxAge(-1).path("/").secure().sameSite(SameSite.None).build())
			.header("Set-Cookie", new CookieBuilder().name("rh_services_portfolio_user_email")	 .value("DELETED").maxAge(-1).path("/").secure().sameSite(SameSite.None).build())
			.location(new URI(returnUrl))
			.build();
	}
	
	@GET
	@Path("/auth/checkToken")
	public Response checkToken(@QueryParam("token") String token) throws JsonGenerationException, JsonMappingException, IOException{
		log.debug("/auth/checkToken - getting tokenInfo using the token");
		if (token==null) return Response.status(500).build();
		Preconditions.checkArgument(token.length()<=300 && token.matches("[\\.a-zA-Z0-9_-]+"), "token max length is 300 chars");
		Map<String, String> tokenInfo=OAuth.get(uri).getTokenInfoAsMap(token);
		return Response.status(!tokenInfo.containsKey("error")?200:500).entity(Json.toJson(tokenInfo)).build();
	}

	@GET
	@Path("/auth/userInfo")
	public Response userInfo(@QueryParam("token") String token) throws JsonGenerationException, JsonMappingException, IOException{
		if (token==null) return Response.status(500).build();
		log.debug("/auth/userInfo - getting userInfo using the token - "+shorten(token,4,5));
		Preconditions.checkArgument(token.length()<=300 && token.matches("[\\.a-zA-Z0-9_-]+"), "token max length is 300 chars");
		String infoJson = OAuth.get(uri).getUserInfo(token);//	get(new StringBuilder("https://www.googleapis.com/oauth2/v1/userinfo?access_token=").append(token).toString());
		
		mjson.Json j=mjson.Json.read(infoJson);
		if (!j.has("email")){
			log.info(String.format("[GET]/auth/userInfo failed. token used was %s. response was %s",shorten(token,4,5),infoJson));
			return Response.status(401).entity(j.toString()).build();
		}
		
		String email=j.at("email").asString();
		List<String> roles=AuthFilter2.databaseUserRoleMappingCache.get(email);
		if (null!=roles) j.set("roles", roles);
		
		log.debug(String.format("[GET]/auth/userInfo %s:%s has roles %s", email, shorten(token,4,5), roles));
		
		if (infoJson.contains("\"error\"")){
			Integer errorCode=j.at("error").at("code").asInteger();
			return Response.status(errorCode).entity(j.toString()).build();
		}else
			return Response.status(200).entity(j.toString()).build();
	}
	/*
	 * on error:
{
	"error": {
		"code": 401,
		"message": "Request is missing required authentication credential. Expected OAuth 2 access token, login cookie or other valid authentication credential. See https://developers.google.com/identity/sign-in/web/devconsole-project.",
		"status": "UNAUTHENTICATED"
	}
}
	 * on success:
{
		"id": "116134366174021833421",
		"email": "xxxx@redhat.com",
		"verified_email": true,
		"name": "Bob Smith",
		"given_name": "Bob",
		"family_name": "Smith",
		"picture": "https://lh3.googleusercontent.com/a-/123456",
		"locale": "en",
		"hd": "redhat.com"
}
 */
	
	@GET
	@Path("/auth/tokenInfo")
	public Response tokenInfo(@QueryParam("token") String token) throws JsonGenerationException, JsonMappingException, IOException{
		log.debug("/auth/tokenInfo - getting tokenInfo using the token");
		Preconditions.checkArgument(token.length() <= 300 && token.matches("[\\.a-zA-Z0-9_-]+"), "token max length is 300 chars");
		String infoJson=OAuth.get(uri).getTokenInfo(token);// get(new StringBuilder("https://www.googleapis.com/oauth2/v1/userinfo?access_token=").append(token).toString());
		log.trace("/auth/tokenInfo - tokenInfo is: "+infoJson);
		return Response.status(200).entity(infoJson).build();
	}
	
	@GET
	@Path("/auth/callback")
	public Response authCallback(@QueryParam("error") String error, @QueryParam("code") String code, @QueryParam("state") String state) throws JsonGenerationException, JsonMappingException, IOException, URISyntaxException{
		log.debug("/auth/callback - raw response -> "+(StringUtils.isNotBlank(error)?"error="+error+"," :"")+"code="+shorten(code)+", state="+state);
		if (error!=null){
			log.info(String.format("%s ServicesPortfolioHub login-error1 %s (code=%s)", sdf.format(new Date()), error, shorten(code)));
			return Response.status(500).entity(error).build();
		}
		
		if (null==state) state="/"; // default state to home page if null
		
		OAuth oauth=OAuth.get(uri);
		String getTokenResponseJson=oauth.getToken(code);
		log.debug("/auth/callback getToken response - "+getTokenResponseJson);
		
		// Using the auth code we can now get the access_token that can be used for auth
		mjson.Json j=mjson.Json.read(getTokenResponseJson);
		if (j.has("error")){
			error="Error getting Token from Callback Code. Error="+ j.at("error").asString() +"/"+ j.at("error_description").asString();
			log.info(String.format("%s ServicesPortfolioHub login-error2 %s (code=%s)", sdf.format(new Date()), error, shorten(code)));
			return Response.status(500).entity(error).build();
		}
		Map<String, String> auth=null;
		try{
			auth=new MapBuilder<String, String>()
					.put("access_token", j.at("access_token").asString())
					.put("refresh_token", j.at("refresh_token").asString())
					.build();
			
		}catch(Exception e){
			log.error("Error in auth response getting token. no access_token or refresh_token params? "+getTokenResponseJson);
			log.info(String.format("%s ServicesPortfolioHub login-error3 %s (code=%s)", sdf.format(new Date()), error, shorten(code)));
			ChatNotification_v2.get().send(ChatEvent.onError, "App down! - likely 504 error");
//			TODO 504 auto fix
			if (getTokenResponseJson.contains("invalid_grant")){ // likely a 504 error in the making ... info here: https://stackoverflow.com/questions/10576386/invalid-grant-trying-to-get-oauth-token-from-google
				log.error("504Fix:: attempting to remove code: "+code);
				Fix504.attemptAutoFix_removeSpecificTokenOnly(oauth, code);
			}
			return Response.status(500).entity(error).build();
		}
		
		// get userInfo using the access_token
		String userInfoJson = oauth.getUserInfo(auth.get("access_token"));
		log.trace("/auth/callback getUserInfo response - "+userInfoJson);
		mjson.Json uj=mjson.Json.read(userInfoJson);
		Map<String, String> userInfo=new MapBuilder<String, String>()
				.put("id", uj.at("id").asString())
				.put("email", uj.at("email").asString())
				.put("picture", uj.at("picture").asString())
				.build();
		
		log.debug("/auth/callback - new token is " + Json.toJson(auth));
		
//		String obfuscatedToken=auth.get("refresh_token");
//		obfuscatedToken=Base64.encode(("!"+obfuscatedToken).getBytes());
		
		// now get the userinfo too
//		oauth.getUserInfo(auth.get("access_token"));
//		String userInfoJson = oauth.get(new StringBuilder("https://www.googleapis.com/oauth2/v1/userinfo?access_token=").append(auth.get("access_token")).toString());
//		mjson.Json userInfo=mjson.Json.read(userInfoJson);
		
		log.info(String.format("[GET]/auth/callback Login Success. %s", userInfo.get("email")));
		log.debug("/auth/callback - storing access/refresh tokens = "+auth.get("access_token")+" / "+auth.get("refresh_token"));
		oauth.storeRefreshToken(auth.get("access_token"), auth.get("refresh_token"));
		
		if (state.contains(" ")) state=state.replaceAll(" ","%20"); // emergency fix for state containing: Caused by: java.net.URISyntaxException: Illegal character in query at index 64: https://portfoliohub.redhat.com/v3/search-maps?salesplay=mission critical automation
//		state=URLEncoder.encode(state, "UTF-8").replace("+","%20");
		
		return Response.status(302)
//				best not to store refresh token in a cookie client-side
				.header("Set-Cookie", new CookieBuilder().name("rh_services_portfolio_access_token") .value(auth.get("access_token")).path("/").secure().sameSite(SameSite.None).build())
//				.header("Set-Cookie", new CookieBuilder().name("rh_services_portfolio_refresh_token").value(obfuscatedToken)				 .path("/").secure().sameSite(SameSite.None).build())
				.header("Set-Cookie", new CookieBuilder().name("rh_services_portfolio_user_email")	 .value(userInfo.get("email"))	 .path("/").secure().sameSite(SameSite.None).build())
				.location(new URI(state))
				.build();
	}
	
	
	// == Auth user & cache management (should require authentication) ==
	
	@GET @Path("/auth") public Response serveAuthPage() throws FileNotFoundException, IOException{ log.debug("[GET]/auth");
		return Http.newOkHtmlResponse(buildPageTemplate("/auth.html")).build();
	}
	
	@POST
	@Path("/auth/users/add")
	public Response addUsers(String pUsers) throws JsonGenerationException, JsonMappingException, IOException{
		log.info("[POST]/auth/users/add -> "+pUsers);
		List<Map<String, Object>> users=Json.toObject(pUsers, new TypeReference<List<Map<String,Object>>>(){});
		Map<String,List<String>> toAdd=Maps.newHashMap();
		for (Map<String,Object> user:users){
			if (StringUtils.isNotBlank((String)user.get("username"))){
				String username=((String)user.get("username")).trim();
				if (!username.toLowerCase().contains("@redhat.com")) username+="@redhat.com";
//				AuthFilter.databaseUserRoleMappingCache.put((String)user.get("username"), (List<String>)user.get("roles")); // add the user immediately, which serializes to disk for pod restarts
				toAdd.put(username, (List<String>)user.get("roles"));
				log.info("added user "+user.get("username")+" with roles "+Json.toJson(user.get("roles")));
				//TODO: I am not going to remove their "no role" user, but allow the AuthFilter2 to check if they have 0 roles, then reload that user, allowing us to keep a log of when they attempted to login
				//AuthFilter2.activeUserRolesCache.remove((String)user.get("username")); // need to wipe user from AuthFilter.requestedUserRolesCache too so their user can be reloaded with new ROLE info, or else they'll have none and wont be able to get in
			}
		}
		AuthFilter2.databaseUserRoleMappingCache.putAll(toAdd); // add them all at once so CacheMap only serializes once rather than on ever item addition
		return Response.ok().build();
	}
	@POST @Path("/auth/users/delete") // delete used by the auth.html page (why do we have this AND the cache delete? because the caches dont know about one another)
	public Response deleteUsers(String pUsers) throws JsonGenerationException, JsonMappingException, IOException{
		log.info("[POST]/auth/users/delete -> "+pUsers);
		List<String> users=Json.toObject(pUsers, new TypeReference<List<String>>(){});
		for (String user:users){
			AuthFilter2.databaseUserRoleMappingCache.remove(user);
			AuthFilter2.activeUserRolesCache.remove(user);
//			log.info("deleted user "+user+" from the system");
		}
		return Response.ok().build();
	}
	
	// serves any cache management page
	@GET @Path("/auth/{page}") public Response serveAdminCachePage(@PathParam("page") String page) throws FileNotFoundException, IOException{
		log.debug("[GET]/admin/"+page);
		return Http.newOkHtmlResponse(buildPageTemplate("/auth-cache.html")).build();
	}
	
	@POST @Path("/auth/cache/{cacheName}/delete") // mallen: change this to a DELETE method. this is used in the auth-cache.html page
	public Response authCacheDelete(@PathParam("cacheName") String cacheName, String pItems) throws JsonGenerationException, JsonMappingException, IOException{
		log.info(String.format("[POST]/auth/cache/%s/delete -> %s",cacheName,pItems));
		List<String> validCacheNames=Lists.newArrayList("all-user-roles","active-users");
		Preconditions.checkArgument(cacheName!=null && validCacheNames.contains(cacheName), "cache name must be in "+validCacheNames);
		
		List<String> keysToDelete=Json.toObject(pItems, new TypeReference<List<String>>(){});
		Cache<String,List<String>> cache=null;
		if ("all-user-roles".equalsIgnoreCase(cacheName)) cache=AuthFilter2.databaseUserRoleMappingCache;
		if ("active-users".equalsIgnoreCase(cacheName)) cache=AuthFilter2.activeUserRolesCache;
		for (String key:keysToDelete){
			cache.remove(key);
			log.info("deleted item "+key+" from cache "+cacheName);
		}
		return Response.ok().build();
	}
	
	@GET @Path("/auth/cache/all-user-roles") // raw data for html page rendering
	public Response showCacheUserRoleMapping(@QueryParam("token") String token) throws JsonGenerationException, JsonMappingException, IOException{
		log.debug("[GET]/auth/cache/all-user-roles");
		return Response.status(200).type("application/json").entity(Json.toJson(AuthFilter2.databaseUserRoleMappingCache)).build();
	}
	@GET @Path("/auth/cache/active-users") // raw data for html page rendering
	public Response showCacheUserRole(@QueryParam("token") String token) throws JsonGenerationException, JsonMappingException, IOException{
		log.debug("[GET]/auth/cache/active-users");
//		AuthFilter2.activeUserRolesCache.cleanupExpiredItems();
		return Response.status(200).type("application/json").entity(Json.toJson(AuthFilter2.activeUserRolesCache)).build();
	}
	
	@GET @Path("/auth/roles/{userEmail}") public Response getUserRolesFor(@PathParam("userEmail") String email) throws JsonProcessingException, ClientProtocolException, IOException {
		return Response.status(200).type("application/json").entity(Json.toJson(new AuthFilter2().getUserRoles(email))).build();
	}
	
}
