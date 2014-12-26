package com.testdatadesigner.tdalloy.client.types;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.testdatadesigner.tdalloy.core.types.Alloyable;
import com.testdatadesigner.tdalloy.core.types.MultipleRelation;
import com.testdatadesigner.tdalloy.core.types.RulesForAlloyable;
import com.testdatadesigner.tdalloy.core.types.Sig;

/**
 * クライアントの、初期設定ビューにGETさせる and 初期設定ビューからPOSTさせる 用の<br/>
 * プレースホルダーオブジェクト。JSONフォーマットで送受信する。
 * 
 * @author tsutsumi
 *
 */
public class DtoForPrepare {
    public List<Table> tables = new ArrayList<>();

    public static enum RelationType {
        // NONE,
        MANY_TO_ONE, ONE_TO_ONE, POLYMOPHIC
    };

    public class Table {
        public String name;
        public List<Column> columns = new ArrayList<>();
        public Boolean ignore = Boolean.FALSE;
    }

    public class Column {
        public String name;
        public Relation relation;
        public Boolean ignore = Boolean.FALSE;
    }

    public class Relation {
        public RelationType type;
        public List<String> refTo = new ArrayList<>();
        public Boolean ignore = Boolean.FALSE;
    }

    public Table constructTable() {
        return new Table();
    }

    public Column constructColumn() {
        return new Column();
    }

    public Relation constructRelation() {
        return new Relation();
    }

    /**
     * DDL読み込み直後の、初期状態Alloyableオブジェクトの内容を、<br/>
     * 対クライアント送信用のDTOとしてビルドする。
     * 
     * @param alloyable
     */
    public void buiildFromAlloyable(Alloyable alloyable) {
        List<Sig> tableSigs =
                alloyable.sigs.stream().filter(sig -> sig.type.equals(Sig.Tipify.ENTITY))
                        .collect(Collectors.toList());
        tableSigs.forEach(sig -> {
            Table table = this.constructTable();
            table.name = sig.originPropertyName;
            // カラム
            List<Sig> columnSigs =
                    alloyable.sigs
                            .stream()
                            .filter(s -> s.getParent() != null && s.getParent().equals(sig)
                                    && (s.type.equals(Sig.Tipify.PROPERTY_PROTOTYPE)))
                            .collect(Collectors.toList());
            columnSigs.forEach(col -> {
                Column column = this.constructColumn();
                column.name = col.originPropertyName;
                table.columns.add(column);
            });
            List<Sig> polymColumnSigs =
                    alloyable.sigs
                            .stream()
                            .filter(s -> s.getParent() != null
                                    && s.getParent().equals(sig)
                                    && (s.type
                                            .equals(Sig.Tipify.PROPERTY_PROTOTYPE_POLIMOPHIC_PROSPECTED)))
                            .collect(Collectors.toList());
            // ポリモーフィック（初期の未決状態）
            polymColumnSigs.forEach(col -> {
                Column column = this.constructColumn();
                column.name = col.originPropertyName;
                column.relation = this.constructRelation();
                column.relation.type = RelationType.POLYMOPHIC;
                table.columns.add(column);
            });
            // 外部キー
            List<? extends com.testdatadesigner.tdalloy.core.types.Relation> relsConcrete =
                    alloyable.relations
                            .stream()
                            .filter(rel -> rel.owner.equals(sig)
                                    && rel.type
                                            .equals(com.testdatadesigner.tdalloy.core.types.Relation.Tipify.RELATION)
                                    && !rel.getClass().equals(MultipleRelation.class))
                            .collect(Collectors.toList());
            relsConcrete.forEach(rel -> {
                Column column = this.constructColumn();
                column.name =
                        RulesForAlloyable.singularize(rel.refTo.originPropertyName)
                                + RulesForAlloyable.FOREIGN_KEY_SUFFIX;
                column.relation = this.constructRelation();
                column.relation.type = RelationType.MANY_TO_ONE;
                column.relation.refTo.add(rel.refTo.originPropertyName);
                table.columns.add(column);
            });

            this.tables.add(table);
        });
    }
}
