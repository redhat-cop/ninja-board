package com.redhat.sso.ninja.utils;

import java.util.*;
import java.util.Map.Entry;

public class MapUtilities{
  public static <K, V extends Comparable<V>> List<Entry<K, V>> sortByValue(Map<K, V> map){
    List<Entry<K, V>> entries= new ArrayList<>(map.entrySet());
    Collections.sort(entries, new ByValue<>());
    return entries;
  }
  public static <K extends Comparable<K>, V> List<Entry<K, V>> sortByKey(Map<K, V> map){
    List<Entry<K, V>> entries= new ArrayList<>(map.entrySet());
    Collections.sort(entries, new ByKey<>());
    return entries;
  }
  private static class ByValue<K, V extends Comparable<V>> implements Comparator<Entry<K, V>>{
    public int compare(Entry<K, V> o1, Entry<K, V> o2){
      return o1.getValue().compareTo(o2.getValue());
    }
  }
  private static class ByKey<K extends Comparable<K>, V> implements Comparator<Entry<K, V>>{
    public int compare(Entry<K, V> o1, Entry<K, V> o2){
      return o1.getKey().compareTo(o2.getKey());
    }
  }
}
