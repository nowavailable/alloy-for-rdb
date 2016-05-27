package com.testdatadesigner.tdalloy.core.translater;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.google.common.base.Joiner;
import com.testdatadesigner.tdalloy.core.naming.IRulesForAlloyable;
import com.testdatadesigner.tdalloy.core.naming.RulesForAlloyableFactory;
import com.testdatadesigner.tdalloy.core.types.*;

public class RelationHandler {

  /**
   * テーブルリレーションを表現するオブジェクトを返す。
   * 
   * @param atomSearchByName
   * @param ownerTableName
   *          外部キー保持テーブル名
   * @param fKeyColumnStrs
   *          外部キーカラム名
   * @param refTableName
   *          参照される側テーブル名
   * @return List<Relation> 外部キー保持側Relation, 参照される側Relation、のペア。
   * @throws IllegalAccessException
   */
  public List<IRelation> build(Function<String, IAtom> atomSearchByName, String ownerTableName,
      List<String> fKeyColumnStrs, String refTableName) throws IllegalAccessException {

    IRulesForAlloyable namingRule = RulesForAlloyableFactory.getInstance().getRule();
    // 外部キー保持側
    IRelation relation = null;

    if (!refTableName.isEmpty()) {
      String refSigName = NamingRuleForAlloyable.tableAtomName(refTableName);
      IAtom refSig = atomSearchByName.apply(refSigName);
      relation = new TableRelation();
      relation.setOriginColumnNames(fKeyColumnStrs);
      relation.setName(namingRule.foreignKeyName(namingRule.fkeyFromTableName(refTableName), ownerTableName));
      relation.setOwner(atomSearchByName.apply(NamingRuleForAlloyable.tableAtomName(ownerTableName)));
      relation.setRefTo(
          refSig == null ? MissingAtomFactory.getInstance().getMissingAtom(refSigName, relation.getOwner()) : refSig);

      // 参照される側
      IRelation relationReversed = new TableRelationReferred();
      String ownerName = NamingRuleForAlloyable.tableAtomName(refTableName);
      IAtom owner = atomSearchByName.apply(ownerName);
      relationReversed.setOwner(owner == null ? MissingAtomFactory.getInstance().getMissingAtom(ownerName) : owner);
      relationReversed.setName(namingRule.foreignKeyNameReversed(refTableName, ownerTableName));
      relationReversed.setRefTo(atomSearchByName.apply(NamingRuleForAlloyable.tableAtomName(ownerTableName)));

      return Arrays.asList(relation, relationReversed);
    } else {
      if (fKeyColumnStrs.size() > 1) {
        throw new IllegalAccessException("複合外部キーなのは分かった。が、だったら、refTableName を引数に渡すこと。");
      }

      String refSigName = NamingRuleForAlloyable.tableAtomNameFromFKey(fKeyColumnStrs.get(0));
      IAtom refSig = atomSearchByName.apply(refSigName);
      relation = new TableRelation();
      relation.setOriginColumnNames(fKeyColumnStrs);
      relation.setName(namingRule.foreignKeyName(fKeyColumnStrs.get(0), ownerTableName));
      relation.setOwner(atomSearchByName.apply(NamingRuleForAlloyable.tableAtomName(ownerTableName)));
      relation.setRefTo(
          refSig == null ? MissingAtomFactory.getInstance().getMissingAtom(refSigName, relation.getOwner()) : refSig);

      // 参照される側
      IRelation relationReversed = new TableRelationReferred();
      String refTable = namingRule.tableNameFromFKey(fKeyColumnStrs.get(0));
      String ownerName = NamingRuleForAlloyable.tableAtomName(refTable);
      IAtom owner = atomSearchByName.apply(ownerName);
      relationReversed.setOwner(owner == null ? MissingAtomFactory.getInstance().getMissingAtom(ownerName) : owner);
      relationReversed.setName(namingRule.foreignKeyNameReversed(refTable, ownerTableName));
      relationReversed.setRefTo(atomSearchByName.apply(NamingRuleForAlloyable.tableAtomName(ownerTableName)));

      return Arrays.asList(relation, relationReversed);
    }
  }

  public Fact buildFact(List<IRelation> relations) {
    String leftStr = new String();
    String rightStr = new String();
    for (IRelation relation : relations) {
      if (relation.getOwner().getClass().equals(MissingAtom.class)
          || relation.getRefTo().getClass().equals(MissingAtom.class)) {
        continue;
      }
      if (relation.getClass().equals(TableRelation.class)) {
        IAtom owner = relation.getOwner();
        rightStr = owner.getName() + "<:" + relation.getName();
      } else if (relation.getClass().equals(TableRelationReferred.class)) {
        leftStr = relation.getOwner().getName() + "<:" + relation.getName();
      }
    }
    if (leftStr.isEmpty() && rightStr.isEmpty()) {
      return null;
    }

    Fact fact = new Fact(Fact.Tipify.RELATION);
    fact.value = leftStr + " = ~(" + rightStr + ")";
    fact.owners.addAll(relations);
    return fact;
  }

  public Fact buildMultiColumnFKeyFact(IRelation mainRelation, List<IRelation> relations,
      List<IRelation> refRelations) {
    Fact fact = new Fact(Fact.Tipify.ROWS_CONSTRAINT);
    StringBuilder builder = new StringBuilder();

    builder.append("all e:");
    builder.append(mainRelation.getOwner().getName());
    builder.append(" | ");
    List<String> fields = new ArrayList<String>();
    for (int i = 0; i < relations.size(); i++) {
      StringBuilder innerBuilder = new StringBuilder();
      innerBuilder.append("e.");
      innerBuilder.append(mainRelation.getName());
      innerBuilder.append(".");
      innerBuilder.append(refRelations.get(i).getName());
      if (refRelations.get(i).getRefTo().getName().equals(Property.TYPE_ON_ALS)) {
        innerBuilder.append(".val");
      }
      innerBuilder.append(" = ");
      innerBuilder.append("e.");
      innerBuilder.append(relations.get(i).getName());
      if (relations.get(i).getRefTo().getName().equals(Property.TYPE_ON_ALS)) {
        innerBuilder.append(".val");
      }
      fields.add(innerBuilder.toString());
    }
    builder.append(Joiner.on(" && ").join(fields));
    fact.value = builder.toString();
    return fact;
  }

  public Fact buildMultiColumnUniqueFact(IAtom ownerAtom, List<IRelation> relations) {
    Fact fact = new Fact(Fact.Tipify.ROWS_CONSTRAINT);
    StringBuilder builder = new StringBuilder();

    builder.append("all e,e':");
    builder.append(ownerAtom.getName());
    builder.append(" | ");
    builder.append("e != e' => ");

    List<String> fields = new ArrayList<String>();
    for (IRelation relation : relations) {
      StringBuilder innerBuilder = new StringBuilder();
      innerBuilder.append("e.");
      innerBuilder.append(relation.getName());
      fields.add(innerBuilder.toString());
    }
    builder.append("(");
    builder.append(Joiner.on("->").join(fields));
    builder.append(")");
    builder.append(" != ");
    fields = new ArrayList<String>();
    for (IRelation relation : relations) {
      StringBuilder innerBuilder = new StringBuilder();
      innerBuilder.append("e'.");
      innerBuilder.append(relation.getName());
      fields.add(innerBuilder.toString());
    }
    builder.append("(");
    builder.append(Joiner.on("->").join(fields));
    builder.append(")");

    fact.value = builder.toString();
    return fact;
  }
}
