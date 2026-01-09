package com.redhat.services.portfolio.utils;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Json{

	
//	// Jackson Implementation
//	
	public static <T> T toObject(InputStream is, Class<T> cls) throws JsonParseException, JsonMappingException, IOException{
		return newObjectMapper(true).readValue(is, cls);
	}
	public static <T> T toObject(InputStream is, TypeReference<T> tr) throws JsonParseException, JsonMappingException, IOException{
		return newObjectMapper(true).readValue(is, tr);
	}
	public static <T> T toObject(String s, Class<T> cls) throws JsonParseException, JsonMappingException, IOException{
		return newObjectMapper(true).readValue(s, cls);
	}
	public static <T> T toObject(String s, TypeReference<T> tr) throws JsonParseException, JsonMappingException, IOException{
		return newObjectMapper(true).readValue(s, tr);
	}
	public static <T> String toJson(T o) throws JsonProcessingException{
		return newObjectMapper(true).writeValueAsString(o);
	}
	public static <T> String toJson(T o, boolean pretty) throws JsonProcessingException{
		return newObjectMapper(pretty).writeValueAsString(o);
	}
  public static ObjectMapper newObjectMapper(boolean pretty){
    ObjectMapper mapper = pretty?new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
    		:new ObjectMapper()
    		;
    		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS).setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
    		mapper.setSerializationInclusion(Include.NON_NULL);
//    mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT,pretty);
//    return mapper.writerWithDefaultPrettyPrinter();
    return mapper;
  }
	public static <T> String toJsonSafe(T o){
		try{
			return newObjectMapper(true).writeValueAsString(o);
		}catch(JsonProcessingException e){
			e.printStackTrace();
			return "ERROR - "+e.getMessage();
		}
	}  
  
  
//  private ObjectMapper m;
//  private static Json j;
//  public static Json mapper(){
//  	j=new Json();
//  	j.m=new ObjectMapper();
//  	j.m.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
//  	j.m.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS).setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
//  	return j;
//  }
//  public static Json pretty(){
//  	j.m.enable(SerializationFeature.INDENT_OUTPUT); return j;
//  }
//  public static Json ignoreNull(){
//  	j.m.setSerializationInclusion(Include.NON_NULL);
//  	return j;
//  }
//  public <T> T toObject2(String s, Class<T> cls) throws JsonMappingException, JsonProcessingException{
//  	return m.readValue(s, cls);
//  }  
//  public <T> String toJson2(T o) throws JsonProcessingException{
//  	return m.writeValueAsString(o);
//  }  
  
  // JsonB Implementation
  
//	public static <T> T toObject(String s, Class<T> cls) throws JsonParseException, JsonMappingException, IOException{
//		return newJsonb().fromJson(s, cls);
//	}
//	public static <T> String toJson(T o) throws JsonProcessingException{
//		return newJsonb().toJson(o);
//	}
//	public static Jsonb newJsonb(){
//		JsonbConfig cfg=new JsonbConfig();
//		cfg.withFormatting(true); // pretty print
//		cfg.withPropertyVisibilityStrategy(new PropertyVisibilityStrategy(){ // allows for pojo's without setters (ie. immutable) to be deserialized
//			@Override public boolean isVisible(Method method){ return true; }
//			@Override public boolean isVisible(Field field){ return true; }
//		});
//		return JsonbBuilder.create(cfg);
//	}
  
}
