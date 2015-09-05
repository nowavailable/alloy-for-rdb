package com.testdatadesigner.tdalloy.core.type_bulder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.testdatadesigner.tdalloy.core.naming.IRulesForAlloyable;
import com.testdatadesigner.tdalloy.core.naming.RulesForAlloyableFactory;
import com.testdatadesigner.tdalloy.core.types.AlloyableHandler;
import com.testdatadesigner.tdalloy.core.types.PseudoAtom;
import com.testdatadesigner.tdalloy.core.types.Fact;
import com.testdatadesigner.tdalloy.core.types.MultipleRelation;
import com.testdatadesigner.tdalloy.core.types.Relation;
import com.testdatadesigner.tdalloy.core.types.Atom;
import com.testdatadesigner.tdalloy.core.types.NamingRuleForAlloyable;

public class PolymorphicHandler {
    IRulesForAlloyable namingRule = RulesForAlloyableFactory.getInstance().getRule();

    public List<Atom> buildDummies(Integer getNamingSeq) {
        // ダミー作成
        Atom dummyRefToAtom_1 = new PseudoAtom(getNamingSeq + 1);
        Atom dummyRefToAtom_2 = new PseudoAtom(getNamingSeq + 1 + 1);
        return Arrays.asList(dummyRefToAtom_1, dummyRefToAtom_2);
    }

    public Relation buildRelationForDummy(Function<String, Atom> atomSearchByName, String ownerTableName,
            String fKeyColumnStr, String refTableName) throws IllegalAccessException {
        // 参照される側
        Relation relation = new Relation(Relation.Typify.ABSTRACT_RELATION_REFERRED);
        String refTable = refTableName.isEmpty() ? namingRule
                .tableNameFromFKey(fKeyColumnStr) : refTableName;
        relation.name = namingRule.foreignKeyNameReversed(ownerTableName, refTable);
        //relation.name = namingRule.foreignKeyName(fKeyColumnStr, ownerTableName);
        relation.setOwner(atomSearchByName.apply(NamingRuleForAlloyable.tableAtomName(ownerTableName)));
        relation.setRefTo(atomSearchByName.apply(NamingRuleForAlloyable.tableAtomNameFromFKey(fKeyColumnStr)));
        return relation;
    }
    
    public Atom buildDummyExtend(String polymorphicStr, Atom dummyAtom, Atom abstructAtom) throws IllegalAccessException {
        return new PseudoAtom(NamingRuleForAlloyable.polymorphicImplAtomName(polymorphicStr, dummyAtom.name), abstructAtom);
    }

    public List<Relation> buildRelation(Function<String, Atom> atomSearchByName,
            //List<? extends Atom> refToAtoms, 
            String polymorphicStr, String ownerTableName, Atom polymAbstructAtom) {
        List<Relation> relList = new ArrayList<>();
        IRulesForAlloyable namingRule = RulesForAlloyableFactory.getInstance().getRule();
        // 1/9
        MultipleRelation valueRelation = new MultipleRelation(Relation.Typify.RELATION_POLYMORPHIC);
        valueRelation.originColumnName = polymorphicStr + namingRule.polymorphicSuffix();
        valueRelation.name =
                NamingRuleForAlloyable.columnRelationName(
                    polymorphicStr + namingRule.polymorphicSuffix(), ownerTableName);
        valueRelation.setOwner(atomSearchByName.apply(NamingRuleForAlloyable.tableAtomName(ownerTableName)));
        //valueRelation.refToTypes = refToAtoms;
        valueRelation.setRefTo(polymAbstructAtom);
        relList.add(valueRelation);

        // 5/9
        MultipleRelation polymRelationReversed =
                new MultipleRelation(Relation.Typify.ABSTRACT_RELATION);
        polymRelationReversed.originColumnName = polymorphicStr + namingRule.polymorphicSuffix();
        polymRelationReversed.name = "refTo_" + NamingRuleForAlloyable.tableAtomName(ownerTableName);
        polymRelationReversed.setRefTo(atomSearchByName.apply(NamingRuleForAlloyable.tableAtomName(ownerTableName)));
        //polymRelationReversed.reverseOfrefToTypes = refToAtoms;
        polymRelationReversed.setOwner(polymAbstructAtom);
        relList.add(polymRelationReversed);
        return relList;
    }
    
    public Relation buildTypifiedRelation(Atom extendedAtom, Atom dummyAtom) {
        Relation relation = new Relation(Relation.Typify.ABSTRACT_RELATION_TYPIFIED);
        relation.name = namingRule.tableize(dummyAtom.name);
        relation.setOwner(extendedAtom);
        relation.setRefTo(dummyAtom);
        return relation;
    }

    public Fact buildFactBase(List<Relation> relations) {
        String leftStr = new String();
        String rightStr = new String();
        for (Relation relation : relations) {
        	Atom owner = AlloyableHandler.getOwner(relation);
            if (relation.type.equals(Relation.Typify.ABSTRACT_RELATION)) {
                rightStr = owner.name + "<:" + relation.name;
            } else if (relation.type.equals(Relation.Typify.RELATION_POLYMORPHIC)) {
                leftStr = owner.name + "<:" + relation.name;
            }
        }
        Fact fact = new Fact(Fact.Tipify.RELATION);
        fact.value = leftStr + " = ~(" + rightStr + ")";
        fact.owners.addAll(relations);
        return fact;
    }

    public Fact buildFactForDummies(Relation dummyRelation, Relation parentRelation) {
    	Atom dummyRelationOwner = AlloyableHandler.getOwner(dummyRelation);
    	Atom parentRelationOwner = AlloyableHandler.getOwner(parentRelation);
        String leftStr = dummyRelationOwner.name + "<:" + dummyRelation.name;
        String rightStr = parentRelationOwner.name + "<:" + parentRelation.name + "." + namingRule.tableize(dummyRelationOwner.name);
        Fact fact = new Fact(Fact.Tipify.RELATION_POLYMOPHIC_COLUMN);
        fact.value = leftStr + " = ~(" + rightStr + ")";
        fact.owners.add(dummyRelation);
        fact.owners.add(parentRelation);
        return fact;
    }
}
