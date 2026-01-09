package com.redhat.services.portfolio.utils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.collect.Maps;

public class MapBuilder<K, V>{
	public enum Type{HashMap, LinkedHashMap}
	private Map<K, V> values;

	public static <K, V> Map<K, V> newHashMap(){
		return new MapBuilder<K, V>().build();
	}

	public static <K, V> Map<K, V> newLinkedHashMap(){
		return new MapBuilder<K, V>(true).build();
	}
	
	public static <K, V> Map<K, V> mergeMaps(Map<K,V> map1, Map<K,V> map2){
		Map<K,V> result=Maps.newHashMap();
		result.putAll(map1);
		result.putAll(map2);
		return result;
	}

	public MapBuilder(){
		values=new HashMap<K, V>();
	}

	public MapBuilder(boolean retainInsertionOrder){
		values=new LinkedHashMap<K, V>();
	}
	public MapBuilder(Type mapType){
		switch(mapType){
		case HashMap: values=new HashMap<K, V>(); break;
		case LinkedHashMap: values=new LinkedHashMap<K, V>(); break;
		default: values=new HashMap<K, V>();
		}
	}

	public MapBuilder<K, V> put(K key, V value){
		values.put(key, value);
		return this;
	}

	public Map<K, V> build(){
		return values;
	}

	public static void lazyInc(Map<String, Integer> map, String key, int i){
		if (!map.containsKey(key)) map.put(key, 0);
		map.put(key, map.get(key)+i);
	}
}