package com.testdatadesigner.tdalloy.core.type_bulder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.testdatadesigner.tdalloy.core.types.PseudoAtom;
import com.testdatadesigner.tdalloy.core.types.Fact;
import com.testdatadesigner.tdalloy.core.types.MultipleRelation;
import com.testdatadesigner.tdalloy.core.types.Relation;
import com.testdatadesigner.tdalloy.core.types.RulesForAlloyable;
import com.testdatadesigner.tdalloy.core.types.Atom;

public class PolymorphicHandler {

    public List<PseudoAtom> buildDummies(Supplier<Integer> getNamingSeq, String ownerTableName) {
        // ダミー作成
        PseudoAtom dummyRefToAtom_1 = new PseudoAtom(Atom.Tipify.ENTITY, getNamingSeq.get());
        PseudoAtom dummyRefToAtom_2 = new PseudoAtom(Atom.Tipify.ENTITY, getNamingSeq.get());
        return Arrays.asList(dummyRefToAtom_1, dummyRefToAtom_2);
    }

    public List<Atom> buildAtom(Function<String, Atom> atomSearchByName, List<? extends Atom> refToAtoms,
            String polymorphicStr, String ownerTableName) throws IllegalAccessException {
        List<Atom> atomList = new ArrayList<>();
        // 4/9
        Atom polymorphicAtom = new Atom(Atom.Tipify.POLYMORPHIC_TYPE_ABSTRACT);
        polymorphicAtom.originPropertyName = polymorphicStr + RulesForAlloyable.POLYMORPHIC_SUFFIX;
        polymorphicAtom.name =
                RulesForAlloyable.columnAtomName(polymorphicAtom.originPropertyName, ownerTableName);
        polymorphicAtom.setParent(atomSearchByName.apply(RulesForAlloyable
                .tableAtomName(ownerTableName)));
        polymorphicAtom.isAbstruct = Boolean.TRUE;
        atomList.add(polymorphicAtom);

        // 6/9
        // 8/9
        if (refToAtoms.get(0).getClass().equals(PseudoAtom.class)) {
            for (Atom dummyAtom : refToAtoms) {
                PseudoAtom polymImpleAtom =
                        new PseudoAtom(Atom.Tipify.POLYMOPHIC_IMPLIMENT, ((PseudoAtom)dummyAtom).namingSeq);
                polymImpleAtom.setParent(polymorphicAtom);
                polymImpleAtom.name =
                        RulesForAlloyable.implementedPolymorphicAtomName(polymorphicStr,
                            dummyAtom.originPropertyName);
                atomList.add(polymImpleAtom);
            }
        } else {
            for (Atom refToAtom : refToAtoms) {
                Atom polymImpleAtom =
                        new Atom(Atom.Tipify.POLYMOPHIC_IMPLIMENT);
                polymImpleAtom.setParent(polymorphicAtom);
                polymImpleAtom.name =
                        RulesForAlloyable.implementedPolymorphicAtomName(polymorphicStr,
                            refToAtom.originPropertyName);
                atomList.add(polymImpleAtom);
            }
        }

        return atomList;
    }

    public List<Relation> buildRelation(Function<String, Atom> atomSearchByName,
            List<? extends Atom> refToAtoms, String polymorphicStr, String ownerTableName) {
        List<Relation> relList = new ArrayList<>();
        // 1/9
        MultipleRelation valueRelation = new MultipleRelation(Relation.Tipify.VALUE);
        valueRelation.name =
                RulesForAlloyable.columnRelationName(
                    polymorphicStr + RulesForAlloyable.POLYMORPHIC_SUFFIX, ownerTableName);
        valueRelation.owner = atomSearchByName.apply(RulesForAlloyable.tableAtomName(ownerTableName));
        valueRelation.refToTypes = refToAtoms;
        relList.add(valueRelation);

        // 5/9
        MultipleRelation polymRelationReversed =
                new MultipleRelation(Relation.Tipify.ABSTRUCT_RELATION);
        polymRelationReversed.name = "refTo_" + RulesForAlloyable.tableAtomName(ownerTableName);
        polymRelationReversed.refTo =
                atomSearchByName.apply(RulesForAlloyable.tableAtomName(ownerTableName));
        polymRelationReversed.reverseOfrefToTypes = refToAtoms;
        relList.add(polymRelationReversed);

        for (Atom refToAtom : refToAtoms) {
            // 2/9
            // 3/9
            Relation relForRef = new Relation(Relation.Tipify.RELATION_REVERSED);
            relForRef.name =
                    RulesForAlloyable
                            .foreignKeyNameReversed(refToAtom.originPropertyName, ownerTableName);
            relForRef.owner = refToAtom;
            relForRef.refTo = valueRelation.owner;
            relList.add(relForRef);
            // 7/9
            // 9/9
            Relation polymImpleRel = new Relation(Relation.Tipify.ABSTRUCT_RELATION_REVERSED);
            polymImpleRel.name = RulesForAlloyable.singularize(refToAtom.name);
            polymImpleRel.refTo = refToAtom;
            polymImpleRel.owner =
                    atomSearchByName.apply(RulesForAlloyable.polymorphicImplAtomName(polymorphicStr,
                            refToAtom.name));
            relList.add(polymImpleRel);
        }
        return relList;
    }
    
    public List<Fact> buildFact(List<Relation> relations, List<? extends Atom> refToAtoms) {

        List<Fact> factList = new ArrayList<>();
        Relation rootOwnerRel = null;

        Fact factForColumn = new Fact(Fact.Tipify.RELATION_POLYMOPHIC_COLUMN);
        String leftStrForColumn = new String();
        String rightStrForColumn = new String();
        for (Relation relation : relations) {
            if (relation.type.equals(Relation.Tipify.ABSTRUCT_RELATION)) {
                rightStrForColumn = relation.name;
                factForColumn.owners.add(relation);
            }
            if (relation.type.equals(Relation.Tipify.VALUE)) {
                leftStrForColumn = relation.name;
                factForColumn.owners.add(relation);
                
                rootOwnerRel = relation;
            }
        }
        factForColumn.value = leftStrForColumn + " = ~" + rightStrForColumn;
        factList.add(factForColumn);
        
        List<Relation> workList = new ArrayList<>();
        relations.forEach(rel -> {
            if (rel.type.equals(Relation.Tipify.RELATION_REVERSED)
                    | rel.type.equals(Relation.Tipify.ABSTRUCT_RELATION_REVERSED)) {
                workList.add(rel);
            }
        });
        
        for (Atom refToAtom : refToAtoms) {
            Fact factForPolymorphic = new Fact(Fact.Tipify.RELATION_POLYMOPHIC);
            String leftStr =
                    workList.stream()
                            .filter(rel -> rel.type.equals(Relation.Tipify.RELATION_REVERSED)
                                    && rel.owner.equals(refToAtom)).collect(Collectors.toList())
                            .get(0).name;
            String rightStr =
                    workList.stream()
                            .filter(rel -> rel.type.equals(Relation.Tipify.ABSTRUCT_RELATION_REVERSED)
                                    && rel.refTo.equals(refToAtom)).collect(Collectors.toList())
                            .get(0).name;
            factForPolymorphic.value = leftStr + " = ~(" + leftStrForColumn + "." + rightStr + ")";
            factForPolymorphic.owners.add(rootOwnerRel);
            
            factList.add(factForPolymorphic);
        }
        return factList;
    }
}
