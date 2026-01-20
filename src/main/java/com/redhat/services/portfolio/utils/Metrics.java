package com.redhat.services.portfolio.utils;

import java.util.Map;

import com.google.common.collect.Maps;
import com.redhat.services.portfolio.utils.TimeUtils;

public class Metrics{
  private Map<String,Object> timings=Maps.newLinkedHashMap();
  private long timings_startWhole;
  private long timings_start;
  public Metrics(String key) {timings.put("cacheKey", key); start();}
  public Metrics start() {timings_startWhole=System.currentTimeMillis(); timings_start=System.currentTimeMillis(); return this;};
  public Metrics reset() {timings_start=System.currentTimeMillis(); return this;}
  public Metrics store(String name, String value){timings.put(name, value); return this;}
  public <T> Metrics store(String name, T value){timings.put(name, value); return this;}
  public Metrics store(String name){
    long t=System.currentTimeMillis()-timings_start;
    boolean isThreadWait=name.contains("thread") || name.contains("wait");
    if (!isThreadWait || (isThreadWait && t>4)){ // remove any threadwaits that didnt really happen - if it's less than 2ms then there was no wait, that's just an execution timing that went over the seconds barrier 
      timings.put(name, TimeUtils.msToSensibleString(t));
//    }else{
//      timings.put(name, "NO_WAIT");
    }
    return reset();
  }
//    public Timings name(String name) {timings.put("key", name); return this;}
  public Map<String,Object> get(){timings.put("timings_wholeMethod", TimeUtils.msToSensibleString(System.currentTimeMillis()-timings_startWhole)); return timings;}
}
