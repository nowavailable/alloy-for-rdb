package com.testdatadesigner.tdalloy.core.type_bulder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.google.common.base.Joiner;
import com.testdatadesigner.tdalloy.core.naming.IRulesForAlloyable;
import com.testdatadesigner.tdalloy.core.naming.RulesForAlloyableFactory;
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
     * @param fKeyColumnStr 外部キーカラム名
     * @param refTableName 参照される側テーブル名
     * @return List<Relation> 外部キー保持側Relation, 参照される側Relation、のペア。
     * @throws IllegalAccessException
     */
    public List<Relation> build(Function<String, Atom> atomSearchByName, String ownerTableName,
            String fKeyColumnStr, String refTableName) throws IllegalAccessException {

        IRulesForAlloyable namingRule = RulesForAlloyableFactory.getInstance().getRule();
        // 外部キー保持側
        Relation relation = null;
        Atom refSig = atomSearchByName.apply(NamingRuleForAlloyable.tableAtomNameFromFKey(fKeyColumnStr));
        // DDL内に参照先が存在していなかったら、単なる値カラムとして扱う
        if (refSig == null) {
            relation = new DefaultColumnHandler().
                buildRelation(atomSearchByName, NamingRuleForAlloyable.tableAtomName(ownerTableName), namingRule.foreignKeyName(fKeyColumnStr, ownerTableName));
            return Arrays.asList(relation);
        } else {
            relation = new Relation(Relation.Typify.RELATION);
            relation.originColumnName = fKeyColumnStr;
            relation.name = namingRule.foreignKeyName(fKeyColumnStr, ownerTableName);
            relation.setOwner(atomSearchByName.apply(NamingRuleForAlloyable.tableAtomName(ownerTableName)));
            relation.setRefTo(refSig);

            // 参照される側
            Relation relationReversed = new Relation(Relation.Typify.RELATION_REFERRED);

            String refTable =
                    refTableName.isEmpty() ? namingRule.tableNameFromFKey(fKeyColumnStr)
                            : refTableName;
            relationReversed.setOwner(atomSearchByName.apply(NamingRuleForAlloyable.tableAtomName(refTable)));
            relationReversed.name = namingRule.foreignKeyNameReversed(refTable, ownerTableName);
            relationReversed.setRefTo(atomSearchByName.apply(NamingRuleForAlloyable.tableAtomName(ownerTableName)));

            return Arrays.asList(relation, relationReversed);
        }
    }
    
    public Fact buildFact(List<Relation> relations) {
        String leftStr = new String();
        String rightStr = new String();
        for (Relation relation : relations) {
            if (relation.type.equals(Relation.Typify.RELATION)) {
                rightStr = relation.getOwner().name + "<:" + relation.name;
            } else if (relation.type.equals(Relation.Typify.RELATION_REFERRED)) {
                leftStr = relation.getOwner().name + "<:" + relation.name;
            }
        }
        Fact fact = new Fact(Fact.Tipify.RELATION);
        fact.value = leftStr + " = ~(" + rightStr + ")";
        fact.owners.addAll(relations);
        return fact;
    } 
    
    public Fact buildMultiColumnUniqueFact(String tableSigName, List<Relation> relations, Integer seq) {
        Fact fact = new Fact(Fact.Tipify.ROWS_CONSTRAINT);
        StringBuilder buff = new StringBuilder();
        String label = "uniqIdx" + seq.toString();
        String labelAnother = label + "'";
        buff.append("all disj ");
        buff.append(label);
        buff.append(",");
        buff.append(labelAnother);
        buff.append(": ");
        buff.append(tableSigName);
        buff.append(" | ");
        buff.append(label);
        buff.append(".(");
        
        List<String> fields = new ArrayList<String>();
        for (Relation relation : relations) {
        	fields.add(tableSigName + "<:" + relation.name);
		}
        buff.append(Joiner.on(" + ").join(fields));
        buff.append(") != ");
        buff.append(labelAnother);
        buff.append(".(");
        buff.append(Joiner.on(" + ").join(fields));
        buff.append(")");
        
        fact.value =  buff.toString();
        fact.owners.addAll(relations);
        return fact;
    }
}
