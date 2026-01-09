package com.redhat.services.portfolio.utils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;

/**
 * Short-term, in memory cache for basic objects such as strings
 */
public class CacheSimple<K, V>{
	private Map<K,V> cache=Maps.newHashMap(); // short term cache, for multiple calls for getToken or getUserInfo within a 30 second time period
	private Map<K,Long> expiry=Maps.newHashMap();
	protected String name;
	protected long defaultTimeoutInMs;
	
	public static class Builder<K,V> extends CacheSimple<K,V>{
		public Builder<K,V> name(String v){this.name=v;return this;}
		public Builder<K,V> timeout(long v){this.defaultTimeoutInMs=v;return this;}
		public Builder<K,V> timeout(String v){defaultTimeoutInMs=TimeUtils.sensibleStringToMs(v);return this;}
		public CacheSimple<K,V> build(){
			CacheSimple<K,V> r=new CacheSimple<K,V>();
			r.defaultTimeoutInMs=defaultTimeoutInMs;
			return r;
		}
	}
	public boolean containsKey(K k){
		cleanupExpiredItems();
		return cache.containsKey(k);
	}
	public void put(K k, V v){
		cache.put(k, v);
		expiry.put(k, System.currentTimeMillis()+defaultTimeoutInMs);
	}
	public V get(K k){
		if (!hasExpired(k)) return cache.get(k);
		cleanupExpiredItems();
		return null;
	}
	public boolean hasExpired(K k){
		if (!expiry.containsKey(k)) return true;
		if (System.currentTimeMillis()>expiry.get(k)) return true;
		return false;
	}
	private void cleanupExpiredItems(){ // delete any timeout expired keys. a maintenance function
		List<K> expiredKeys=cache.keySet().stream().filter(e->hasExpired(e)).collect(Collectors.toList());
		for(K k:expiredKeys){cache.remove(k); expiry.remove(k);}
//		for(K k:expiry.keySet()) if (hasExpired(k)) cache.remove(k);
//		for(K k:cache.keySet()) if (!expiry.containsKey(k)) cache.remove(k);
//		expiry=expiry.entrySet().stream().filter(e->expired.test(e.getKey())).collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), HashMap::putAll);
	}
}
