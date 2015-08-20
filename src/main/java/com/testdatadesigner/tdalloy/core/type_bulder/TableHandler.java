package com.testdatadesigner.tdalloy.core.type_bulder;

import com.testdatadesigner.tdalloy.core.naming.RulesForAlloyableFactory;
import com.testdatadesigner.tdalloy.core.types.Atom;

public class TableHandler {

    public Atom build(String tableName) {
        Atom atom = new Atom(Atom.Tipify.ENTITY);
        atom.originPropertyName = tableName;
        atom.name = RulesForAlloyableFactory.getInstance().getRule().tableAtomName(tableName);
        return atom;
    }
}
