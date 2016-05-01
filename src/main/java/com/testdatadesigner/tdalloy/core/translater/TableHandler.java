package com.testdatadesigner.tdalloy.core.translater;

import com.testdatadesigner.tdalloy.core.types.Entity;
import com.testdatadesigner.tdalloy.core.types.IAtom;
import com.testdatadesigner.tdalloy.core.types.NamingRuleForAlloyable;

public class TableHandler {

  public IAtom build(String tableName) {
    IAtom atom = new Entity();
    atom.setOriginPropertyName(tableName);
    atom.setName(NamingRuleForAlloyable.tableAtomName(tableName));
    return atom;
  }
}
