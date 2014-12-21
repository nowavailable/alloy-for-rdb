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

    // TODO: 自動で生成出来ない部分についての情報フィールド

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
    private List<String> polymophicColumns = new ArrayList<String>();
    private List<String> foreignKeys = new ArrayList<String>();
    static final String INTERNAL_SEPERATOR = "_#_";

    public Alloyable buildTableSigs(List<CreateTableNode> parsedDDLList) {
        for (CreateTableNode tableNode : parsedDDLList) {
            Sig sig = new Sig(Sig.Tipify.ENTITY);
            sig.originPropertyName = tableNode.getFullName();
            sig.name = RulesForAlloyable.tableSigName(tableNode
                    .getFullName());
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
            }
        }
        return this;
    }

    /**
     * ポリモーフィック関連推論と （Constraints で定義されていない）外部キー推論
     * 
     * @param parsedDDLList
     * @return this
     * @throws IllegalAccessException
     */
    public Alloyable buildInferencedRelations(
            List<CreateTableNode> parsedDDLList) throws IllegalAccessException {
        for (CreateTableNode tableNode : parsedDDLList) {
            List<String> columnNames = new ArrayList<String>();
            for (TableElementNode tableElement : tableNode
                    .getTableElementList()) {
                if (tableElement.getClass().equals(ColumnDefinitionNode.class)) {
                    ColumnDefinitionNode column = (ColumnDefinitionNode) tableElement;
                    columnNames.add(column.getName());
                }
            }
            List<List<String>> inferenced = RulesForAlloyable
                    .inferencedRelations(columnNames);
            List<String> polymophicSet = inferenced.get(0);
            List<String> foreignKeySet = inferenced.get(1);
            if (!polymophicSet.isEmpty()) {
                this.isRailsOriented = Boolean.TRUE;
                for (String keyStr : polymophicSet) {
                    // スキップ定義
                    skipElementListForColumn.add(tableNode.getFullName()
                            + INTERNAL_SEPERATOR + keyStr + RulesForAlloyable.FOREIGN_KEY_SUFFIX);
                    skipElementListForColumn.add(tableNode.getFullName()
                            + INTERNAL_SEPERATOR + keyStr + RulesForAlloyable.POLYMOPHIC_SUFFIX);
                    polymophicColumns.add(tableNode.getFullName()
                            + INTERNAL_SEPERATOR + keyStr);

                    // 1/9
                    Relation valueRelation = new Relation(Relation.Tipify.VALUE);
                    valueRelation.originPropertyName = keyStr;
                    valueRelation.name = RulesForAlloyable
                            .colmnRelationName(keyStr + RulesForAlloyable.POLYMOPHIC_SUFFIX, tableNode.getFullName());
                    valueRelation.originOwner = tableNode.getFullName();
                    valueRelation.owner = searchSig(RulesForAlloyable
                            .tableSigName(valueRelation.originOwner));
                    // NOTICE: valueRelation.refTo PR_xxxs_xxxxType は、抽象sigの継承先なので、この時点では分からない。
                    this.relations.add(valueRelation);

                    // 2/9
                    // 3/9
                    // 4/9
                    Sig columnSig = new Sig(Sig.Tipify.POLYMOPHIC_TYPE_ABSTRACT);
                    columnSig.originPropertyName = keyStr + RulesForAlloyable.POLYMOPHIC_SUFFIX;
                    columnSig.name = RulesForAlloyable.colmnSigName(
                            columnSig.originPropertyName, tableNode.getFullName());
                    columnSig.isAbstruct = Boolean.TRUE;
                    columnSig.setParent(searchSig(RulesForAlloyable
                            .tableSigName(tableNode.getFullName())));
                    this.sigs.add(columnSig);
                    
                    // 5/9
                    Relation valueReversedRelation = new Relation(Relation.Tipify.VALUE_REVERSED);
                    valueReversedRelation.name = "refTo_" + RulesForAlloyable
                            .tableSigName(tableNode.getFullName());
                    valueReversedRelation.refTo = searchSig(RulesForAlloyable
                            .tableSigName(tableNode.getFullName()));
                    // NOTICE: originPropertyName, originOwner, owner はこの時点では分からない。
                    this.relations.add(valueReversedRelation);
                    
                    // 6/9
                    // 7/9
                    // 8/9
                    // 9/9
                }
            }

            if (!foreignKeySet.isEmpty()) {
                this.isRailsOriented = Boolean.TRUE;
                for (String keyStr : foreignKeySet) {
                    // スキップ定義
                    skipElementListForColumn.add(tableNode.getFullName()
                            + INTERNAL_SEPERATOR + keyStr);
                    foreignKeys.add(tableNode.getFullName()
                            + INTERNAL_SEPERATOR + keyStr);

                    // 外部キー保持側
                    Relation relation = new Relation(Relation.Tipify.RELATION);
                    relation.originPropertyName = keyStr;
                    relation.name = RulesForAlloyable.foreignKeyName(
                            relation.originPropertyName,
                            tableNode.getFullName());
                    relation.originOwner = tableNode.getFullName();
                    relation.owner = searchSig(RulesForAlloyable
                            .tableSigName(relation.originOwner));
                    relation.refTo = searchSig(RulesForAlloyable
                            .tableSigNameFromFKey(relation.originPropertyName));

                    this.relations.add(relation);

                    // 参照される側
                    Relation relationReversed = new Relation(Relation.Tipify.RELATION_REVERSED);
                    relationReversed.originOwner = RulesForAlloyable
                            .tableNameFromFKey(keyStr);
                    relationReversed.owner = searchSig(RulesForAlloyable
                            .tableSigName(relationReversed.originOwner));
                    relationReversed.name = RulesForAlloyable
                            .foreignKeyNameReversed(
                                    relationReversed.originOwner,
                                    tableNode.getFullName());
                    relationReversed.refTo = searchSig(RulesForAlloyable
                            .tableSigName(tableNode.getFullName()));

                    this.relations.add(relationReversed);
                }
            }
        }
        return this;
    }

    /**
     * Constraintsに定義されている外部キーによる関連
     * @param parsedDDLList
     * @return this
     */
    public Alloyable buildForeignKeyRelations(
            List<CreateTableNode> parsedDDLList) {
        for (CreateTableNode tableNode : parsedDDLList) {
            for (TableElementNode tableElement : tableNode
                    .getTableElementList()) {
                // 外部キー
                if (tableElement.getClass().equals(
                        FKConstraintDefinitionNode.class)) {
                    FKConstraintDefinitionNode constraint = (FKConstraintDefinitionNode) tableElement;

                    // 外部キー保持側
                    Relation relation = new Relation(Relation.Tipify.RELATION);
                    relation.originPropertyName = ((ResultColumn) constraint
                            .getColumnList().get(0)).getName();
                    relation.name = RulesForAlloyable.foreignKeyName(
                            relation.originPropertyName,
                            tableNode.getFullName());
                    relation.originOwner = tableNode.getFullName();
                    relation.owner = searchSig(RulesForAlloyable
                            .tableSigName(relation.originOwner));
                    relation.refTo = searchSig(RulesForAlloyable
                            .tableSigName(constraint.getRefTableName()
                                    .getFullTableName()));

                    this.relations.add(relation);

                    // 参照される側
                    Relation relationReversed = new Relation(Relation.Tipify.RELATION_REVERSED);
                    relationReversed.originOwner = constraint.getRefTableName()
                            .getFullTableName();
                    relationReversed.owner = searchSig(RulesForAlloyable
                            .tableSigName(relationReversed.originOwner));
                    relationReversed.name = RulesForAlloyable
                            .foreignKeyNameReversed(
                                    relationReversed.originOwner,
                                    tableNode.getFullName());
                    relationReversed.refTo = searchSig(RulesForAlloyable
                            .tableSigName(tableNode.getFullName()));

                    this.relations.add(relationReversed);

                    // スキップ定義
                    foreignKeys.add(tableNode.getFullName()
                            + INTERNAL_SEPERATOR
                            + relation.originPropertyName);
                }
            }
        }
        return this;
    }

    public Alloyable buildColumnSigs(List<CreateTableNode> parsedDDLList) {
        for (CreateTableNode tableNode : parsedDDLList) {
            for (TableElementNode tableElement : tableNode
                    .getTableElementList()) {
                if (tableElement.getClass().equals(ColumnDefinitionNode.class)) {
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
                        Relation relation = new Relation(Relation.Tipify.VALUE);
                        relation.originOwner = tableNode.getFullName();
                        relation.owner = searchSig(RulesForAlloyable
                                .tableSigName(relation.originOwner));
                        relation.name = RulesForAlloyable
                                .colmnRelationName(column.getName(),
                                        relation.originOwner);
                        relation.refTo = new Sig(Sig.Tipify.BOOLEAN_FACTOR);
                        this.relations.add(relation);
                        continue;
                    }

                    Sig sigColomn = new Sig(Sig.Tipify.PROPERTY_PROTOTYPE);
                    sigColomn.originPropertyName = column.getName();
                    sigColomn.name = RulesForAlloyable.colmnSigName(
                            column.getName(), tableNode.getFullName());
                    sigColomn.isAbstruct = Boolean.TRUE;
                    sigColomn.setParent(searchSig(RulesForAlloyable
                            .tableSigName(tableNode.getFullName())));
                    this.sigs.add(sigColomn);

                    List<Sig> propertyFactorSigs = RulesForAlloyable
                            .defaultPropertyFactor(column.getName(),
                                    tableNode.getFullName());
                    for (Sig propertyFactorSig : propertyFactorSigs) {
                        this.sigs.add(propertyFactorSig);
                    }
                    
                    new Relation(Relation.Tipify.VALUE);
                }
            }
        }
        return this;
    }
}
