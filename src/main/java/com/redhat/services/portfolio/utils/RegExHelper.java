package com.redhat.services.portfolio.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExHelper{
	private static Map<String,Pattern> cache=new HashMap<String,Pattern>();
	
	private static Matcher buildMatcher(String regex, String input){
//		System.out.println("regex (inCache="+cache.containsKey(regex)+")="+regex+" - input="+input);
		if (!cache.containsKey(regex)) cache.put(regex, Pattern.compile(regex));
		Pattern  p=cache.get(regex);
		return p.matcher(input);
	}
	
	public static int getIndexOf(String input, String regex){
		Matcher m=buildMatcher(regex, input);
		if (m.find()){
			return m.start();
		}
		return -1;
	}
	
	public static String extract(String input, String regex){
		return extract(input, regex, 1);
	}
	public static String extract(String input, String regex, int group){
		if (input==null) return null;
		Matcher m=buildMatcher(regex, input);
		if (m.find()){
			return m.group(group);
		}
		return null;
	}
	public static String extract(String input, Pattern regexPattern, int group){
//		Matcher m=buildMatcher(regex, input);
		Matcher m=regexPattern.matcher(input);
		if (m.find()){
			return m.group(group);
		}
		return null;
	}
}
