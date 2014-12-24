package com.testdatadesigner.tdalloy.core.type_bulder;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

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

    public List<Sig> buildSig(Function<String, Sig> sigSearchByName, List<DummySig> twoDummyRefToSigs,
            String polymophicStr, String ownerTableName) throws IllegalAccessException {
        // 4/9
        Sig polymophicSig = new Sig(Sig.Tipify.POLYMOPHIC_TYPE_ABSTRACT);
        polymophicSig.originPropertyName = polymophicStr + RulesForAlloyable.POLYMOPHIC_SUFFIX;
        polymophicSig.name =
                RulesForAlloyable.colmnSigName(polymophicSig.originPropertyName, ownerTableName);
        polymophicSig.setParent(sigSearchByName.apply(RulesForAlloyable
                .tableSigName(ownerTableName)));
        polymophicSig.isAbstruct = Boolean.TRUE;

        // 6/9
        // 8/9
        DummySig polymImpleSig_1 =
                new DummySig(Sig.Tipify.POLYMOPHIC_IMPLIMENT, twoDummyRefToSigs.get(0).namingSeq);
        polymImpleSig_1.setParent(polymophicSig);
        polymImpleSig_1.name =
                RulesForAlloyable.implimentedPolymophicSigName(polymophicStr,
                        twoDummyRefToSigs.get(0).originPropertyName);
        DummySig polymImpleSig_2 =
                new DummySig(Sig.Tipify.POLYMOPHIC_IMPLIMENT, twoDummyRefToSigs.get(1).namingSeq);
        polymImpleSig_2.setParent(polymophicSig);
        polymImpleSig_2.name =
                RulesForAlloyable.implimentedPolymophicSigName(polymophicStr,
                        twoDummyRefToSigs.get(1).originPropertyName);

        return Arrays.asList(polymophicSig, polymImpleSig_1, polymImpleSig_2);
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
    
//    public List<Fact> buildFact(List<Relation> relations) {
//        Fact factForColumn = new Fact(Fact.Tipify.RELATION_POLYMOPHIC_COLUMN);
//        String leftStrForColumn = new String();
//        String rightStrForColumn = new String();
//        for (Relation relation : relations) {
//            if (relation.type.equals(Relation.Tipify.ABSTRUCT_RELATION)) {
//                rightStrForColumn = relation.name;
//            }
//            if (relation.type.equals(Relation.Tipify.VALUE)) {
//                leftStrForColumn = relation.name;
//            }
//        }
//        factForColumn.value = leftStrForColumn + " = ~" + rightStrForColumn;
//        
//        return Arrays.asList(factForColumn);
//    }
}
