package com.redhat.cop.giveback.auth;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.redhat.cop.giveback.Config;
import com.redhat.cop.giveback.Initialization;
import com.redhat.services.portfolio.utils.Cache;
import com.redhat.services.portfolio.utils.CookieBuilder;
import com.redhat.services.portfolio.utils.CookieBuilder.SameSite;
import com.redhat.services.portfolio.utils.Json;
import com.redhat.services.portfolio.utils.MapBuilder;
import com.redhat.services.portfolio.utils.TimeUtils;

@Provider
@PreMatching
public class AuthFilter2 implements ContainerRequestFilter{
	private final SimpleDateFormat sdf=new SimpleDateFormat(Initialization.DATE_FORMAT);
	private static final Logger log=LoggerFactory.getLogger(AuthFilter2.class);
	private @Context UriInfo uri;
	private @Context SecurityContext ctx;
	
	private static final Map<String, List<String>> defaultAuthenticatedPaths=new MapBuilder<String, List<String>>()
			.put("/admin/.+", Lists.newArrayList("ADMIN")) // needs admin ROLE
//			.put("/config.*", Lists.newArrayList("ADMIN")) // needs admin ROLE
			// else unauthenticated is ok
			.build();
	private static final Map<String, List<String>> defaultUserRoleMapping=new MapBuilder<String, List<String>>()
				.put("mallen@redhat.com", Lists.newArrayList("ADMIN","ACCOUNT"))
				.put("ablock@redhat.com", Lists.newArrayList("ADMIN","ACCOUNT"))
			.build();
	
	private String getToken(ContainerRequestContext rc){
		for (Cookie c:rc.getCookies().values())
			if (c.getName().equals("rh_services_portfolio_access_token"))
				return c.getValue();
		return null;
	}
	
	private static Map<String, List<String>> authenticatedPaths=null;
	private List<String> getAuthenticatedPathRoles(String path){
		boolean print=(authenticatedPaths==null);
		String cfg=Config.get().getProperty("AUTHENTICATED_PATHS");
		if (cfg!=null && authenticatedPaths==null)
			try{ authenticatedPaths=Json.toObject(cfg, new TypeReference<Map<String,List<String>>>(){}); }catch (IOException sinkAndDeferToDefault){
				log.error("Error loading AUTHENTICATED_PATHS, falling back to default - "+sinkAndDeferToDefault.getMessage());
			}
		if (authenticatedPaths==null) authenticatedPaths=defaultAuthenticatedPaths;
		List<String> result=null;
		for(Entry<String, List<String>> e:authenticatedPaths.entrySet()){
			if (path.matches(e.getKey())){
				result=e.getValue(); break;
			}
		}
		try{if (print) log.debug("printing getAuthenticatedPathRoles() (is cached from now on) -> "+Json.toJson(authenticatedPaths));}catch(Exception e){e.printStackTrace();}
		return result;
	}
	
	//TODO: need to create a GET to display caches in admin console
	static long userRoleMappingTimeout=Long.parseLong(Config.get().getPropertySilent("auth.userrolesmapping.cache.timeout", String.valueOf(TimeUtils.sensibleStringToMs("0"))));
	public static Cache<String, List<String>> databaseUserRoleMappingCache=Cache.newCache("all-user-roles", userRoleMappingTimeout, true);
	public Map<String, List<String>> getUserRoleMapping(){
		if (databaseUserRoleMappingCache.isEmpty()/* isInitialized() */){ // first use, then load/cache them
			// load from properties
			String x;
			if ((x=Config.get().getProperty("AUTHENTICATED_USER_ROLES_DEFAULT"))!=null){ // basic list of user/roles
				try{ databaseUserRoleMappingCache.putAll(Json.toObject(x, new TypeReference<Map<String,List<String>>>(){})); }catch (IOException sinkAndDeferToDefault){
					log.error("Error loading AUTHENTICATED_USER_ROLES_DEFAULT, falling back to hard coded defaults - "+sinkAndDeferToDefault.getMessage());
				}
			}
			
			// then add from serialized user addendum?
			//	- not necessary bcuz the CacheMaps serializes as long is it's persisted to persistent storage on the deployed platform
			
			// then add to from any plugins configured
//			if ((x=Config.get().getProperty("AUTHENTICATED_USER_ROLES_PLUGIN"))!=null){ // use a plugin to get the user/roles
//				try{
//					AuthFilter2GetUsersAndRolesPlugin p=(AuthFilter2GetUsersAndRolesPlugin)Class.forName(x).newInstance();
//					databaseUserRoleMappingCache.putAll(p.getUsersAndRoles());
//				}catch (InstantiationException | IllegalAccessException | ClassNotFoundException sinkAndDeferToDefault){
//					log.error("Error loading AUTHENTICATED_USER_ROLES_PLUGIN, falling back to default - "+sinkAndDeferToDefault.getMessage());
//				}
//			}
			if (databaseUserRoleMappingCache.isEmpty()/* isInitialized() */) databaseUserRoleMappingCache.putAll(defaultUserRoleMapping); // fall back to default user/roles
			try{if (true) log.info("printing getUserRoleMapping() (is cached from now on) -> "+Json.toJson(databaseUserRoleMappingCache.get()));}catch(Exception e){e.printStackTrace();}
		}
		
		return databaseUserRoleMappingCache.get();
	}
	
	static long activeUserRolesTimeout=Long.parseLong(Config.get().getPropertySilent("auth.userroles.cache.timeout", String.valueOf(TimeUtils.sensibleStringToMs("1d"))));
	public static Cache<String, List<String>> activeUserRolesCache=Cache.newCache("active-user-roles", activeUserRolesTimeout, true);
	public /*TODO: would prefer protected, but SearchController needs this because I cant set a security context in here so it has to re-look up from the token again*/ 
	List<String> getUserRoles(String email) throws ClientProtocolException, IOException{
		if (null==email) return null; // they're unauthenticated
		boolean hasLoggedInBefore=activeUserRolesCache.get(email)!=null && List.class.isInstance(activeUserRolesCache.get(email).getClass());
//		System.out.println("expired. activeUserRolesCache.keySet()="+activeUserRolesCache.get().keySet());
		// if a) they've not logged in yet, b) it's been more than 24 hours since their last login, or c) they logged in but hadnt got any roles at the time, then try to re-load their roles
		boolean rolesSameAsBefore=!hasLoggedInBefore?false:activeUserRolesCache.get(email)!=null && CollectionUtils.isEqualCollection(getUserRoleMapping().get(email), activeUserRolesCache.get(email));
//		System.out.println(String.format("getUserRoles(): rolesSameAsBefore=%s, activeRoles=%s, dbRoles=%s",rolesSameAsBefore,activeUserRolesCache.get(email),getUserRoleMapping().get(email)));
		if (!activeUserRolesCache.get().containsKey(email) || activeUserRolesCache.hasExpired(email) || !rolesSameAsBefore){
			List<String> userRoles=getUserRoleMapping().get(email);
//			System.out.println("userrolesmapping="+getUserRoleMapping());
//			System.out.println("new userroles="+userRoles);
			if (log.isDebugEnabled()) log.debug("Adding to activeUserCache - found these roles for "+email+" -> "+userRoles);
//			if (userRoles!=null && userRoles.size()>0) {
//				activeUserRolesCache.put(email, userRoles);	
//			}
			if (userRoles==null) userRoles=Lists.newArrayList();
			activeUserRolesCache.put(email, userRoles);
//			System.out.println("activeUserRolesCache="+Json.toJson(activeUserRolesCache));
		}else { // they have a non-expired active login with >0 roles, so just use them until expiry (1d)
//			System.out.println("not expired, so it's ("+(activeUserRolesCache.get(email))+") "+activeUserRolesCache.get(email));
		}
		if (log.isDebugEnabled()) log.debug("getUserRoles for "+(email.length()>15?email.substring(0,15):email)+"... returning "+Json.toJson(activeUserRolesCache.get(email)));
		return activeUserRolesCache.get(email);
	}
	
	@Override
	public void filter(ContainerRequestContext context) {
		if (!"true".equalsIgnoreCase(Config.get().getProperty("google.oauth.enabled"))) return;
		String path=uri.getPath();
//		String preCacheKey=uri.getQueryParameters().containsKey(PreCache.preCacheKeyName) && uri.getQueryParameters().get(PreCache.preCacheKeyName).size()>=1?uri.getQueryParameters().get(PreCache.preCacheKeyName).get(0):null;
//		if (null!=preCacheKey && PreCache.preCacheKeys.contains(preCacheKey)) return;
		
		List<String> pathRolesRequired=getAuthenticatedPathRoles(path);
		if (pathRolesRequired!=null){ // not null means it requires authentication of some sort
			if (log.isDebugEnabled()) log.debug(path+" needs role(s) "+pathRolesRequired);
			try{
				OAuth oauth=OAuth.get(uri);
				path=path.contains("?")?path.substring(0,path.indexOf("?")):path; // strip the part after a ? if it exists
				
				boolean isIframe=null!=uri.getRequestUri().getQuery() && uri.getRequestUri().getQuery().toLowerCase().contains("iframe=1");
				
//				log.debug("AuthFilter:: filter():: redirecturi="+oauth.getRedirectUri());
				// change google redirect url to an https endpoint (so ocp edge termination denying non-secure traffic doesn't cause a redirect failure)
				String signInUrl=(oauth.getRedirectUri().startsWith("https:")?uri.getBaseUri().toString().replaceFirst("http:", "https:"):uri.getBaseUri().toString());
				
//				log.debug("just about to strip ["+signInUrl.substring(signInUrl.length()-1)+"] from ["+signInUrl+"].. would like to know why...");
				
				if (signInUrl.charAt(signInUrl.length()-1)=='/') signInUrl=signInUrl.substring(0,signInUrl.length()-1); // what am I stripping from the end? a slash?
				String signInPath=!isIframe?Config.get().getProperty("google.oauth.signIn"):Config.get().getProperty("google.oauth.signIn.iframe");
				if (signInPath.startsWith("http")){
					signInUrl=signInPath;
				}else
					signInUrl+=signInPath;
				
//				log.debug("AuthFilter:: filter():: signInUrl[2]="+signInUrl);
				
				// changed to using uri.requestUri() over uri.absolutePath because it includes the query parameters too, and appears to provide an absolute URL with scheme, host etc...
				String pathQuery=oauth.getRedirectUri().startsWith("https:")?uri.getRequestUri().toString().replaceFirst("http:", "https:"):uri.getRequestUri().toString();
				pathQuery=pathQuery.replace("&", "%26"); // this replaces any & characters (and prevents &amp; too, which also causes params to get lost with google oauth)
				pathQuery=new String(Base64.encodeBase64(pathQuery.getBytes()), "utf-8"); // this prevents google stripping more than the first parameter
	
				Cookie accessTokenCookie=context.getCookies().get("rh_services_portfolio_access_token");
				
				if (null!=accessTokenCookie){
					String accessToken=accessTokenCookie.getValue();
					Map<String,String> tokenInfo=oauth.getTokenInfoAsMap(accessToken);
					log.info(String.format("XXXXX %s:: tokenInfo for [%s] is %s", path, shorten(accessToken), Json.toJson(shorten(tokenInfo))));
					
					boolean tokenValid=!tokenInfo.containsKey("error");
					accessToken=accessToken.startsWith("X")?accessToken.substring(1):accessToken; // this is for testing invalid accessTokens and forcing a refresh
					
					if (!tokenValid){
						if (log.isInfoEnabled()) log.info(String.format("%s:: requires role(s) %s - invalid token found [%s], attempting a refresh...", path, pathRolesRequired, shorten(accessToken)));
						
						// TODO: 504 auto fix .... try this line, if it times out, then wipe the refresh tokens file and send them to google login
						String refreshToken=oauth.getRefreshToken(accessToken);
						// TODO: 504 investigation; changed to info level debug for now from debug
						if (log.isDebugEnabled()) log.debug(path+":: loaded refresh token from store ["+refreshToken+"], using it to get new accessToken...");
						
						String newAccessToken=null;
						boolean attemptingAutoFixOf504Error=false;
						if (refreshToken!=null){
							// TODO: 504 auto fix .... try this line, if it times out, then wipe the refresh tokens file and send them to google login
							try{
								newAccessToken=oauth.refreshToken(refreshToken);
							}catch(Exception e){
								if (log.isInfoEnabled()) log.info("ATTEMPTING AUTOMATIC FIX OF 504 ERROR! DID THIS WORK?");
								oauth.removeRefreshToken(refreshToken);
								newAccessToken=null;
								attemptingAutoFixOf504Error=true;
							}
						}
				
						String pathQueryDecoded=new String(pathQuery);
						if (null!=newAccessToken){
							pathQuery=new String(Base64.decodeBase64(pathQuery));
							// TODO: 504 investigation; changed to info level debug for now from debug
							if (log.isInfoEnabled()) log.info(String.format("%s:: refreshed, redirecting back to %s with new cookie rh_services_portfolio_access_token = %s%s", path, pathQueryDecoded, shorten(newAccessToken), attemptingAutoFixOf504Error?" (after attempted fix for 504 error)":""));
//							if (attemptingAutoFixOf504Error && log.isInfoEnabled()) log.info("");
							// can't set a cookie in an interceptor (even with context.getHeaders().add) .. so aborting and redirecting to get desired effect
							oauth.storeRefreshToken(newAccessToken, refreshToken);
							oauth.removeRefreshToken(accessToken);
							context.abortWith(Response.status(302).location(new URI(pathQuery))
									.header("Set-Cookie", new CookieBuilder().name("rh_services_portfolio_access_token").value(newAccessToken).path("/").sameSite(SameSite.Lax).build())
									.build());
						}else{ // failure to refresh
							// TODO: 504 investigation; changed to info level debug for now from debug
							log.info(String.format("%s:: failure to refresh sending user back to signin (signinUrl=%s, returnUrl=%s%s", path, signInUrl, pathQueryDecoded, (attemptingAutoFixOf504Error?", after attempted fix for 504 error)":")")));
							context.abortWith(Response.status(302).location(new URI(signInUrl+"?returnUrl="+pathQuery)).build());
							return;
						}
							
					}else{
						log.trace(String.format("%s:: requires role(s) %s - valid accessToken found [%s]",path,pathRolesRequired,shorten(accessToken)));
						context.setProperty("access_token", accessToken);
						String email=tokenInfo.get("email");
						List<String> userRoles=getUserRoles(email);
						
						// user=[], page=[] = good // auth'd only, no role requirement for page
						// user=[], page=[ADMIN] = bad // not got the required role
						// user=null, page=[] or [ADMIN] = bad // unathenticated
						// user=[ADMIN], page=[] = good // user has role
						
						String method=context.getMethod();
						if (userRoles==null){ // user is unauthenticated
	//										log.info(path+":: failure. user unauthenticated. does not have the page ROLE requirement (userRoles="+userRoles+", pageRoles "+pathRolesRequired+")");
							logInfo(String.format("%s::%s:%s::access=DENY; user unauthenticated. (userRoles=%s, pageNeeds=%s)",path,email,shorten(accessToken,4,5), userRoles,pathRolesRequired));
							context.abortWith(Response.status(302).location(new URI("/error401?error=user-roles")).build());
							return;
	
						}else{
							//	FYI, given page roles ["ADMIN","AUTH"], and user roles ["AUTH"], then disjoint returns false (ie. they dont need ALL the roles)
							boolean x=Collections.disjoint(pathRolesRequired, userRoles); // used to return 'true' if the two specified collections have no elements in common
							log.debug(String.format("[%s]%s::%s path.roles=%s, user.roles=%s, disjoint=%s (true if roles dont match)",method,path,email,Json.toJson(pathRolesRequired),Json.toJson(userRoles), x));
							if (pathRolesRequired.size()>0 && Collections.disjoint(pathRolesRequired, userRoles)){
								logInfo(String.format("[%s]%s::%s:%s::access=DENY; page Role requirement failure; (userRoles=%s, pageNeeds=%s)",method,path,email,shorten(accessToken,4,5), userRoles,pathRolesRequired));
//								context.abortWith(Response.status(302).location(new URI("/error401?error=user-permissions")).build());
								URI response=uri.getBaseUriBuilder().scheme("https").path("/error401").build();
								context.abortWith(Response.status(302).location(response).build());
								return;
							}
							
							if (pathRolesRequired!=null && pathRolesRequired.size()==0 && userRoles!=null) 
								logInfo(String.format("[%s]%s::%s:%s::access=ALLOW; requires only authentication; no roles check done",method,path,email,shorten(accessToken,4,5)));
							if (pathRolesRequired!=null && pathRolesRequired.size()>0){
								if (!x){
									logInfo(String.format("[%s]%s::%s:%s::access=ALLOW; requires %s; User has %s",method,path,email,shorten(accessToken,4,5),pathRolesRequired,userRoles));
								}else{
									logInfo(String.format("[%s]%s::%s:%s::access=DENY; requires role(s) %s; User role(s) %s; User DID NOT MATCH roles",method,path,email,shorten(accessToken,4,5),pathRolesRequired,userRoles));
								}
							}
							
						}
						
					}
				}else{
					// BUG: known issue were the /signin redirect always goes to http when openshift security is edge terminated
					log.debug(path+":: accessToken not found at all (redirecting to signinUrl="+signInUrl+", returnUrl is "+pathQuery+")");
					context.abortWith(Response.status(302).location(new URI(signInUrl+"?returnUrl="+pathQuery)).build());
	//							erm... why does this redirect to http rather than the original https call??? huh?? - because OpenShift is edge terminating, the app never sees https at all
					return;
				}
				
			}catch(Exception e){
				e.printStackTrace();
				throw new RuntimeException("Failure to redirect to authenticate", e);
			}
		}else{
	//					System.out.println("NO AUTH NEEDED = "+path);
			log.trace(path+":: does not require authentication");
		}
	}
	
	private void logInfo(String msg){
		boolean isPageDataRequestOnly=msg.contains("/_"); // not nice bcuz the message MUST have the page injected for this to work, but for example /admin/_cache/all-user-roles vs the page request of /admin/cache/all-user-roles, so we dont need to log those too
		boolean isNotices=msg.contains("/notices");// is just the notices request, so no need to log this
		if (!isPageDataRequestOnly && !isNotices){
			log.info(msg);
		}else{ // the suppress the debug to make the logs cleaner/readable
			log.debug(msg);
		}
	}
	
	public static String shorten(String token){
		return shorten(token,7,6,"...");
	}
	public static String shorten(String token, int startLen, int endLen){
		return shorten(token,startLen,endLen,"...");
	}
	private static String shorten(String token, int startLen, int endLen, String middleChars){
		if (token==null) return token;
		String start=token.length()>startLen?token.substring(0,startLen):token;
		String end=token.length()>endLen?token.substring(token.length()-endLen):token;
		return start+middleChars+end;
	}
	private Map<String,String> shorten(Map<String,String> tokenInfo){
		return shorten(tokenInfo, "issued_to", "audience", "verified_email", "access_type");
	}
	private Map<String,String> shorten(Map<String,String> tokenInfo, String... keys){
		for(String k:keys) tokenInfo.remove(k);
		return tokenInfo;
	}
	
}
