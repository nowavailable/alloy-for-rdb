package com.testdatadesigner.tdalloy.core.type_bulder;

import java.util.function.Function;

import com.testdatadesigner.tdalloy.core.naming.IRulesForAlloyable;
import com.testdatadesigner.tdalloy.core.naming.RulesForAlloyableFactory;
import com.testdatadesigner.tdalloy.core.types.Relation;
import com.testdatadesigner.tdalloy.core.types.Atom;

public class DefaultColumnHandler {

    public Relation buildRelation(Function<String, Atom> atomSearchByName, String ownerTableName,
            String columnName) {
        Relation relation = new Relation(Relation.Tipify.VALUE);
        relation.owner = atomSearchByName.apply(RulesForAlloyableFactory.getInstance().getRule().tableAtomName(ownerTableName));
        relation.name = columnName;
        Atom column = new Atom(Atom.Tipify.PROPERTY);
        column.name = "Boundary";
        relation.refTo = column;
        return relation;
    }

    public Atom buildAtom(Function<String, Atom> atomSearchByName, String ownerTableName,
            String columnName) throws IllegalAccessException {
        Atom colomnAtom = new Atom(Atom.Tipify.PROPERTY);
        IRulesForAlloyable namingRule = RulesForAlloyableFactory.getInstance().getRule();
        colomnAtom.originPropertyName = columnName;
        colomnAtom.name = namingRule.columnAtomName(columnName, ownerTableName);
        colomnAtom.setParent(atomSearchByName.apply(namingRule.tableAtomName(ownerTableName)));
        return colomnAtom;
    }

    public Atom buildAtomPolymorphicProspected(Function<String, Atom> atomSearchByName, String ownerTableName,
            String columnName) throws IllegalAccessException {
        Atom colomnAtom = buildAtom(atomSearchByName, ownerTableName, columnName);
        colomnAtom.type = Atom.Tipify.POLYMORPHIC_ABSTRACT;
        return colomnAtom;
    }
    
//    public List<Atom> buildFactorAtoms(String ownerTableName, String columnName) {
//        List<String> factors = new ArrayList<String>() {
//            {
//                Inflector inflector = Inflector.getInstance();
//                this.add(new String(ownerTableName + RulesForAlloyable.COUPLER
//                        + inflector.upperCamelCase(columnName) + "HIGH"));
//                this.add(new String(ownerTableName + RulesForAlloyable.COUPLER
//                        + inflector.upperCamelCase(columnName) + "LOW"));
//            }
//        };
//        List<Atom> atoms = new ArrayList<>();
//        for (String factor : factors) {
//            Atom atom = new Atom(Atom.Tipify.PROPERTY_FACTOR);
//            atom.originPropertyName = columnName;
//            atom.name = factor;
//            atoms.add(atom);
//        }
//        return atoms;
//    }

//    public Relation buildRelation(Function<String, Atom> atomSearchByName, String ownerTableName,
//            String columnName, List<Atom> propertyFactorAtoms) {
//        MultipleRelation colomnRel = new MultipleRelation(Relation.Tipify.VALUE);
//        colomnRel.name = RulesForAlloyable.columnRelationName(columnName, ownerTableName);
//        colomnRel.owner = atomSearchByName.apply(RulesForAlloyable.tableAtomName(ownerTableName));
//        colomnRel.refToTypes = propertyFactorAtoms;
//        return colomnRel;
//    }
}
