package com.testdatadesigner.tdalloy.core.types;

public interface IRelation {
	public IAtom getRefTo();
	public void setRefTo(IAtom refTo) throws IllegalAccessException;
	public IAtom getOwner();
	public void setOwner(IAtom owner) throws IllegalAccessException;
	public String getName();
	public void setName(String name);
	public String getOriginColumnName();
	public void setOriginColumnName(String originColumnName) ;
	public Boolean getIgnore();
	public void setIgnore(Boolean ignore);
	public Boolean getIsNotEmpty() ;
	public void setIsNotEmpty(Boolean isNotEmpty);
	public Boolean getIsUnique();
	public void setIsUnique(Boolean isUnique);
}
