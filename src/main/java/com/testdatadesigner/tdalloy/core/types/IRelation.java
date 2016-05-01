package com.testdatadesigner.tdalloy.core.types;

import java.util.List;

public interface IRelation {
  public IAtom getRefTo();

  public void setRefTo(IAtom refTo) throws IllegalAccessException;

  public IAtom getOwner();

  public void setOwner(IAtom owner) throws IllegalAccessException;

  public String getName();

  public void setName(String name);

  public List<String> getOriginColumnNames();

  public void setOriginColumnNames(List<String> originColumnName);

  public Boolean getIgnore();

  public void setIgnore(Boolean ignore);

  public Boolean getIsNotEmpty();

  public void setIsNotEmpty(Boolean isNotEmpty);

  public Boolean getIsUnique();

  public void setIsUnique(Boolean isUnique);
}
