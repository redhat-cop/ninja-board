package com.redhat.sso.ninja.chart;

import java.util.ArrayList;
import java.util.List;

public class DataSetLine2 {
//  private String fillColor; //ie "rgba(220,220,220,0.2)"
//  private String strokeColor; //ie "rgba(220,220,220,1)"
  
  private String label;
  private boolean fill;
  private String borderColor;
  private double lineTension;
  private List<Integer> data;
  
//  private Integer borderWidth;
//  private List<String> backgroundColor;
//  private List<String> borderColor;
  
  public String getLabel() {
    return label;
  }
  public void setLabel(String label) {
    this.label = label;
  }
  
  public boolean isFill() {
    return fill;
  }
  public void setFill(boolean fill) {
    this.fill = fill;
  }
  public String getBorderColor() {
    return borderColor;
  }
  public void setBorderColor(String borderColor) {
    this.borderColor = borderColor;
  }
  public double getLineTension() {
    return lineTension;
  }
  public void setLineTension(double lineTension) {
    this.lineTension = lineTension;
  }
  //  public String getFillColor() {
//    return fillColor;
//  }
//  public void setFillColor(String fillColor) {
//    this.fillColor=fillColor;
//  }
//  public String getStrokeColor() {
//    return strokeColor;
//  }
//  public void setStrokeColor(String strokeColor) {
//    this.strokeColor=strokeColor;
//  }
  public List<Integer> getData() {
    if (data==null) data=new ArrayList<Integer>();
    return data;
  }
  public void setData(List<Integer> data) {
    this.data=data;
  }
//  public Integer getBorderWidth() {
//    return borderWidth;
//  }
//  public void setBorderWidth(Integer borderWidth) {
//    this.borderWidth = borderWidth;
//  }
//  public List<String> getBackgroundColor() {
//    if (backgroundColor==null) backgroundColor=new ArrayList<String>();
//    return backgroundColor;
//  }
//  public void setBackgroundColor(List<String> backgroundColor) {
//    this.backgroundColor = backgroundColor;
//  }
//  public List<String> getBorderColor() {
//    if (borderColor==null) borderColor=new ArrayList<String>();
//    return borderColor;
//  }
//  public void setBorderColor(List<String> borderColor) {
//    this.borderColor = borderColor;
//  }
  
  
}
