package com.redhat.sso.ninja.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FluentCalendar {
  private Calendar c=Calendar.getInstance();
  public static FluentCalendar get(){
    return new FluentCalendar();
  }
  public static FluentCalendar now(){
    return new FluentCalendar(new Date(System.currentTimeMillis()));
  }
  public static FluentCalendar get(Date time){
    return new FluentCalendar(time);
  }
  public FluentCalendar(){}
  public FluentCalendar(Date time){
    c.setTime(time);
  }
  
  public FluentCalendar add(int field, int amount){
    c.add(field, amount);
    return this;
  }
  public FluentCalendar set(int field, int amount){
    c.set(field, amount);
    return this;
  }
  
  public FluentCalendar firstDayOfMonth(){
  	c.set(Calendar.DAY_OF_MONTH, 1);
  	return this;
  }
  public FluentCalendar lastDayOfMonth(){
  	c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
  	return this;
  }
  public FluentCalendar startOfDay(){
  	c.set(Calendar.HOUR_OF_DAY, 0);
  	c.set(Calendar.MINUTE, 0);
  	c.set(Calendar.SECOND, 0);
  	return this;
  }
  public FluentCalendar endOfDay(){
  	c.set(Calendar.HOUR_OF_DAY, 23);
  	c.set(Calendar.MINUTE, 59);
  	c.set(Calendar.SECOND, 59);
  	return this;
  }
  
  
  /** yyyy-MM-dd HH:mm:ss Z */
  public String getString(String format){
    return new SimpleDateFormat(format).format(c.getTime());
  }
  
  public Calendar build(){
    return c;
  }
}
