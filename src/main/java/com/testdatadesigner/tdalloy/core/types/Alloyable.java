package com.testdatadesigner.tdalloy.core.types;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Alloyable implements Serializable {

  public static final long serialVersionUID = 1L;
  public List<IAtom> atoms = new ArrayList<>();
  public List<IRelation> relations = new ArrayList<>();
  public List<Fact> facts = new ArrayList<>();
  public Boolean isRailsOriented = Boolean.FALSE;
  public List<MissingAtom> missingAtoms = new ArrayList<>();

//  public void addAllToRelations(List<IRelation> relations) {
//    // 同じsrcから同じdstへと結ばれるrelationは、ひとつあればよい。
//    for (IRelation relation : relations) {
//      List<IRelation> exists = this.relations.stream().filter(rel -> rel.getOwner().equals(relation.getOwner()) && rel.getRefTo().equals(relation.getRefTo())).collect(Collectors.toList());
//      if (!exists.isEmpty()) {
//        continue;
//      }
//      this.relations.add(relation);
//    }
//  }

  public void addToFacts(Fact f) {
    List<Fact> dups = facts.stream().filter(fct -> fct.equals(f)).collect(Collectors.toList());
    if (dups.isEmpty())
      this.facts.add(f);
  }

  public List<IAtom> getPseudoAtoms() {
    return
        this.atoms.stream().filter(
          atom -> atom.getClass().equals(PseudoAtom.class)
        ).collect(Collectors.toList());
  }
}
