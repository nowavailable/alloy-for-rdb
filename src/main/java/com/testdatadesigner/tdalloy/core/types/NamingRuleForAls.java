package com.testdatadesigner.tdalloy.core.types;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class NamingRuleForAls {

	private static String ONE = "one";
	private static String LONE = "lone";
	private static String SOME = "some";
	private static String SET = "set";
	private static Function<Relation, String> makeDisjoint = rel -> {return (rel.isUnique) ? "disj " : "";};
	private static Function<Relation, String> makeMultiRelation = rel -> {return rel.isNotEmpty ? SOME : SET;};
	private static Function<Relation, String> makeOneRelation = rel -> {return rel.isNotEmpty ? ONE : LONE;};

	private static Map<Relation.Typify, Function<Relation, String>> quantifierMap = new HashMap<Relation.Typify, Function<Relation, String>>() {
		{
			put(Relation.Typify.RELATION, makeOneRelation);
			put(Relation.Typify.RELATION_REFERRED, makeMultiRelation);
			put(Relation.Typify.RELATION_POLYMORPHIC, makeOneRelation);
			put(Relation.Typify.ABSTRACT_RELATION, makeOneRelation);
			put(Relation.Typify.ABSTRACT_RELATION_REFERRED, makeMultiRelation);
			put(Relation.Typify.ABSTRACT_RELATION_TYPIFIED, makeMultiRelation);
			put(Relation.Typify.VALUE, makeOneRelation);
		}
	};
	
	public String searchQuantifierMap(Relation relation) {
		String quqntifier = makeDisjoint.apply(relation) + 
				(relation.type.equals(Relation.Typify.ABSTRACT_RELATION_TYPIFIED) ? SOME : 
					quantifierMap.get(relation.type).apply(relation));
		return quqntifier;
	}

}
