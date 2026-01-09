package com.redhat.services.portfolio.utils;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/* think this is the latest version */
public class CookieBuilder{
	public static final Logger log=LoggerFactory.getLogger(CookieBuilder.class);
	public enum SameSite{Strict,Lax,None}
//	private static final String IPV4_REGEX =
//			"^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
//			"(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
//			"(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
//			"(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
//	private final Pattern IPv4_PATTERN = Pattern.compile(IPV4_REGEX);
	protected String name,value,domain,path,maxAge,sameSite,secure,httpOnly;
	public CookieBuilder name(String v){this.name=v;return this;}
	public CookieBuilder value(String v){this.value=v;return this;}
	public CookieBuilder path(String v){this.path=v;return this;}
	public CookieBuilder domainStr(String v){this.domain=v;return this;}
	public CookieBuilder domainUrl(String v){String d=getDomainForCookie(v);System.out.println("Cookie.domain"+d); this.domain=d;return this;}
	public CookieBuilder maxAge(Integer v){this.maxAge=String.valueOf(v);return this;}
	public CookieBuilder maxAge(String v){this.maxAge=v;return this;}
	public CookieBuilder httpOnly(){this.httpOnly="HttpOnly";return this;}
	public CookieBuilder secure(){this.secure="Secure";return this;}
	public CookieBuilder sameSite(SameSite v){this.sameSite=v.name();return this;}
	public String build(){
//		if (!value.matches("^[a-zA-Z0-9!#$%&'*+-.^_`|~]$")) throw new RuntimeException("Invalid cookie value!");
		return name+"="+value+";"+
				(path!=null?  "Path="+path+";":"")+
				(domain!=null?"Domain="+domain+";":"")+
				(maxAge!=null?"Max-Age="+maxAge+";":"")+
				(httpOnly!=null?httpOnly+";":"")+
				(secure!=null?secure+";":"")+
				(sameSite!=null?"SameSite="+sameSite+";":"")+
				"";
	}
	
	private String getDomainForCookie(String url){
		String result="";
		try{
			URI x=new URI(url);
			return x.getHost().contains("www")?x.getHost().replace("www.", ""):x.getHost();
		}catch (URISyntaxException e){
			log.error("unable to identify domain for the cookie");
		}
		
		return result;
	}
}
	
