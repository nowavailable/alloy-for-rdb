package com.testdatadesigner.tdalloy.core.types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NamingRuleForAls {

    private static String ONE = "one";
    private static String LONE = "lone";
    private static String SOME = "some";
    private static String SET = "set";
    private static Function<Relation, String> makeDisjoint = rel -> {return (rel.isUnique) ? "disj " : "";};
    private static Function<Relation, String> makeMultiRelation = rel -> {return rel.isNotEmpty ? SOME : SET;};
    private static Function<Relation, String> makeOneRelation = rel -> {return rel.isNotEmpty ? ONE : LONE;};
    private static BiFunction<Relation, List<Relation>, Boolean> oneToOneOrMany = (referredRel, allRels) -> {
        return allRels.stream()
                .filter(r -> r.type.equals(Relation.Typify.RELATION))
                .filter(r -> r.refTo.name.equals(referredRel.owner.name))
                .collect(Collectors.toList()).get(0).isUnique;
    };

    private static Map<Relation.Typify, BiFunction<Relation, List<Relation>, String>> quantifierMap = new HashMap<Relation.Typify, BiFunction<Relation, List<Relation>, String>>() {
        {
            put(Relation.Typify.RELATION, (rel, allRels) -> 
                { return makeDisjoint.apply(rel) + makeOneRelation.apply(rel);});
            put(Relation.Typify.RELATION_REFERRED, (rel, allRels) -> 
                { return "disj " + (oneToOneOrMany.apply(rel, allRels) ? makeOneRelation.apply(rel) : makeMultiRelation.apply(rel)); });
            put(Relation.Typify.RELATION_POLYMORPHIC, (rel, allRels) ->
                { return makeOneRelation.apply(rel);});
            put(Relation.Typify.ABSTRACT_RELATION, (rel, allRels) -> 
                { return ONE;});
            put(Relation.Typify.ABSTRACT_RELATION_REFERRED, (rel, allRels) -> 
                { return "disj " + makeMultiRelation.apply(rel); });
            put(Relation.Typify.ABSTRACT_RELATION_TYPIFIED, (rel, allRels) -> 
                {return rel.isUnique ? ONE : SOME;});
            put(Relation.Typify.VALUE, (rel, allRels) -> 
                { return makeDisjoint.apply(rel) + makeOneRelation.apply(rel);});
        }
    };
    
    public String searchQuantifierMap(Relation relation, List<Relation> allRelations) {
        return quantifierMap.get(relation.type).apply(relation, allRelations);
    }

}
