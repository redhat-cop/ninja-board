package com.redhat.services.portfolio.utils;

import java.util.Map;

public class StringUtils{

  public static Object shorten(Object secret){
    if (String.class.isAssignableFrom(secret.getClass())) {
      return shorten((String)secret);
    }
    return secret;
  }

  public static String shorten(String secret){
    return shorten(secret,4,5,"...");
  }
  public static String shorten(String token, int startLen, int endLen){
    return shorten(token,startLen,endLen,"...");
  }
  private static String shorten(String token, int startLen, int endLen, String middleChars){
    if (token==null) return token;
    String start=token.length()>startLen?token.substring(0,startLen):token;
    String end=token.length()>endLen?token.substring(token.length()-endLen):token;
    return start+(endLen>0?middleChars:"")+end;
  }
  private Map<String,String> shorten(Map<String,String> tokenInfo){
    return shorten(tokenInfo, "issued_to", "audience", "verified_email", "access_type");
  }
  private Map<String,String> shorten(Map<String,String> tokenInfo, String... keys){
    for(String k:keys) tokenInfo.remove(k);
    return tokenInfo;
  }
}
