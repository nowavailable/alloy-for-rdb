package com.testdatadesigner.tdalloy.core.type_bulder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.testdatadesigner.tdalloy.core.naming.IRulesForAlloyable;
import com.testdatadesigner.tdalloy.core.naming.RulesForAlloyableFactory;
import com.testdatadesigner.tdalloy.core.types.PseudoAtom;
import com.testdatadesigner.tdalloy.core.types.Fact;
import com.testdatadesigner.tdalloy.core.types.MultipleRelation;
import com.testdatadesigner.tdalloy.core.types.Relation;
import com.testdatadesigner.tdalloy.core.types.Atom;

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
		Relation relation = new Relation(Relation.Tipify.ABSTRUCT_RELATION_REFERRED);
		String refTable = refTableName.isEmpty() ? namingRule
				.tableNameFromFKey(fKeyColumnStr) : refTableName;
        relation.name = namingRule.foreignKeyNameReversed(ownerTableName, refTable);
        //relation.name = namingRule.foreignKeyName(fKeyColumnStr, ownerTableName);
        relation.owner = atomSearchByName.apply(namingRule.tableAtomName(ownerTableName));
        relation.refTo =
                atomSearchByName.apply(namingRule.tableAtomNameFromFKey(fKeyColumnStr));
        return relation;
    }
    
    public Atom buildDummyExtend(String polymorphicStr, Atom dummyAtom, Atom abstructAtom) throws IllegalAccessException {
    	return new PseudoAtom(namingRule.polymorphicImplAtomName(polymorphicStr, dummyAtom.name), abstructAtom);
    }

//    public List<Atom> buildAtom(Function<String, Atom> atomSearchByName, List<? extends Atom> refToAtoms,
//            String polymorphicStr, String ownerTableName) throws IllegalAccessException {
//        List<Atom> atomList = new ArrayList<>();
//        // 4/9
//        Atom polymorphicAtom = new Atom(Atom.Tipify.POLYMORPHIC_ABSTRACT);
//        polymorphicAtom.originPropertyName = polymorphicStr + RulesForAlloyable.POLYMORPHIC_SUFFIX;
//        polymorphicAtom.name =
//                RulesForAlloyable.columnAtomName(polymorphicAtom.originPropertyName, ownerTableName);
//        polymorphicAtom.setParent(atomSearchByName.apply(RulesForAlloyable
//                .tableAtomName(ownerTableName)));
//        polymorphicAtom.isAbstruct = Boolean.TRUE;
//        atomList.add(polymorphicAtom);
//
//        // 6/9
//        // 8/9
//        if (refToAtoms.get(0).getClass().equals(PseudoAtom.class)) {
//            for (Atom dummyAtom : refToAtoms) {
//                PseudoAtom polymImpleAtom =
//                        new PseudoAtom(Atom.Tipify.POLYMOPHIC_IMPLIMENT, ((PseudoAtom)dummyAtom).namingSeq);
//                polymImpleAtom.setParent(polymorphicAtom);
//                polymImpleAtom.name =
//                        RulesForAlloyable.implementedPolymorphicAtomName(polymorphicStr,
//                            dummyAtom.originPropertyName);
//                atomList.add(polymImpleAtom);
//            }
//        } else {
//            for (Atom refToAtom : refToAtoms) {
//                Atom polymImpleAtom =
//                        new Atom(Atom.Tipify.POLYMOPHIC_IMPLIMENT);
//                polymImpleAtom.setParent(polymorphicAtom);
//                polymImpleAtom.name =
//                        RulesForAlloyable.implementedPolymorphicAtomName(polymorphicStr,
//                            refToAtom.originPropertyName);
//                atomList.add(polymImpleAtom);
//            }
//        }
//
//        return atomList;
//    }

    public List<Relation> buildRelation(Function<String, Atom> atomSearchByName,
            //List<? extends Atom> refToAtoms, 
            String polymorphicStr, String ownerTableName, Atom polymAbstructAtom) {
        List<Relation> relList = new ArrayList<>();
        IRulesForAlloyable namingRule = RulesForAlloyableFactory.getInstance().getRule();
        // 1/9
        MultipleRelation valueRelation = new MultipleRelation(Relation.Tipify.RELATION_POLYMOPHIC);
        valueRelation.name =
                namingRule.columnRelationName(
                    polymorphicStr + namingRule.polymorphic_suffix(), ownerTableName);
        valueRelation.owner = atomSearchByName.apply(namingRule.tableAtomName(ownerTableName));
        //valueRelation.refToTypes = refToAtoms;
        valueRelation.refTo = polymAbstructAtom;
        relList.add(valueRelation);

        // 5/9
        MultipleRelation polymRelationReversed =
                new MultipleRelation(Relation.Tipify.ABSTRUCT_RELATION);
        polymRelationReversed.name = "refTo_" + namingRule.tableAtomName(ownerTableName);
        polymRelationReversed.refTo =
                atomSearchByName.apply(namingRule.tableAtomName(ownerTableName));
        //polymRelationReversed.reverseOfrefToTypes = refToAtoms;
        polymRelationReversed.owner = polymAbstructAtom;
        relList.add(polymRelationReversed);

//        for (Atom refToAtom : refToAtoms) {
//            // 2/9
//            // 3/9
//            Relation relForRef = new Relation(Relation.Tipify.RELATION_REVERSED);
//            relForRef.name =
//                    RulesForAlloyable
//                            .foreignKeyNameReversed(refToAtom.originPropertyName, ownerTableName);
//            relForRef.owner = refToAtom;
//            relForRef.refTo = valueRelation.owner;
//            relList.add(relForRef);
//            // 7/9
//            // 9/9
//            Relation polymImpleRel = new Relation(Relation.Tipify.ABSTRUCT_RELATION_REVERSED);
//            polymImpleRel.name = RulesForAlloyable.singularize(refToAtom.name);
//            polymImpleRel.refTo = refToAtom;
//            polymImpleRel.owner =
//                    atomSearchByName.apply(RulesForAlloyable.polymorphicImplAtomName(polymorphicStr,
//                            refToAtom.name));
//            relList.add(polymImpleRel);
//        }
        return relList;
    }

    public Fact buildFactBase(List<Relation> relations) {
        String leftStr = new String();
        String rightStr = new String();
        for (Relation relation : relations) {
            if (relation.type.equals(Relation.Tipify.ABSTRUCT_RELATION)) {
                rightStr = relation.owner.name + "<:" + relation.name;
            } else if (relation.type.equals(Relation.Tipify.RELATION_POLYMOPHIC)) {
                leftStr = relation.owner.name + "<:" + relation.name;
            }
        }
        Fact fact = new Fact(Fact.Tipify.RELATION);
        fact.value = leftStr + " = ~(" + rightStr + ")";
        fact.owners.addAll(relations);
        return fact;
    }
    
//    public List<Fact> buildFact(List<Relation> relations, List<? extends Atom> refToAtoms) {
//
//        List<Fact> factList = new ArrayList<>();
//        Relation rootOwnerRel = null;
//
//        Fact factForColumn = new Fact(Fact.Tipify.RELATION_POLYMOPHIC_COLUMN);
//        String leftStrForColumn = new String();
//        String rightStrForColumn = new String();
//        for (Relation relation : relations) {
//            if (relation.type.equals(Relation.Tipify.ABSTRUCT_RELATION)) {
//                rightStrForColumn = relation.name;
//                factForColumn.owners.add(relation);
//            }
//            if (relation.type.equals(Relation.Tipify.RELATION_POLYMOPHIC)) {
//                leftStrForColumn = relation.name;
//                factForColumn.owners.add(relation);
//                
//                rootOwnerRel = relation;
//            }
//        }
//        factForColumn.value = leftStrForColumn + " = ~" + rightStrForColumn;
//        factList.add(factForColumn);
//        
//        List<Relation> workList = new ArrayList<>();
//        relations.forEach(rel -> {
//            if (rel.type.equals(Relation.Tipify.RELATION_REVERSED)
//                    | rel.type.equals(Relation.Tipify.ABSTRUCT_RELATION_REVERSED)) {
//                workList.add(rel);
//            }
//        });
//        
//        for (Atom refToAtom : refToAtoms) {
//            Fact factForPolymorphic = new Fact(Fact.Tipify.RELATION_POLYMOPHIC);
//            String leftStr =
//                    workList.stream()
//                            .filter(rel -> rel.type.equals(Relation.Tipify.RELATION_REVERSED)
//                                    && rel.owner.equals(refToAtom)).collect(Collectors.toList())
//                            .get(0).name;
//            String rightStr =
//                    workList.stream()
//                            .filter(rel -> rel.type.equals(Relation.Tipify.ABSTRUCT_RELATION_REVERSED)
//                                    && rel.refTo.equals(refToAtom)).collect(Collectors.toList())
//                            .get(0).name;
//            factForPolymorphic.value = leftStr + " = ~(" + leftStrForColumn + "." + rightStr + ")";
//            factForPolymorphic.owners.add(rootOwnerRel);
//            
//            factList.add(factForPolymorphic);
//        }
//        return factList;
//    }
}
