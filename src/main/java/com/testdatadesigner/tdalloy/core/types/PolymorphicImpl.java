package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;

public class PolymorphicImpl extends Atom implements Serializable, IAtom, IPolymorphicColumn {
  private static final long serialVersionUID = 1L;

  private IAtom parent = null;
  private PolymorphicAbstract extended;

  public PolymorphicImpl() {
    super();
  }

  @Override
  public IAtom getParent() {
    return this.parent;
  }

  @Override
  public void setParent(IAtom parent) throws IllegalAccessException {
    throw new IllegalAccessException("No need parent.");
  }

  public PolymorphicAbstract getExtended() {
    return this.extended;
  }

  public void setExtended(PolymorphicAbstract extended) {
    this.extended = extended;
  }
}
