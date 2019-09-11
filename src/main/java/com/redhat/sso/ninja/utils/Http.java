package com.redhat.sso.ninja.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

public class Http{
	private static final Logger log = LogManager.getLogger(Http.class);
	
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
	
	public static Response get(String url){
		return http("GET", url, null, null);
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
			
			
			log.info("Http call '"+method+"' to '"+url+"'"+(null!=data?" (with data length of "+data.length()+" characters)":"")+" - ResponseCode: "+response.responseCode);
			
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
