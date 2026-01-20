package com.redhat.cop.giveback.google;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;

public final class GooglePredicates{

	public static class Has extends MapHasKeyValue{
		public Has(String key, String value){
			super(key, value);
		}}
	public static class MapHasKeyValue implements Predicate<Map<String,String>>{
		private String key;
		private String value;
		public MapHasKeyValue(String key, String value){
			this.key=key; this.value=value;
		}
		@Override public boolean apply(Map<String, String> input){
			return input!=null && input.containsKey(key) && input.get(key).equals(value);
		}
	}
	// usage example: http://localhost:8083/api/integrations/v1/portfolio/products?keys=Type,Pillar,Name&format=json&filters=HasTypes(Journey,Solution,Labs,Support);HasStates(Geo)
	public abstract static class HasFieldValuesGeneric implements Predicate<Map<String,String>>{
		private List<String> values;
		public abstract String getFieldName();
		public HasFieldValuesGeneric(String values) {
			this.values=Arrays.asList(values.toLowerCase().split(","));
		}
		@Override public boolean apply(Map<String, String> input){
			return input!=null &&
					(input.containsKey(getFieldName()) && input.get(getFieldName())!=null && values.contains(input.get(getFieldName()).toLowerCase())
					||
					input.containsKey(getFieldName().toLowerCase()) && input.get(getFieldName().toLowerCase())!=null && values.contains(input.get(getFieldName().toLowerCase()).toLowerCase()))
					;
		}
	}
	
	public static class NotMapRedirect implements Predicate<Map<String,String>>{
		@Override public boolean apply(Map<String, String> input){
			//Section = Page && SubSection != "" then it's a redirect for maps
			return !(
					input!=null && 
					input.containsKey("Section")    && input.get("Section")!=null    && "Page".equalsIgnoreCase(input.get("Section")) &&
					input.containsKey("SubSection") && input.get("SubSection")!=null && input.get("SubSection").length()>0
					);
		}
	}
	
	public static class HasTypes extends HasFieldValuesGeneric{ public HasTypes(String values){super(values);}
		public String getFieldName(){return "Type";}
	}
	public static class HasStates extends HasFieldValuesGeneric{ public HasStates(String values){super(values);}
		public String getFieldName(){return "State";}
	}
	public static class NotHasTypes extends HasFieldValuesGeneric{ public NotHasTypes(String values){super(values);}
	public String getFieldName(){return "Type";}
		@Override public boolean apply(Map<String, String> input){ return !super.apply(input); }
	}
	public static class NotHasStates extends HasFieldValuesGeneric{ public NotHasStates(String values){super(values);}
	public String getFieldName(){return "State";}
		@Override public boolean apply(Map<String, String> input){ return !super.apply(input); }
	}
	
	// usage example: http://localhost:8083/api/integrations/v1/portfolio/products?keys=Type,Pillar,Name&format=json&filters=HasFieldValues(Type,Journey,Solution,Labs,Support)
	public static class HasFieldValues implements Predicate<Map<String,String>>{
		private String field;
		private List<String> values;
		public HasFieldValues(String params){
			values=Lists.newArrayList(Splitter.on(",").split(params).iterator());
			field=values.remove(0); // the first item of the params is the field name
		}
		@Override public boolean apply(Map<String, String> input){
//			System.out.println("filter: containsKey="+input.containsKey(field)+", values="+values+", inputType="+input.get(field)+", hasTypes="+values.contains(input.get(field)));
			return input!=null && input.containsKey(field) && values.contains(input.get(field));
		}
	}

//usage example: http://localhost:8083/api/integrations/v1/portfolio/products?keys=Type,Pillar,Name&format=json&filters=NotHasFieldValues(Region,NAPS)
	public static class NotHasFieldValues implements Predicate<Map<String,String>>{
		private String field;
		private List<String> values;
		public NotHasFieldValues(String params){
			values=Lists.newArrayList(Splitter.on(",").split(params).iterator());
			field=values.remove(0); // the first item of the params is the field name
		}
		@Override public boolean apply(Map<String, String> input){
//			System.out.println("filter: containsKey="+input.containsKey(field)+", values="+values+", inputType="+input.get(field)+", hasTypes="+values.contains(input.get(field)));
			return input!=null && input.containsKey(field) && !values.contains(input.get(field));
		}
	}

	public static class NotEmpty implements Predicate<Map<String,Object>>{
		private String key;
		public NotEmpty(String key){
			this.key=key;
		}
		@Override public boolean apply(Map<String, Object> input){
			return input!=null && input.containsKey(key) && String.class.isAssignableFrom(input.get(key).getClass()) && StringUtils.isNotBlank((String)input.get(key));
		}
	}
	
	public static class IsEmpty implements Predicate<Map<String,Object>>{
		private String key;
		public IsEmpty(String key){
			this.key=key;
		}
		@Override public boolean apply(Map<String, Object> input){
			return input!=null && !input.containsKey(key) || input.containsKey(key) && String.class.isAssignableFrom(input.get(key).getClass()) && StringUtils.isBlank((String)input.get(key));
		}
	}
	
}
