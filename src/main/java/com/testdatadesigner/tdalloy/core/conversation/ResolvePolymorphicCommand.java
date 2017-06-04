package com.testdatadesigner.tdalloy.core.conversation;

import java.util.ArrayList;
import java.util.List;

import com.testdatadesigner.tdalloy.core.types.PseudoAtom;

import java.io.Serializable;

public class ResolvePolymorphicCommand implements Serializable {
  private static final long serialVersionUID = 1L;

  public List<PseudoAtom> targetPseudoAtoms = new ArrayList<>();
}
