package com.testdatadesigner.tdalloy.core.type_bulder;

import com.testdatadesigner.tdalloy.core.types.RulesForAlloyable;
import com.testdatadesigner.tdalloy.core.types.Atom;

public class TableHandler {

    public Atom build(String tableName) {
        Atom atom = new Atom(Atom.Tipify.ENTITY);
        atom.originPropertyName = tableName;
        atom.name = RulesForAlloyable.tableAtomName(tableName);
        return atom;
    }
}
