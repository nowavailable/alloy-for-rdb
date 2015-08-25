package com.testdatadesigner.tdalloy.core.types;

import java.util.HashMap;
import java.util.Map;

public class NamingRuleForAls {

	private static Map<Relation.Typify, String> quqntifierMap = new HashMap<Relation.Typify, String>() {
		{
			put(Relation.Typify.RELATION, "one");
			put(Relation.Typify.RELATION_REFERRED, "some");
			put(Relation.Typify.RELATION_POLYMORPHIC, "one");
			put(Relation.Typify.ABSTRACT_RELATION, "one");
			put(Relation.Typify.ABSTRACT_RELATION_REFERRED, "disj some"); // or "disj one" or "disj set"
			put(Relation.Typify.ABSTRACT_RELATION_TYPIFIED, "some");      // or "disj one" or "disj set"
			put(Relation.Typify.VALUE, "one");
		}
	};
	
	public String searchQuantifierMap(Relation relation) {
		String quqntifier = quqntifierMap.get(relation.type);
		return quqntifier;
	}

}
