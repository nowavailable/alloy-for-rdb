package com.testdatadesigner.tdalloy.core.type_bulder;

import java.util.function.Function;

import com.testdatadesigner.tdalloy.core.types.Relation;
import com.testdatadesigner.tdalloy.core.types.RulesForAlloyable;
import com.testdatadesigner.tdalloy.core.types.Sig;

public class DefaultColumnHandler {

    public Relation buildRelation(Function<String, Sig> sigSearchByName, String ownerTableName,
            String columnName) {
        Relation relation = new Relation(Relation.Tipify.VALUE);
        relation.owner = sigSearchByName.apply(RulesForAlloyable.tableSigName(ownerTableName));
        relation.name = columnName;
        Sig column = new Sig(Sig.Tipify.PROPERTY);
        column.name = "Boundary";
        relation.refTo = column;
        return relation;
    }

    public Sig buildSig(Function<String, Sig> sigSearchByName, String ownerTableName,
            String columnName) throws IllegalAccessException {
        Sig colomnSig = new Sig(Sig.Tipify.PROPERTY);
        colomnSig.originPropertyName = columnName;
        colomnSig.name = RulesForAlloyable.columnSigName(columnName, ownerTableName);
        colomnSig.setParent(sigSearchByName.apply(RulesForAlloyable.tableSigName(ownerTableName)));
        return colomnSig;
    }

    public Sig buildSigPolymorphicProspected(Function<String, Sig> sigSearchByName, String ownerTableName,
            String columnName) throws IllegalAccessException {
        Sig colomnSig = buildSig(sigSearchByName, ownerTableName, columnName);
        colomnSig.type = Sig.Tipify.POLIMORPHIC_PROTOTYPE;
        return colomnSig;
    }
    
//    public List<Sig> buildFactorSigs(String ownerTableName, String columnName) {
//        List<String> factors = new ArrayList<String>() {
//            {
//                Inflector inflector = Inflector.getInstance();
//                this.add(new String(ownerTableName + RulesForAlloyable.COUPLER
//                        + inflector.upperCamelCase(columnName) + "HIGH"));
//                this.add(new String(ownerTableName + RulesForAlloyable.COUPLER
//                        + inflector.upperCamelCase(columnName) + "LOW"));
//            }
//        };
//        List<Sig> sigs = new ArrayList<>();
//        for (String factor : factors) {
//            Sig sig = new Sig(Sig.Tipify.PROPERTY_FACTOR);
//            sig.originPropertyName = columnName;
//            sig.name = factor;
//            sigs.add(sig);
//        }
//        return sigs;
//    }

//    public Relation buildRelation(Function<String, Sig> sigSearchByName, String ownerTableName,
//            String columnName, List<Sig> propertyFactorSigs) {
//        MultipleRelation colomnRel = new MultipleRelation(Relation.Tipify.VALUE);
//        colomnRel.name = RulesForAlloyable.columnRelationName(columnName, ownerTableName);
//        colomnRel.owner = sigSearchByName.apply(RulesForAlloyable.tableSigName(ownerTableName));
//        colomnRel.refToTypes = propertyFactorSigs;
//        return colomnRel;
//    }
}
