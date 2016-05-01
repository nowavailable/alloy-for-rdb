package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Relation implements Serializable {

  private static final long serialVersionUID = 1L;

  public String name;
  public List<String> originColumnNames = new ArrayList<>();
  public Boolean ignore = Boolean.FALSE;

  public Boolean isNotEmpty = Boolean.FALSE;
  public Boolean isUnique = Boolean.FALSE;

  public Relation() {
    super();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getOriginColumnNames() {
    return originColumnNames;
  }

  public void setOriginColumnNames(List<String> originColumnName) {
    this.originColumnNames = originColumnName;
  }

  public Boolean getIgnore() {
    return ignore;
  }

  public void setIgnore(Boolean ignore) {
    this.ignore = ignore;
  }

  public Boolean getIsNotEmpty() {
    return isNotEmpty;
  }

  public void setIsNotEmpty(Boolean isNotEmpty) {
    this.isNotEmpty = isNotEmpty;
  }

  public Boolean getIsUnique() {
    return isUnique;
  }

  public void setIsUnique(Boolean isUnique) {
    this.isUnique = isUnique;
  }
}
