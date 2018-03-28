package com.redhat.sso.ninja.chart;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Chart2LineJson {
  private Set<String> labels=new LinkedHashSet<String>();
  private List<DataSetLine2> datasets=new ArrayList<DataSetLine2>();
  
  public Set<String> getLabels() {
    return labels;
  }
  public void setLabels(Set<String> labels) {
    this.labels=labels;
  }
  public List<DataSetLine2> getDatasets() {
    return datasets;
  }
  public void setDatasets(List<DataSetLine2> datasets) {
    this.datasets=datasets;
  }
  
  
}
