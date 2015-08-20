package com.testdatadesigner.tdalloy.core.type_bulder;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.testdatadesigner.tdalloy.core.naming.IRulesForAlloyable;
import com.testdatadesigner.tdalloy.core.naming.RulesForAlloyableFactory;
import com.testdatadesigner.tdalloy.core.types.Fact;
import com.testdatadesigner.tdalloy.core.types.Relation;
import com.testdatadesigner.tdalloy.core.types.Atom;

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
        Relation relation = new Relation(Relation.Tipify.RELATION);
        relation.name = namingRule.foreignKeyName(fKeyColumnStr, ownerTableName);
        relation.owner = atomSearchByName.apply(namingRule.tableAtomName(ownerTableName));
        relation.refTo =
                atomSearchByName.apply(namingRule.tableAtomNameFromFKey(fKeyColumnStr));

        // 参照される側
        Relation relationReversed = new Relation(Relation.Tipify.RELATION_REVERSED);

        String refTable =
                refTableName.isEmpty() ? namingRule.tableNameFromFKey(fKeyColumnStr)
                        : refTableName;
        relationReversed.owner = atomSearchByName.apply(namingRule.tableAtomName(refTable));
        relationReversed.name = namingRule.foreignKeyNameReversed(refTable, ownerTableName);
        relationReversed.refTo =
                atomSearchByName.apply(namingRule.tableAtomName(ownerTableName));

        return Arrays.asList(relation, relationReversed);
    }
    
    public Fact buildFact(List<Relation> relations) {
        String leftStr = new String();
        String rightStr = new String();
        for (Relation relation : relations) {
            if (relation.type.equals(Relation.Tipify.RELATION)) {
                rightStr = relation.owner.name + "<:" + relation.name;
            } else if (relation.type.equals(Relation.Tipify.RELATION_REVERSED)) {
                leftStr = relation.owner.name + "<:" + relation.name;
            }
        }
        Fact fact = new Fact(Fact.Tipify.RELATION);
        fact.value = leftStr + " = ~(" + rightStr + ")";
        fact.owners.addAll(relations);
        return fact;
    }
}
