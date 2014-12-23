package com.testdatadesigner.tdalloy.core.type_bulder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.testdatadesigner.tdalloy.core.types.MultipleRelation;
import com.testdatadesigner.tdalloy.core.types.Relation;
import com.testdatadesigner.tdalloy.core.types.RulesForAlloyable;
import com.testdatadesigner.tdalloy.core.types.Sig;
import com.testdatadesigner.tdalloy.util.Inflector;

public class DefaultColumnHandler {

    public Sig buildSig(Function<String, Sig> sigSearchByName, String ownerTableName,
            String columnName) throws IllegalAccessException {
        Sig colomnSig = new Sig(Sig.Tipify.PROPERTY_PROTOTYPE);
        colomnSig.originPropertyName = columnName;
        colomnSig.name = RulesForAlloyable.colmnSigName(columnName, ownerTableName);
        colomnSig.setParent(sigSearchByName.apply(RulesForAlloyable.tableSigName(ownerTableName)));
        colomnSig.isAbstruct = Boolean.TRUE;
        return colomnSig;
    }

    public List<Sig> buildFactorSigs(String ownerTableName, String columnName) {
        List<String> factors = new ArrayList<String>() {
            {
                Inflector inflector = Inflector.getInstance();
                this.add(new String(ownerTableName + RulesForAlloyable.COUPLER
                        + inflector.upperCamelCase(columnName) + "HIGH"));
                this.add(new String(ownerTableName + RulesForAlloyable.COUPLER
                        + inflector.upperCamelCase(columnName) + "LOW"));
            }
        };
        List<Sig> sigs = new ArrayList<>();
        for (String factor : factors) {
            Sig sig = new Sig(Sig.Tipify.PROPERTY_FACTOR);
            sig.originPropertyName = columnName;
            sig.name = factor;
            sigs.add(sig);
        }
        return sigs;
    }

    public Relation buildRelation(Function<String, Sig> sigSearchByName, String ownerTableName,
            String columnName, List<Sig> propertyFactorSigs) {
        MultipleRelation<Sig> colomnRel = new MultipleRelation<>(Relation.Tipify.VALUE);
        colomnRel.name = RulesForAlloyable.colmnRelationName(columnName, ownerTableName);
        colomnRel.owner = sigSearchByName.apply(RulesForAlloyable.tableSigName(ownerTableName));
        colomnRel.refToTypes.addAll(propertyFactorSigs);
        return colomnRel;
    }
}
