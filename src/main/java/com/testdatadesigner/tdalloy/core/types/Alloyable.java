package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.foundationdb.sql.parser.ColumnDefinitionNode;
import com.foundationdb.sql.parser.ConstraintDefinitionNode;
import com.foundationdb.sql.parser.CreateTableNode;
import com.foundationdb.sql.parser.FKConstraintDefinitionNode;
import com.foundationdb.sql.parser.ResultColumn;
import com.foundationdb.sql.parser.TableElementNode;
import com.foundationdb.sql.parser.ConstraintDefinitionNode.ConstraintType;

public class Alloyable implements Serializable {
    private static final long serialVersionUID = 1L;
    public List<Sig> sigs = new ArrayList<Sig>();
    public List<Relation> relations = new ArrayList<Relation>();
    public List<Fact> facts = new ArrayList<Fact>();
    public Boolean isRailsOriented = Boolean.FALSE;

    public String toJson() {
        return null;
    }

    private Sig searchSig(String name) {
        int idx = 0;
        for (int i = 0; i < this.sigs.size(); i++) {
            if (this.sigs.get(i).name.equals(name)) {
                idx = i;
                break;
            }
        }
        return this.sigs.get(idx);
    }

    // -- 振る舞い系メソッド。いずれMix-inの形に
    // --------------------------------------------------------------
    public void fixPolymophic() {

    }

    public void fixOneToOne() {

    }

    public void omitColumns() {

    }

    private List<String> skipElementListForColumn = new ArrayList<String>();
    static final String INTERNAL_SEPERATOR = "_#_";

    public Alloyable buildTableSigs(List<CreateTableNode> parsedDDLList) {
        for (CreateTableNode tableNode : parsedDDLList) {
            Sig sig = new Sig();
            sig.originPropertyName = tableNode.getFullName();
            sig.name = RulesForAlloyable.generateTableSigName(tableNode
                    .getFullName());
            sig.type = com.testdatadesigner.tdalloy.core.types.Sig.Tipify.ENTITY;
            // TODO: seqはどうする？出力後にしか無い情報。
            this.sigs.add(sig);

            for (TableElementNode tableElement : tableNode
                    .getTableElementList()) {
                // 外部キーはスキップ。
                if (tableElement.getClass().equals(
                        FKConstraintDefinitionNode.class)) {
                    FKConstraintDefinitionNode constraint = (FKConstraintDefinitionNode) tableElement;
                    skipElementListForColumn
                            .add(tableNode.getFullName()
                                    + INTERNAL_SEPERATOR
                                    + ((ResultColumn) constraint
                                            .getColumnList().get(0)).getName());
                }
                // プライマリキーはスキップ
                if (tableElement.getClass().equals(
                        ConstraintDefinitionNode.class)) {
                    ConstraintDefinitionNode constraint = (ConstraintDefinitionNode) tableElement;
                    if (constraint.getConstraintType().equals(
                            ConstraintType.PRIMARY_KEY)) {
                        skipElementListForColumn.add(tableNode.getFullName()
                                + INTERNAL_SEPERATOR
                                + ((ResultColumn) constraint.getColumnList()
                                        .get(0)).getName());
                    }
                }
                // TODO: Constraints定義されていない外部キー推論
            }
        }
        return this;
    }

    public Alloyable buildForeignKeyRelations(List<CreateTableNode> parsedDDLList) {
        for (CreateTableNode tableNode : parsedDDLList) {
            for (TableElementNode tableElement : tableNode
                    .getTableElementList()) {
                // TODO:ポリモーフィック関連推論スキップ

                // 外部キー
                if (tableElement.getClass().equals(
                        FKConstraintDefinitionNode.class)) {
                    FKConstraintDefinitionNode constraint = (FKConstraintDefinitionNode) tableElement;
                    Relation relation = new Relation();
                    relation.type = com.testdatadesigner.tdalloy.core.types.Relation.Tipify.RELATION;
                    relation.originPropertyName = 
                            ((ResultColumn) constraint.getColumnList().get(0)).getName();
                    relation.name = RulesForAlloyable.generateForeignKeyName(
                            relation.originPropertyName, tableNode.getFullName());
                    relation.originOwner = tableNode.getFullName();
                    relation.owner = searchSig(RulesForAlloyable
                            .generateTableSigName(relation.originOwner));
                    relation.referTo = searchSig(RulesForAlloyable
                            .generateTableSigName(constraint.getRefTableName().getFullTableName()));

                    this.relations.add(relation);
                    
                    // 反対側
                    Relation relationReversed = new Relation();
                    relationReversed.type = com.testdatadesigner.tdalloy.core.types.Relation.Tipify.RELATION_REVERSED;
                    relationReversed.originOwner = constraint.getRefTableName().getFullTableName();
                    relationReversed.owner = searchSig(RulesForAlloyable
                            .generateTableSigName(relationReversed.originOwner));
                    relationReversed.name = RulesForAlloyable.generateForeignKeyNameReversed(
                            relationReversed.originOwner, tableNode.getFullName());
                    relationReversed.referTo = searchSig(RulesForAlloyable
                            .generateTableSigName(tableNode.getFullName()));

                    this.relations.add(relationReversed);
                }
                
                
                // TODO: Constraints定義されていない外部キー推論

                // booleanフィールド
                
                // その他の属性
            }
        }
        return this;
    }
    // TODO:（Constraints で定義されていない）外部キー推論
    public Alloyable buildInferredForeignKeyRelations(List<CreateTableNode> parsedDDLList) {

        return this;
    }

    // TODO:ポリモーフィック関連推論
    public Alloyable buildPolymophic(List<CreateTableNode> parsedDDLList) {

        return this;
    }

    public Alloyable buildColumnSigs(List<CreateTableNode> parsedDDLList) {
        for (CreateTableNode tableNode : parsedDDLList) {
            for (TableElementNode tableElement : tableNode
                    .getTableElementList()) {
                if (tableElement.getClass().equals(ColumnDefinitionNode.class)) {
                    Sig sigColomn = new Sig();
                    ColumnDefinitionNode column = (ColumnDefinitionNode) tableElement;
                    // スキップ
                    if (skipElementListForColumn.contains(tableNode
                            .getFullName()
                            + INTERNAL_SEPERATOR
                            + column.getName())) {
                        continue;
                    }

                    // Booleanフィールドはsigとしては扱わないのでスキップ
                    if (column.getType().getSQLstring().equals("TINYINT")) {
                        continue;
                    }
                    // TODO:ポリモーフィック関連推論スキップ

                    sigColomn.originPropertyName = column.getName();
                    sigColomn.name = RulesForAlloyable.generateColmnSigName(
                            column.getName(), tableNode.getFullName());
                    sigColomn.type = com.testdatadesigner.tdalloy.core.types.Sig.Tipify.PROPERTY_PROTOTYPE;
                    sigColomn.isAbstruct = Boolean.TRUE;
                    // TODO: seqはどうする？出力後にしか無い情報。
                    this.sigs.add(sigColomn);

                    List<Sig> propertyFactorSigs = RulesForAlloyable
                            .generateDefaultPropertyFactor(column.getName(),
                                    tableNode.getFullName());
                    for (Sig propertyFactorSig : propertyFactorSigs) {
                        this.sigs.add(propertyFactorSig);
                    }
                }
            }
        }
        return this;
    }
}
