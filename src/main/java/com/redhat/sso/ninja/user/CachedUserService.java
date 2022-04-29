package com.redhat.sso.ninja.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

public class CachedUserService extends UserService{
	private Map<String,List<User>> cache=new HashMap<>();
	int cachedCount=0;
	int notCachedCount=0;
	
	@Override
	public List<User> search(String field, String value) throws NamingException{
		boolean cached=true;
		if (!cache.containsKey(field+value)){
			notCachedCount+=1;
			cache.put(field+value, super.search(field, value));
		}
		if (cached)
			cachedCount+=1;//System.out.println("returned cached user: "+cache.get(field+value).get(0).getUid());
		return cache.get(field+value);
	}
	
	public void printCacheStats(){
		System.out.println("cached ["+cachedCount+"], not cached ["+notCachedCount+"]");
		cachedCount=0;
		notCachedCount=0;
	}
}
