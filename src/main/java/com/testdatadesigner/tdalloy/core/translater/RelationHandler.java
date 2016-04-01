package com.testdatadesigner.tdalloy.core.translater;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.google.common.base.Joiner;
import com.testdatadesigner.tdalloy.core.naming.IRulesForAlloyable;
import com.testdatadesigner.tdalloy.core.naming.RulesForAlloyableFactory;
import com.testdatadesigner.tdalloy.core.types.Fact;
import com.testdatadesigner.tdalloy.core.types.IAtom;
import com.testdatadesigner.tdalloy.core.types.IRelation;
import com.testdatadesigner.tdalloy.core.types.MissingAtom;
import com.testdatadesigner.tdalloy.core.types.MissingAtomFactory;
import com.testdatadesigner.tdalloy.core.types.NamingRuleForAlloyable;
import com.testdatadesigner.tdalloy.core.types.TableRelation;
import com.testdatadesigner.tdalloy.core.types.TableRelationReferred;

public class RelationHandler {

    /**
     * テーブルリレーションを表現するオブジェクトを返す。
     * 
     * @param atomSearchByName
     * @param ownerTableName 外部キー保持テーブル名
     * @param fKeyColumnStrs 外部キーカラム名
     * @param refTableName 参照される側テーブル名
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
            relation.setName(namingRule.foreignKeyName(namingRule.fkeyFromTableName(refTableName), ownerTableName));;
            relation.setOwner(atomSearchByName.apply(NamingRuleForAlloyable.tableAtomName(ownerTableName)));
            relation.setRefTo(refSig == null ? MissingAtomFactory.getInstance().getMissingAtom(refSigName, relation.getOwner()) : refSig);

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
            relation.setRefTo(refSig == null ? MissingAtomFactory.getInstance().getMissingAtom(refSigName, relation.getOwner()) : refSig);

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
            if (relation.getOwner().getClass().equals(MissingAtom.class) ||
            		relation.getRefTo().getClass().equals(MissingAtom.class)) {
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
    
    /**
     * @param tableSigName
     * @param colNames
     * @param relName 複合外部キーがあるなら、その参照先テーブル由来のrelation名が渡されること。
     * @return
     */
    public Fact buildMultiColumnUniqueFact(String tableSigName, List<String> colNames, String relName) {
        IRulesForAlloyable namingRule = RulesForAlloyableFactory.getInstance().getRule();
        List<String> alloyFieldNames = new ArrayList<>(); 
        for (String colName : colNames) {
			alloyFieldNames.add(namingRule.singularize(namingRule.tableNameFromFKey(colName)));
		}
        Fact fact = new Fact(Fact.Tipify.ROWS_CONSTRAINT);
        StringBuilder builder = new StringBuilder();

        builder.append("all e,e':");
        builder.append(tableSigName);
        builder.append(" | ");
        builder.append("e != e' => ");

        List<String> fields_left = new ArrayList<String>();
        List<String> fields_right = new ArrayList<String>();
        String previous_field = null;
        for (String fieldName : alloyFieldNames) {
        	if (previous_field == null) {
        		previous_field = fieldName;
        		continue;
        	}
        	Object refSigNameAsField = relName.isEmpty() ? "" : relName + ".";
        	fields_left.add("e." + refSigNameAsField + previous_field + " -> " + "e." + refSigNameAsField + fieldName);
        	fields_right.add("e'." + refSigNameAsField + previous_field + " -> " + "e'." + refSigNameAsField + fieldName);
		}
        List<String> fields = new ArrayList<String>();
		for (int i = 0; i < fields_left.size(); i++) {
			fields.add(
			    "(" + fields_left.get(i) + " != " + fields_right.get(i) + ")"
			);
		}
        builder.append(Joiner.on(" && ").join(fields));
        fact.value =  builder.toString();
        //fact.owners.addAll(relations);
        return fact;
    }
}
