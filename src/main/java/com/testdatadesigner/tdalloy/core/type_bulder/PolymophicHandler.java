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

public class PolymophicHandler {

    public List<DummySig> buildDummies(Supplier<Integer> getNamingSeq, String ownerTableName) {
        // ダミー作成
        DummySig dummyRefToSig_1 = new DummySig(Sig.Tipify.ENTITY, getNamingSeq.get());
        DummySig dummyRefToSig_2 = new DummySig(Sig.Tipify.ENTITY, getNamingSeq.get());
        return Arrays.asList(dummyRefToSig_1, dummyRefToSig_2);
    }

    public List<Sig> buildSig(Function<String, Sig> sigSearchByName, List<? extends Sig> refToSigs,
            String polymophicStr, String ownerTableName) throws IllegalAccessException {
        List<Sig> sigList = new ArrayList<>();
        // 4/9
        Sig polymophiSig = new Sig(Sig.Tipify.POLYMOPHIC_TYPE_ABSTRACT);
        polymophiSig.originPropertyName = polymophicStr + RulesForAlloyable.POLYMOPHIC_SUFFIX;
        polymophiSig.name =
                RulesForAlloyable.colmnSigName(polymophiSig.originPropertyName, ownerTableName);
        polymophiSig.setParent(sigSearchByName.apply(RulesForAlloyable
                .tableSigName(ownerTableName)));
        polymophiSig.isAbstruct = Boolean.TRUE;
        sigList.add(polymophiSig);

        // 6/9
        // 8/9
        if (refToSigs.get(0).getClass().equals(DummySig.class)) {
            for (Sig dummySig : refToSigs) {
                DummySig polymImpleSig =
                        new DummySig(Sig.Tipify.POLYMOPHIC_IMPLIMENT, ((DummySig)dummySig).namingSeq);
                polymImpleSig.setParent(polymophiSig);
                polymImpleSig.name =
                        RulesForAlloyable.implimentedPolymophicSigName(polymophicStr,
                                dummySig.originPropertyName);
                sigList.add(polymImpleSig);
            }
        } else {
            for (Sig refToSig : refToSigs) {
                Sig polymImpleSig =
                        new Sig(Sig.Tipify.POLYMOPHIC_IMPLIMENT);
                polymImpleSig.setParent(polymophiSig);
                polymImpleSig.name =
                        RulesForAlloyable.implimentedPolymophicSigName(polymophicStr,
                                refToSig.originPropertyName);
                sigList.add(polymImpleSig);
            }
        }

        return sigList;
    }

    public List<Relation> buildRelation(Function<String, Sig> sigSearchByName,
            List<DummySig> twoDummyRefToSigs, String polymophicStr, String ownerTableName) {
        // 1/9
        MultipleRelation<DummySig> valueRelation = new MultipleRelation<>(Relation.Tipify.VALUE);
        valueRelation.name =
                RulesForAlloyable.colmnRelationName(polymophicStr
                        + RulesForAlloyable.POLYMOPHIC_SUFFIX, ownerTableName);
        valueRelation.owner = sigSearchByName.apply(RulesForAlloyable.tableSigName(ownerTableName));
        valueRelation.refToTypes = Arrays.asList(twoDummyRefToSigs.get(0), twoDummyRefToSigs.get(1));
        // 2/9
        // 3/9
        Relation relForDummy1 = new Relation(Relation.Tipify.RELATION_REVERSED);
        relForDummy1.name =
                RulesForAlloyable
                        .foreignKeyNameReversed(twoDummyRefToSigs.get(0).originPropertyName, ownerTableName);
        relForDummy1.owner = twoDummyRefToSigs.get(0);
        relForDummy1.refTo = valueRelation.owner;

        Relation relForDummy2 = new Relation(Relation.Tipify.RELATION_REVERSED);
        relForDummy2.name =
                RulesForAlloyable
                        .foreignKeyNameReversed(twoDummyRefToSigs.get(1).originPropertyName, ownerTableName);
        relForDummy2.owner = twoDummyRefToSigs.get(1);
        relForDummy2.refTo = valueRelation.owner;

        // 5/9
        MultipleRelation<DummySig> polymRelationReversed =
                new MultipleRelation<>(Relation.Tipify.ABSTRUCT_RELATION);
        polymRelationReversed.name = "refTo_" + RulesForAlloyable.tableSigName(ownerTableName);
        polymRelationReversed.refTo =
                sigSearchByName.apply(RulesForAlloyable.tableSigName(ownerTableName));
        polymRelationReversed.reverseOfrefToTypes =
                Arrays.asList(twoDummyRefToSigs.get(0), twoDummyRefToSigs.get(1));

        // 7/9
        // 9/9
        Relation polymImpleRel_1 = new Relation(Relation.Tipify.ABSTRUCT_RELATION_REVERSED);
        polymImpleRel_1.name = RulesForAlloyable.singularize(twoDummyRefToSigs.get(0).name);
        polymImpleRel_1.refTo = twoDummyRefToSigs.get(0);
        polymImpleRel_1.owner =
                sigSearchByName.apply(RulesForAlloyable.polymophicImplSigName(polymophicStr,
                        twoDummyRefToSigs.get(0).name));
        Relation polymImpleRel_2 = new Relation(Relation.Tipify.ABSTRUCT_RELATION_REVERSED);
        polymImpleRel_2.name = RulesForAlloyable.singularize(twoDummyRefToSigs.get(1).name);
        polymImpleRel_2.refTo = twoDummyRefToSigs.get(1);
        polymImpleRel_2.owner =
                sigSearchByName.apply(RulesForAlloyable.polymophicImplSigName(polymophicStr,
                        twoDummyRefToSigs.get(1).name));

        return Arrays.asList(valueRelation, relForDummy1, relForDummy2, polymRelationReversed,
                polymImpleRel_1, polymImpleRel_2);
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
            Fact factForPolymophic = new Fact(Fact.Tipify.RELATION_POLYMOPHIC);
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
            factForPolymophic.value = leftStr + " = ~(" + leftStrForColumn + "." + rightStr + ")";
            factForPolymophic.owners.add(rootOwnerRel);
            
            factList.add(factForPolymophic);
        }
        return factList;
    }
}
