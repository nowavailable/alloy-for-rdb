package com.testdatadesigner.tdalloy.core.types;

import com.testdatadesigner.tdalloy.core.naming.IRulesForAlloyable;
import com.testdatadesigner.tdalloy.core.naming.RulesForAlloyableFactory;
import com.testdatadesigner.tdalloy.util.Inflector;

public class NamingRuleForAlloyable {

    public static String tableAtomName(String originalTableName) {
        Inflector inflector = Inflector.getInstance();
        return inflector.upperCamelCase(inflector.singularize(originalTableName));
    }

    public static String tableAtomNameFromFKey(String originalColumnName) throws IllegalAccessException {
        Inflector inflector = Inflector.getInstance();
        IRulesForAlloyable namingRule = RulesForAlloyableFactory.getInstance().getRule();
        return inflector.upperCamelCase(inflector
                .singularize(namingRule.tableNameFromFKey(originalColumnName)));
    }

    public static String columnAtomName(String originalColumnName, String originalTableName) {
        Inflector inflector = Inflector.getInstance();
        return inflector.upperCamelCase(originalColumnName);
    }

    public static String columnFieldName(String originalColumnName, String originalTableName) {
        return originalColumnName;
    }

    public static String columnRelationName(String originalColumnName, String originalTableName) {
        Inflector inflector = Inflector.getInstance();
        return inflector.lowerCamelCase(originalColumnName);
    }

    public static String polymorphicAbstractAtomName(String polymorphicColumnStr, String originalTableName) {
        Inflector inflector = Inflector.getInstance();
        return tableAtomName(originalTableName) + '_' + inflector.upperCamelCase(polymorphicColumnStr);
    }

    public static String polymorphicImplAtomName(String polymorphicStr, String refToAtomName) {
        Inflector inflector = Inflector.getInstance();
        return inflector.upperCamelCase(polymorphicStr) + refToAtomName;
    }

    public static String implementedPolymorphicAtomName(String keystr, String ownerTableName) {
        return tableAtomName(keystr) + tableAtomName(ownerTableName);
    }
}
