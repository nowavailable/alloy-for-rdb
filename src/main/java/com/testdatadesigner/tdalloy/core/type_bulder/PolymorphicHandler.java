package com.testdatadesigner.tdalloy.core.type_bulder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.testdatadesigner.tdalloy.core.naming.IRulesForAlloyable;
import com.testdatadesigner.tdalloy.core.naming.RulesForAlloyableFactory;
import com.testdatadesigner.tdalloy.core.types.RelationPolymorphicMain;
import com.testdatadesigner.tdalloy.core.types.RelationPolymorphicTypified;
import com.testdatadesigner.tdalloy.core.types.AlloyableHandler;
import com.testdatadesigner.tdalloy.core.types.IAtom;
import com.testdatadesigner.tdalloy.core.types.IRelation;
import com.testdatadesigner.tdalloy.core.types.PolymorphicAbstract;
import com.testdatadesigner.tdalloy.core.types.PseudoAtom;
import com.testdatadesigner.tdalloy.core.types.Fact;
import com.testdatadesigner.tdalloy.core.types.NamingRuleForAlloyable;
import com.testdatadesigner.tdalloy.core.types.RelationPolymorphicTypeHolder;

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
        IRelation relation = new RelationPolymorphicMain();
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

        RelationPolymorphicTypeHolder valueRelation = new RelationPolymorphicTypeHolder();
        valueRelation.originColumnNames = Arrays.asList(polymorphicStr + namingRule.polymorphicSuffix());
        valueRelation.name =
                NamingRuleForAlloyable.columnRelationName(
                    polymorphicStr + namingRule.polymorphicSuffix(), ownerTableName);
        valueRelation.setOwner(atomSearchByName.apply(NamingRuleForAlloyable.tableAtomName(ownerTableName)));
        valueRelation.setRefTo(polymAbstructAtom);
        relList.add(valueRelation);
        return relList;
    }
    
    public IRelation buildTypifiedRelation(IAtom extendedAtom, IAtom dummyAtom) throws IllegalAccessException {
        IRelation relation = new RelationPolymorphicTypified();
        relation.setName(namingRule.tableize(dummyAtom.getName()));
        relation.setOwner(extendedAtom);
        relation.setRefTo(dummyAtom);
        return relation;
    }

    public Fact buildFactBase(List<IRelation> relations) {
        String leftStr = new String();
        for (IRelation relation : relations) {
        	IAtom owner = relation.getOwner();
        	if (relation.getClass().equals(RelationPolymorphicTypeHolder.class)) {
	            String alias = owner.getOriginPropertyName().substring(0, 1);
	            String f = "all " + alias + ":" + owner.getName() + " | "
	            		+ alias + " = " + "(" + owner.getName() + "<:" + relation.getName() + ").(" + alias + ".(" +  owner.getName() + "<:" + relation.getName() + "))";
	            leftStr = "(" + owner.getName() + "." + relation.getName() + " = " + relation.getRefTo().getName() + ") and " 
	            		+ "(" + f + ")";
            }
        }
        Fact fact = new Fact(Fact.Tipify.RELATION);
        fact.value = leftStr; //+ " = ~(" + rightStr + ")";
        fact.owners.addAll(relations);
        return fact;
    }

    public Fact buildFactForDummies(IRelation dummyRelation, IRelation parentRelation) {
    	IAtom dummyRelationOwner = dummyRelation.getOwner();
    	IAtom parentRelationOwner = parentRelation.getOwner();
        String leftStr = dummyRelationOwner.getName() + "<:" + dummyRelation.getName();
        String rightStr = parentRelationOwner.getName() + "<:" + parentRelation.getName() + "." + namingRule.tableize(dummyRelationOwner.getName());
        Fact fact = new Fact(Fact.Tipify.RELATION_POLYMOPHIC_COLUMN);
        fact.value = leftStr + " = ~(" + rightStr + ")";
        fact.owners.add(dummyRelation);
        fact.owners.add(parentRelation);
        return fact;
    }
}
