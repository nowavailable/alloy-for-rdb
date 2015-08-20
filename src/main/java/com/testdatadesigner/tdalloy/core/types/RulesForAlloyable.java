package com.testdatadesigner.tdalloy.core.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.testdatadesigner.tdalloy.util.Inflector;

public class RulesForAlloyable {

    public static final String FOREIGN_KEY_SUFFIX = "_id";
    public static final String POLYMORPHIC_SUFFIX = "_type";
    public static final String COUPLER = "_";
    static Pattern foreignKeyPattern = Pattern.compile("^(.+)(" + FOREIGN_KEY_SUFFIX + ")$");
    static Pattern patternsForPolymorphic = Pattern.compile("^(.+)(" + POLYMORPHIC_SUFFIX + ")$");
    public static final String COLUMN_ATOM_PREFIX = "";

    /**
     * カラム名から、外部キーと、ポリモーフィック関連用カラム群を推測する。
     * @param columnNames
     * @return 1番目がポリモーフィック関連用カラム群、2番目が外部キーカラム群
     */
    public static List<List<String>> inferencedRelations(List<String> columnNames) {
        final List<String> matchedPolymorphic = new ArrayList<>();
        List<String> candidate = new ArrayList<>();
        for (String columnName : columnNames) {
            Matcher idMatcher = patternsForPolymorphic.matcher(columnName);
            if (idMatcher.find()) {
                candidate.add(idMatcher.group(1));
            }
        }
        for (String columnName : columnNames) {
            for (String keyStr : candidate) {
                Pattern pattern = Pattern.compile("^(" + keyStr + ")(_id)$");
                Matcher matcher = pattern.matcher(columnName);
                if (matcher.find()) {
                    matchedPolymorphic.add(keyStr);
                }
            }
        }

        final List<String> matchedForeignKey = new ArrayList<>();
        for (String columnName : columnNames) {
            Matcher matcher = foreignKeyPattern.matcher(columnName);
            if (matcher.find()) {
                if (matchedPolymorphic.contains(matcher.group(1))) {
                    continue;
                }
                matchedForeignKey.add(columnName);
            }
        }
        return Arrays.asList(matchedPolymorphic, matchedForeignKey);
    }
    
    public static Boolean isInferencedPolymorphic(String originalColumnName, List<String> list) {
        return list.stream().anyMatch(
                str -> str.equals(originalColumnName.replaceAll(FOREIGN_KEY_SUFFIX + "$", "")
                        .replaceAll(POLYMORPHIC_SUFFIX + "$", "")));
    }

    public static String singularize(String originalTableName) {
        Inflector inflector = Inflector.getInstance();
        return inflector.underscore(inflector.singularize(originalTableName));
    }

    public static String reverse(String atomName) {
        Inflector inflector = Inflector.getInstance();
        return inflector.underscore(inflector.pluralize(atomName));
    }

    public static String tableAtomName(String originalTableName) {
        Inflector inflector = Inflector.getInstance();
        return inflector.upperCamelCase(inflector.singularize(originalTableName));
    }

    public static String tableAtomNameFromFKey(String originalColumnName)
            throws IllegalAccessException {
        Inflector inflector = Inflector.getInstance();
        return inflector.upperCamelCase(inflector
                .singularize(tableNameFromFKey(originalColumnName)));
    }

    public static String tableNameFromFKey(String originalColumnName) throws IllegalAccessException {
        Matcher matcher = foreignKeyPattern.matcher(originalColumnName);
        if (!matcher.find()) {
            throw new IllegalAccessException("this is not foreign key.");
        }
        return reverse(matcher.group(1));
    }

    public static String columnAtomName(String originalColumnName, String originalTableName) {
        Inflector inflector = Inflector.getInstance();
        //return COLUMN_ATOM_PREFIX + inflector.upperCamelCase(originalTableName) + COUPLER
        //        + inflector.upperCamelCase(originalColumnName);
        return inflector.upperCamelCase(originalColumnName);
    }

    public static String columnFieldName(String originalColumnName, String originalTableName) {
        return originalColumnName;
    }

    public static String polymorphicImplAtomName(String polymorphicStr, String refToAtomName) {
        Inflector inflector = Inflector.getInstance();
        return inflector.upperCamelCase(polymorphicStr) + refToAtomName;
    }

    public static String implementedPolymorphicAtomName(String keystr, String ownerTableName) {
        return tableAtomName(keystr) + tableAtomName(ownerTableName);
    }

    public static String columnRelationName(String originalColumnName, String originalTableName) {
        Inflector inflector = Inflector.getInstance();
        //return originalTableName + COUPLER + inflector.upperCamelCase(originalColumnName);
        return inflector.lowerCamelCase(originalColumnName);
    }

    public static String foreignKeyName(String originalColumnName, String originalTableName) {
        String optimized = originalColumnName;
        Pattern pattern = foreignKeyPattern;
        Matcher matcher = pattern.matcher(originalColumnName);
        while (matcher.find()) {
            optimized = matcher.replaceAll("$1");
        }
        //return originalTableName + COUPLER + tableAtomName(optimized);
        Inflector inflector = Inflector.getInstance();
        return inflector.singularize(optimized);
    }

    public static String foreignKeyNameReversed(String refTableName, String originalTableName) {
        Inflector inflector = Inflector.getInstance();
        // NOTICE: defaultは複数形。OneToManyのOne側と見做して。
        //return refTableName + COUPLER + inflector.upperCamelCase(originalTableName);
        return inflector.pluralize(originalTableName);
    }

}
