package com.redhat.sso.ninja.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * MapBuilder
 */
public class MapBuilder<K,V>{
    Map<K, V> values=new HashMap<K, V>();
    boolean noNullValues;
    public MapBuilder(){}
    public MapBuilder(boolean noNullValues){
    	this.noNullValues=noNullValues;
		}
		public MapBuilder<K,V> put(K key, V value){
			if (noNullValues && value==null) return this;
      values.put(key, value); return this;
    }
    public Map<K, V> build(){
      return values;
    }
}