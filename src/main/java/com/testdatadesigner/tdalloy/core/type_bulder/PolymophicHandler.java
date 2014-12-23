package com.testdatadesigner.tdalloy.core.type_bulder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.testdatadesigner.tdalloy.core.types.DummySig;
import com.testdatadesigner.tdalloy.core.types.MultipleRelation;
import com.testdatadesigner.tdalloy.core.types.Relation;
import com.testdatadesigner.tdalloy.core.types.RulesForAlloyable;
import com.testdatadesigner.tdalloy.core.types.Sig;

public class PolymophicHandler {

    public List<DummySig> buildDummies(Supplier<Integer> getNamingSeq, String ownerTableName) {
        // ダミー作成
        DummySig refToDummySig_1 = new DummySig(Sig.Tipify.ENTITY, getNamingSeq.get());
        DummySig refToDummySig_2 = new DummySig(Sig.Tipify.ENTITY, getNamingSeq.get());
        return Arrays.asList(refToDummySig_1, refToDummySig_2);
    }

    public List<Sig> buildSig(Function<String, Sig> sigSearchByName, List<DummySig> twoDummySigs,
            String polymophicStr, String ownerTableName) throws IllegalAccessException {
        List<Sig> list = new ArrayList<>();
        // 4/9
        Sig polymophicSig = new Sig(Sig.Tipify.POLYMOPHIC_TYPE_ABSTRACT);
        polymophicSig.originPropertyName = polymophicStr + RulesForAlloyable.POLYMOPHIC_SUFFIX;
        polymophicSig.name =
                RulesForAlloyable.colmnSigName(polymophicSig.originPropertyName, ownerTableName);
        polymophicSig.setParent(sigSearchByName.apply(RulesForAlloyable
                .tableSigName(ownerTableName)));
        polymophicSig.isAbstruct = Boolean.TRUE;
        list.add(polymophicSig);

        // 6/9
        // 8/9
        DummySig polymImpleSig_1 =
                new DummySig(Sig.Tipify.POLYMOPHIC_IMPLIMENT, twoDummySigs.get(0).namingSeq);
        polymImpleSig_1.setParent(polymophicSig);
        polymImpleSig_1.name =
                RulesForAlloyable.implimentedPolymophicSigName(polymophicStr,
                        twoDummySigs.get(0).originPropertyName);
        list.add(polymImpleSig_1);
        DummySig polymImpleSig_2 =
                new DummySig(Sig.Tipify.POLYMOPHIC_IMPLIMENT, twoDummySigs.get(1).namingSeq);
        polymImpleSig_2.setParent(polymophicSig);
        polymImpleSig_2.name =
                RulesForAlloyable.implimentedPolymophicSigName(polymophicStr,
                        twoDummySigs.get(1).originPropertyName);
        list.add(polymImpleSig_2);

        return list;
    }

    public List<Relation> buildRelation(Function<String, Sig> sigSearchByName,
            List<DummySig> dummyRefToSigs, String polymophicStr, String ownerTableName) {
        List<Relation> list = new ArrayList<>();
        // 1/9
        MultipleRelation<DummySig> valueRelation = new MultipleRelation<>(Relation.Tipify.VALUE);
        valueRelation.name =
                RulesForAlloyable.colmnRelationName(polymophicStr
                        + RulesForAlloyable.POLYMOPHIC_SUFFIX, ownerTableName);
        valueRelation.owner = sigSearchByName.apply(RulesForAlloyable.tableSigName(ownerTableName));
        valueRelation.refToTypes = Arrays.asList(dummyRefToSigs.get(0), dummyRefToSigs.get(1));
        list.add(valueRelation);
        // 2/9
        // 3/9
        Relation relForDummy1 = new Relation(Relation.Tipify.RELATION_REVERSED);
        relForDummy1.name =
                RulesForAlloyable
                        .foreignKeyNameReversed(ownerTableName, dummyRefToSigs.get(0).name);
        relForDummy1.owner = dummyRefToSigs.get(0);
        relForDummy1.refTo = valueRelation.owner;
        list.add(relForDummy1);

        Relation relForDummy2 = new Relation(Relation.Tipify.RELATION_REVERSED);
        relForDummy2.name =
                RulesForAlloyable
                        .foreignKeyNameReversed(ownerTableName, dummyRefToSigs.get(1).name);
        relForDummy2.owner = dummyRefToSigs.get(1);
        relForDummy2.refTo = valueRelation.owner;
        list.add(relForDummy2);

        // 5/9
        MultipleRelation<DummySig> polymRelationReversed =
                new MultipleRelation<>(Relation.Tipify.ABSTRUCT_RELATION);
        polymRelationReversed.name = "refTo_" + RulesForAlloyable.tableSigName(ownerTableName);
        polymRelationReversed.refTo =
                sigSearchByName.apply(RulesForAlloyable.tableSigName(ownerTableName));
        polymRelationReversed.reverseOfrefToTypes =
                Arrays.asList(dummyRefToSigs.get(0), dummyRefToSigs.get(1));
        list.add(polymRelationReversed);

        // 7/9
        // 9/9
        Relation polymImpleRel_1 = new Relation(Relation.Tipify.ABSTRUCT_RELATION_REVERSED);
        polymImpleRel_1.name = RulesForAlloyable.singularize(dummyRefToSigs.get(0).name);
        polymImpleRel_1.refTo = dummyRefToSigs.get(0);
        polymImpleRel_1.owner = dummyRefToSigs.get(0);
        list.add(polymImpleRel_1);
        Relation polymImpleRel_2 = new Relation(Relation.Tipify.ABSTRUCT_RELATION_REVERSED);
        polymImpleRel_2.name = RulesForAlloyable.singularize(dummyRefToSigs.get(1).name);
        polymImpleRel_2.refTo = dummyRefToSigs.get(1);
        polymImpleRel_2.owner = dummyRefToSigs.get(1);
        list.add(polymImpleRel_2);

        return list;
    }
}
