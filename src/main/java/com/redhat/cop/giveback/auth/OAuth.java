package com.redhat.cop.giveback.auth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.ws.rs.core.UriInfo;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jboss.logging.Logger;
//import org.openjdk.jmh.annotations.*;
//import org.openjdk.jmh.runner.*;
//import org.openjdk.jmh.runner.options.*;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.redhat.cop.giveback.Config;
import com.redhat.services.portfolio.utils.CacheSimple;
import com.redhat.services.portfolio.utils.Json;
import com.redhat.services.portfolio.utils.MapBuilder;

import static com.redhat.cop.giveback.auth.AuthFilter2.shorten;
import static org.jboss.logging.Logger.Level.DEBUG;
import static org.jboss.logging.Logger.Level.ERROR;
import static org.jboss.logging.Logger.Level.WARN;

//@Fork(1)
//@State(Scope.Benchmark)
//@OutputTimeUnit(TimeUnit.MILLISECONDS)
//@Measurement(iterations = 10)
//@Warmup(iterations = 10)
//@BenchmarkMode(Mode.Throughput) // AverageTime
public class OAuth{
	private static final Logger log=Logger.getLogger(OAuth.class);
	protected String clientId;
	protected String secret;
	protected String redirectUri; public String getRedirectUri(){ return redirectUri; }
	private CacheSimple<String,String> tokenCache=new CacheSimple.Builder<String,String>().name("token-cache").timeout("10s").build();
	private CacheSimple<String,String> userCache=new CacheSimple.Builder<String,String>().name("user-cache").timeout("10s").build();
	
	public enum Urls{
		USER_INFO /*GET*/ ("https://www.googleapis.com/oauth2/v1/userinfo?access_token=%s"),
		TOKEN_INFO/*GET*/ ("https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=%s"),
		GET_TOKEN /*POST*/("https://accounts.google.com/o/oauth2/token"), // with json payload params: code, client_id, client_secret, redirect_uri, grant_type
		AUTH      /*POST*/("https://accounts.google.com/o/oauth2/auth");  // with querystring params added on
		protected String url; private Urls(String url){this.url=url;}
//		@Benchmark
		protected String build(Object... args){return args==null?url:String.format(url, args);}
	}
	
//	public static void main(String[] args) throws IOException, RunnerException {
////		org.openjdk.jmh.Main.main(args);
//    Options options = new OptionsBuilder()
//        .include(OAuth.class.getSimpleName())
//        .build();
//    new Runner(options).run();
//	}
	
	private OAuth(){}
	/**
	 * @param clientId - google auth console value
	 * @param secret - google auth console value
	 * @param redirectUri - uri that's configured in google auth admin console as the return url. usually .../callback
	 */
	public static class OAuthBuilder extends OAuth{
		public OAuthBuilder clientId(String clientId){this.clientId=clientId;return this;}
		public OAuthBuilder secret(String secret){this.secret=secret;return this;}
		public OAuthBuilder redirectUri(String redirectUrl){this.redirectUri=redirectUrl;return this;}
		public OAuth build(){
			Preconditions.checkArgument(clientId!=null || secret!=null || redirectUri!=null, String.format("Google OAuth requires a clientId[%s], secret[%s] & redirectUri[%s]. Please check your environment variables", clientId, secret, redirectUri));
			OAuth r=new OAuth();
			r.clientId=clientId;
			r.secret=secret;
			r.redirectUri=redirectUri;
			log.debug(String.format("New OAuth built: [clientId=%s, secret=%s, redirectUri=%s]",r.clientId, shorten(r.secret), r.redirectUri));
			return r;
		}
	}
	
	private static OAuth _oauth;
	private static OAuth get(){ /** Used for local testing only */
		if (null==_oauth){
		_oauth=new OAuth.OAuthBuilder()
				.clientId   (Config.get().getProperty("google.oauth.clientId"))
				.secret     (Config.get().getProperty("google.oauth.secret"))
				.redirectUri(Config.get().getProperty("google.oauth.redirectUri"))
				.build();
		}
		return _oauth;
	}
	public static OAuth get(UriInfo uri){
		if (null==_oauth){
			String redirectUri=null;
			if (StringUtils.isNotEmpty(Config.get().getProperty("google.oauth.redirectUri"))){
				redirectUri=Config.get().getProperty("google.oauth.redirectUri");
			}else if ("localhost".equals(uri.getRequestUri().getHost().toLowerCase())){
				redirectUri=uri.getRequestUri().getScheme()+"://"+uri.getRequestUri().getHost()+(-1!=uri.getRequestUri().getPort()?":"+uri.getRequestUri().getPort():"")+"/auth/callback";
			}else{
				redirectUri="https://"+uri.getRequestUri().getHost()+(-1!=uri.getRequestUri().getPort()?":"+uri.getRequestUri().getPort():"")+"/auth/callback";
			}
			_oauth=new OAuth.OAuthBuilder()
					.clientId   (Config.get().getProperty("google.oauth.clientId"))
					.secret     (Config.get().getProperty("google.oauth.secret"))
					.redirectUri(redirectUri)
					.build();
		}
		return _oauth;
	}
	
	public String getToken(String callbackCode) throws ClientProtocolException, IOException{
		// go get the token from google using the code
	  String tokenInfo=httpPost(/*https://accounts.google.com/o/oauth2/token*/Urls.GET_TOKEN, ImmutableMap.<String,String>builder()
			     .put("code", callbackCode)
			     .put("client_id", clientId)
			     .put("client_secret", secret)
			     .put("redirect_uri", redirectUri)
			     .put("grant_type", "authorization_code").build());
	  log.debug(String.format("/getToken: POST %s. Params include code, client_id, client_secret, redirect_uri, grant_type. Response is %s",Urls.GET_TOKEN.build(),tokenInfo));
	  return tokenInfo;
	}
	public String refreshToken(String refreshToken) throws ClientProtocolException, IOException{
		if (null==refreshToken) return null;
	  String tokenInfo=httpPost(/*https://accounts.google.com/o/oauth2/token*/Urls.GET_TOKEN, ImmutableMap.<String,String>builder()
//		     .put("code", code)
		     .put("refresh_token", refreshToken)
		     .put("client_id", clientId)
		     .put("client_secret", secret)
//		     .put("redirect_uri", getRedirectUri(uri))
		     .put("grant_type", "refresh_token").build());
	  
		mjson.Json j=mjson.Json.read(tokenInfo);
		log.log(j.has("access_token")?DEBUG:WARN,String.format("/refreshToken: POST %s. Params include refresh_token, client_id, client_secret, grant_type. Response is %s",Urls.GET_TOKEN.build(),tokenInfo));
		if (j.has("access_token"))
			return j.at("access_token").asString();
	  
		return null; // denoting failure to refresh
//		throw new RuntimeException("Unable to generate access token from refresh token");
	}
	
	public static void main(String[] args) throws JsonMappingException, JsonProcessingException {
		String json="{\"access_token\":\"12345\"}";
		mjson.Json test=mjson.Json.read(json);
		System.out.println("mjson.Json.read().at() returns an mjson.Json object = "+mjson.Json.class.isAssignableFrom(test.at("access_token").getClass()));
		System.out.println("mjson.Json.read().at(\"access_token\").asString() returns 12345 = "+ "12345".equals(test.at("access_token").asString()));
		System.out.println("mjson.Json.read().has(\"access_token\") = "+ test.has("access_token"));
		
		String json2="{\"response\":{\"access_token\":\"12345\", \"error\":\"\"}}";
		mjson.Json test2=mjson.Json.read(json2);
		System.out.println("mjson.Json.read().has(\"response\") = "+ test2.has("response"));
		System.out.println("mjson.Json.read().at(\"response\").has(\"access_token\") = "+ test2.at("response").has("access_token"));
		System.out.println("mjson.Json.read().at(\"response\").at(\"access_token\").asString() returns 12345 = "+ "12345".equals(test2.at("response").at("access_token").asString()));
		System.out.println("test2.toString() = "+test2.toString());
	}
	
	public String getUserInfo(String token) throws ClientProtocolException, IOException{
		if (null==token) Preconditions.checkArgument(token!=null, "Token provided to Oauth.getUserInfo cannot be null");
		if (userCache.containsKey(token)) return userCache.get(token);
		String userInfo=httpGet(Urls.USER_INFO.build(token));
		log.log(userInfo.contains("error")?ERROR:DEBUG, String.format("/getUserInfo: GET %s. Response is %s", Urls.USER_INFO.build(shorten(token)), userInfo));
		if (!userInfo.contains("error")) userCache.put(token, userInfo);
		return userInfo;
	}

  public String getTokenInfo(String token) throws ClientProtocolException, IOException{
    if (tokenCache.containsKey(token)) return tokenCache.get(token);
    String tokenInfo=httpGet(Urls.TOKEN_INFO.build(token));
    log.log(tokenInfo.contains("error") && !tokenInfo.contains("invalid_token")?WARN:DEBUG, String.format("/getTokenInfo: GET %s. Response is %s", Urls.TOKEN_INFO.build(shorten(token)), tokenInfo)); // "'error':'invalid value'" comes back a lot, but seems to only mean it's expired, not something actually invalid 
    if (!tokenInfo.contains("error")) tokenCache.put(token, tokenInfo);
    return tokenInfo;
  }
	public Map<String,String> getTokenInfoAsMap(String token) throws ClientProtocolException, IOException{
  	return Json.toObject(getTokenInfo(token), new TypeReference<Map<String,String>>(){});
	}
	
	public boolean checkToken(String token) throws ClientProtocolException, IOException{
		log.debug("/checkToken - getting tokenInfo using the token "+shorten(token));
		String tokenInfo=tokenCache.containsKey(token)?tokenCache.get(token):httpGet(Urls.TOKEN_INFO.build(token));//get(new StringBuilder("https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=").append(token).toString());
  	mjson.Json j=mjson.Json.read(tokenInfo);
  	log.log(j.has("error")?WARN:DEBUG, String.format("/checkToken: GET %s. Response is %s", Urls.TOKEN_INFO.build(shorten(token)), tokenInfo));
  	return !j.has("error");
	}
	
	public String getReturnUrl(UriInfo uriInfo, String uri){
		return uriInfo.getBaseUri().getScheme()+"://"+uriInfo.getBaseUri().getHost()+":"+uriInfo.getBaseUri().getPort()+uri;
	}
	
	public String generateAuthUrl(String redirectUri, String state){
//		state+="&utm_source=test";
		state=state.replace("&", "&amp;");
		StringBuilder oauthUrl=new StringBuilder().append(/*https://accounts.google.com/o/oauth2/auth*/Urls.AUTH.build()).append("?client_id=").append(clientId) // the client id from the api console
				.append("&response_type=code").append("&scope=openid%20email") // scope is the api permissions we are requesting
				.append("&redirect_uri=" + redirectUri) // the servlet that google redirects to after
				.append("&state=" + state)  // this_can_be_anything_to_help_correlate_the_response%3Dlike_session_id
				.append("&access_type=offline")                // here we are asking to access to user's data while they are not signed in
				.append("&approval_prompt=force");             // this requires them to verify which account to use, if they are already signed in
		
				// add utm params?
//		oauthUrl.append("&utm_source=test");
		
//		System.out.println("XXX OAuth.generateAuthUrl == "+oauthUrl.toString());
		return oauthUrl.toString();
	}

	private String httpGet(String url) throws ClientProtocolException, IOException{
		return execute(new HttpGet(url));
	}

	private String httpPost(Urls url, Map<String, String> formParams) throws ClientProtocolException, IOException{
		return httpPost(url.build(), formParams);
	}
	private String httpPost(String url, Map<String, String> formParams) throws ClientProtocolException, IOException{
		HttpPost request=new HttpPost(url);
		List<NameValuePair> nvps=new ArrayList<NameValuePair>();
		for (String key:formParams.keySet())
			nvps.add(new BasicNameValuePair(key, formParams.get(key)));
		request.setEntity(new UrlEncodedFormEntity(nvps));
		return execute(request);
	}

	private String execute(HttpRequestBase request) throws ClientProtocolException, IOException{
		HttpClient httpClient=HttpClientBuilder.create().build();
		HttpResponse response=httpClient.execute(request);
		HttpEntity entity=response.getEntity();
		String body=EntityUtils.toString(entity);
		return body;
	}
	
//	static long msPerYear = 60000  * 60      * 24      * 7         * 52;
//	static long msPerWeek = 60000  * 60      * 24      * 7;

  //                          ms/min x mins/hr x hrs/day x days/week x wks/year
	static long msPerDay      = 60000  * 60      * 24l;
	static long msPerWeek     = msPerDay                   * 7l;
	static long msPerFortnite = msPerDay                   * 7 * 2l;
	static long msPerYear     = msPerWeek                              * 52l;
	
	private static long maxRefreshTokens=1000l;
	private static long maxRefreshTokenAge=msPerFortnite;
	
	// refresh token storage
	private static File refreshTokenStorage=new File(Config.get().getStorageRoot(), "refreshTokens.json");
	public static boolean resetRefreshTokens(){
		String date=new SimpleDateFormat("yyMMdd").format(new Date());
		try{
			if (Files.exists(Paths.get(refreshTokenStorage.getPath()))){
				File newFile=new File(refreshTokenStorage.getAbsolutePath()+date);
				Files.move(Paths.get(refreshTokenStorage.getPath()), Path.of(newFile.getPath()), StandardCopyOption.REPLACE_EXISTING);
				log.warn("Moved refresh tokens file to: "+newFile.getAbsolutePath());
			}else{
				log.warn("refresh tokens file doesnt exist, so not attempting to move it");
			}
		}catch(IOException e){
			log.error("Unable to move refresh tokens file: "+e.getMessage(), e);
		}
		return Files.notExists(Path.of(refreshTokenStorage.getAbsolutePath()));
	}
	private Map<String,Map<String,String>> loadRefreshTokens(){
		if (!refreshTokenStorage.exists()) return new LinkedHashMap<String, Map<String,String>>();
		try{
			String storage=IOUtils.toString(new FileInputStream(refreshTokenStorage), "UTF-8");
			return Json.toObject(storage, new TypeReference<Map<String,Map<String,String>>>(){});
		}catch(Exception e){
			// assume its broken and return a fresh storage
			return new LinkedHashMap<String, Map<String,String>>();
		}
	}
	
	private Map<String, Map<String,String>> refreshTokenHousekeeping(Map<String, Map<String,String>> refreshTokens) throws JsonProcessingException, FileNotFoundException, IOException{
		List<String> removeList=Lists.newArrayList();
		// no longer than a list of 1000
		List<String> keyList=new ArrayList<String>(refreshTokens.keySet());
		Collections.reverse(keyList);
		while (keyList.size()>maxRefreshTokens)
			refreshTokens.remove(keyList.iterator().next());
		
		// nothing older than a year
		for (Entry<String, Map<String, String>> e:refreshTokens.entrySet()){
			String ts=e.getValue().get("ts");
			if ((Long.parseLong(ts)+maxRefreshTokenAge)<System.currentTimeMillis()){
				// it's too old so remove it
				removeList.add(e.getKey());
			}
		}
		return refreshTokens;
	}
	public String getRefreshToken(String oldAccessToken) throws JsonParseException, JsonMappingException, FileNotFoundException, IOException{
		Map<String, Map<String,String>> refreshTokens=loadRefreshTokens();
		String result=refreshTokens.containsKey(oldAccessToken)?refreshTokens.get(oldAccessToken).get("rt"):null;
		log.log(result==null?WARN:DEBUG,String.format("Refresh token"+(result==null?" NOT":"")+" found in the store for accessToken (accessToken=%s, storage=%s)", shorten(oldAccessToken), refreshTokenStorage.getAbsolutePath()));
		return result;
	}
	public void saveRefreshTokens(Map<String, Map<String,String>> refreshTokens) throws JsonProcessingException, FileNotFoundException, IOException{
		refreshTokens=refreshTokenHousekeeping(refreshTokens);
		refreshTokenStorage.getParentFile().mkdirs();
		try{
			IOUtils.write(Json.toJson(refreshTokens), new FileOutputStream(refreshTokenStorage), "UTF-8");
		}catch(JsonMappingException e){
			// added as a result of null key being entered into refresh tokens file - 2025-09-08 14:39:54,161 ERROR [org.jbo.res.plu.pro.jackson] (executor-thread-1894) RESTEASY-JACKSON000100: Not able to deserialize data provided: com.fasterxml.jackson.databind.JsonMappingException: Null key for a Map not allowed in JSON (use a converting NullKeySerializer?) (through reference chain: java.util.LinkedHashMap["null"])
			resetRefreshTokens(); // 
		}
	}
	public void removeRefreshToken(String oldAccessToken) throws JsonProcessingException, FileNotFoundException, IOException{
		Map<String, Map<String,String>> refreshTokens=loadRefreshTokens();
		log.info("Oauth:: removing oldRefreshToken from refresh tokens file: "+shorten(oldAccessToken));
		refreshTokens.remove(oldAccessToken);
		saveRefreshTokens(refreshTokens);
	}
	public void storeRefreshToken(String accessToken, String refreshToken) throws JsonParseException, JsonMappingException, FileNotFoundException, IOException{
		Map<String, Map<String,String>> refreshTokens=loadRefreshTokens();
		if (refreshToken!=null) {
  			Map<String,String> toStore=new MapBuilder<String,String>()
				.put("ts", ""+System.currentTimeMillis())
				.put("rt", refreshToken)
				.build();
  			refreshTokens.put(accessToken, toStore);
  			saveRefreshTokens(refreshTokens);
		}
	}
	
	
}
