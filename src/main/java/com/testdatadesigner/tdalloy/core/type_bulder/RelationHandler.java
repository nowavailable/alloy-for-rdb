package com.testdatadesigner.tdalloy.core.type_bulder;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.testdatadesigner.tdalloy.core.types.Relation;
import com.testdatadesigner.tdalloy.core.types.RulesForAlloyable;
import com.testdatadesigner.tdalloy.core.types.Sig;

public class RelationHandler {

    /**
     * テーブルリレーションを表現するオブジェクトを返す。
     * 
     * @param sigSearchByName
     * @param ownerTableName 外部キー保持テーブル名
     * @param fKeyColumnStr 外部キーカラム名
     * @param refTableName 参照される側テーブル名
     * @return List<Relation> 外部キー保持側Relation, 参照される側Relation
     * @throws IllegalAccessException
     */
    public List<Relation> build(Function<String, Sig> sigSearchByName, String ownerTableName,
            String fKeyColumnStr, String refTableName) throws IllegalAccessException {

        // 外部キー保持側
        Relation relation = new Relation(Relation.Tipify.RELATION);
        relation.name = RulesForAlloyable.foreignKeyName(fKeyColumnStr, ownerTableName);
        relation.owner = sigSearchByName.apply(RulesForAlloyable.tableSigName(ownerTableName));
        relation.refTo =
                sigSearchByName.apply(RulesForAlloyable.tableSigNameFromFKey(fKeyColumnStr));

        // 参照される側
        Relation relationReversed = new Relation(Relation.Tipify.RELATION_REVERSED);

        String refTable =
                refTableName.isEmpty() ? RulesForAlloyable.tableNameFromFKey(fKeyColumnStr)
                        : refTableName;
        relationReversed.owner = sigSearchByName.apply(RulesForAlloyable.tableSigName(refTable));
        relationReversed.name = RulesForAlloyable.foreignKeyNameReversed(refTable, ownerTableName);
        relationReversed.refTo =
                sigSearchByName.apply(RulesForAlloyable.tableSigName(ownerTableName));

        return Arrays.asList(relation, relationReversed);
    }
}
