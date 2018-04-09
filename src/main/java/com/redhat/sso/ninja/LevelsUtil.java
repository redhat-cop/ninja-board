package com.redhat.sso.ninja;

import java.util.LinkedList;
import java.util.List;

import com.redhat.sso.ninja.utils.Tuple;

public class LevelsUtil{
  public static void main(String[] asd){
    LevelsUtil l=new LevelsUtil("0:NONE,5:BLUE,15:RED,25:BROWN");
    System.out.println("base - expect NONE = "+l.base.getRight());
    
    for(int i=0;i<=27;i++){
      System.out.println("getLevel("+i+") - = "+l.getLevelGivenPoints(i));// +", nextLevel will be "+l.getNextLevel(i));
    }
    
    for(Tuple<Integer, String> x:l.levels){
      System.out.println("getNextLevel("+x.getRight()+") = "+l.getNextLevel(x.getRight()));
    }
  }
  
  private List<Tuple<Integer,String>> levels=new LinkedList<Tuple<Integer,String>>();
  private Tuple<Integer,String> base;
  private Tuple<Integer,String> top;
  public LevelsUtil(String levelConfig){
    for(String levelValueAndText:levelConfig.split(",")){
      String[] level=levelValueAndText.split(":");
      levels.add(new Tuple<Integer,String>(Integer.valueOf(level[0]), level[1]));
    }
    base=levels.get(0);
    top=levels.get(levels.size()-1);
  }
  
  public Tuple<Integer,String> getBaseLevel(){
    return base;
  }
  
  public Tuple<Integer,String> getLevel(String levelName){
    for(Tuple<Integer, String> l:levels){
      if (l.getRight().equals(levelName)) return l;
    }
    return null;
  }
  
//  // logic: first one you hit where the points are less than required for the level
//  public Tuple<Integer,String> getNextLevel(Integer points){
//    for(Tuple<Integer, String> l:levels)
//      if (points<l.getLeft()) return l;
//    return top;
//  }
  
  public Tuple<Integer,String> getNextLevel(String currentLevelName){
    for(int i=0;i<levels.size();i++){
      if (levels.get(i).getRight().equals(currentLevelName)) return levels.get((i+1)==levels.size()?i:i+1);
    }
    return null; // should never happen
    
//    for(Tuple<Integer, String> l:levels){
//      if (l.getRight().equals(currentLevelName))
//    }
  }
  
  //logic: last one you hit where the points are greater than required for the level
  public Tuple<Integer,String> getLevelGivenPoints(Integer points){
    Tuple<Integer, String> result=base;
    for(Tuple<Integer, String> l:levels){
      if (points>=l.getLeft()) result=l;
    }
    return result;
  }
  
}
