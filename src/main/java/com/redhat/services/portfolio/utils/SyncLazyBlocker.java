package com.redhat.services.portfolio.utils;

import java.util.Map;

import com.google.common.collect.Maps;

public class SyncLazyBlocker{
	private static volatile Map<String,Object> blocker=Maps.newHashMap();
	public Object get(String key){
		if (!blocker.containsKey(key)) blocker.put(key, new Object());
		return blocker.get(key);
	}
}
