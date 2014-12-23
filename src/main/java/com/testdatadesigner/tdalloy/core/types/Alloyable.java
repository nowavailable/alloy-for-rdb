package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;
import java.util.ArrayList;
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

    // TODO: 自動で生成出来ない部分についての情報フィールド

    public String toJson() {
        return null;
    }

    private Sig searchSig(String name) {
        return this.sigs.stream().filter(s -> s.name.equals(name))
                .collect(Collectors.toList()).get(0);
    }

    // -- 振る舞い系メソッド。いずれMix-inの形に?
    // --------------------------------------------------------------
    public void fixPolymophic() {

    }

    public void fixOneToOne() {

    }

    public void omitColumns() {

    }

    private TableHandler tableHandler = new TableHandler();
    private RelationHandler relationHander = new RelationHandler();
    private DefaultColumnHandler columnHandler = new DefaultColumnHandler();
    private BooleanColumnHandler booleanColumnHandler = new BooleanColumnHandler();
    private PolymophicHandler polymRelHandler = new PolymophicHandler();

    private List<String> skipElementListForColumn = new ArrayList<>();
    private List<String> polymophicColumns = new ArrayList<>();
    private List<String> foreignKeys = new ArrayList<>();
    private Integer dummyNamingSeq = new Integer(-1);
    static final String INTERNAL_SEPERATOR = "_#_";

    public Alloyable buildFromTable(List<CreateTableNode> parsedDDLList) {
        for (CreateTableNode tableNode : parsedDDLList) {

            this.sigs.add(tableHandler.build(tableNode));

            for (TableElementNode tableElement : tableNode
                    .getTableElementList()) {
                // 外部キーはスキップ対象に。
                if (tableElement.getClass().equals(
                        FKConstraintDefinitionNode.class)) {
                    FKConstraintDefinitionNode constraint = (FKConstraintDefinitionNode) tableElement;
                    skipElementListForColumn
                            .add(tableNode.getFullName()
                                    + INTERNAL_SEPERATOR
                                    + ((ResultColumn) constraint
                                            .getColumnList().get(0)).getName());
                }
                // プライマリキーはスキップ対象に
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
    public Alloyable buildByInference(List<CreateTableNode> parsedDDLList)
            throws IllegalAccessException {

        Supplier<Integer> getNamingSeq = () -> {
            this.dummyNamingSeq++;
            return this.dummyNamingSeq;
        };
        
        for (CreateTableNode tableNode : parsedDDLList) {
            List<String> columnNames = new ArrayList<>();
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
                            + INTERNAL_SEPERATOR + keyStr
                            + RulesForAlloyable.FOREIGN_KEY_SUFFIX);
                    skipElementListForColumn.add(tableNode.getFullName()
                            + INTERNAL_SEPERATOR + keyStr
                            + RulesForAlloyable.POLYMOPHIC_SUFFIX);
                    polymophicColumns.add(tableNode.getFullName()
                            + INTERNAL_SEPERATOR + keyStr);

                    List<DummySig> dummyRefToSigs = polymRelHandler.buildDummies(
                            getNamingSeq, tableNode.getFullName());
                            
                    Function<String, Sig> sigSearchByName = name -> this.sigs
                            .stream().filter(s -> s.name.equals(name))
                            .collect(Collectors.toList()).get(0);
                    List<Sig> builtSigs = polymRelHandler.buildSig(
                            sigSearchByName, getNamingSeq, dummyRefToSigs, keyStr,
                            tableNode.getFullName());

                    builtSigs.forEach(s -> this.sigs.add(s));

                    sigSearchByName = name -> this.sigs.stream()
                            .filter(s -> s.name.equals(name))
                            .collect(Collectors.toList()).get(0);
                    List<Relation> builtRelations = polymRelHandler
                            .buildRelation(sigSearchByName, dummyRefToSigs, keyStr,
                                    tableNode.getFullName());

                    builtRelations.forEach(s -> this.relations.add(s));
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

                    Function<String, Sig> sigSearchByName = name -> this.sigs
                            .stream().filter(s -> s.name.equals(name))
                            .collect(Collectors.toList()).get(0);
                    List<Relation> relations = relationHander.build(
                            sigSearchByName, tableNode.getFullName(), keyStr,
                            String.valueOf(""));
                    relations.forEach(rel -> this.relations.add(rel));
                }
            }
        }
        return this;
    }

    /**
     * Constraintsに定義されている外部キーによる関連
     * 
     * @param parsedDDLList
     * @return this
     * @throws IllegalAccessException
     */
    public Alloyable buildFromForeignKey(List<CreateTableNode> parsedDDLList)
            throws IllegalAccessException {
        for (CreateTableNode tableNode : parsedDDLList) {
            for (TableElementNode tableElement : tableNode
                    .getTableElementList()) {
                // 外部キー
                if (tableElement.getClass().equals(
                        FKConstraintDefinitionNode.class)) {
                    FKConstraintDefinitionNode constraint = (FKConstraintDefinitionNode) tableElement;
                    // スキップ定義
                    foreignKeys
                            .add(tableNode.getFullName()
                                    + INTERNAL_SEPERATOR
                                    + ((ResultColumn) constraint
                                            .getColumnList().get(0)).getName());

                    Function<String, Sig> sigSearchByName = name -> this.sigs
                            .stream().filter(s -> s.name.equals(name))
                            .collect(Collectors.toList()).get(0);

                    List<Relation> relations = relationHander.build(
                            sigSearchByName, tableNode.getFullName(),
                            ((ResultColumn) constraint.getColumnList().get(0))
                                    .getName(), constraint.getRefTableName()
                                    .getFullTableName());
                    relations.forEach(rel -> this.relations.add(rel));
                }
            }
        }
        return this;
    }

    public Alloyable buildFromColumn(List<CreateTableNode> parsedDDLList)
            throws IllegalAccessException {
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
                        Function<String, Sig> sigSearchByName = name -> this.sigs
                                .stream().filter(s -> s.name.equals(name))
                                .collect(Collectors.toList()).get(0);
                        this.relations.add(booleanColumnHandler.build(
                                sigSearchByName, tableNode.getFullName(),
                                column.getName()));
                        continue;
                    }

                    Function<String, Sig> sigSearchByName = name -> this.sigs
                            .stream().filter(s -> s.name.equals(name))
                            .collect(Collectors.toList()).get(0);
                    this.sigs.add(columnHandler.buildSig(sigSearchByName,
                            tableNode.getFullName(), column.getName()));

                    List<Sig> propertyFactorSigs = columnHandler
                            .buildFactorSigs(tableNode.getFullName(),
                                    column.getName());
                    propertyFactorSigs.forEach(sig -> this.sigs.add(sig));

                    sigSearchByName = name -> this.sigs
                            .stream().filter(s -> s.name.equals(name))
                            .collect(Collectors.toList()).get(0);
                    this.relations.add(columnHandler.buildRelation(
                            sigSearchByName, tableNode.getFullName(),
                            column.getName(), propertyFactorSigs));
                }
            }
        }
        return this;
    }
}
