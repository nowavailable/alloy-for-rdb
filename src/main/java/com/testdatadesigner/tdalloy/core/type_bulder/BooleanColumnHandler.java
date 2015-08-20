package com.testdatadesigner.tdalloy.core.type_bulder;

import java.util.function.Function;

import com.testdatadesigner.tdalloy.core.naming.RulesForAlloyableFactory;
import com.testdatadesigner.tdalloy.core.types.Relation;
import com.testdatadesigner.tdalloy.core.types.Atom;

public class BooleanColumnHandler {

    public Relation build(Function<String, Atom> atomSearchByName, String ownerTableName,
            String columnName) {
        Relation relation = new Relation(Relation.Tipify.VALUE);
        relation.owner = atomSearchByName.apply(RulesForAlloyableFactory.getInstance().getRule().tableAtomName(ownerTableName));
        //relation.name = RulesForAlloyable.columnRelationName(columnName, ownerTableName);
        relation.name = columnName;
        Atom boolenValue = new Atom(Atom.Tipify.BOOLEAN_FACTOR);
        boolenValue.name = "Bool";
        relation.refTo = boolenValue;
        return relation;
    }
}
