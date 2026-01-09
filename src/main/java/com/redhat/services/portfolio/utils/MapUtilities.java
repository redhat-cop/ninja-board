package com.redhat.services.portfolio.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Deprecated
public class MapUtilities{
	public enum Direction{ASC(1),DESC(2); public int multiplier;private Direction(int multiplier) {this.multiplier=multiplier;}}
  public static <K, V extends Comparable<V>> List<Entry<K, V>> sortByValue(Map<K, V> map){return sortByValue(map, Direction.ASC);}
	public static <K, V extends Comparable<V>> List<Entry<K, V>> sortByValue(Map<K, V> map, Direction d){
    List<Entry<K, V>> entries=new ArrayList<Entry<K, V>>(map.entrySet());
    Collections.sort(entries, new ByValue<K, V>(d));
    return entries;
  }
	public static <K extends Comparable<K>, V> List<Entry<K, V>> sortByKey(Map<K, V> map){return sortByKey(map, Direction.ASC);}
	public static <K extends Comparable<K>, V> List<Entry<K, V>> sortByKey(Map<K, V> map, Direction d){
    List<Entry<K, V>> entries=new ArrayList<Entry<K, V>>(map.entrySet());
    Collections.sort(entries, new ByKey<K, V>(d));
    return entries;
  }
  private static class ByValue<K, V extends Comparable<V>> implements Comparator<Entry<K, V>>{
  	private Direction d;public ByValue(Direction d){this.d=d;}
    public int compare(Entry<K, V> o1, Entry<K, V> o2){
      return o1.getValue().compareTo(o2.getValue()) * d.multiplier;
    }
  }
  private static class ByKey<K extends Comparable<K>, V> implements Comparator<Entry<K, V>>{
  	private Direction d;public ByKey(Direction d){this.d=d;}
  	public int compare(Entry<K, V> o1, Entry<K, V> o2){
      return o1.getKey().compareTo(o2.getKey()) * d.multiplier;
    }
  }
  
}
