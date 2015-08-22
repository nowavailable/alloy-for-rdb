package com.testdatadesigner.tdalloy.core.type_bulder;

import com.testdatadesigner.tdalloy.core.types.Atom;
import com.testdatadesigner.tdalloy.core.types.NamingRuleForAlloyable;

public class TableHandler {

    public Atom build(String tableName) {
        Atom atom = new Atom(Atom.Tipify.ENTITY);
        atom.originPropertyName = tableName;
        atom.name = NamingRuleForAlloyable.tableAtomName(tableName);
        return atom;
    }
}
