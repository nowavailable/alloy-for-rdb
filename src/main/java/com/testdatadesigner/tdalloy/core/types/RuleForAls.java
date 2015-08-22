package com.testdatadesigner.tdalloy.core.types;

import java.util.HashMap;
import java.util.Map;

public class RuleForAls {

	private static Map<Relation.Tipify, String> quqntifierMap = new HashMap<Relation.Tipify, String>() {
		{
			put(Relation.Tipify.RELATION, "one");
			put(Relation.Tipify.RELATION_REFERRED, "some");
			put(Relation.Tipify.RELATION_POLYMOPHIC, "one");
			put(Relation.Tipify.ABSTRUCT_RELATION, "one");
			put(Relation.Tipify.ABSTRUCT_RELATION_REFERRED, "disj set"); // or "disj one" or "disj set"
			put(Relation.Tipify.ABSTRUCT_RELATION_TYPIFIED, "some");      // or "disj one"
			put(Relation.Tipify.VALUE, "one");
		}
	};
	
	public String searchQuantifierMap(Relation relation) {
		String quqntifier = quqntifierMap.get(relation.type);
		return quqntifier;
	}

}
