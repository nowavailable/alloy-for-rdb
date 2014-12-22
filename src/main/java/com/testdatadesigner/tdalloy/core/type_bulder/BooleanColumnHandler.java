package com.testdatadesigner.tdalloy.core.type_bulder;

import java.util.function.Function;

import com.testdatadesigner.tdalloy.core.types.Relation;
import com.testdatadesigner.tdalloy.core.types.RulesForAlloyable;
import com.testdatadesigner.tdalloy.core.types.Sig;

public class BooleanColumnHandler {

    public Relation build(Function<String, Sig> sigSearchByName, String ownerTableName, String columnName) {
        Relation relation = new Relation(Relation.Tipify.VALUE);
        // relation.originOwner = tableNode.getFullName();
        relation.owner = sigSearchByName.apply(RulesForAlloyable
                .tableSigName(ownerTableName));
        relation.name = RulesForAlloyable.colmnRelationName(
                columnName, ownerTableName);
        relation.refTo = new Sig(Sig.Tipify.BOOLEAN_FACTOR);
        return relation;
    }
}
