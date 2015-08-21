package com.testdatadesigner.tdalloy.core.types;

import com.foundationdb.sql.parser.*;
import com.foundationdb.sql.parser.ConstraintDefinitionNode.ConstraintType;
import com.google.common.base.Joiner;
import com.testdatadesigner.tdalloy.core.naming.IRulesForAlloyable;
import com.testdatadesigner.tdalloy.core.naming.RulesForAlloyableFactory;
import com.testdatadesigner.tdalloy.core.naming.RulesForAlloyableRails;
import com.testdatadesigner.tdalloy.core.type_bulder.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Alloyable implements Serializable {
    private static final long serialVersionUID = 1L;
    public List<Atom> atoms = new ArrayList<>();
    public List<Relation> relations = new ArrayList<>();
    public List<Fact> facts = new ArrayList<>();
    public Boolean isRailsOriented = Boolean.FALSE;

    private TableHandler tableHandler = new TableHandler();
    private RelationHandler relationHandler = new RelationHandler();
    private DefaultColumnHandler columnHandler = new DefaultColumnHandler();
    private BooleanColumnHandler booleanColumnHandler = new BooleanColumnHandler();
    private PolymorphicHandler polymorphicHandler = new PolymorphicHandler();

    private List<String> skipElementListForColumn = new ArrayList<>();
    HashMap<String, List<String>> allInferencedPolymorphicSet = new HashMap<String, List<String>>();
    static final String INTERNAL_SEPARATOR = "_#_";

    /*
     * TODO: 自動で生成出来ない部分についての情報フィールド
     */

    Function<String, Atom> atomSearchByName = name -> this.atoms.stream()
            .filter(s -> s.name.equals(name)).collect(Collectors.toList()).get(0);

    IRulesForAlloyable namingRule = RulesForAlloyableFactory.getInstance().getRule();

    private void addToSkip(String tableName, String keyStr) {
        skipElementListForColumn.add(tableName + INTERNAL_SEPARATOR + keyStr);
    }

    /**
     * テーブルの処理。 Constraintsに定義されている外部キーによる関連の処理
     * ポリモーフィック関連推論と （Constraints で定義されていない）外部キー推論。 カラムの処理。
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

            this.atoms.add(tableHandler.build(tableNode.getFullName()));

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
                            relationHandler.build(atomSearchByName, tableNode.getFullName(),
                                    ((ResultColumn) constraint.getColumnList().get(0)).getName(),
                                    constraint.getRefTableName().getFullTableName());
                    this.relations.addAll(relations);
                    this.facts.add(relationHandler.buildFact(relations));
                    // スキップ対象にadd
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
            List<List<String>> inferenced = namingRule.inferencedRelations(columnNames);
            List<String> inferencedPolymorphicSet = inferenced.get(0);
            List<String> inferencedForeignKeySet = inferenced.get(1);
            allInferencedPolymorphicSet.put(tableNode.getFullName(), inferencedPolymorphicSet);

            // ポリモーフィック
            if (!inferencedPolymorphicSet.isEmpty()) {
                this.isRailsOriented = Boolean.TRUE;
                for (String polymorphicStr : inferencedPolymorphicSet) {
                    // スキップ対象にadd
                    addToSkip(tableNode.getFullName(), polymorphicStr
                            + RulesForAlloyableRails.FOREIGN_KEY_SUFFIX);
                    addToSkip(tableNode.getFullName(), polymorphicStr
                            + RulesForAlloyableRails.POLYMORPHIC_SUFFIX);
                }
            }
            // 外部キー
            if (!inferencedForeignKeySet.isEmpty()) {
                this.isRailsOriented = Boolean.TRUE;
                for (String keyStr : inferencedForeignKeySet) {
                    // スキップ
                    if (skipElementListForColumn.contains(tableNode.getFullName()
                            + INTERNAL_SEPARATOR + keyStr)) {
                        continue;
                    }
                    List<Relation> relations =
                            relationHandler.build(atomSearchByName, tableNode.getFullName(), keyStr,
                                    String.valueOf(""));
                    this.relations.addAll(relations);
                    this.facts.add(relationHandler.buildFact(relations));
                    // スキップ対象にadd
                    addToSkip(tableNode.getFullName(), keyStr);
                }
            }
        }

        /*
         * カラムの処理（含 ポリモーフィックの、typeのほうの、sig化）。
         */
        for (CreateTableNode tableNode : parsedDDLList) {
            // for polymorphic relations
            int buildPolymRelationCount = 0;

            for (TableElementNode tableElement : tableNode.getTableElementList()) {
                if (tableElement.getClass().equals(ColumnDefinitionNode.class)) {
                    ColumnDefinitionNode column = (ColumnDefinitionNode) tableElement;
                    /*
                     * ポリモーフィック関連
                     */
                    if (skipElementListForColumn.contains(tableNode.getFullName()
                        + INTERNAL_SEPARATOR + column.getName())) {
                        if (namingRule.isInferencedPolymorphic(column.getName(),
                            allInferencedPolymorphicSet.get(tableNode.getFullName()))) {
                            // as sig
                            Atom polymAbstructAtom =
                                columnHandler.buildAtomPolymorphicProspected(atomSearchByName,
                                    tableNode.getFullName(), column.getName());
                            polymAbstructAtom.originTypeName = column.getType().getTypeName();
                            this.atoms.add(polymAbstructAtom);
                            // as fields
                            if (buildPolymRelationCount == 0) {
                                for (String polymorphicStr : allInferencedPolymorphicSet.get(tableNode.getFullName())) {
                                    List<Relation> polymophicRelation =
                                        polymorphicHandler.buildRelation(atomSearchByName, polymorphicStr, tableNode.getFullName(), polymAbstructAtom);
                                    this.relations.addAll(polymophicRelation);

                                    // as basic fact
                                    this.facts.add(polymorphicHandler.buildFactBase(polymophicRelation));
                                }
                                buildPolymRelationCount++;
                            }
                        }
                        continue;
                    }
                    /*
                     * その他ふつうのカラム
                     */
                    if (column.getType().getSQLstring().equals("TINYINT")) {
                        this.relations.add(booleanColumnHandler.build(atomSearchByName,
                            tableNode.getFullName(), column.getName()));
                    } else {
                        this.relations.add(columnHandler.buildRelation(atomSearchByName,
                            tableNode.getFullName(), column.getName()));
                    }
                }
            }
        }
        return this;
    }

    /**
     * Alloyableインスタンスからalloy定義を生成。
     *
     * @return String
     * @throws IOException
     */
    public File outputToAls() throws IOException {
        File tempFile = File.createTempFile("tdalloyToAlsFromAlloyable", "als");
        tempFile.deleteOnExit();

        RuleForAls ruleForAls = new RuleForAls();

        Function<Atom, List<Relation>> atomSearchByRelationOwner = atom -> this.relations.stream()
            .filter(rel -> rel.owner.name.equals(atom.name)).collect(Collectors.toList());

        String indent = "  ";
        try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8"))){
            StringBuffer strBuff = new StringBuffer();

            strBuff.append("open util/boolean\n");
            strBuff.append("sig Boundary { val: one Int }\n");  // FIXME: 仮実装
            strBuff.append("\n");
            writer.write(strBuff.toString());
            strBuff.setLength(0);

            for (Atom atom : this.atoms) {
                StringBuffer sigStrBuff = new StringBuffer();
                /*
                 * sig にする。
                 */
                String sigStr = atom.type.equals(Atom.Tipify.POLYMORPHIC_ABSTRACT) ? "abstract sig " : "sig ";
                sigStrBuff.append(sigStr);
                sigStrBuff.append(atom.name);
                sigStrBuff.append(" {");
                sigStrBuff.append("\n");
            	/*
            	 * それを参照しているRELATIONを探してfieldにする。
            	 */
                List<Relation> relations = atomSearchByRelationOwner.apply(atom);
                List<String> fields = new ArrayList<String>();
                for (Relation relation : relations) {
                    fields.add(relation.name + ": " + ruleForAls.searchQuantifierMap(relation) + " " + relation.refTo.name);
                }
                sigStrBuff.append(indent);
                sigStrBuff.append(Joiner.on(",\n" + indent).join(fields));

                sigStrBuff.append("\n");
                sigStrBuff.append("}");
                sigStrBuff.append("\n");

                writer.write(sigStrBuff.toString());
            }

            strBuff.append("\n");
            strBuff.append("fact {\n");
            writer.write(strBuff.toString());
            strBuff.setLength(0);
            for (Fact fact : this.facts) {
                StringBuffer factStrBuff = new StringBuffer();
                factStrBuff.append(indent);
                factStrBuff.append(fact.value);
                factStrBuff.append("\n");
                writer.write(factStrBuff.toString());
            }
            strBuff.append("}\n");
            writer.write(strBuff.toString());
            strBuff.setLength(0);
        }
        return tempFile;
    }

    public void fixPolymorphic() {
        // ダミーAtomを実在Atomにマージ
    }

    public void fixOneToOne() {

    }

    public void omitColumns() {

    }

    public String toJson() {
        return null;
    }

}
