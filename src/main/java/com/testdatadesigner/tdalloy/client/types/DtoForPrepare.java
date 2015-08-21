package com.testdatadesigner.tdalloy.client.types;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.testdatadesigner.tdalloy.core.naming.IRulesForAlloyable;
import com.testdatadesigner.tdalloy.core.naming.RulesForAlloyableFactory;
import com.testdatadesigner.tdalloy.core.naming.RulesForAlloyableRails;
import com.testdatadesigner.tdalloy.core.types.Alloyable;
import com.testdatadesigner.tdalloy.core.types.MultipleRelation;
import com.testdatadesigner.tdalloy.core.types.Atom;

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
        MANY_TO_ONE, ONE_TO_ONE, POLYMORPHIC
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
        List<Atom> tableAtoms =
                alloyable.atoms.stream().filter(atom -> atom.type.equals(Atom.Tipify.ENTITY))
                        .collect(Collectors.toList());
        tableAtoms.forEach(atom -> {
            Table table = this.constructTable();
            table.name = atom.originPropertyName;
            // カラム
            List<Atom> columnAtoms =
                    alloyable.atoms
                            .stream()
                            .filter(a -> a.getParent() != null && a.getParent().equals(atom)
                                    && (a.type.equals(Atom.Tipify.PROPERTY)))
                            .collect(Collectors.toList());
            columnAtoms.forEach(col -> {
                Column column = this.constructColumn();
                column.name = col.originPropertyName;
                table.columns.add(column);
            });
            List<Atom> polymColumnAtoms =
                    alloyable.atoms
                            .stream()
                            .filter(a -> a.getParent() != null
                                    && a.getParent().equals(atom)
                                    && (a.type
                                            .equals(Atom.Tipify.POLYMORPHIC_ABSTRACT)))
                            .collect(Collectors.toList());
            // ポリモーフィック（初期の未決状態）
            polymColumnAtoms.forEach(col -> {
                Column column = this.constructColumn();
                column.name = col.originPropertyName;
                column.relation = this.constructRelation();
                column.relation.type = RelationType.POLYMORPHIC;
                table.columns.add(column);
            });
            // 外部キー
            List<? extends com.testdatadesigner.tdalloy.core.types.Relation> relsConcrete =
                    alloyable.relations
                            .stream()
                            .filter(rel -> rel.owner.equals(atom)
                                    && rel.type
                                            .equals(com.testdatadesigner.tdalloy.core.types.Relation.Tipify.RELATION)
                                    && !rel.getClass().equals(MultipleRelation.class))
                            .collect(Collectors.toList());
            relsConcrete.forEach(rel -> {
                Column column = this.constructColumn();
                column.name = RulesForAlloyableFactory.getInstance().getRule().singularize(rel.refTo.originPropertyName)
                                + RulesForAlloyableRails.foreign_key_suffix();
                column.relation = this.constructRelation();
                column.relation.type = RelationType.MANY_TO_ONE;
                column.relation.refTo.add(rel.refTo.originPropertyName);
                table.columns.add(column);
            });

            this.tables.add(table);
        });
    }
}
