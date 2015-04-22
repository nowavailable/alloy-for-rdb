package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.foundationdb.sql.parser.ColumnDefinitionNode;
import com.foundationdb.sql.parser.ConstraintDefinitionNode;
import com.foundationdb.sql.parser.CreateTableNode;
import com.foundationdb.sql.parser.FKConstraintDefinitionNode;
import com.foundationdb.sql.parser.ResultColumn;
import com.foundationdb.sql.parser.TableElementNode;
import com.foundationdb.sql.parser.ConstraintDefinitionNode.ConstraintType;
import com.testdatadesigner.tdalloy.core.type_bulder.BooleanColumnHandler;
import com.testdatadesigner.tdalloy.core.type_bulder.DefaultColumnHandler;
//import com.testdatadesigner.tdalloy.core.type_bulder.PolymorphicHandler;
import com.testdatadesigner.tdalloy.core.type_bulder.RelationHandler;
import com.testdatadesigner.tdalloy.core.type_bulder.TableHandler;

public class Alloyable implements Serializable {
    private static final long serialVersionUID = 1L;
    public List<Sig> sigs = new ArrayList<>();
    public List<Relation> relations = new ArrayList<>();
    public List<Fact> facts = new ArrayList<>();
    public Boolean isRailsOriented = Boolean.FALSE;

    private TableHandler tableHandler = new TableHandler();
    private RelationHandler relationHander = new RelationHandler();
    private DefaultColumnHandler columnHandler = new DefaultColumnHandler();
    private BooleanColumnHandler booleanColumnHandler = new BooleanColumnHandler();

    private List<String> skipElementListForColumn = new ArrayList<>();
    HashMap<String, List<String>> allInferencedPolymorphicSet = new HashMap<String, List<String>>();
    static final String INTERNAL_SEPERATOR = "_#_";

    /*
     * TODO: 自動で生成出来ない部分についての情報フィールド
     */

    Function<String, Sig> sigSearchByName = name -> this.sigs.stream()
            .filter(s -> s.name.equals(name)).collect(Collectors.toList()).get(0);

    private void addToSkip(String tableName, String keyStr) {
        skipElementListForColumn.add(tableName + INTERNAL_SEPERATOR + keyStr);
    }

    /**
     * テーブルの処理。 Constraintsに定義されている外部キーによる関連の処理 ポリモーフィック関連推論と （Constraints で定義されていない）外部キー推論。 カラムの処理。
     * という順。
     * 
     * @param parsedDDLList
     * @return this
     * @throws IllegalAccessException
     */
    public Alloyable buildFromDDL(List<CreateTableNode> parsedDDLList)
            throws IllegalAccessException {
        /*
         * テーブルの処理。
         */
        for (CreateTableNode tableNode : parsedDDLList) {

            this.sigs.add(tableHandler.build(tableNode.getFullName()));

            for (TableElementNode tableElement : tableNode.getTableElementList()) {
                // 外部キーはスキップ対象に。
                if (tableElement.getClass().equals(FKConstraintDefinitionNode.class)) {
                    FKConstraintDefinitionNode constraint =
                            (FKConstraintDefinitionNode) tableElement;
                    addToSkip(tableNode.getFullName(), ((ResultColumn) constraint.getColumnList()
                            .get(0)).getName());
                }
                // プライマリキーはスキップ対象に
                if (tableElement.getClass().equals(ConstraintDefinitionNode.class)) {
                    ConstraintDefinitionNode constraint = (ConstraintDefinitionNode) tableElement;
                    if (constraint.getConstraintType().equals(ConstraintType.PRIMARY_KEY)) {
                        addToSkip(tableNode.getFullName(), ((ResultColumn) constraint
                                .getColumnList().get(0)).getName());
                    }
                }
            }
        }
        /*
         * Constraintsに定義されている外部キーによる関連の処理
         */
        for (CreateTableNode tableNode : parsedDDLList) {
            for (TableElementNode tableElement : tableNode.getTableElementList()) {
                if (tableElement.getClass().equals(FKConstraintDefinitionNode.class)) {
                    FKConstraintDefinitionNode constraint =
                            (FKConstraintDefinitionNode) tableElement;
                    List<Relation> relations =
                            relationHander.build(sigSearchByName, tableNode.getFullName(),
                                    ((ResultColumn) constraint.getColumnList().get(0)).getName(),
                                    constraint.getRefTableName().getFullTableName());
                    this.relations.addAll(relations);
                    this.facts.add(relationHander.buildFact(relations));
                    // スキップ定義
                    addToSkip(tableNode.getFullName(), ((ResultColumn) constraint.getColumnList()
                            .get(0)).getName());
                }
            }
        }

        /*
         * ポリモーフィック関連推論と （Constraints で定義されていない）外部キー推論。
         */
        for (CreateTableNode tableNode : parsedDDLList) {
            List<String> columnNames = new ArrayList<>();
            for (TableElementNode tableElement : tableNode.getTableElementList()) {
                if (tableElement.getClass().equals(ColumnDefinitionNode.class)) {
                    ColumnDefinitionNode column = (ColumnDefinitionNode) tableElement;
                    columnNames.add(column.getName());
                }
            }
            List<List<String>> inferenced = RulesForAlloyable.inferencedRelations(columnNames);
            List<String> inferencedPolymorphicSet = inferenced.get(0);
            List<String> inferencedForeignKeySet = inferenced.get(1);
            allInferencedPolymorphicSet.put(tableNode.getFullName(), inferencedPolymorphicSet);

            // ポリモーフィック
            if (!inferencedPolymorphicSet.isEmpty()) {
                this.isRailsOriented = Boolean.TRUE;
                for (String polymorphicStr : inferencedPolymorphicSet) {
                    // スキップ定義
                    addToSkip(tableNode.getFullName(), polymorphicStr
                            + RulesForAlloyable.FOREIGN_KEY_SUFFIX);
                    addToSkip(tableNode.getFullName(), polymorphicStr
                            + RulesForAlloyable.POLYMORPHIC_SUFFIX);
                }
            }
            // 外部キー
            if (!inferencedForeignKeySet.isEmpty()) {
                this.isRailsOriented = Boolean.TRUE;
                for (String keyStr : inferencedForeignKeySet) {
                    // スキップ
                    if (skipElementListForColumn.contains(tableNode.getFullName()
                            + INTERNAL_SEPERATOR + keyStr)) {
                        continue;
                    }
                    List<Relation> relations =
                            relationHander.build(sigSearchByName, tableNode.getFullName(), keyStr,
                                    String.valueOf(""));
                    this.relations.addAll(relations);
                    this.facts.add(relationHander.buildFact(relations));
                    // スキップ定義
                    addToSkip(tableNode.getFullName(), keyStr);
                }
            }
        }

        /*
         * カラムの処理。
         */
        for (CreateTableNode tableNode : parsedDDLList) {
            for (TableElementNode tableElement : tableNode.getTableElementList()) {
                if (tableElement.getClass().equals(ColumnDefinitionNode.class)) {
                    ColumnDefinitionNode column = (ColumnDefinitionNode) tableElement;
                    // スキップ
                    if (skipElementListForColumn.contains(tableNode.getFullName()
                            + INTERNAL_SEPERATOR + column.getName())) {

                        if (RulesForAlloyable.isInferencedPolymorphic(column.getName(),
                                allInferencedPolymorphicSet.get(tableNode.getFullName()))) {
                            Sig polymColumnSig =
                                    columnHandler.buildSigPolymorphicProspected(sigSearchByName,
                                            tableNode.getFullName(), column.getName());
                            polymColumnSig.originTypeName = column.getType().getTypeName();
                            this.sigs.add(polymColumnSig);
                        }

                        continue;
                    }
                    // Booleanフィールドはsigとしては扱わないのでスキップ
                    if (column.getType().getSQLstring().equals("TINYINT")) {
                        this.relations.add(booleanColumnHandler.build(sigSearchByName,
                                tableNode.getFullName(), column.getName()));
                        continue;
                    }

                    Sig columnSig = columnHandler.buildSig(sigSearchByName, tableNode.getFullName(),
                            column.getName());
                    columnSig.originTypeName = column.getType().getTypeName();
                    this.sigs.add(columnSig);
                    List<Sig> propertyFactorSigs =
                            columnHandler
                                    .buildFactorSigs(tableNode.getFullName(), column.getName());
                    propertyFactorSigs.forEach(sig -> this.sigs.add(sig));
                    this.relations.add(columnHandler.buildRelation(sigSearchByName,
                            tableNode.getFullName(), column.getName(), propertyFactorSigs));
                }
            }
        }
        return this;
    }

    public void fixPolymorphic() {
        // ダミーSigを実在Sigにマージ
    }

    public void fixOneToOne() {

    }

    public void omitColumns() {

    }

    public String toJson() {
        return null;
    }

}
