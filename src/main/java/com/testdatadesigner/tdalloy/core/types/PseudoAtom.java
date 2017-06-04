package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;

public class PseudoAtom extends Entity implements Serializable, IAtom, ITable, IPolymorphicColumn {
  private static final long serialVersionUID = 1L;

  private PolymorphicAbstract extended;
  public IAtom shouldReplaceTo;
  public static String MARK_OF_PSEUDO = "Dummy";

  public PseudoAtom(Integer seq) {
    super();
    this.setName(MARK_OF_PSEUDO + String.valueOf(seq));
    this.setOriginPropertyName(
        MARK_OF_PSEUDO.toLowerCase() + "_" + String.valueOf(seq) + "s"
    );
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
