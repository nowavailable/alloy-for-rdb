package com.testdatadesigner.tdalloy.core.translater;

import java.util.Arrays;
import java.util.function.Function;

import com.testdatadesigner.tdalloy.core.types.BooleanFactor;
import com.testdatadesigner.tdalloy.core.types.RelationProperty;
import com.testdatadesigner.tdalloy.core.types.IAtom;
import com.testdatadesigner.tdalloy.core.types.IRelation;
import com.testdatadesigner.tdalloy.core.types.NamingRuleForAlloyable;

public class BooleanColumnHandler {

    public IRelation build(Function<String, IAtom> atomSearchByName, String ownerTableName,
            String columnName) throws IllegalAccessException {
        IRelation relation = new RelationProperty();
        relation.setOriginColumnNames(Arrays.asList(columnName));
        relation.setOwner(atomSearchByName.apply(NamingRuleForAlloyable.tableAtomName(ownerTableName)));
        //relation.name = RulesForAlloyable.columnRelationName(columnName, ownerTableName);
        relation.setName(columnName);
        IAtom boolenValue = new BooleanFactor();
        boolenValue.setName("Bool");
        relation.setRefTo(boolenValue);
        return relation;
    }
}
