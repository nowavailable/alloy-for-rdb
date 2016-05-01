package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MissingAtom extends Atom implements Serializable, IAtom, ITable {
  private static final long serialVersionUID = 1L;

  private IAtom parent;
  private List<IAtom> owners = new ArrayList<>();

  public MissingAtom(String name) {
    this.name = name;
  }

  public List<IAtom> getOwners() {
    return owners;
  }

  public void addOwners(IAtom owner) {
    this.owners.add(owner);
  }

  @Override
  public IAtom getParent() {
    return this.parent;
  }

  @Override
  public void setParent(IAtom parent) throws IllegalAccessException {
    this.parent = parent;
  }

}
