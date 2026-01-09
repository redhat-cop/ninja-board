package com.redhat.services.portfolio.utils;

import static java.lang.String.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.redhat.cop.giveback.Config;
import com.redhat.cop.giveback.Initialization;

/**
 * Persisted (serialized) cache/storage for more complex objects such as maps
 * last updated: 2025-09-23
 */
public class Cache<K,V>{
	private static final Logger log=LoggerFactory.getLogger(Cache.class);
	protected String						name; public String getName(){return name;}
	protected long							defaultTimeoutInMs;
	protected boolean						onDisk=false;
	protected Map<K,Long>				expiry=Maps.newConcurrentMap();
	protected Map<K,V>					cache	=Maps.newConcurrentMap();
	protected Map<K,Integer>    hits  =Maps.newConcurrentMap();
	private File								cacheStore;
	private File								expiryStore;

	public static void main(String[] args){ // Tests
		Cache<String,Map<String,String>> test=Cache.newCache("test", 1000, false);
		test.put("1", Maps.newHashMap());
		System.out.println(format("Testing hasExpired before timeout = %s (expected: %s)", test.hasExpired("1"), false));
		try{Thread.currentThread().sleep(1100);}catch(Exception sink){}
		System.out.println(format("Testing hasExpired after timeout  = %s (expected: %s)", test.hasExpired("1"), true));
		
		Cache<String,Map<String,String>> test2=Cache.newCache("test2", 10000, false);
		test2.put("1", Maps.newHashMap());
		boolean f2=test.remove("2"); // shouldnt find it
		boolean f1=test.remove("1"); // should find it
		System.out.println(String.format("f2 should be false, f1 should be true, f2=%s, f1=%s",f2,f1));
	}
	
	public static class Builder<K,V>{
		private String name;
		private long defaultTimeoutInMs;
		private boolean onDisk=false;
		public Builder<K,V> name(String v){this.name=v;return this;}
		public Builder<K,V> timeout(long v){this.defaultTimeoutInMs=v;return this;}
		public Builder<K,V> timeout(String v){this.defaultTimeoutInMs=TimeUtils.sensibleStringToMs(v);return this;}
		public Builder<K,V> noTimeout(){this.defaultTimeoutInMs=0;return this;}
		public Builder<K,V> onDisk(boolean v){this.onDisk=v;return this;}
		public Cache<K,V> build(){
			return new Cache<K,V>(name, defaultTimeoutInMs, onDisk);
		}
	}
	public static <K,V> Cache<K,V> newCache(String name, long timeoutInMs){
		return new Builder<K,V>().name(name).timeout(timeoutInMs).onDisk(false).build();
	}
	public static <K,V> Cache<K,V> newCache(String name, long timeoutInMs, boolean onDisk){
		return new Builder<K,V>().name(name).timeout(timeoutInMs).onDisk(onDisk).build();
	}
	
	public Cache(String name, long timeoutInMs, boolean onDisk) {
		this.name=name;
		this.defaultTimeoutInMs=timeoutInMs;
		this.cacheStore=new File(Config.get().getStorageRoot(), "__cacheMaps_"+name+".ser");
		log.info(format("Cache[%s]:: timeout of %s, and is "+(onDisk?"stored in "+this.cacheStore:"not stored on disk")+"", name, TimeUtils.msToSensibleString(timeoutInMs)));
		this.onDisk=onDisk;
		if (onDisk) {
			this.cacheStore=new File(Config.get().getStorageRoot(), "__cacheMaps_"+name+".ser");
			if (expiryEnabled()) // if timeout is zero or less, there is no expiry
				this.expiryStore=new File(Config.get().getStorageRoot(), "__cacheMaps_"+name+"_timeouts.ser");
			
			if (cacheStore.exists()){
				ObjectInputStream cois=null;
				try{
					cois=new ObjectInputStream(new FileInputStream(cacheStore));
					cache=(Map<K,V>)cois.readObject();
					log.debug(format("Cache[%s]:: Loading cache from disk with %s items",name,cache.size()));
				}catch(Exception sink){
					sink.printStackTrace();
				}finally{
					if (null!=cois) try{cois.close();}catch(Exception sink){sink.printStackTrace();}
				}
			}
			
			if (expiryStore!=null && expiryStore.exists()){
				ObjectInputStream eois=null;
				try{
					eois=new ObjectInputStream(new FileInputStream(expiryStore));
					expiry=(Map<K,Long>)eois.readObject();
					log.debug(format("Cache[%s]:: Loading expiry for cache from disk with %s items",name,cache.size()));
				}catch(Exception sink){
					sink.printStackTrace();
				}finally{
					if (null!=eois) try{eois.close();}catch(Exception sink){sink.printStackTrace();}
				}
			}
		}
	}
	public long getDefaultTimeoutInMs(){
		return defaultTimeoutInMs;
	}
	public void updateDefaultTimeoutInMs(long timeoutInMs){
		defaultTimeoutInMs=timeoutInMs;
	}
	
	public String getDefaultTimeout(){
		return TimeUtils.msToSensibleString(defaultTimeoutInMs);
	}
	public boolean expiryEnabled(){return getDefaultTimeoutInMs()>0;}
	public void clear() {
		cache.clear();
		if (Objects.nonNull(cacheStore)) cacheStore.delete();
		if (Objects.nonNull(expiry)) expiry.clear();
		if (Objects.nonNull(hits)) hits.clear();
		if (Objects.nonNull(expiryStore)) expiryStore.delete();
		if (onDisk) saveToDisk();
	}
	public boolean remove(K k) {
		Object found=cache.remove(k);
		if (Objects.nonNull(expiry)) expiry.remove(k);
		if (Objects.nonNull(hits)) hits.remove(k);
		if (Objects.nonNull(found)){
			log.info(format("Cache[%s]:: found & deleted item [%s] from cache (newsize=%s)",name,k,cache.size()));
			if (onDisk) saveToDisk();
		}else{
			log.warn(format("Cache[%s]:: nothing was found for key [%s] in cache",name,k));
		}
		return Objects.nonNull(found);
	}
	public boolean isEmpty(){
		return cache.isEmpty();
	}
	public Map<K,V> get(){
		return cache;
	}
	private void lazyInc(Map<K, Integer> map, K key, int i){ if (!map.containsKey(key)) map.put(key, 0); map.put(key, map.get(key)+i); }
	public V get(K k){
		if (!hasExpired(k)){
		  lazyInc(hits,k,1);
		  return cache.get(k);
		}
		cleanupExpiredItems();
		return null;
	}
	public boolean containsKey(K k){return cache.containsKey(k);}
	/** is this time from now to the time it expires? or the literal time that it expires? I think the latter, but it should be the former */
	public long getExpiryInMs(K k){
		boolean expiryDisabled=expiry.get(k)==null || getDefaultTimeoutInMs()<=0;
		if (hasExpired(k) || expiryDisabled) return 0;
		return expiry.get(k);
	}
	public long getTimeToExpiryInMs(K k){
		boolean expiryDisabled=expiry.get(k)==null || getDefaultTimeoutInMs()<=0;
		if (hasExpired(k) || expiryDisabled) return 0;
		return expiry.get(k)-System.currentTimeMillis(); // expiry is a future time
	}
	public String getExpiry(K k){
		return TimeUtils.msToSensibleString(getExpiryInMs(k));
	}
	public void reserve(K key){ // used to signify that we know we're about to put something in the cache, but it's not there yet
//		System.out.println("XXX cache="+cache+", key="+key);
//		if (key==null) return;
//		cache.put(key, new V());
//		System.out.println("XXXXXXXXX reserved - "+key);
		if (expiryEnabled()) expiry.put(key, -1l);
		if (onDisk) saveToDisk();
	}
	public void put(K key, V value){
		if (value==null){ log.warn(format("Cache[%s]:: Attempt to add null value to cache for key [%s]",name,key)); return;};
		cache.put(key, value);
		if (expiryEnabled()) expiry.put(key, System.currentTimeMillis()+defaultTimeoutInMs);
		if (onDisk) saveToDisk();
	}
	public void putAll(Map<K,V> setTheCacheToThis){
		cache.putAll(setTheCacheToThis);
		for(K k:cache.keySet())
			if (expiryEnabled()) expiry.put(k, System.currentTimeMillis()+defaultTimeoutInMs);
		if (onDisk) saveToDisk();
	}
	/** returns True/Expired if key doesnt exist in storage, or if current time is greater than the expiry time if the key doesnt exist */
	public boolean hasExpired(K key){
		return hasExpired(key, true);
	}
	/** returns true if key has expired or doesnt exist */
	private boolean hasExpired(K key, boolean andCleanup){
//		System.out.println(format("%s::expiry.keys.count=%s, expiry.keys=%s, cache.keys.count=%s, cache.keys=%s",name, expiry.size(),expiry.keySet(),cache.size(),cache.keySet()));
//		System.out.println(format("%s::hasExpired = k=%s, v=%s, expiry=%s, expFormatted=%s", name, key, cache.get(key), expiry.get(key), null!=expiry.get(key)?Initialization.sdf.format(new Date(expiry.get(key))):null));
//		long c=System.currentTimeMillis();
//		System.out.println(format("cache(%s)=%s, currentTimeMillis(%s) > expiry(%s)=%s == %s",key,cache.get(key),c, key,expiry.get(key), c>expiry.get(key)));
		if (!cache.containsKey(key)) return true; // regardless of whether expiry is enabled or not, if it doesnt exist, it's considered expired
		if (!expiryEnabled()){
			return false; // doesnt expire if no expiry was set
		}
		if (expiry.isEmpty() || !expiry.containsKey(key)) {
//			System.out.println("expiry empty or doesnt contain key = expired=true");
			return true; // expired if it doesnt exist
		}
//		System.out.println(name+":expiry="+expiry.get(key)+", is expiry>current? = "+(System.currentTimeMillis()>expiry.get(key)));
		if (andCleanup && System.currentTimeMillis()>expiry.get(key)){ // expired if current time is greater than expiry time
			cleanupExpiredItems(); // function wouldve returned if it was unexpired, so if it still exists, it should be removed
			return true;
		}
		return false;
	}
	public void cleanupExpiredItems(){
		List<K> expiredKeys=cache.keySet().stream().filter(e->hasExpired(e,false) || cache.get(e)==null).collect(Collectors.toList());
		for(K k:expiredKeys){
			cache.remove(k);
			expiry.remove(k);
			hits.remove(k);
			log.info(format("Cache[%s]::expiry/cleanup: removing key %s, expiry is %s, current time is %s", name, k, !expiryEnabled()?"disabled":Initialization.sdf.format(new Date(expiry.get(k))), Initialization.sdf.format(new Date(System.currentTimeMillis()))));
		}
		if (onDisk) saveToDisk();
	}
	
	/* this needs to write "this" to the store, not the cache and expiry separately, cos not "hits" is being missed */
	private void saveToDisk(){
		ObjectOutputStream coos=null,eoos=null;
		try{
			log.trace("Cache[%s]:: Saving to disk with "+cache.size()+" items - "+Json.toJson(cache));
			if (!cacheStore.exists()){ cacheStore.getParentFile().mkdirs(); /*cacheStore.createNewFile();*/ }
			if (expiryEnabled() && !expiryStore.exists()){ expiryStore.getParentFile().mkdirs(); /*expiryStore.createNewFile();*/ }
			coos=new ObjectOutputStream(new FileOutputStream(cacheStore));
			coos.writeObject(cache);
			if (expiryEnabled()){
				eoos=new ObjectOutputStream(new FileOutputStream(expiryStore));
				eoos.writeObject(expiry);
			}
		}catch (IOException e){
			log.error("Enable to save to disk. error = "+e.getMessage());
			e.printStackTrace();
		}finally{
			try{if (coos!=null)coos.close();}catch(Exception sink){sink.printStackTrace();}
			try{if (eoos!=null)eoos.close();}catch(Exception sink){sink.printStackTrace();}
		}
	}
}
