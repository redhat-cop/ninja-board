package com.redhat.sso.ninja.utils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeUtils{
	public static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	
//	public static int daysBetweenDates(Date d1, Date d2){
//		return (int)((d1.getTime() - d2.getTime()) / TimeUnit.DAYS.toMillis(1));
//	}
	
	public static long getMillisToNextTime(String time) throws ParseException{
  	SimpleDateFormat sdf=new SimpleDateFormat("HH:mm");
  	Calendar start=newCalendar(System.currentTimeMillis());
  	Calendar cTime=newCalendar(sdf.parse(time).getTime());
  	start.set(Calendar.HOUR_OF_DAY, cTime.get(Calendar.HOUR_OF_DAY));
  	start.set(Calendar.MINUTE, cTime.get(Calendar.MINUTE));
  	start.set(Calendar.SECOND, cTime.get(Calendar.SECOND));
  	
  	Calendar now=newCalendar(System.currentTimeMillis());
  	
  	int nowHour=now.get(Calendar.HOUR_OF_DAY);
  	int startHour=start.get(Calendar.HOUR_OF_DAY);
  	if (nowHour>=startHour){
  		// too late today, move to tomorrow
  		start.set(Calendar.DAY_OF_MONTH, start.get(Calendar.DAY_OF_MONTH)+1);
  	}
  	return start.getTimeInMillis()-now.getTimeInMillis();
  }
  private static Calendar newCalendar(long millis){
  	Calendar c=Calendar.getInstance();
  	c.setTime(new Date(millis));
  	return c;
  }
  private static int[] s=new int[]{1000,60,60,24,365};
  private static String[] d=new String[]{"ms","s","m","h","d"};
  public static String msToSensibleString(long ms){
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
  }
}
