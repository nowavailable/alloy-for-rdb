package com.testdatadesigner.tdalloy.core.types;

public interface IAtom {
  public IAtom getParent();

  public void setParent(IAtom parent) throws IllegalAccessException;

  public String getName();

  public void setName(String name);

  public Integer getSeq();

  public void setSeq(Integer seq);

  public Boolean getIsAbstruct();

  public void setIsAbstruct(Boolean isAbstruct);

  public String getOriginPropertyName();

  public void setOriginPropertyName(String originPropertyName);

  public String getOriginTypeName();

  public void setOriginTypeName(String originTypeName);

  public Boolean getIgnore();

  public void setIgnore(Boolean ignore);
}
