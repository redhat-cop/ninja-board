package com.redhat.sso.ninja.chart;

import java.util.ArrayList;
import java.util.List;

public class DataSet {
  private String label;
  private String fillColor; //ie "rgba(220,220,220,0.2)"
  private String strokeColor; //ie "rgba(220,220,220,1)"
  private String pointColor; //ie "rgba(220,220,220,1)"
  private String pointStrokeColor; //ie "#fff"
  private String pointHighlightFill; //ie "#fff"
  private String pointHighlightStroke; //ie "rgba(220,220,220,1)"
  
  private List<String> backgroundColor;
  public List<String> getBackgroundColor() {
    if (backgroundColor==null) backgroundColor=new ArrayList<String>();
    return backgroundColor;
  }
  private List<String> borderColor;
  public List<String> getBorderColor() {
    if (borderColor==null) borderColor=new ArrayList<String>();
    return borderColor;
  }
  
  private List<Integer> data;
  
  public String getLabel() {
    return label;
  }
  public void setLabel(String label) {
    this.label=label;
  }
  public String getFillColor() {
    return fillColor;
  }
  public void setFillColor(String fillColor) {
    this.fillColor=fillColor;
  }
  public String getStrokeColor() {
    return strokeColor;
  }
  public void setStrokeColor(String strokeColor) {
    this.strokeColor=strokeColor;
  }
  public List<Integer> getData() {
    if (data==null) data=new ArrayList<Integer>();
    return data;
  }
  public void setData(List<Integer> data) {
    this.data=data;
  }
  public String getPointColor() {
    return pointColor;
  }
  public void setPointColor(String pointColor) {
    this.pointColor = pointColor;
  }
  public String getPointStrokeColor() {
    return pointStrokeColor;
  }
  public void setPointStrokeColor(String pointStrokeColor) {
    this.pointStrokeColor = pointStrokeColor;
  }
  public String getPointHighlightFill() {
    return pointHighlightFill;
  }
  public void setPointHighlightFill(String pointHighlightFill) {
    this.pointHighlightFill = pointHighlightFill;
  }
  public String getPointHighlightStroke() {
    return pointHighlightStroke;
  }
  public void setPointHighlightStroke(String pointHighlightStroke) {
    this.pointHighlightStroke = pointHighlightStroke;
  }
}
