package com.testdatadesigner.tdalloy.core.type_bulder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import com.testdatadesigner.tdalloy.core.naming.IRulesForAlloyable;
import com.testdatadesigner.tdalloy.core.naming.RulesForAlloyableFactory;
import com.testdatadesigner.tdalloy.core.types.AlloyableHandler;
import com.testdatadesigner.tdalloy.core.types.Fact;
import com.testdatadesigner.tdalloy.core.types.Relation;
import com.testdatadesigner.tdalloy.core.types.Atom;
import com.testdatadesigner.tdalloy.core.types.NamingRuleForAlloyable;

public class RelationHandler {

    /**
     * テーブルリレーションを表現するオブジェクトを返す。
     * 
     * @param atomSearchByName
     * @param ownerTableName 外部キー保持テーブル名
     * @param fKeyColumnStrs 外部キーカラム名
     * @param refTableName 参照される側テーブル名
     * @return List<Relation> 外部キー保持側Relation, 参照される側Relation、のペア。
     * @throws IllegalAccessException 
     */
    public List<Relation> build(Function<String, Atom> atomSearchByName, String ownerTableName,
            List<String> fKeyColumnStrs, String refTableName) throws IllegalAccessException {

        IRulesForAlloyable namingRule = RulesForAlloyableFactory.getInstance().getRule();
        // 外部キー保持側
        Relation relation = null;

        if (!refTableName.isEmpty()) {
        	Atom refSig = atomSearchByName.apply(NamingRuleForAlloyable.tableAtomName(refTableName));
            relation = new Relation(Relation.Typify.RELATION);
            //relation.originColumnName = namingRule.fkeyFromTableName(refTableName);
            relation.originColumnName = fKeyColumnStrs.toString();
            relation.name = namingRule.foreignKeyName(namingRule.fkeyFromTableName(refTableName), ownerTableName);
            relation.setOwner(atomSearchByName.apply(NamingRuleForAlloyable.tableAtomName(ownerTableName)));
            relation.setRefTo(refSig);

            // 参照される側
            Relation relationReversed = new Relation(Relation.Typify.RELATION_REFERRED);
            relationReversed.setOwner(atomSearchByName.apply(NamingRuleForAlloyable.tableAtomName(refTableName)));
            relationReversed.name = namingRule.foreignKeyNameReversed(refTableName, ownerTableName);
            relationReversed.setRefTo(atomSearchByName.apply(NamingRuleForAlloyable.tableAtomName(ownerTableName)));
            
            return Arrays.asList(relation, relationReversed);

        } else {
        	if (fKeyColumnStrs.size() > 1) {
        		throw new IllegalAccessException("複合外部キーなのは分かった。が、だったら、refTableName を引数に渡すこと。");
        	}

        	Atom refSig = atomSearchByName.apply(NamingRuleForAlloyable.tableAtomNameFromFKey(fKeyColumnStrs.get(0)));
            // DDL内に参照先が存在していなかったら、単なる値カラムとして扱う
            if (refSig == null) {
                relation = new DefaultColumnHandler().
                    buildRelation(atomSearchByName, NamingRuleForAlloyable.tableAtomName(ownerTableName), namingRule.foreignKeyName(fKeyColumnStrs.get(0), ownerTableName));
                return Arrays.asList(relation);
            } else {
                relation = new Relation(Relation.Typify.RELATION);
                relation.originColumnName = fKeyColumnStrs.get(0);
                relation.name = namingRule.foreignKeyName(fKeyColumnStrs.get(0), ownerTableName);
                relation.setOwner(atomSearchByName.apply(NamingRuleForAlloyable.tableAtomName(ownerTableName)));
                relation.setRefTo(refSig);

                // 参照される側
                Relation relationReversed = new Relation(Relation.Typify.RELATION_REFERRED);
                String refTable = namingRule.tableNameFromFKey(fKeyColumnStrs.get(0));
                relationReversed.setOwner(atomSearchByName.apply(NamingRuleForAlloyable.tableAtomName(refTable)));
                relationReversed.name = namingRule.foreignKeyNameReversed(refTable, ownerTableName);
                relationReversed.setRefTo(atomSearchByName.apply(NamingRuleForAlloyable.tableAtomName(ownerTableName)));

                return Arrays.asList(relation, relationReversed);
            }
        }
    }
    
    public Fact buildFact(List<Relation> relations) {
        String leftStr = new String();
        String rightStr = new String();
        for (Relation relation : relations) {
            if (relation.type.equals(Relation.Typify.RELATION)) {
            	Atom owner = AlloyableHandler.getOwner(relation);
                rightStr = owner.name + "<:" + relation.name;
            } else if (relation.type.equals(Relation.Typify.RELATION_REFERRED)) {
                leftStr = AlloyableHandler.getOwner(relation).name + "<:" + relation.name;
            }
        }
        Fact fact = new Fact(Fact.Tipify.RELATION);
        fact.value = leftStr + " = ~(" + rightStr + ")";
        fact.owners.addAll(relations);
        return fact;
    } 
    
    public Fact buildMultiColumnUniqueFact(String tableSigName, List<String> colNames) {
        IRulesForAlloyable namingRule = RulesForAlloyableFactory.getInstance().getRule();
        List<String> alloyFieldNames = new ArrayList<>(); 
        for (String colName : colNames) {
			alloyFieldNames.add(namingRule.singularize(namingRule.tableNameFromFKey(colName)));
		}
        Fact fact = new Fact(Fact.Tipify.ROWS_CONSTRAINT);
        StringBuilder builder = new StringBuilder();

        builder.append("all ent,ent':");
        builder.append(tableSigName);
        builder.append(" | ");
        builder.append("ent != ent' => ");

        List<String> fields_left = new ArrayList<String>();
        List<String> fields_right = new ArrayList<String>();
        String previous_field = null;
        for (String fieldName : alloyFieldNames) {
        	if (previous_field == null) {
        		previous_field = fieldName;
        		continue;
        	}
        	fields_left.add("ent." + previous_field + " -> " + "ent." + fieldName);
        	fields_right.add("ent'." + previous_field + " -> " + "ent'." + fieldName);
		}
        List<String> fields = new ArrayList<String>();
		for (int i = 0; i < fields_left.size(); i++) {
			fields.add(
			    "(" + fields_left.get(i) + " != " + fields_right.get(i) + ")"
			);
		}
        builder.append(Joiner.on(" && ").join(fields));
        fact.value =  builder.toString();
        //fact.owners.addAll(relations);
        return fact;
    }
}
