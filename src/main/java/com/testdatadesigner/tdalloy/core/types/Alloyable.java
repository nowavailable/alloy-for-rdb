package com.testdatadesigner.tdalloy.core.types;

import com.foundationdb.sql.parser.*;
import com.foundationdb.sql.parser.ConstraintDefinitionNode.ConstraintType;
import com.google.common.base.Joiner;
import com.testdatadesigner.tdalloy.core.naming.IRulesForAlloyable;
import com.testdatadesigner.tdalloy.core.naming.RulesForAlloyableFactory;
import com.testdatadesigner.tdalloy.core.type_bulder.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    static final String INTERNAL_SEPARATOR = "_#_";

    private List<String> postponeListForColumn = new ArrayList<>();
    private HashMap<String, List<String>> allInferencedPolymorphicSet = new HashMap<String, List<String>>();

    private Function<String, Atom> atomSearchByName = name -> this.atoms.stream()
        .filter(s -> s.name.equals(name)).collect(Collectors.toList()).get(0);

    private IRulesForAlloyable namingRule = RulesForAlloyableFactory.getInstance().getRule();


    private void postpone(String tableName, String keyStr) {
        postponeListForColumn.add(tableName + INTERNAL_SEPARATOR + keyStr);
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

        List<ColumnDefinitionNode> allColumns = new ArrayList<>();
        Function<String, ColumnDefinitionNode> columnSearchByName = name -> allColumns.stream().
            filter(col -> col.getName().equals(name)).collect(Collectors.toList()).get(0);
        Pattern pattern = Pattern.compile(" NOT NULL");

        for (CreateTableNode tableNode : parsedDDLList) {

            this.atoms.add(tableHandler.build(tableNode.getFullName()));

            for (TableElementNode tableElement : tableNode.getTableElementList()) {
                // 外部キーはあとで処理。
                if (tableElement.getClass().equals(FKConstraintDefinitionNode.class)) {
                    FKConstraintDefinitionNode constraint =
                        (FKConstraintDefinitionNode) tableElement;
                    postpone(tableNode.getFullName(),
                        ((ResultColumn) constraint.getColumnList().get(0)).getName());
                }
                // プライマリキーはあとで処理
                else if (tableElement.getClass().equals(ConstraintDefinitionNode.class)) {
                    ConstraintDefinitionNode constraint = (ConstraintDefinitionNode) tableElement;
                    if (constraint.getConstraintType().equals(ConstraintType.PRIMARY_KEY)) {
                        postpone(tableNode.getFullName(),
                            ((ResultColumn) constraint.getColumnList().get(0)).getName());
                    }
                }
                // それ以外のelementをとりあえずぜんぶ保存
                else if (tableElement.getClass().equals(ColumnDefinitionNode.class)) {
                    allColumns.add((ColumnDefinitionNode)tableElement);
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
                    for (ResultColumn resultColumn : constraint.getColumnList()) {
                        List<Relation> relations =
                            relationHandler.build(atomSearchByName, tableNode.getFullName(),
                                resultColumn.getName(), constraint.getRefTableName().getFullTableName());
                        // カラムの制約
                        ColumnDefinitionNode column = columnSearchByName.apply(resultColumn.getName());
                        Matcher matcher = pattern.matcher(column.getType().toString());
                        relations.stream().
                            filter(rel -> rel.type.equals(Relation.Tipify.RELATION)).collect(Collectors.toList()).
                            get(0).isNotEmpty = matcher.find();
                        this.relations.addAll(relations);

                        this.facts.add(relationHandler.buildFact(relations));
                    }
                    // あとでさらに処理する。
                    postpone(tableNode.getFullName(),
                        ((ResultColumn) constraint.getColumnList().get(0)).getName());
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
            List<List<String>> guessed = namingRule.guessedRelations(columnNames);
            List<String> guessedPolymorphicSet = guessed.get(0);
            List<String> guessedForeignKeySet = guessed.get(1);
            allInferencedPolymorphicSet.put(tableNode.getFullName(), guessedPolymorphicSet);

            // ポリモーフィック
            if (!guessedPolymorphicSet.isEmpty()) {
                this.isRailsOriented = Boolean.TRUE;
                for (String polymorphicStr : guessedPolymorphicSet) {
                    // あとで処理する
                    postpone(tableNode.getFullName(),
                        polymorphicStr + namingRule.foreignKeySuffix());
                    postpone(tableNode.getFullName(),
                        polymorphicStr + namingRule.polymorphicSuffix());
                }
            }
            // 外部キー
            if (!guessedForeignKeySet.isEmpty()) {
                this.isRailsOriented = Boolean.TRUE;
                for (String keyStr : guessedForeignKeySet) {
                    // あとで処理するぶんはスキップ
                    if (postponeListForColumn.contains(tableNode.getFullName()
                        + INTERNAL_SEPARATOR + keyStr)) {
                        continue;
                    }
                    List<Relation> relations =
                        relationHandler.build(atomSearchByName, tableNode.getFullName(), keyStr,
                            String.valueOf(""));
                    // カラムの制約
                    ColumnDefinitionNode column = columnSearchByName.apply(keyStr);
                    Matcher matcher = pattern.matcher(column.getType().toString());
                    relations.stream().
                        filter(rel -> rel.type.equals(Relation.Tipify.RELATION)).collect(Collectors.toList()).
                        get(0).isNotEmpty = matcher.find();
                    this.relations.addAll(relations);

                    this.facts.add(relationHandler.buildFact(relations));
                    // あとでさらに処理する。
                    postpone(tableNode.getFullName(), keyStr);
                }
            }
        }

        /*
         * カラムの処理（含 ポリモーフィックの、typeのほうの、sig化）。
         */
        for (CreateTableNode tableNode : parsedDDLList) {
            // for polymorphic relations
            int buildPolymRelationCount = 0;
            int dummySigCount = 0;

            for (TableElementNode tableElement : tableNode.getTableElementList()) {
                if (tableElement.getClass().equals(ColumnDefinitionNode.class)) {
                    ColumnDefinitionNode column = (ColumnDefinitionNode) tableElement;
                    /*
                     * ポリモーフィック関連
                     */
                    if (postponeListForColumn.contains(tableNode.getFullName()
                        + INTERNAL_SEPARATOR + column.getName())) {
                        if (namingRule.isGuessedPolymorphic(column.getName(),
                            allInferencedPolymorphicSet.get(tableNode.getFullName()))) {
                            // as sig
                            Atom polymAbstructAtom =
                                columnHandler.buildAtomPolymorphicAbstract(atomSearchByName,
                                    tableNode.getFullName(), column.getName());
                            polymAbstructAtom.originTypeName = column.getType().getTypeName();
                            this.atoms.add(polymAbstructAtom);
                            // as fields
                            if (buildPolymRelationCount == 0) {
                                for (String polymorphicStr : allInferencedPolymorphicSet.get(tableNode.getFullName())) {
                                    List<Relation> polymophicRelations =
                                        polymorphicHandler.buildRelation(atomSearchByName, polymorphicStr, tableNode.getFullName(), polymAbstructAtom);
                                    this.relations.addAll(polymophicRelations);

                                    // as basic fact
                                    this.facts.add(polymorphicHandler.buildFactBase(polymophicRelations));

                                    // as sig by referrer and their fields
                                    List<Atom> dummies = polymorphicHandler.buildDummies(dummySigCount);
                                    this.atoms.addAll(dummies);

                                    dummySigCount = dummySigCount + dummies.size();

                                    // their dummy columns
                                    for (Atom dummyAtom : dummies) {
                                        Relation relation =
                                            polymorphicHandler.buildRelationForDummy(atomSearchByName, dummyAtom.originPropertyName,
                                                namingRule.fkeyFromTableName(polymAbstructAtom.getParent().originPropertyName),
                                                polymAbstructAtom.getParent().originPropertyName);
                                        this.relations.add(relation);
                                        // extend sig
                                        Atom polymImplAtom = polymorphicHandler.buildDummyExtend(polymorphicStr, dummyAtom, polymAbstructAtom);
                                        this.atoms.add(polymImplAtom);
                                        // and their field
                                        Relation polymRelation = polymorphicHandler.buildTypifiedRelation(polymImplAtom, dummyAtom);
                                        this.relations.add(polymRelation);
                                        // and fact
                                        this.facts.add(
                                            polymorphicHandler.buildFactForDummies(relation,
                                                polymophicRelations.stream().filter(rel -> rel.type.equals(Relation.Tipify.RELATION_POLYMOPHIC)).
                                                    collect(Collectors.toList()).get(0)));
                                    }
                                }
                                buildPolymRelationCount++;
                            }
                        }
                        continue;
                    }
                    /*
                     * その他ふつうのカラム
                     */
                    Relation relation = null;
                    if (column.getType().getSQLstring().equals("TINYINT")) {
                        relation = booleanColumnHandler
                            .build(atomSearchByName, tableNode.getFullName(), column.getName());
                    } else {
                        relation = columnHandler.buildRelation(atomSearchByName,
                            tableNode.getFullName(), column.getName());
                    }
                    // カラムの制約
                    Matcher matcher = pattern.matcher(column.getType().toString());
                    relation.isNotEmpty = matcher.find();
                    this.relations.add(relation);
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

        NamingRuleForAls ruleForAls = new NamingRuleForAls();

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
                if (atom.getExtended() != null) {
                    sigStrBuff.append(" extends ");
                    sigStrBuff.append(atom.getExtended().name);
                }
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

            strBuff.append("\n");
            strBuff.append("run {}\n");

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
