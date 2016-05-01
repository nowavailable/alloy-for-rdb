package com.testdatadesigner.tdalloy.core.types;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Alloyable implements Serializable {

  public static final long serialVersionUID = 1L;
  public List<IAtom> atoms = new ArrayList<>();
  public List<IRelation> relations = new ArrayList<>();
  public List<Fact> facts = new ArrayList<>();
  public Boolean isRailsOriented = Boolean.FALSE;
  public List<MissingAtom> missingAtoms = new ArrayList<>();
}
