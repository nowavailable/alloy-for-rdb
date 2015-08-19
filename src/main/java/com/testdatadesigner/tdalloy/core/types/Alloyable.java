package com.testdatadesigner.tdalloy.core.types;

import java.io.File;
import java.io.IOException;
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
import com.testdatadesigner.tdalloy.core.type_bulder.PolymorphicHandler;
import com.testdatadesigner.tdalloy.core.type_bulder.RelationHandler;
import com.testdatadesigner.tdalloy.core.type_bulder.TableHandler;

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
            List<List<String>> inferenced = RulesForAlloyable.inferencedRelations(columnNames);
            List<String> inferencedPolymorphicSet = inferenced.get(0);
            List<String> inferencedForeignKeySet = inferenced.get(1);
            allInferencedPolymorphicSet.put(tableNode.getFullName(), inferencedPolymorphicSet);

            // ポリモーフィック
            if (!inferencedPolymorphicSet.isEmpty()) {
                this.isRailsOriented = Boolean.TRUE;
                for (String polymorphicStr : inferencedPolymorphicSet) {
                    // スキップ対象にadd
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
                    if (skipElementListForColumn.contains(tableNode.getFullName()
                            + INTERNAL_SEPARATOR + column.getName())) {
                        if (RulesForAlloyable.isInferencedPolymorphic(column.getName(),
                                allInferencedPolymorphicSet.get(tableNode.getFullName()))) {
							// as fields
                        	if (buildPolymRelationCount == 0) {
                            	for (String polymorphicStr : allInferencedPolymorphicSet.get(tableNode.getFullName())) {
                                	List<Relation> polymophicRelation = polymorphicHandler.buildRelation(atomSearchByName, polymorphicStr, tableNode.getFullName());
                                	this.relations.addAll(polymophicRelation);

                                	// as basic fact
                                    this.facts.add(polymorphicHandler.buildFactBase(polymophicRelation));
                            	}
                            	buildPolymRelationCount++;	
                        	}
                        	// as sig
                        	Atom polymAtom =
                                    columnHandler.buildAtomPolymorphicProspected(atomSearchByName,
                                            tableNode.getFullName(), column.getName());
                            polymAtom.originTypeName = column.getType().getTypeName();
                            this.atoms.add(polymAtom);
                        }

                        continue;
                    }

                    // Booleanフィールドはsigとしては扱わないのでスキップ
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

//    /**
//     * Alloyableインスタンスからalloy定義を生成。
//     * 
//     * @return String
//     * @throws IOException 
//     */
//    public String outputToAls() throws IOException {
//    	String als = null;
//    	File tempFile = File.createTempFile("tdalloyToAlsFromAlloyable", "als");
//    	tempFile.deleteOnExit();
//
//        Function<Atom, List<Relation>> atomSearchByRelationOwner = atom -> this.relations.stream()
//                .filter(rel -> rel.owner.equals(atom.getClass())).collect(Collectors.toList());
//
//        StringBuffer sigStrBuff = new StringBuffer();
//        for (Atom atom : this.atoms) {
//        	// sig にする。
//        	// TODO: ポリモーフィックなら、abstract sig が出てくる。
//        	sigStrBuff.append("sig ");
//        	sigStrBuff.append(atom.name);
//        	sigStrBuff.append(" { ");
//        	
//        	// それを参照しているRELATIONを探してfieldにする。
//        	List<Relation> relations = atomSearchByRelationOwner.apply(atom);
//        	List<String> fields = new ArrayList<String>();
//        	for (Relation relation : relations) {
//            	sigStrBuff.append(" ");
//            	sigStrBuff.append(relation.name);
//            	sigStrBuff.append(": ");
//            	// 型と限量子
//        		
//        	}
//        }
//
//        for (Fact fact : this.facts) {
//        	// fact
//        	
//        }
//        
//    	return als;
//    }

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
