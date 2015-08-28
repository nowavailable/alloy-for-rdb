package com.testdatadesigner.tdalloy.core.type_bulder;

import java.util.function.Function;

import com.testdatadesigner.tdalloy.core.naming.IRulesForAlloyable;
import com.testdatadesigner.tdalloy.core.naming.RulesForAlloyableFactory;
import com.testdatadesigner.tdalloy.core.types.Relation;
import com.testdatadesigner.tdalloy.core.types.Atom;
import com.testdatadesigner.tdalloy.core.types.NamingRuleForAlloyable;

public class DefaultColumnHandler {

    public Relation buildRelation(Function<String, Atom> atomSearchByName, String ownerTableName,
            String columnName) {
        Relation relation = new Relation(Relation.Typify.VALUE);
        relation.setOwner(atomSearchByName.apply(NamingRuleForAlloyable.tableAtomName(ownerTableName)));
        relation.name = columnName;
        Atom column = new Atom(Atom.Tipify.PROPERTY);
        column.name = "Boundary";
        relation.setRefTo(column);
        return relation;
    }

    public Atom buildAtom(Function<String, Atom> atomSearchByName, String ownerTableName,
            String columnName) throws IllegalAccessException {
        Atom colomnAtom = new Atom(Atom.Tipify.PROPERTY);
        IRulesForAlloyable namingRule = RulesForAlloyableFactory.getInstance().getRule();
        colomnAtom.originPropertyName = columnName;
        colomnAtom.name = NamingRuleForAlloyable.columnAtomName(columnName, ownerTableName);
        colomnAtom.setParent(atomSearchByName.apply(NamingRuleForAlloyable.tableAtomName(ownerTableName)));
        return colomnAtom;
    }

    public Atom buildAtomPolymorphicAbstract(Function<String, Atom> atomSearchByName, String ownerTableName,
            String columnName) throws IllegalAccessException {
        Atom colomnAtom = buildAtom(atomSearchByName, ownerTableName, columnName);
        colomnAtom.type = Atom.Tipify.POLYMORPHIC_ABSTRACT;
        return colomnAtom;
    }
}
