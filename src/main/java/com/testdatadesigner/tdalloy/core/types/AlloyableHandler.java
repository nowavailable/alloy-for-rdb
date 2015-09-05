package com.testdatadesigner.tdalloy.core.types;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.foundationdb.sql.parser.ColumnDefinitionNode;
import com.foundationdb.sql.parser.ConstraintDefinitionNode;
import com.foundationdb.sql.parser.CreateTableNode;
import com.foundationdb.sql.parser.FKConstraintDefinitionNode;
import com.foundationdb.sql.parser.ResultColumn;
import com.foundationdb.sql.parser.ResultColumnList;
import com.foundationdb.sql.parser.TableElementNode;
import com.foundationdb.sql.parser.ConstraintDefinitionNode.ConstraintType;
import com.google.common.base.Joiner;
import com.testdatadesigner.tdalloy.core.naming.IRulesForAlloyable;
import com.testdatadesigner.tdalloy.core.naming.RulesForAlloyableFactory;
import com.testdatadesigner.tdalloy.core.type_bulder.BooleanColumnHandler;
import com.testdatadesigner.tdalloy.core.type_bulder.DefaultColumnHandler;
import com.testdatadesigner.tdalloy.core.type_bulder.PolymorphicHandler;
import com.testdatadesigner.tdalloy.core.type_bulder.RelationHandler;
import com.testdatadesigner.tdalloy.core.type_bulder.TableHandler;

public class AlloyableHandler {

	public Alloyable alloyable;
    private TableHandler tableHandler = new TableHandler();
    private RelationHandler relationHandler = new RelationHandler();
    private DefaultColumnHandler columnHandler = new DefaultColumnHandler();
    private BooleanColumnHandler booleanColumnHandler = new BooleanColumnHandler();
    private PolymorphicHandler polymorphicHandler = new PolymorphicHandler();
    private List<String> postponeListForColumn = new ArrayList<>();
    private HashMap<String, List<String>> allInferencedPolymorphicSet = new HashMap<String, List<String>>();
	private Function<String, Atom> atomSearchByName = name -> {
		List<Atom> arr = this.alloyable.atoms.stream().filter(s -> s.name.equals(name))
				.collect(Collectors.toList());
		return arr.isEmpty() ? null : arr.get(0);
	};
    private IRulesForAlloyable namingRule = RulesForAlloyableFactory.getInstance().getRule();
    static final String INTERNAL_SEPARATOR = "_#_";

    public AlloyableHandler(Alloyable alloyable) {
		this.alloyable = alloyable;
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

            this.alloyable.atoms.add(tableHandler.build(tableNode.getFullName()));

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
                        this.alloyable.relations.addAll(relations);

                        this.alloyable.facts.add(relationHandler.buildFact(relations));
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
                this.alloyable.isRailsOriented = Boolean.TRUE;
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
                this.alloyable.isRailsOriented = Boolean.TRUE;
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
                    this.alloyable.relations.addAll(relations);

                    List<Relation> collects = relations.stream().filter(rel -> !rel.type.equals(Relation.Typify.VALUE)).collect(Collectors.toList());
                    if (!collects.isEmpty()) {
                    	this.alloyable.facts.add(relationHandler.buildFact(relations.stream().filter(rel -> !rel.type.equals(Relation.Typify.VALUE)).collect(Collectors.toList())));
                    }
                    
                    // あとでさらに処理する。
                    postpone(tableNode.getFullName(), keyStr);
                }
            }
        }

        /*
         * 外部キーのisNotNullを、その参照先に反映させる。
         */
        for (Relation relation : this.alloyable.relations) {
            if (relation.type.equals(Relation.Typify.RELATION)) {
                this.alloyable.relations.stream()
                    .filter(rel -> rel.type.equals(Relation.Typify.RELATION_REFERRED))
                    .filter(rel -> AlloyableHandler.getOwner(rel).name.equals(AlloyableHandler.getRefTo(relation).name))
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
                            this.alloyable.atoms.add(polymAbstructAtom);
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
                                    this.alloyable.relations.addAll(polymophicRelations);

                                    // as basic fact
                                    this.alloyable.facts.add(polymorphicHandler.buildFactBase(polymophicRelations));

                                    // as sig by referrer and their fields
                                    List<Atom> dummies = polymorphicHandler.buildDummies(dummySigCount);
                                    this.alloyable.atoms.addAll(dummies);

                                    dummySigCount = dummySigCount + dummies.size();

                                    // their dummy columns
                                    for (Atom dummyAtom : dummies) {
                                        Relation relation =
                                            polymorphicHandler.buildRelationForDummy(atomSearchByName, dummyAtom.originPropertyName,
                                                namingRule.fkeyFromTableName(polymAbstructAtom.getParent().originPropertyName),
                                                polymAbstructAtom.getParent().originPropertyName);
                                        // カラムの制約
                                        relation.isNotEmpty = isNotEmptyPolymorphicColumn;
                                        this.alloyable.relations.add(relation);
                                        // extend sig
                                        Atom polymImplAtom = polymorphicHandler.buildDummyExtend(polymorphicStr, dummyAtom, polymAbstructAtom);
                                        this.alloyable.atoms.add(polymImplAtom);
                                        // and their field
                                        Relation polymRelation = polymorphicHandler.buildTypifiedRelation(polymImplAtom, dummyAtom);
                                        polymRelation.isNotEmpty = isNotEmptyPolymorphicColumn;
                                        this.alloyable.relations.add(polymRelation);
                                        // and fact
                                        this.alloyable.facts.add(
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
                        this.alloyable.relations.add(relation);	
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
						return this.alloyable.relations.stream()
								.filter(rel -> rel.originColumnName != null && AlloyableHandler.getOwner(rel) != null
										&& rel.originColumnName.equals(s)
										&& AlloyableHandler.getOwner(rel).name.equals(tableSigName))
								.collect(Collectors.toList()).get(0);
					}).collect(Collectors.toList());
			Fact multiColumnUniqueFact = relationHandler.buildMultiColumnUniqueFact(tableSigName, relations, uniqueIdxCounter);
			this.alloyable.facts.add(multiColumnUniqueFact);
        	uniqueIdxCounter ++;
        }
        return this.alloyable;
    }

    private void postpone(String tableName, String keyStr) {
        postponeListForColumn.add(tableName + INTERNAL_SEPARATOR + keyStr);
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

        Function<Atom, List<Relation>> atomSearchByRelationOwner = atom -> this.alloyable.relations.stream()
            .filter(rel -> AlloyableHandler.getOwner(rel).name.equals(atom.name)).collect(Collectors.toList());

        String indent = "  ";
        try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8"))){
            StringBuffer strBuff = new StringBuffer();

            strBuff.append("open util/boolean\n");
            strBuff.append("sig Boundary { val: one Int }\n");  // FIXME: 仮実装
            strBuff.append("\n");
            writer.write(strBuff.toString());
            strBuff.setLength(0);

            for (Atom atom : this.alloyable.atoms) {
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
                	Atom refTo = AlloyableHandler.getRefTo(relation);
                    fields.add(relation.name + ": " + ruleForAls.searchQuantifierMap(relation, this.alloyable.relations) + " " + refTo.name);
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
            for (Fact fact : this.alloyable.facts) {
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

    public static Atom getOwner(Relation relation) {
    	Atom owner;
    	try {
    		owner = relation.getOwner();
		} catch (IllegalAccessError e) {
			// 
			
			owner = new Atom();
		}
		return owner;
	}

	public static Atom getRefTo(Relation relation) {
    	Atom refTo;
    	try {
    		refTo = relation.getRefTo();
		} catch (IllegalAccessError e) {
			// 
			
			refTo = new Atom();
		}
		return refTo;
	}
}