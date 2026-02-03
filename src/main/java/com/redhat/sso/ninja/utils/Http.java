package com.redhat.sso.ninja.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;


public class Http{
	private static final Logger log = Logger.getLogger(Http.class);
	public static boolean loggingEnabled=true;
	
	public static class Response{
		public Response(int responseCode, String response){
			this.responseCode=responseCode;
			this.response=response;
		}
		public int responseCode;
		public String response;
		public int getResponseCode(){
			return responseCode;
		}
		public String getString(){
			return response;
		}
	}
	
  public static ResponseBuilder newResponse(int status, String contentType, String entity){
    String nonce="";boolean addNonce=false;
    if (addNonce){
      nonce=RandomStringUtils.random(10, 0, 10, true, true, "ABCDEFGHIJKLMOPQRSTUVWXYZ1234567890".toCharArray(), new SecureRandom());
      if (null!=entity)
        entity=entity.replace("$NONCE", nonce);
    }
    
    return jakarta.ws.rs.core.Response.status(status)
        .entity(entity)
       .header("Access-Control-Allow-Origin",  "*")
       .header("Content-Type",contentType)
       .header("Cache-Control", "no-store, must-revalidate, no-cache, max-age=0")
       .header("Pragma", "no-cache")
//       .header("Content-Security-Policy", "default-src 'self' data: 'unsafe-inline' "+(addNonce?"'nonce-"+nonce+"'":"")+" www.redhat.com http://cdn.datatables.net https://cdn.jsdelivr.net https://cdnjs.cloudflare.com http://bartaz.github.io https://lh3.googleusercontent.com")
       .header("X-Content-Type-Options", "nosniff");
  }
  @Deprecated public static ResponseBuilder newOkHtmlResponse(){ // deprecated because it skips script security header info
    return newResponse(200, "text/html; charset=UTF-8", null);
  }
	
	public static Response get(String url){
		return http("GET", url, null, null);
	}
	public static Response get(String url, Map<String,String> headers){
		return http("GET", url, null, headers);
	}
	
	public static Response post(String url, String data){
		return http("POST", url, data, null);
	}
	public static Response post(String url, String data, Map<String,String> headers){
		return http("POST", url, data, headers);
	}
	
	public static synchronized Response http(String method, String url, String data, Map<String,String> headers){
		try {
//			log.info("Http call '"+method+"' to '"+url+"'"+(null!=data?" (with data length of "+data.length()+" characters)":""));
			URL obj=new URL(url);
			HttpURLConnection cnn=(HttpURLConnection)obj.openConnection();
			cnn.setRequestMethod(method.toUpperCase());
			
			if (headers!=null){
				for(Entry<String, String> e:headers.entrySet()){
					cnn.setRequestProperty(e.getKey(), e.getValue());
				}
			}
			
			if ("POST".equalsIgnoreCase(method) && null!=data){
				cnn.setDoOutput(true);
				OutputStream os = cnn.getOutputStream();
        os.write(data.getBytes());
        os.flush();
			}
			
			Response response=buildResponse(cnn);
//			log.info("Http call responded with code: "+response.responseCode);
			
			
			if (loggingEnabled) log.debug("Http call '"+method+"' to '"+url+"'"+(null!=data?" (with data length of "+data.length()+" characters)":"")+" - ResponseCode: "+response.responseCode);
			
			cnn.disconnect();
			return response;
		}catch(IOException e) {
//			return new Response(999, null);
			log.error("Failure to make call '"+method+"' to '"+url+"'"+(null!=data?" (with data length of "+data.length()+" characters)":""));
			log.error("Http library mis-handled the http response most likely - see exception message: "+ e.getMessage());
			e.printStackTrace();
			return new Response(504, "Connection Timeout");
//			throw new RuntimeException("Http library mis-handled the http response most likely - see exception", e);
		}
	}
	
	private static Response buildResponse(HttpURLConnection cnn) throws IOException{
		int responseCode=cnn.getResponseCode();
		StringBuffer response=new StringBuffer();
		if (200 == responseCode){
			BufferedReader in=new BufferedReader(new InputStreamReader(cnn.getInputStream()));
			String inputLine;
			while ((inputLine=in.readLine()) != null)
				response.append(inputLine);
			in.close();
		}
		return new Response(responseCode, response.toString());
	}
}
