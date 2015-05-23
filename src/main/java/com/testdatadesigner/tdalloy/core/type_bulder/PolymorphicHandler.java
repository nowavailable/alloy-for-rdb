package com.testdatadesigner.tdalloy.core.type_bulder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.testdatadesigner.tdalloy.core.types.DummySig;
import com.testdatadesigner.tdalloy.core.types.Fact;
import com.testdatadesigner.tdalloy.core.types.MultipleRelation;
import com.testdatadesigner.tdalloy.core.types.Relation;
import com.testdatadesigner.tdalloy.core.types.RulesForAlloyable;
import com.testdatadesigner.tdalloy.core.types.Sig;

public class PolymorphicHandler {

    public List<DummySig> buildDummies(Supplier<Integer> getNamingSeq, String ownerTableName) {
        // ダミー作成
        DummySig dummyRefToSig_1 = new DummySig(Sig.Tipify.ENTITY, getNamingSeq.get());
        DummySig dummyRefToSig_2 = new DummySig(Sig.Tipify.ENTITY, getNamingSeq.get());
        return Arrays.asList(dummyRefToSig_1, dummyRefToSig_2);
    }

    public List<Sig> buildSig(Function<String, Sig> sigSearchByName, List<? extends Sig> refToSigs,
            String polymorphicStr, String ownerTableName) throws IllegalAccessException {
        List<Sig> sigList = new ArrayList<>();
        // 4/9
        Sig polymorphicSig = new Sig(Sig.Tipify.POLYMORPHIC_TYPE_ABSTRACT);
        polymorphicSig.originPropertyName = polymorphicStr + RulesForAlloyable.POLYMORPHIC_SUFFIX;
        polymorphicSig.name =
                RulesForAlloyable.columnSigName(polymorphicSig.originPropertyName, ownerTableName);
        polymorphicSig.setParent(sigSearchByName.apply(RulesForAlloyable
                .tableSigName(ownerTableName)));
        polymorphicSig.isAbstruct = Boolean.TRUE;
        sigList.add(polymorphicSig);

        // 6/9
        // 8/9
        if (refToSigs.get(0).getClass().equals(DummySig.class)) {
            for (Sig dummySig : refToSigs) {
                DummySig polymImpleSig =
                        new DummySig(Sig.Tipify.POLYMOPHIC_IMPLIMENT, ((DummySig)dummySig).namingSeq);
                polymImpleSig.setParent(polymorphicSig);
                polymImpleSig.name =
                        RulesForAlloyable.implementedPolymorphicSigName(polymorphicStr,
                            dummySig.originPropertyName);
                sigList.add(polymImpleSig);
            }
        } else {
            for (Sig refToSig : refToSigs) {
                Sig polymImpleSig =
                        new Sig(Sig.Tipify.POLYMOPHIC_IMPLIMENT);
                polymImpleSig.setParent(polymorphicSig);
                polymImpleSig.name =
                        RulesForAlloyable.implementedPolymorphicSigName(polymorphicStr,
                            refToSig.originPropertyName);
                sigList.add(polymImpleSig);
            }
        }

        return sigList;
    }

    public List<Relation> buildRelation(Function<String, Sig> sigSearchByName,
            List<? extends Sig> refToSigs, String polymorphicStr, String ownerTableName) {
        List<Relation> relList = new ArrayList<>();
        // 1/9
        MultipleRelation valueRelation = new MultipleRelation(Relation.Tipify.VALUE);
        valueRelation.name =
                RulesForAlloyable.columnRelationName(
                    polymorphicStr + RulesForAlloyable.POLYMORPHIC_SUFFIX, ownerTableName);
        valueRelation.owner = sigSearchByName.apply(RulesForAlloyable.tableSigName(ownerTableName));
        valueRelation.refToTypes = refToSigs;
        relList.add(valueRelation);

        // 5/9
        MultipleRelation polymRelationReversed =
                new MultipleRelation(Relation.Tipify.ABSTRUCT_RELATION);
        polymRelationReversed.name = "refTo_" + RulesForAlloyable.tableSigName(ownerTableName);
        polymRelationReversed.refTo =
                sigSearchByName.apply(RulesForAlloyable.tableSigName(ownerTableName));
        polymRelationReversed.reverseOfrefToTypes = refToSigs;
        relList.add(polymRelationReversed);

        for (Sig refToSig : refToSigs) {
            // 2/9
            // 3/9
            Relation relForRef = new Relation(Relation.Tipify.RELATION_REVERSED);
            relForRef.name =
                    RulesForAlloyable
                            .foreignKeyNameReversed(refToSig.originPropertyName, ownerTableName);
            relForRef.owner = refToSig;
            relForRef.refTo = valueRelation.owner;
            relList.add(relForRef);
            // 7/9
            // 9/9
            Relation polymImpleRel = new Relation(Relation.Tipify.ABSTRUCT_RELATION_REVERSED);
            polymImpleRel.name = RulesForAlloyable.singularize(refToSig.name);
            polymImpleRel.refTo = refToSig;
            polymImpleRel.owner =
                    sigSearchByName.apply(RulesForAlloyable.polymorphicImplSigName(polymorphicStr,
                            refToSig.name));
            relList.add(polymImpleRel);
        }
        return relList;
    }
    
    public List<Fact> buildFact(List<Relation> relations, List<? extends Sig> refToSigs) {

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
        
        for (Sig refToSig : refToSigs) {
            Fact factForPolymorphic = new Fact(Fact.Tipify.RELATION_POLYMOPHIC);
            String leftStr =
                    workList.stream()
                            .filter(rel -> rel.type.equals(Relation.Tipify.RELATION_REVERSED)
                                    && rel.owner.equals(refToSig)).collect(Collectors.toList())
                            .get(0).name;
            String rightStr =
                    workList.stream()
                            .filter(rel -> rel.type.equals(Relation.Tipify.ABSTRUCT_RELATION_REVERSED)
                                    && rel.refTo.equals(refToSig)).collect(Collectors.toList())
                            .get(0).name;
            factForPolymorphic.value = leftStr + " = ~(" + leftStrForColumn + "." + rightStr + ")";
            factForPolymorphic.owners.add(rootOwnerRel);
            
            factList.add(factForPolymorphic);
        }
        return factList;
    }
}
