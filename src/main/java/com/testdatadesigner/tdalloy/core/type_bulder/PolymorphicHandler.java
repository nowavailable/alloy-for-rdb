package com.testdatadesigner.tdalloy.core.type_bulder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.testdatadesigner.tdalloy.core.naming.IRulesForAlloyable;
import com.testdatadesigner.tdalloy.core.naming.RulesForAlloyableFactory;
import com.testdatadesigner.tdalloy.core.types.AbstractRelationPolymorphic;
import com.testdatadesigner.tdalloy.core.types.AbstractRelationPolymorphicReferred;
import com.testdatadesigner.tdalloy.core.types.AbstractRelationPolymorphicTypified;
import com.testdatadesigner.tdalloy.core.types.AlloyableHandler;
import com.testdatadesigner.tdalloy.core.types.IAtom;
import com.testdatadesigner.tdalloy.core.types.IRelation;
import com.testdatadesigner.tdalloy.core.types.PolymorphicAbstract;
import com.testdatadesigner.tdalloy.core.types.PseudoAtom;
import com.testdatadesigner.tdalloy.core.types.Fact;
import com.testdatadesigner.tdalloy.core.types.MultipleRelation;
import com.testdatadesigner.tdalloy.core.types.Relation;
import com.testdatadesigner.tdalloy.core.types.Atom;
import com.testdatadesigner.tdalloy.core.types.NamingRuleForAlloyable;
import com.testdatadesigner.tdalloy.core.types.RelationPolymorphic;

public class PolymorphicHandler {
    IRulesForAlloyable namingRule = RulesForAlloyableFactory.getInstance().getRule();

    public List<IAtom> buildDummies(Integer getNamingSeq) {
        // ダミー作成
        IAtom dummyRefToAtom_1 = new PseudoAtom(getNamingSeq + 1);
        IAtom dummyRefToAtom_2 = new PseudoAtom(getNamingSeq + 1 + 1);
        return Arrays.asList(dummyRefToAtom_1, dummyRefToAtom_2);
    }

    public IRelation buildRelationForDummy(Function<String, IAtom> atomSearchByName, String ownerTableName,
            String fKeyColumnStr, String refTableName) throws IllegalAccessException {
        // 参照される側
        IRelation relation = new AbstractRelationPolymorphicReferred();
        String refTable = refTableName.isEmpty() ? namingRule
                .tableNameFromFKey(fKeyColumnStr) : refTableName;
        relation.setName(namingRule.foreignKeyNameReversed(ownerTableName, refTable));
        //relation.name = namingRule.foreignKeyName(fKeyColumnStr, ownerTableName);
        relation.setOwner(atomSearchByName.apply(NamingRuleForAlloyable.tableAtomName(ownerTableName)));
        relation.setRefTo(atomSearchByName.apply(NamingRuleForAlloyable.tableAtomNameFromFKey(fKeyColumnStr)));
        return relation;
    }
    
    public IAtom buildDummyExtend(String polymorphicStr, IAtom dummyAtom, PolymorphicAbstract abstructAtom) throws IllegalAccessException {
        return new PseudoAtom(NamingRuleForAlloyable.polymorphicImplAtomName(polymorphicStr, dummyAtom.getName()), abstructAtom);
    }

    public List<IRelation> buildRelation(Function<String, IAtom> atomSearchByName,
            //List<? extends Atom> refToAtoms, 
            String polymorphicStr, String ownerTableName, IAtom polymAbstructAtom) throws IllegalAccessException {
        List<IRelation> relList = new ArrayList<>();
        IRulesForAlloyable namingRule = RulesForAlloyableFactory.getInstance().getRule();
        // 1/9
        MultipleRelation valueRelation = new MultipleRelation<RelationPolymorphic>();
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
                new MultipleRelation<AbstractRelationPolymorphic>();
        polymRelationReversed.originColumnName = polymorphicStr + namingRule.polymorphicSuffix();
        polymRelationReversed.name = "refTo_" + NamingRuleForAlloyable.tableAtomName(ownerTableName);
        polymRelationReversed.setRefTo(atomSearchByName.apply(NamingRuleForAlloyable.tableAtomName(ownerTableName)));
        //polymRelationReversed.reverseOfrefToTypes = refToAtoms;
        polymRelationReversed.setOwner(polymAbstructAtom);
        relList.add(polymRelationReversed);
        return relList;
    }
    
    public IRelation buildTypifiedRelation(IAtom extendedAtom, IAtom dummyAtom) throws IllegalAccessException {
        IRelation relation = new AbstractRelationPolymorphicTypified();
        relation.setName(namingRule.tableize(dummyAtom.getName()));
        relation.setOwner(extendedAtom);
        relation.setRefTo(dummyAtom);
        return relation;
    }

    public Fact buildFactBase(List<IRelation> relations) {
        String leftStr = new String();
        String rightStr = new String();
        for (IRelation relation : relations) {
        	IAtom owner = AlloyableHandler.getOwner(relation);
            if (relation.getClass().equals(AbstractRelationPolymorphic.class)) {
                rightStr = owner.getName() + "<:" + relation.getName();
            } else if (relation.getClass().equals(RelationPolymorphic.class)) {
                leftStr = owner.getName() + "<:" + relation.getName();
            }
        }
        Fact fact = new Fact(Fact.Tipify.RELATION);
        fact.value = leftStr + " = ~(" + rightStr + ")";
        fact.owners.addAll(relations);
        return fact;
    }

    public Fact buildFactForDummies(IRelation dummyRelation, IRelation parentRelation) {
    	IAtom dummyRelationOwner = AlloyableHandler.getOwner(dummyRelation);
    	IAtom parentRelationOwner = AlloyableHandler.getOwner(parentRelation);
        String leftStr = dummyRelationOwner.getName() + "<:" + dummyRelation.getName();
        String rightStr = parentRelationOwner.getName() + "<:" + parentRelation.getName() + "." + namingRule.tableize(dummyRelationOwner.getName());
        Fact fact = new Fact(Fact.Tipify.RELATION_POLYMOPHIC_COLUMN);
        fact.value = leftStr + " = ~(" + rightStr + ")";
        fact.owners.add(dummyRelation);
        fact.owners.add(parentRelation);
        return fact;
    }
}
