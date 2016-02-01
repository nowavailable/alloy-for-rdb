package com.testdatadesigner.tdalloy.core.type_bulder;

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
        relation.setOriginColumnName(columnName);
        relation.setOwner(atomSearchByName.apply(NamingRuleForAlloyable.tableAtomName(ownerTableName)));
        relation.setName(columnName);
        IAtom column = new Property();
        column.setName("Boundary");
        relation.setRefTo(column);
        return relation;
    }

    public IAtom buildAtom(Function<String, IAtom> atomSearchByName, String ownerTableName,
            String columnName) throws IllegalAccessException {
        IAtom colomnAtom = new Property();
        colomnAtom.setOriginPropertyName(columnName);
        colomnAtom.setName(NamingRuleForAlloyable.columnAtomName(columnName, ownerTableName));
        colomnAtom.setParent(atomSearchByName.apply(NamingRuleForAlloyable.tableAtomName(ownerTableName)));
        return colomnAtom;
    }

    public PolymorphicAbstract buildAtomPolymorphicAbstract(Function<String, IAtom> atomSearchByName, String ownerTableName,
            String columnName) throws IllegalAccessException {
    	PolymorphicAbstract colomnAtom = new PolymorphicAbstract();
        colomnAtom.setOriginPropertyName(columnName);
        colomnAtom.setParent(atomSearchByName.apply(NamingRuleForAlloyable.tableAtomName(ownerTableName)));
        colomnAtom.setName(NamingRuleForAlloyable.polymorphicAbstractAtomName(columnName, ownerTableName));;
        return colomnAtom;
    }
}
