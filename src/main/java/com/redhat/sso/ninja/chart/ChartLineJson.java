package com.redhat.sso.ninja.chart;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ChartLineJson {
  private Set<String> labels=new LinkedHashSet<String>();
  private List<DataSetLine> datasets=new ArrayList<DataSetLine>();
  
  public Set<String> getLabels() {
    return labels;
  }
  public void setLabels(Set<String> labels) {
    this.labels=labels;
  }
  public List<DataSetLine> getDatasets() {
    return datasets;
  }
  public void setDatasets(List<DataSetLine> datasets) {
    this.datasets=datasets;
  }
  
  
}
