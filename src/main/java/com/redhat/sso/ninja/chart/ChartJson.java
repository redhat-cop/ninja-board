package com.redhat.sso.ninja.chart;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ChartJson {
  private Set<String> labels=new LinkedHashSet<String>(); // not sure this should be a Set, I think it should be a list since each corresponds to a dataset item. Sets would collapse identical text causing an unintended offset
  private List<String> custom1=new LinkedList<String>();
  private List<String> custom2=new LinkedList<String>();
  private List<DataSet> datasets=new ArrayList<DataSet>();
  
  public List<String> getCustom1() {return custom1;} public void setCustom1(List<String> value) { this.custom1=value; }
  public List<String> getCustom2() {return custom2;} public void setCustom2(List<String> value) { this.custom2=value; }
  
  public Set<String> getLabels() {
    return labels;
  }
  public void setLabels(Set<String> labels) {
    this.labels=labels;
  }
  public List<DataSet> getDatasets() {
    return datasets;
  }
  public void setDatasets(List<DataSet> datasets) {
    this.datasets=datasets;
  }
  
  
}
