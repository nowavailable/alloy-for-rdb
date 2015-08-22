package com.testdatadesigner.tdalloy.core.type_bulder;

import com.testdatadesigner.tdalloy.core.types.Atom;
import com.testdatadesigner.tdalloy.core.types.RuleForAlloyable;

public class TableHandler {

    public Atom build(String tableName) {
        Atom atom = new Atom(Atom.Tipify.ENTITY);
        atom.originPropertyName = tableName;
        atom.name = RuleForAlloyable.tableAtomName(tableName);
        return atom;
    }
}
