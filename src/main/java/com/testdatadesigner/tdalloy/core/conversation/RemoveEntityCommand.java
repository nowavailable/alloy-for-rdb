package com.testdatadesigner.tdalloy.core.conversation;

import com.testdatadesigner.tdalloy.core.types.IAtom;

import java.io.Serializable;
import java.util.List;

public class RemoveEntityCommand implements Serializable {
  private static final long serialVersionUID = 1L;

  public List<IAtom> dirtyAtoms;
}
