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
    private static String DISJ = "disj ";
    private static Function<IRelation, String> makeDisjoint = rel -> {return (rel.getIsUnique()) ? DISJ : "";};
    private static Function<IRelation, String> makeMultiRelation = rel -> {return rel.getIsNotEmpty() ? SOME : SET;};
    private static Function<IRelation, String> makeOneRelation = rel -> {return rel.getIsNotEmpty() ? ONE : LONE;};
    private static BiFunction<IRelation, List<IRelation>, Boolean> oneToOneOrMany = (referredRel, allRels) -> {
        return allRels.stream()
            .filter(r -> r.getClass().equals(TableRelation.class))
            .filter(r -> AlloyableHandler.getRefTo(r).getName().equals(AlloyableHandler.getOwner(referredRel).getName())).collect(Collectors.toList())
            .get(0).getIsUnique();
    };

    private static Map<Class, BiFunction<IRelation, List<IRelation>, String>> quantifierMap = 
    		new HashMap<Class, BiFunction<IRelation, List<IRelation>, String>>() {
        {
            put(TableRelation.class, (rel, allRels) -> 
                { return makeDisjoint.apply(rel) + makeOneRelation.apply(rel);});
            put(TableRelationReferred.class, (rel, allRels) -> 
                { return /*DISJ + */ (oneToOneOrMany.apply(rel, allRels) ? LONE : SET); });
            put(RelationPolymorphic.class, (rel, allRels) ->
                { return makeOneRelation.apply(rel);});
            put(AbstractRelationPolymorphic.class, (rel, allRels) -> 
                { return ONE;});
            put(AbstractRelationPolymorphicReferred.class, (rel, allRels) -> 
                { return /*DISJ + */ makeMultiRelation.apply(rel); });
            put(AbstractRelationPolymorphicTypified.class, (rel, allRels) -> 
                {return rel.getIsNotEmpty() ? ONE : LONE;});
            put(ColumnValue.class, (rel, allRels) -> 
                { return makeDisjoint.apply(rel) + makeOneRelation.apply(rel);});
        }
    };
    
    public String searchQuantifierMap(IRelation relation, List<IRelation> allRelations) {
        return quantifierMap.get(relation.getClass()).apply(relation, allRelations);
    }

}
