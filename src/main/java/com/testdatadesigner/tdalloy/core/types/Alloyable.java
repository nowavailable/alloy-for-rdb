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
    private List<String> polymophicColumns = new ArrayList<String>();
    private List<String> foreignKeys = new ArrayList<String>();
    static final String INTERNAL_SEPERATOR = "_#_";

    public Alloyable buildTableSigs(List<CreateTableNode> parsedDDLList) {
        for (CreateTableNode tableNode : parsedDDLList) {
            Sig sig = new Sig();
            sig.originPropertyName = tableNode.getFullName();
            sig.name = RulesForAlloyable.generateTableSigName(tableNode
                    .getFullName());
            sig.type = com.testdatadesigner.tdalloy.core.types.Sig.Tipify.ENTITY;
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
    public Alloyable buildinferencedRelations(
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
                for (String keyStr : polymophicSet) {
                    skipElementListForColumn.add(tableNode.getFullName()
                            + INTERNAL_SEPERATOR + keyStr + "_id");
                    skipElementListForColumn.add(tableNode.getFullName()
                            + INTERNAL_SEPERATOR + keyStr + "_type");
                    polymophicColumns.add(tableNode.getFullName()
                            + INTERNAL_SEPERATOR + keyStr);

                    // 1/9
                    
                    // 2/9
                    // 3/9
                    // 4/9
                    
                    // 5/9
                    
                    // 6/9
                    // 7/9
                    // 8/9
                    // 9/9
                }
            }

            if (!foreignKeySet.isEmpty()) {
                for (String keyStr : foreignKeySet) {
                    skipElementListForColumn.add(tableNode.getFullName()
                            + INTERNAL_SEPERATOR + keyStr);
                    foreignKeys.add(tableNode.getFullName()
                            + INTERNAL_SEPERATOR + keyStr);

                    // 外部キー保持側
                    Relation relation = new Relation();
                    relation.type = com.testdatadesigner.tdalloy.core.types.Relation.Tipify.RELATION;
                    relation.originPropertyName = keyStr;
                    relation.name = RulesForAlloyable.generateForeignKeyName(
                            relation.originPropertyName,
                            tableNode.getFullName());
                    relation.originOwner = tableNode.getFullName();
                    relation.owner = searchSig(RulesForAlloyable
                            .generateTableSigName(relation.originOwner));
                    relation.referTo = searchSig(RulesForAlloyable
                            .generateTableSigNameFromFKey(relation.originPropertyName));

                    this.relations.add(relation);

                    // 参照される側
                    Relation relationReversed = new Relation();
                    relationReversed.type = com.testdatadesigner.tdalloy.core.types.Relation.Tipify.RELATION_REVERSED;
                    relationReversed.originOwner = RulesForAlloyable
                            .generateTableNameFromFKey(keyStr);
                    relationReversed.owner = searchSig(RulesForAlloyable
                            .generateTableSigName(relationReversed.originOwner));
                    relationReversed.name = RulesForAlloyable
                            .generateForeignKeyNameReversed(
                                    relationReversed.originOwner,
                                    tableNode.getFullName());
                    relationReversed.referTo = searchSig(RulesForAlloyable
                            .generateTableSigName(tableNode.getFullName()));

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
                    Relation relation = new Relation();
                    relation.type = com.testdatadesigner.tdalloy.core.types.Relation.Tipify.RELATION;
                    relation.originPropertyName = ((ResultColumn) constraint
                            .getColumnList().get(0)).getName();
                    relation.name = RulesForAlloyable.generateForeignKeyName(
                            relation.originPropertyName,
                            tableNode.getFullName());
                    relation.originOwner = tableNode.getFullName();
                    relation.owner = searchSig(RulesForAlloyable
                            .generateTableSigName(relation.originOwner));
                    relation.referTo = searchSig(RulesForAlloyable
                            .generateTableSigName(constraint.getRefTableName()
                                    .getFullTableName()));

                    this.relations.add(relation);

                    // 参照される側
                    Relation relationReversed = new Relation();
                    relationReversed.type = com.testdatadesigner.tdalloy.core.types.Relation.Tipify.RELATION_REVERSED;
                    relationReversed.originOwner = constraint.getRefTableName()
                            .getFullTableName();
                    relationReversed.owner = searchSig(RulesForAlloyable
                            .generateTableSigName(relationReversed.originOwner));
                    relationReversed.name = RulesForAlloyable
                            .generateForeignKeyNameReversed(
                                    relationReversed.originOwner,
                                    tableNode.getFullName());
                    relationReversed.referTo = searchSig(RulesForAlloyable
                            .generateTableSigName(tableNode.getFullName()));

                    this.relations.add(relationReversed);

                    // スキップ
                    foreignKeys.add(tableNode.getFullName()
                            + INTERNAL_SEPERATOR
                            + relation.originPropertyName);
                }

                // booleanフィールド

                // その他の属性
            }
        }
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

                    sigColomn.originPropertyName = column.getName();
                    sigColomn.name = RulesForAlloyable.generateColmnSigName(
                            column.getName(), tableNode.getFullName());
                    sigColomn.type = com.testdatadesigner.tdalloy.core.types.Sig.Tipify.PROPERTY_PROTOTYPE;
                    sigColomn.isAbstruct = Boolean.TRUE;
                    sigColomn.setParent(searchSig(RulesForAlloyable
                            .generateTableSigName(tableNode.getFullName())));
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
