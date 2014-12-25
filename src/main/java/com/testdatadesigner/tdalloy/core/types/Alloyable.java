package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
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
import com.testdatadesigner.tdalloy.core.type_bulder.PolymophicHandler;
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
    private PolymophicHandler polymRelHandler = new PolymophicHandler();

    private List<String> skipElementListForColumn = new ArrayList<>();
    HashMap<String, List<String>> allInferencedPolymophicSet = new HashMap<String, List<String>>();
    private Integer dummyNamingSeq = new Integer(-1);
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

        Supplier<Integer> getNamingSeq = () -> {
            this.dummyNamingSeq++;
            return this.dummyNamingSeq;
        };

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
            List<String> inferencedPolymophicSet = inferenced.get(0);
            List<String> inferencedForeignKeySet = inferenced.get(1);
            allInferencedPolymophicSet.put(tableNode.getFullName(), inferencedPolymophicSet);

            // ポリモーフィック
            if (!inferencedPolymophicSet.isEmpty()) {
                this.isRailsOriented = Boolean.TRUE;
                for (String polymophicStr : inferencedPolymophicSet) {
//                    List<DummySig> twoDummySigs =
//                            polymRelHandler.buildDummies(getNamingSeq, tableNode.getFullName());
//                    List<Sig> builtSigs =
//                            polymRelHandler.buildSig(sigSearchByName, twoDummySigs, polymophicStr,
//                                    tableNode.getFullName());
//                    this.sigs.addAll(builtSigs);
//                    List<Relation> builtRelations =
//                            polymRelHandler.buildRelation(sigSearchByName, twoDummySigs,
//                                    polymophicStr, tableNode.getFullName());
//                    this.relations.addAll(builtRelations);
//                    this.facts.addAll(polymRelHandler.buildFact(builtRelations, twoDummySigs));
//                    
                    // スキップ定義
                    addToSkip(tableNode.getFullName(), polymophicStr
                            + RulesForAlloyable.FOREIGN_KEY_SUFFIX);
                    addToSkip(tableNode.getFullName(), polymophicStr
                            + RulesForAlloyable.POLYMOPHIC_SUFFIX);
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

                        if (RulesForAlloyable.isInferencedPolymophic(column.getName(),
                                allInferencedPolymophicSet.get(tableNode.getFullName()))) {
                            Sig polymColumnSig =
                                    columnHandler.buildSigPolymophicProspected(sigSearchByName,
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

    public void fixPolymophic() {
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
