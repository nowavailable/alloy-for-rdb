package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
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
    private Integer dummyNamingSeq = new Integer(0);
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

                    // ダミー作成
                    DummySig refToDummySig_1 = new DummySig(
                            Sig.Tipify.POLYMOPHIC_IMPLIMENT, dummyNamingSeq);
                    dummyNamingSeq++;
                    DummySig refToDummySig_2 = new DummySig(
                            Sig.Tipify.POLYMOPHIC_IMPLIMENT, dummyNamingSeq);
                    dummyNamingSeq++;

                    // 1/9
                    MultipleRelation<DummySig> valueRelation = new MultipleRelation<>(
                            Relation.Tipify.VALUE);
                    // valueRelation.originPropertyName = keyStr;
                    // valueRelation.originOwner = tableNode.getFullName();
                    valueRelation.name = RulesForAlloyable.colmnRelationName(
                            keyStr + RulesForAlloyable.POLYMOPHIC_SUFFIX,
                            tableNode.getFullName());
                    valueRelation.owner = searchSig(RulesForAlloyable
                            .tableSigName(tableNode.getFullName()));
                    valueRelation.refToTypes = Arrays.asList(refToDummySig_1,
                            refToDummySig_2);
                    this.relations.add(valueRelation);

                    // 2/9
                    // 3/9
                    Relation relForDummy1 = new Relation(
                            Relation.Tipify.RELATION_REVERSED);
                    relForDummy1.name = RulesForAlloyable
                            .foreignKeyNameReversed(tableNode.getFullName(),
                                    refToDummySig_1.name);
                    relForDummy1.owner = refToDummySig_1;
                    relForDummy1.refTo = valueRelation.owner;
                    this.relations.add(relForDummy1);
                    Relation relForDummy2 = new Relation(
                            Relation.Tipify.RELATION_REVERSED);
                    relForDummy2.name = RulesForAlloyable
                            .foreignKeyNameReversed(tableNode.getFullName(),
                                    refToDummySig_2.name);
                    relForDummy2.owner = refToDummySig_2;
                    relForDummy2.refTo = valueRelation.owner;
                    this.relations.add(relForDummy2);

                    // 4/9
                    Sig polymophicSig = new Sig(
                            Sig.Tipify.POLYMOPHIC_TYPE_ABSTRACT);
                    polymophicSig.originPropertyName = keyStr
                            + RulesForAlloyable.POLYMOPHIC_SUFFIX;
                    polymophicSig.name = RulesForAlloyable.colmnSigName(
                            polymophicSig.originPropertyName,
                            tableNode.getFullName());
                    polymophicSig.setParent(searchSig(RulesForAlloyable
                            .tableSigName(tableNode.getFullName())));
                    polymophicSig.isAbstruct = Boolean.TRUE;
                    this.sigs.add(polymophicSig);

                    // 5/9
                    MultipleRelation<DummySig> polymRelationReversed = new MultipleRelation<>(
                            Relation.Tipify.ABSTRUCT_RELATION);
                    polymRelationReversed.name = "refTo_"
                            + RulesForAlloyable.tableSigName(tableNode
                                    .getFullName());
                    polymRelationReversed.refTo = searchSig(RulesForAlloyable
                            .tableSigName(tableNode.getFullName()));

                    // 6/9
                    // 8/9
                    DummySig polymImpleSig_1 = new DummySig(
                            Sig.Tipify.POLYMOPHIC_IMPLIMENT, dummyNamingSeq);
                    dummyNamingSeq++;
                    polymImpleSig_1.setParent(polymophicSig);
                    polymImpleSig_1.name = RulesForAlloyable
                            .implimentedPolymophicSigName(keyStr,
                                    refToDummySig_1.originPropertyName);
                    this.sigs.add(polymImpleSig_1);
                    DummySig polymImpleSig_2 = new DummySig(
                            Sig.Tipify.POLYMOPHIC_IMPLIMENT, dummyNamingSeq);
                    dummyNamingSeq++;
                    polymImpleSig_2.setParent(polymophicSig);
                    polymImpleSig_2.name = RulesForAlloyable
                            .implimentedPolymophicSigName(keyStr,
                                    refToDummySig_2.originPropertyName);
                    this.sigs.add(polymImpleSig_2);

                    // 5/9 の続き。
                    polymRelationReversed.reverseOfrefToTypes = Arrays.asList(
                            polymImpleSig_1, polymImpleSig_2);
                    this.relations.add(polymRelationReversed);

                    // 7/9
                    // 9/9
                    Relation polymImpleRel_1 = new Relation(
                            Relation.Tipify.ABSTRUCT_RELATION_REVERSED);
                    polymImpleRel_1.name = RulesForAlloyable
                            .singularize(refToDummySig_1.name);
                    polymImpleRel_1.refTo = refToDummySig_1;
                    polymImpleRel_1.owner = polymImpleSig_1;
                    this.relations.add(polymImpleRel_1);
                    Relation polymImpleRel_2 = new Relation(
                            Relation.Tipify.ABSTRUCT_RELATION_REVERSED);
                    polymImpleRel_2.name = RulesForAlloyable
                            .singularize(refToDummySig_2.name);
                    polymImpleRel_2.refTo = refToDummySig_2;
                    polymImpleRel_2.owner = polymImpleSig_2;
                    this.relations.add(polymImpleRel_2);
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
