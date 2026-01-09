package mjson;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Json{
	private JsonNode n;
	public Json(JsonNode n){this.n=n;}
	public static Json read(String json) throws JsonMappingException, JsonProcessingException{
		final ObjectMapper m=new ObjectMapper();
		return new Json(m.readTree(json));
	}
	public boolean has(String fieldName){
		return n.has(fieldName);
	}
	public Json at(String fieldName){
		return new Json(n.at("/"+fieldName));
	}
	public <T> Json set(String fieldName, List<T> values){
		ObjectNode o=(ObjectNode)n;
		o.set(fieldName, createArrayNode(new ObjectMapper(), values));
		return this;
	}
	private <T extends Object> ArrayNode createArrayNode(ObjectMapper m, List<T> values){
		ArrayNode v=m.getNodeFactory().arrayNode(values.size());
		Class<?> type=Object.class; if (values.size()>0) type=values.get(0).getClass();
		for (T x:values){
			if (Integer.class.isAssignableFrom(type)) v.add((Integer)x);
			if (String.class.isAssignableFrom(type))  v.add((String)x);
			if (Boolean.class.isAssignableFrom(type)) v.add((Boolean)x);
			if (Double.class.isAssignableFrom(type))  v.add((Double)x);
			if (Float.class.isAssignableFrom(type))   v.add((Float)x);
			if (Long.class.isAssignableFrom(type))    v.add((Long)x);
		}
		if (v.size()!=values.size()) throw new RuntimeException("mjson.Json.set() trying to set an object type that's unsupported. please add the implementation here");
		return v;
	}
	public String asString(){
		return n.textValue();
	}
	public Integer asInteger(){
		return n.intValue();
	}
	public String toString(){ // overloads to return the json payload represented by this Json object
		return n.toPrettyString();
	}
}