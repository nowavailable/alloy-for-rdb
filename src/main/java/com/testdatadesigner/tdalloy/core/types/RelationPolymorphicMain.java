package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;
import java.util.Arrays;

public class RelationPolymorphicMain extends Relation implements Serializable, IRelation {
  private static final long serialVersionUID = 1L;

  private Entity refTo;
  private ITable owner;

  public RelationPolymorphicMain() {
    super();
  }

  @Override
  public IAtom getRefTo() {
    return this.refTo;
  }

  @Override
  public void setRefTo(IAtom refTo) throws IllegalAccessException {
    if (!refTo.getClass().equals(Entity.class)) {
      throw new IllegalAccessException(refTo.getClass().toString() + " is not for refTo.");
    }
    this.refTo = (Entity) refTo;
  }

  @Override
  public IAtom getOwner() {
    return (IAtom) this.owner;
  }

  @Override
  public void setOwner(IAtom owner) throws IllegalAccessException {
    if (!Arrays.asList(owner.getClass().getInterfaces()).contains(ITable.class)) {
      throw new IllegalAccessException(owner.getClass().toString() + " is not for refTo.");
    }
    this.owner = (ITable) owner;
  }
}
