package com.testdatadesigner.tdalloy.core.conversation;

import com.testdatadesigner.tdalloy.core.naming.IRulesForAlloyable;
import com.testdatadesigner.tdalloy.core.naming.RulesForAlloyableFactory;
import com.testdatadesigner.tdalloy.core.types.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PolymorphicRelationResolver {
  public ResolvePolymorphicCommand resolvePolymorphicCommand;
  IRulesForAlloyable namingRule = RulesForAlloyableFactory.getInstance().getRule();

  public Alloyable proc(ResolvePolymorphicCommand resolvePolymorphicCommand, Alloyable alloyable)
    throws IllegalParameterError{

    resolvePolymorphicCommand.targetPseudoAtoms.forEach((PseudoAtom pseudoAtom) -> {
      // rename予定のPseudoAtomを名前で検索
      Pattern polymorphicPattern = Pattern.compile(".+" + pseudoAtom.getName() + "$");
      PseudoAtom polymorphicAtom = (PseudoAtom) alloyable.getPseudoAtoms().stream().
        filter(atom -> {
          Matcher matcher = polymorphicPattern.matcher(atom.getName());
          return matcher.find();
        }).
        collect(Collectors.toList()).get(0);

      // 消込予定のPseudoAtomを名前で検索
      Pattern entityPattern = Pattern.compile("^" + pseudoAtom.getName());
      PseudoAtom dummyEntity = (PseudoAtom) alloyable.getPseudoAtoms().stream().
        filter(atom -> {
          Matcher matcher = entityPattern.matcher(atom.getName());
          return matcher.find();
        }).
        collect(Collectors.toList()).get(0);

      // 置換予定のAtom
      Atom entity = (Atom) pseudoAtom.shouldReplaceTo;

      /*
       * polymorphicAtom の内容を書き換え
       */
      IRelation polymorphicRelation =
        alloyable.relations.stream().
          filter((rel) -> (rel.getRefTo().equals(dummyEntity) && rel.getOwner().equals(polymorphicAtom))).
          collect(Collectors.toList()).get(0);
      try {
        // 参照先を正しいものに
        polymorphicRelation.setRefTo((IAtom) entity);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
        throw new IllegalParameterError(e.getMessage());
      }
      // 保持するrelation名を変更。
      polymorphicRelation.setName(namingRule.tableize(entity.getName()));
      // name を変更。
      Pattern replacePattern = Pattern.compile(dummyEntity.getName());
      Matcher replaceMatcher = replacePattern.matcher(polymorphicAtom.getName());
      polymorphicAtom.setName(replaceMatcher.replaceFirst(entity.getName()));

      /*
       * Factも。
       */
      Fact needFixFact = alloyable.facts.stream().
        filter((fact) -> (replacePattern.matcher(fact.value).find())).
        collect(Collectors.toList()).
        get(0);
      Matcher factMatcher1 = replacePattern.matcher(needFixFact.value);
      needFixFact.value = factMatcher1.replaceAll(entity.getName());
      Pattern factPattern = Pattern.compile(namingRule.tableize(dummyEntity.getName()));
      Matcher factMatcher2 = factPattern.matcher(needFixFact.value);
      needFixFact.value = factMatcher2.replaceAll(namingRule.tableize(entity.getName()));

      /*
       * dummyEntity の保持するrelationを付け替えのうえ、削除
       */
      IRelation relation =
        alloyable.relations.stream().
          filter((rel) -> (rel.getOwner().equals(dummyEntity))).
          collect(Collectors.toList()).get(0);
      try {
        relation.setOwner((IAtom) entity);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
        throw new IllegalParameterError(e.getMessage());
      }
      alloyable.atoms.remove(dummyEntity);
    });

    return alloyable;
  }
}
