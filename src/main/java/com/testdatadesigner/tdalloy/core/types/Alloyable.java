package com.testdatadesigner.tdalloy.core.types;

import com.foundationdb.sql.parser.*;
import com.foundationdb.sql.parser.ConstraintDefinitionNode.ConstraintType;
import com.google.common.base.Joiner;
import com.testdatadesigner.tdalloy.core.naming.IRulesForAlloyable;
import com.testdatadesigner.tdalloy.core.naming.RulesForAlloyableFactory;
import com.testdatadesigner.tdalloy.core.type_bulder.*;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.DirectoryStream.Filter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

	private Function<String, Atom> atomSearchByName = name -> {
		List<Atom> arr = this.atoms.stream().filter(s -> s.name.equals(name))
				.collect(Collectors.toList());
		return arr.isEmpty() ? null : arr.get(0);
	};

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
        List<ColumnDefinitionNode> omitColumns = new ArrayList<>();
        Function<String, ColumnDefinitionNode> columnSearchByName = name -> allColumns.stream().
            filter(col -> col.getName().equals(name)).collect(Collectors.toList()).get(0);
        Map<String, List<String>> uniqueConstraints = new LinkedHashMap<>();
        Pattern isNotNullPattern = Pattern.compile(" NOT NULL");

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
                else if (tableElement.getClass().equals(ConstraintDefinitionNode.class)) {
                    // プライマリキーはあとで処理
                    ConstraintDefinitionNode constraint = (ConstraintDefinitionNode) tableElement;
                    if (constraint.getConstraintType().equals(ConstraintType.PRIMARY_KEY)) {
                        postpone(tableNode.getFullName(),
                            ((ResultColumn) constraint.getColumnList().get(0)).getName());
                    // （複合カラム）ユニーク制約はテーブル名をkeyにしたMapに
                    } else if (constraint.getConstraintType().equals(ConstraintType.UNIQUE)) {
                    	ResultColumnList columnList = constraint.getColumnList();
                    	if (columnList.size() > 1) {
                    		List<String> columnNameList = new ArrayList<>();
                    		for (ResultColumn resultColumn : columnList) {
                    			columnNameList.add(resultColumn.getName());
							}
							uniqueConstraints.put(tableNode.getFullName(), columnNameList);
						}
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
                        Matcher matcher = isNotNullPattern.matcher(column.getType().toString());
                        relations.stream().
                            filter(rel -> rel.type.equals(Relation.Typify.RELATION)).collect(Collectors.toList()).
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
                            polymorphicStr + namingRule.polymorphicSuffix());
                    // ※ポリモーフィック関連用の、xxx_id は、とりえあず使わない。
                    //postpone(tableNode.getFullName(),
                    //    polymorphicStr + namingRule.foreignKeySuffix());
                    omitColumns.add(
                        (ColumnDefinitionNode) columnSearchByName.apply(polymorphicStr + namingRule.foreignKeySuffix()));
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
                    // ※解析失敗したら、単なる値カラムとして扱う。
                    List<Relation> relations =
                        relationHandler.build(atomSearchByName, tableNode.getFullName(), keyStr,
                            String.valueOf(""));
                    // カラムの制約
                    ColumnDefinitionNode column = columnSearchByName.apply(keyStr);
                    Matcher matcher = isNotNullPattern.matcher(column.getType().toString());
                    List<Relation> rels = relations.stream().
                        filter(rel -> rel.type.equals(Relation.Typify.RELATION)).collect(Collectors.toList());
                    if (!rels.isEmpty()) {
                    	rels.get(0).isNotEmpty = matcher.find();
                    }
                    this.relations.addAll(relations);

                    List<Relation> collects = relations.stream().filter(rel -> !rel.type.equals(Relation.Typify.VALUE)).collect(Collectors.toList());
                    if (!collects.isEmpty()) {
                    	this.facts.add(relationHandler.buildFact(relations.stream().filter(rel -> !rel.type.equals(Relation.Typify.VALUE)).collect(Collectors.toList())));
                    }
                    
                    // あとでさらに処理する。
                    postpone(tableNode.getFullName(), keyStr);
                }
            }
        }

        /*
         * 外部キーのisNotNullを、その参照先に反映させる。
         */
        for (Relation relation : this.relations) {
            if (relation.type.equals(Relation.Typify.RELATION)) {
                this.relations.stream()
                    .filter(rel -> rel.type.equals(Relation.Typify.RELATION_REFERRED))
                    .filter(rel -> rel.getOwner().name.equals(relation.getRefTo().name))
                    .collect(Collectors.toList())
                    .forEach(rel ->rel.isNotEmpty = relation.isNotEmpty);
            }
        }

        /*
         * カラムの処理（含 ポリモーフィックの、typeのほうの、sig化）。
         */
        int dummySigCount = 0;
        for (CreateTableNode tableNode : parsedDDLList) {
            // for polymorphic relations
            int buildPolymRelationCount = 0;

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
                                      Boolean isNotEmptyPolymorphicColumn = false;
                                    List<Relation> polymophicRelations =
                                        polymorphicHandler.buildRelation(atomSearchByName, polymorphicStr, tableNode.getFullName(), polymAbstructAtom);
                                    for (Relation relation : polymophicRelations) {
                                        if (relation.type.equals(Relation.Typify.RELATION_POLYMORPHIC)) {
                                            // カラムの制約
                                            ColumnDefinitionNode c = columnSearchByName.apply(polymorphicStr + namingRule.polymorphicSuffix());
                                            Matcher matcher = isNotNullPattern.matcher(c.getType().toString());
                                            isNotEmptyPolymorphicColumn = matcher.find();
                                            relation.isNotEmpty = isNotEmptyPolymorphicColumn;
                                        } else if (relation.type.equals(Relation.Typify.ABSTRACT_RELATION)) {
                                            relation.isNotEmpty = true;
                                        }
                                    }
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
                                        // カラムの制約
                                        relation.isNotEmpty = isNotEmptyPolymorphicColumn;
                                        this.relations.add(relation);
                                        // extend sig
                                        Atom polymImplAtom = polymorphicHandler.buildDummyExtend(polymorphicStr, dummyAtom, polymAbstructAtom);
                                        this.atoms.add(polymImplAtom);
                                        // and their field
                                        Relation polymRelation = polymorphicHandler.buildTypifiedRelation(polymImplAtom, dummyAtom);
                                        polymRelation.isNotEmpty = isNotEmptyPolymorphicColumn;
                                        this.relations.add(polymRelation);
                                        // and fact
                                        this.facts.add(
                                            polymorphicHandler.buildFactForDummies(relation,
                                                polymophicRelations.stream().filter(rel -> rel.type.equals(
                                                    Relation.Typify.RELATION_POLYMORPHIC)).
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
                    Matcher matcher = isNotNullPattern.matcher(column.getType().toString());
                    relation.isNotEmpty = matcher.find();

                    if (!omitColumns.contains(column)) {
                        this.relations.add(relation);	
                    }
                }
            }
        }

        /*
         * 複合カラムユニークインデックスのためのfactを生成
         */
        int uniqueIdxCounter = 0;
        for (String tableName : uniqueConstraints.keySet()) {
        	String tableSigName = NamingRuleForAlloyable.tableAtomName(tableName);
        	List<String> list = uniqueConstraints.get(tableName);
			List<Relation> relations = list
					.stream()
					.map(s -> {
						return this.relations.stream()
								.filter(rel -> rel.originColumnName != null && rel.getOwner() != null
										&& rel.originColumnName.equals(s)
										&& rel.getOwner().name.equals(tableSigName))
								.collect(Collectors.toList()).get(0);
					}).collect(Collectors.toList());
			Fact multiColumnUniqueFact = relationHandler.buildMultiColumnUniqueFact(tableSigName, relations, uniqueIdxCounter);
			this.facts.add(multiColumnUniqueFact);
        	uniqueIdxCounter ++;
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
            .filter(rel -> rel.getOwner().name.equals(atom.name)).collect(Collectors.toList());

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
                    fields.add(relation.name + ": " + ruleForAls.searchQuantifierMap(relation, this.relations) + " " + relation.getRefTo().name);
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
                StringBuilder builder = new StringBuilder();
                builder.append(indent);
                builder.append(fact.value);
                builder.append("\n");
                writer.write(builder.toString());
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
