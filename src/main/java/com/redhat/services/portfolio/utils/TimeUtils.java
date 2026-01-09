package com.redhat.services.portfolio.utils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtils{

  static int[] s=new int[]{1000,60,60,24,365};
  static String[] d=new String[]{"ms","s","m","h","d"};
  public static String msToSensibleString(long ms){
    try {
      double result=ms;
      String denomination=d[0];
      for(int i=0;i<=s.length;i++){
        if (result>=s[i]){
          result=result/(double)s[i];
          denomination=d[i+1];
        }else
          break;
      }
      return new DecimalFormat("##.###").format(result)+denomination;
    }catch(ArrayIndexOutOfBoundsException e) {System.out.println("input was "+ms); e.printStackTrace();}
    return "BROKEN";
  }
  
  /**
   * 1234 gets converted to 1234 (assumes it's already millis)
   * 10s gets converted to 10,000
   * 1m gets converted to 60,000
   * 10m gets converted to 600,000 etc..
   * secondsFromNow(30) gets converted to 30 seconds from the System.currentTimeMillis();
   * */
  public static long sensibleStringToMs(String time){
    if (null!=time){
      if (time.matches("\\d{1,8}[(ms)|s|m|h|d]+")){
        long multiplier=1;
        if (time.contains("ms")){ multiplier=1; }
        else if (time.contains("s")){ multiplier=1000; }
        else if (time.contains("m")){ multiplier=(1000 * 60); }
        else if (time.contains("h")){ multiplier=(1000 * 60 * 60); }
        else if (time.contains("d")){ multiplier=(1000 * 60 * 60 * 24); }
        return multiplier * Long.parseLong(time.replaceAll("[^0-9]", ""));
//      }else if (time.matches(".*secondsFromNow.*")){
//        if (time.contains("secondsFromNow")){
//          Matcher m=Pattern.compile("secondsFromNow\\((\\d+)\\)").matcher(time);
//          if (m.find()){
//            time=secondsFromNowString(Integer.parseInt(m.group(1)));
//            return getMillisToNextTime(time);
//          }
//        }
      }else if(time.matches("\\d{1,8}")){ // assume ms
        return Long.parseLong(time);
      }
    }
    return 0;
  }
  
//  public static long parseStartTimeToLong(String startTime, String defaultTime){
//    return TimeUtils.sensibleStringToMs(parseStartTime(startTime, defaultTime));
//  }

//  public static Date parseTimeToDate(String startTime){
//    return parseTimeToDate(startTime, "09:00");
//  }
//  public static Date parseTimeToDate(String startTime, String defaultTime){
//    Calendar c=newCalendar(System.currentTimeMillis());
//    try{
//      SimpleDateFormat sdf=new SimpleDateFormat("HH:mm");
//      if (null!=startTime){
//        if (startTime.contains("secondsFromNow")){
//          Matcher m=Pattern.compile("secondsFromNow\\((\\d+)\\)").matcher(startTime);
//          if (m.find())
//            return secondsFromNow(Integer.parseInt(m.group(1)));
//        }else if (startTime.matches("\\d{1,2}:\\d{2}")){ // already a time declaration
//          return sdf.parse(startTime);
//        }
//      }
//      return sdf.parse(defaultTime);// defaultTime; // default to 21:00
//    }catch(ParseException e){
//      e.printStackTrace();
//      return new Date();
//    }
//  }

  
  public static String parseTime(String startTime){
    return parseTime(startTime, "21:00");
  }
  public static String parseTime(String startTime, String defaultTime){ // startTime can be a literal 09:00, or a secondFromNow(40)
    if (null!=startTime){
      if (startTime.contains("secondsFromNow")){
        Matcher m=Pattern.compile("secondsFromNow\\((\\d+)\\)").matcher(startTime);
        if (m.find())
          return secondsFromNowString(Integer.parseInt(m.group(1)));
      }else if (startTime.matches("\\d{1,2}:\\d{2}")){ // already a time declaration
        return startTime;
      }
    }
    return defaultTime; // default to 21:00
  }
  public static Date secondsFromNow(int seconds){
    Calendar c=newCalendar(System.currentTimeMillis());
    c.add(Calendar.SECOND, seconds);
    return c.getTime();
  }
  public static String secondsFromNowString(int seconds){
    SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss");
    return sdf.format(secondsFromNow(seconds));
  }
  public static long getMillisToNextTime(String time){
    try {
      SimpleDateFormat sdf=new SimpleDateFormat(time.split(":").length <= 2?"HH:mm":"HH:mm:ss");
      Calendar start=newCalendar(System.currentTimeMillis());
      Calendar cTime=newCalendar(sdf.parse(time).getTime());
      start.set(Calendar.HOUR_OF_DAY, cTime.get(Calendar.HOUR_OF_DAY));
      start.set(Calendar.MINUTE, cTime.get(Calendar.MINUTE));
      start.set(Calendar.SECOND, cTime.get(Calendar.SECOND));
      
      Calendar now=newCalendar(System.currentTimeMillis());
      
      if (now.getTimeInMillis() >= start.getTimeInMillis()){
        // too late today, move to tomorrow
        start.set(Calendar.DAY_OF_MONTH, start.get(Calendar.DAY_OF_MONTH) + 1);
      }
      return start.getTimeInMillis() - now.getTimeInMillis();
    }catch(ParseException e) {
      e.printStackTrace();
      return -1;
    }
  }
  private static Calendar newCalendar(long millis){
    Calendar c=Calendar.getInstance();
    c.setTime(new Date(millis));
    return c;
  }
  
  static public class Counter{
    private long start,end;
    public Counter go(){this.start=System.currentTimeMillis(); return this;}
    public Counter stop(){this.end=System.currentTimeMillis(); return this;}
    public long durationInMs(){ return end-start;}
    public String duration(){ return TimeUtils.msToSensibleString(end-start);}
    public String durationRestart(){ String r=TimeUtils.msToSensibleString(end-start); go(); return r;}
  }
  
  public static void main(String[] args){
    System.out.println(new TimeUtils().sensibleStringToMs("1000ms") + " should be 1000");
    System.out.println(new TimeUtils().sensibleStringToMs("60s")    + " should be 60000");
    System.out.println(new TimeUtils().sensibleStringToMs("1m")     + " should be 60000");
    System.out.println(new TimeUtils().sensibleStringToMs("5m")     + " should be 300000");
    System.out.println(new TimeUtils().sensibleStringToMs("1h")     + " should be 3600000");
    System.out.println(new TimeUtils().sensibleStringToMs("1d")     + " should be 86400000");
  }
  
}
