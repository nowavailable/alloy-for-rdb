package com.testdatadesigner.tdalloy.core.translater;

import java.util.Arrays;
import java.util.function.Function;

import com.testdatadesigner.tdalloy.core.types.RelationProperty;
import com.testdatadesigner.tdalloy.core.types.IAtom;
import com.testdatadesigner.tdalloy.core.types.IRelation;
import com.testdatadesigner.tdalloy.core.types.PolymorphicAbstract;
import com.testdatadesigner.tdalloy.core.types.Property;
import com.testdatadesigner.tdalloy.core.types.NamingRuleForAlloyable;

public class DefaultColumnHandler {

    public IRelation buildRelation(Function<String, IAtom> atomSearchByName, String ownerTableName,
            String columnName) throws IllegalAccessException {
        IRelation relation = new RelationProperty();
        relation.setOriginColumnNames(Arrays.asList(columnName));
        relation.setOwner(atomSearchByName.apply(NamingRuleForAlloyable.tableAtomName(ownerTableName)));
        relation.setName(columnName);
        IAtom column = new Property();
        column.setName(Property.TYPE_ON_ALS);
        relation.setRefTo(column);
        return relation;
    }

    public PolymorphicAbstract buildAtomPolymorphicAbstract(Function<String, IAtom> atomSearchByName, String ownerTableName,
            String columnName) throws IllegalAccessException {
    	PolymorphicAbstract columnAtom = new PolymorphicAbstract();
        columnAtom.setOriginPropertyName(columnName);
        columnAtom.setParent(atomSearchByName.apply(NamingRuleForAlloyable.tableAtomName(ownerTableName)));
        columnAtom.setName(NamingRuleForAlloyable.polymorphicAbstractAtomName(columnName, ownerTableName));;
        return columnAtom;
    }
}
