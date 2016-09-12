package com.testdatadesigner.tdalloy.core.translater;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
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
import com.testdatadesigner.tdalloy.core.io.IOGateway;
import com.testdatadesigner.tdalloy.core.naming.IRulesForAlloyable;
import com.testdatadesigner.tdalloy.core.naming.RulesForAlloyableFactory;
import com.testdatadesigner.tdalloy.core.types.Alloyable;
import com.testdatadesigner.tdalloy.core.types.Fact;
import com.testdatadesigner.tdalloy.core.types.IAtom;
import com.testdatadesigner.tdalloy.core.types.IRelation;
import com.testdatadesigner.tdalloy.core.types.MissingAtom;
import com.testdatadesigner.tdalloy.core.types.MissingAtomFactory;
import com.testdatadesigner.tdalloy.core.types.NamingRuleForAlloyable;
import com.testdatadesigner.tdalloy.core.types.NamingRuleForAls;
import com.testdatadesigner.tdalloy.core.types.PolymorphicAbstract;
import com.testdatadesigner.tdalloy.core.types.Property;
import com.testdatadesigner.tdalloy.core.types.PseudoAtom;
import com.testdatadesigner.tdalloy.core.types.RelationPolymorphicTypeHolder;
import com.testdatadesigner.tdalloy.core.types.RelationProperty;
import com.testdatadesigner.tdalloy.core.types.TableRelation;
import com.testdatadesigner.tdalloy.core.types.TableRelationReferred;

public class AlloyableHandler {

  public Alloyable alloyable;
  private TableHandler tableHandler = new TableHandler();
  private RelationHandler relationHandler = new RelationHandler();
  private DefaultColumnHandler columnHandler = new DefaultColumnHandler();
  private BooleanColumnHandler booleanColumnHandler = new BooleanColumnHandler();
  private PolymorphicHandler polymorphicHandler = new PolymorphicHandler();
  private List<String> postponeListForColumn = new ArrayList<>();
  private HashMap<String, List<String>> allInferredPolymorphicSet = new HashMap<String, List<String>>();
  private Function<String, IAtom> atomSearchByName = name -> {
    List<IAtom> arr = this.alloyable.atoms.stream().filter(s -> s.getName().equals(name)).collect(Collectors.toList());
    return arr.isEmpty() ? null : arr.get(0);
  };
  private IRulesForAlloyable namingRule = RulesForAlloyableFactory.getInstance().getRule();
  static final String INTERNAL_SEPARATOR = "_#_";

  public AlloyableHandler(Alloyable alloyable) {
    this.alloyable = alloyable;
  }

  /**
   * テーブルの処理。 Constraintsに定義されている外部キーによる関連の処理 ポリモーフィック関連推論と （Constraints で定義されていない）外部キー推論。 カラムの処理。 という順。
   *
   * @param parsedDDLList,setWarning
   * @return Alloyable
   * @throws IllegalAccessException
   */
  public Alloyable buildFromDDL(List<CreateTableNode> parsedDDLList) // , Consumer<Serializable> setWarning
      throws IllegalAccessException {
    /*
     * テーブルの処理。
     */

    Map<String, List<ColumnDefinitionNode>> allColumns = new HashMap<String, List<ColumnDefinitionNode>>();
    BiConsumer<CreateTableNode, ColumnDefinitionNode> addToAllColumns = (tableNode, columnNode) -> {
      List<ColumnDefinitionNode> exist = allColumns.get(tableNode.getFullName());
      if (exist == null) {
        allColumns.put(tableNode.getFullName(), new ArrayList<ColumnDefinitionNode>() {{
          this.add(columnNode);
        }});
      } else {
        exist.add(columnNode);
      }
    };
    BiFunction<String, String, ColumnDefinitionNode> columnSearchByName = (tabName, colName) -> allColumns.get(tabName)
        .stream().filter(col -> col.getColumnName().equals(colName)).collect(Collectors.toList()).get(0);
    Map<String, List<ColumnDefinitionNode>> omitColumns = new HashMap<String, List<ColumnDefinitionNode>>();
    BiPredicate<String, ColumnDefinitionNode> isOmitted = (tabName, col) -> omitColumns.get(tabName) != null
        && omitColumns.get(tabName).contains(col);
    BiConsumer<String, ColumnDefinitionNode> omit = (tabName, col) -> {
      if (omitColumns.get(tabName) == null) {
        omitColumns.put(tabName, new ArrayList<ColumnDefinitionNode>() {
          {
            this.add(col);
          }
        });
      } else {
        omitColumns.get(tabName).add(col);
      }
    };
    Map<String, List<String>> compositeUniqueConstraints = new LinkedHashMap<>();
    /*
     * これはFactの生成に使うための変数。
     * 参照元テーブル名=>[カラム1,カラム2], 参照先テーブル名=>[カラム1の参照先カラム,カラム2の参照先カラム]
     * という2要素リストの、リスト。
     */
    List<List<Map<String, List<String>>>> compositeUniqueConstraintsByFKey = new ArrayList<>();

    Pattern isNotNullPattern = Pattern.compile(" NOT NULL");

    for (CreateTableNode tableNode : parsedDDLList) {
      this.alloyable.atoms.add(tableHandler.build(tableNode.getFullName()));

      for (TableElementNode tableElement : tableNode.getTableElementList()) {
        // 外部キーはあとで処理。
        if (tableElement.getClass().equals(FKConstraintDefinitionNode.class)) {
          FKConstraintDefinitionNode constraint = (FKConstraintDefinitionNode) tableElement;
          ResultColumnList refColumnList = constraint.getRefResultColumnList();
          ResultColumnList columnList = constraint.getColumnList();
          if (refColumnList.size() > 1) {
            /*
             * 複合外部キーは、テーブル名をkeyにしたMapに
             */
            List<String> columnNameList = new ArrayList<>();
            for (ResultColumn resultColumn : columnList) {
              columnNameList.add(resultColumn.getName());
            }
            List<String> refColumnNameList = new ArrayList<>();
            for (ResultColumn resultColumn : refColumnList) {
              refColumnNameList.add(resultColumn.getName());
            }
            compositeUniqueConstraintsByFKey.add(new ArrayList<Map<String, List<String>>>() {{
              // 参照元
              this.add(new LinkedHashMap<String, List<String>>() {{
                  this.put(tableNode.getFullName(), columnNameList);
                }});
              // 参照先
              this.add(new LinkedHashMap<String, List<String>>() {{
                  this.put(constraint.getRefTableName().toString(), refColumnNameList);
                }});
            }});
            // あと、columnNameList内の通常カラムは、allColumnsリストに追加
            List<String> notRels = columnNameList.stream().
              filter(colName -> namingRule.tableNameFromFKey(colName) == colName).
              collect(Collectors.toList());
            for (String colName : notRels) {
              for (TableElementNode node : tableNode.getTableElementList()) {
                if ((node instanceof ColumnDefinitionNode) && ((ColumnDefinitionNode) node).getName().equals(colName)) {
                  addToAllColumns.accept(tableNode, (ColumnDefinitionNode) node);
                  break;
                }
              }
            }
          } else {
            postpone(tableNode.getFullName(), ((ResultColumn) columnList.get(0)).getName());
          }
        } else if (tableElement.getClass().equals(ConstraintDefinitionNode.class)) {
          // プライマリキーはあとで処理
          ConstraintDefinitionNode constraint = (ConstraintDefinitionNode) tableElement;
          if (constraint.getConstraintType().equals(ConstraintType.PRIMARY_KEY)) {
            ResultColumnList columnList = constraint.getColumnList();
            // 複合主キーは、テーブル名をkeyにしたMapに
            if (columnList.size() > 1) {
              List<String> columnNameList = new ArrayList<>();
              for (ResultColumn resultColumn : columnList) {
                columnNameList.add(resultColumn.getName());
              }
              compositeUniqueConstraints.put(tableNode.getFullName(), columnNameList);
            } else {
              postpone(tableNode.getFullName(), ((ResultColumn) constraint.getColumnList().get(0)).getName());
            }
          } else if (constraint.getConstraintType().equals(ConstraintType.UNIQUE)) {
            ResultColumnList columnList = constraint.getColumnList();
            // （複合カラム）ユニーク制約は、テーブル名をkeyにしたMapに
            //if (columnList.size() > 1) {
            List<String> columnNameList = new ArrayList<>();
            for (ResultColumn resultColumn : columnList) {
              columnNameList.add(resultColumn.getName());
            }
            compositeUniqueConstraints.put(tableNode.getFullName(), columnNameList);
            //}
          }
        }
        // それ以外のelementをとりあえずぜんぶ保存
        else if (tableElement.getClass().equals(ColumnDefinitionNode.class)) {
          addToAllColumns.accept(tableNode, (ColumnDefinitionNode) tableElement);
        }
      }
    }

    /*
     * Constraintsに定義されている外部キーによる関連の処理
     */
    for (CreateTableNode tableNode : parsedDDLList) {
      for (TableElementNode tableElement : tableNode.getTableElementList()) {
        if (tableElement.getClass().equals(FKConstraintDefinitionNode.class)) {
          FKConstraintDefinitionNode constraint = (FKConstraintDefinitionNode) tableElement;
          List<String> fkeyColmnNames = new ArrayList<>();
          for (ResultColumn resultColumn : constraint.getColumnList()) {
            fkeyColmnNames.add(resultColumn.getName());
          }
          List<IRelation> relations = relationHandler.build(atomSearchByName, tableNode.getFullName(), fkeyColmnNames,
              constraint.getRefTableName().getFullTableName());
          // カラムの制約
          for (ResultColumn resultColumn : constraint.getColumnList()) {
            ColumnDefinitionNode column = columnSearchByName.apply(tableNode.getFullName(), resultColumn.getName());
            Matcher matcher = isNotNullPattern.matcher(column.getType().toString());
            relations.stream().filter(rel -> rel.getClass().equals(TableRelation.class)).collect(Collectors.toList())
                .get(0).setIsNotEmpty(matcher.find());
          }

          Fact relationFact = relationHandler.buildFact(relations);
          if (relationFact != null) {
            this.alloyable.addToFacts(relationFact);
          }

          this.alloyable.relations.addAll(relations);
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
          if (isOmitted.test(tableNode.getFullName(), column)) {
            continue;
          }
          columnNames.add(column.getName());
        }
      }
      List<List<String>> guessed = namingRule.guessedRelations(columnNames);
      List<String> guessedPolymorphicSet = guessed.get(0);
      List<String> guessedForeignKeySet = guessed.get(1);
      allInferredPolymorphicSet.put(tableNode.getFullName(), guessedPolymorphicSet);

      // ポリモーフィック
      if (!guessedPolymorphicSet.isEmpty()) {
        this.alloyable.isRailsOriented.equals(Boolean.TRUE);
        for (String polymorphicStr : guessedPolymorphicSet) {
          // あとで処理する
          postpone(tableNode.getFullName(), polymorphicStr + namingRule.polymorphicSuffix());
          // ※ポリモーフィック関連用の、xxx_id は、とりえあず使わない。
          // postpone(tableNode.getFullName(),
          // polymorphicStr + namingRule.foreignKeySuffix());

          omit.accept(tableNode.getFullName(), (ColumnDefinitionNode) columnSearchByName.apply(tableNode.getFullName(),
              polymorphicStr + namingRule.foreignKeySuffix()));
        }
      }

      /*
       * Constraintsに定義されていない外部キー
       */
      if (!guessedForeignKeySet.isEmpty()) {
        this.alloyable.isRailsOriented.equals(Boolean.TRUE);
        for (String keyStr : guessedForeignKeySet) {
          // あとで処理するぶんはスキップ
          if (postponeListForColumn.contains(tableNode.getFullName() + INTERNAL_SEPARATOR + keyStr)) {
            continue;
          }
          // ※解析失敗したら、単なる値カラムとして扱う。
          List<IRelation> relations = relationHandler.build(atomSearchByName, tableNode.getFullName(),
              Arrays.asList(keyStr), String.valueOf(""));
          // カラムの制約
          ColumnDefinitionNode column = columnSearchByName.apply(tableNode.getFullName(), keyStr);
          Matcher matcher = isNotNullPattern.matcher(column.getType().toString());
          List<IRelation> rels = relations.stream().filter(rel -> rel.getClass().equals(TableRelation.class))
              .collect(Collectors.toList());
          if (!rels.isEmpty()) {
            rels.get(0).setIsNotEmpty(matcher.find());
          }
          this.alloyable.relations.addAll(relations);

          List<IRelation> collects = relations.stream().filter(rel -> !rel.getClass().equals(RelationProperty.class))
              .collect(Collectors.toList());
          if (!collects.isEmpty()) {
            Fact relationFact = relationHandler.buildFact(relations.stream()
                .filter(rel -> !rel.getClass().equals(RelationProperty.class)).collect(Collectors.toList()));
            if (relationFact != null) {
              this.alloyable.addToFacts(relationFact);
            }
          }

          // あとでさらに処理する。
          postpone(tableNode.getFullName(), keyStr);
        }
      }
    }

    /*
     * 外部キーのisNotNullを、その参照先に反映させる。
     */
    for (IRelation relation : this.alloyable.relations) {
      if (relation.getClass().equals(TableRelation.class)) {
        this.alloyable.relations.stream().filter(rel -> rel.getClass().equals(TableRelationReferred.class))
            .filter(rel -> rel.getOwner().getName().equals(relation.getRefTo().getName())).collect(Collectors.toList())
            .forEach(rel -> rel.setIsNotEmpty(relation.getIsNotEmpty()));
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
          if (isOmitted.test(tableNode.getFullName(), column)) {
            continue;
          }
          /*
           * ポリモーフィック関連
           */
          if (postponeListForColumn.contains(tableNode.getFullName() + INTERNAL_SEPARATOR + column.getName())) {
            if (namingRule.isGuessedPolymorphic(column.getName(),
                allInferredPolymorphicSet.get(tableNode.getFullName()))) {
              // as sig
              PolymorphicAbstract polymAbstructAtom = columnHandler.buildAtomPolymorphicAbstract(atomSearchByName,
                  tableNode.getFullName(), column.getName());
              polymAbstructAtom.setOriginTypeName(column.getType().getTypeName());
              this.alloyable.atoms.add(polymAbstructAtom);
              // as fields
              if (buildPolymRelationCount == 0) {
                for (String polymorphicStr : allInferredPolymorphicSet.get(tableNode.getFullName())) {
                  Boolean isNotEmptyPolymorphicColumn = false;
                  List<IRelation> polymophicRelations = polymorphicHandler.buildRelation(atomSearchByName,
                      polymorphicStr, tableNode.getFullName(), polymAbstructAtom);
                  for (IRelation relation : polymophicRelations) {
                    if (relation.getClass().equals(RelationPolymorphicTypeHolder.class)) {
                      // カラムの制約
                      ColumnDefinitionNode c = columnSearchByName.apply(tableNode.getFullName(),
                          polymorphicStr + namingRule.polymorphicSuffix());
                      Matcher matcher = isNotNullPattern.matcher(c.getType().toString());
                      isNotEmptyPolymorphicColumn = matcher.find();
                      relation.setIsNotEmpty(isNotEmptyPolymorphicColumn);
                    }
                    // else if (relation.getClass().equals(RelationPolymorphicTypeBundler.class)) {
                    // relation.setIsNotEmpty(true);
                    // }
                  }
                  this.alloyable.relations.addAll(polymophicRelations);

                  // as basic fact
                  this.alloyable.addToFacts(polymorphicHandler.buildFactBase(polymophicRelations));

                  // as sig by referrer and their fields
                  List<IAtom> dummies = polymorphicHandler.buildDummies(dummySigCount);
                  this.alloyable.atoms.addAll(dummies);

                  dummySigCount = dummySigCount + dummies.size();

                  // their dummy columns
                  for (IAtom dummyAtom : dummies) {
                    IRelation relation = polymorphicHandler.buildRelationForDummy(atomSearchByName,
                        dummyAtom.getOriginPropertyName(),
                        namingRule.fkeyFromTableName(polymAbstructAtom.getParent().getOriginPropertyName()),
                        polymAbstructAtom.getParent().getOriginPropertyName());
                    // カラムの制約
                    relation.setIsNotEmpty(isNotEmptyPolymorphicColumn);
                    this.alloyable.relations.add(relation);
                    // extend sig
                    IAtom polymImplAtom = polymorphicHandler.buildDummyExtend(polymorphicStr, dummyAtom,
                        polymAbstructAtom);
                    this.alloyable.atoms.add(polymImplAtom);
                    // and their field
                    IRelation polymRelation = polymorphicHandler.buildTypifiedRelation(polymImplAtom, dummyAtom);
                    polymRelation.setIsNotEmpty(isNotEmptyPolymorphicColumn);
                    this.alloyable.relations.add(polymRelation);
                    // and fact
                    this.alloyable.addToFacts(polymorphicHandler.buildFactForDummies(relation,
                        polymophicRelations.stream()
                            .filter(rel -> rel.getClass().equals(RelationPolymorphicTypeHolder.class))
                            .collect(Collectors.toList()).get(0)));
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
          IRelation relation = null;
          if (column.getType().getSQLstring().equals("TINYINT")) {
            relation = booleanColumnHandler.build(atomSearchByName, tableNode.getFullName(), column.getName());
          } else {
            relation = columnHandler.buildRelation(atomSearchByName, tableNode.getFullName(), column.getName());
          }
          // カラムの制約
          Matcher matcher = isNotNullPattern.matcher(column.getType().toString());
          relation.setIsNotEmpty(matcher.find());

          if (!isOmitted.test(tableNode.getFullName(), column)) {
            this.alloyable.relations.add(relation);
          }
        }
      }
    }

    /*
     * 複合カラムユニークインデックスのためのfactを生成
     */
    for (String tableName : compositeUniqueConstraints.keySet()) {
      String tableSigName = NamingRuleForAlloyable.tableAtomName(tableName);
      IAtom ownerAtom = atomSearchByName.apply(NamingRuleForAlloyable.tableAtomName(tableSigName));
      List<String> list = compositeUniqueConstraints.get(tableName);
      List<IRelation> relations = new ArrayList<>();
      for (String colName : list) {
        relations.add(
          this.alloyable.relations.stream().filter(rel -> rel.getOwner().equals(ownerAtom)
              && rel.getOriginColumnNames().equals(Arrays.asList(colName))).
          collect(Collectors.toList()).get(0)
        );
      }
      Fact multiColumnUniqueFact = relationHandler.buildMultiColumnUniqueFact(ownerAtom, relations);
      this.alloyable.facts.add(multiColumnUniqueFact);
    }
    /*
     * 複合外部キーのためのfactを生成
     */
    for (List<Map<String, List<String>>> pair : compositeUniqueConstraintsByFKey ) {
      String ownerTableName = pair.get(0).keySet().toArray()[0].toString();
      String refTableName = pair.get(1).keySet().toArray()[0].toString();
      IAtom ownerAtom = atomSearchByName.apply(NamingRuleForAlloyable.tableAtomName(ownerTableName));
      String relName = namingRule.foreignKeyName(namingRule.fkeyFromTableName(refTableName), ownerTableName);
      IRelation mainRelation = 
          this.alloyable.relations.stream().filter(rel -> rel.getOwner().equals(ownerAtom) 
              && rel.getName().equals(relName)).
          collect(Collectors.toList()).get(0);
      List<IRelation> relations = new ArrayList<>();
      for (String column : pair.get(0).get(ownerTableName)) {
        relations.add(
          this.alloyable.relations.stream().filter(rel -> rel.getOwner().equals(ownerAtom) 
              && rel.getOriginColumnNames().equals(Arrays.asList(column))).
          collect(Collectors.toList()).get(0)
        );
      }
      List<IRelation> refRelations = new ArrayList<>();
      for (String column : pair.get(1).get(refTableName)) {
        refRelations.add(
          this.alloyable.relations.stream().filter(rel -> rel.getOwner().equals(mainRelation.getRefTo()) 
              && rel.getOriginColumnNames().equals(Arrays.asList(column))).
          collect(Collectors.toList()).get(0)
        );
      }
      Fact multiColumnFKeyFact = 
          relationHandler.buildMultiColumnFKeyFact(mainRelation, relations, refRelations);
      this.alloyable.facts.add(multiColumnFKeyFact);
    }

    this.alloyable.missingAtoms = MissingAtomFactory.getInstance().getMissingAtoms();

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
  public BufferedReader outputToAls() throws IOException {
    File tempFile = File.createTempFile("tdalloyToAlsFromAlloyable", "als");
    tempFile.deleteOnExit();

    NamingRuleForAls ruleForAls = new NamingRuleForAls();

    Function<IAtom, List<IRelation>> atomSearchByRelationOwner = atom -> this.alloyable.relations.stream()
        .filter(rel -> rel.getOwner().getName().equals(atom.getName())).collect(Collectors.toList());

    String indent = "  ";
    try (BufferedWriter writer = IOGateway.getTempFileWriter(tempFile)) {
      StringBuffer strBuff = new StringBuffer();

      strBuff.append("open util/boolean\n");
      strBuff.append("sig " + Property.TYPE_ON_ALS + " { val: one Int }\n"); // FIXME: 仮実装
      strBuff.append("\n");
      writer.write(strBuff.toString());
      strBuff.setLength(0);

      for (IAtom atom : this.alloyable.atoms) {
        StringBuffer sigStrBuff = new StringBuffer();
        /*
         * sig にする。
         */
        String sigStr = atom.getClass().equals(PolymorphicAbstract.class) ? "abstract sig " : "sig ";
        sigStrBuff.append(sigStr);
        sigStrBuff.append(atom.getName());
        if (atom.getClass().equals(PseudoAtom.class) && ((PseudoAtom) atom).getExtended() != null) {
          sigStrBuff.append(" extends ");
          sigStrBuff.append(((PseudoAtom) atom).getExtended().getName());
        }
        sigStrBuff.append(" {");
        sigStrBuff.append("\n");
        /*
         * それを参照しているRELATIONを探してfieldにする。
         * 重複は避ける。
         */
        List<IRelation> relations = atomSearchByRelationOwner.apply(atom);
        List<IRelation> outputs = new ArrayList<>();
        List<String> fields = new ArrayList<String>();
        for (IRelation relation : relations) {
          List<IRelation> exists = 
              outputs.stream().filter(rel -> rel.getRefTo().equals(relation.getRefTo()) && rel.getOwner().equals(relation.getOwner())).
              collect(Collectors.toList());
          if (!exists.isEmpty()) {
            continue;
          }
          IAtom refTo = relation.getRefTo();
          fields.add(relation.getName() + ": " + ruleForAls.searchQuantifierMap(relation, this.alloyable.relations)
              + " " + (refTo.getClass().equals(MissingAtom.class) ? Property.TYPE_ON_ALS : refTo.getName()));
          outputs.add(relation);
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
    return IOGateway.getTempFileReader(tempFile);
  }

}
