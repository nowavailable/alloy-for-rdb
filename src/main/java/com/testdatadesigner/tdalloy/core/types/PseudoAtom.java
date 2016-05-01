package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;

public class PseudoAtom extends Entity implements Serializable, IAtom, ITable, IPolymorphicColumn {
  private static final long serialVersionUID = 1L;

  private PolymorphicAbstract extended;

  public PseudoAtom(Integer seq) {
    super();
    this.setName("Dummy" + String.valueOf(seq));
    this.setOriginPropertyName("dummy_" + String.valueOf(seq) + "s");
  }

  public PseudoAtom(String name, PolymorphicAbstract abstructAtom) throws IllegalAccessException {
    this.setName(name);
    this.setExtended(abstructAtom);
  }

  public PolymorphicAbstract getExtended() {
    return this.extended;
  }

  public void setExtended(PolymorphicAbstract extended) {
    this.extended = extended;
  }

  @Override
  public IAtom getParent() {
    return super.getParent();
  }

  @Override
  public void setParent(IAtom parent) throws IllegalAccessException {
    super.setParent(parent);
  }
}
