package com.testdatadesigner.tdalloy.core.types;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.testdatadesigner.tdalloy.util.Inflector;

public class RulesForAlloyable {
    
    static Pattern foreignKeyPattern = Pattern.compile("^(.+)(_id)$");
    static Pattern patternsForPolymophic = Pattern.compile("^(.+)(_type)$");
    
    public static List<List<String>> inferencedRelations(List<String> columnNames) {
        final List<String> matchedPolymophic = new ArrayList<String>();
        List<String> candidate = new ArrayList<String>();
        for (String columnName : columnNames) {
            Matcher idMatcher = patternsForPolymophic.matcher(columnName);
            if (idMatcher.find()) {
                candidate.add(idMatcher.group(1));
            }
        }
        for (String columnName : columnNames) {
            for (String keyStr : candidate) {
                Pattern pattern = Pattern.compile("^(" + keyStr + ")(_id)$");
                Matcher matcher = pattern.matcher(columnName);
                if (matcher.find()) {
                    matchedPolymophic.add(keyStr);
                }
            }
        }

        final List<String> matchedForeignKey = new ArrayList<String>();
        for (String columnName : columnNames) {
            Matcher matcher = foreignKeyPattern.matcher(columnName);
            if (matcher.find()) {
                if (matchedPolymophic.contains(matcher.group(1))) {
                    continue;
                }
                matchedForeignKey.add(columnName);
            }
        }
        return new ArrayList<List<String>>(){{
            this.add(matchedPolymophic);
            this.add(matchedForeignKey);
        }};
    }

    public static String generateTableSigName(String originalTableName) {
        Inflector inflector = Inflector.getInstance();
        return inflector.upperCamelCase(inflector.singularize(originalTableName));
    }

    public static String generateTableSigNameFromFKey(String originalColumnName)
            throws IllegalAccessException {
        Inflector inflector = Inflector.getInstance();
        return inflector
                .upperCamelCase(inflector.singularize(generateTableNameFromFKey(originalColumnName)));
    }

    public static String generateTableNameFromFKey(String originalColumnName)
            throws IllegalAccessException {
        Matcher matcher = foreignKeyPattern.matcher(originalColumnName);
        if (!matcher.find()) {
            throw new IllegalAccessException("this is not foreign key.");
        }
        return matcher.group(1);
    }
    
    public static String generateColmnSigName(String originalColumnName,
            String originalTableName) {
        Inflector inflector = Inflector.getInstance();
        return "PR_" + originalTableName + "_"
                + inflector.upperCamelCase(originalColumnName);
    }
    
    public static String generateForeignKeyName(String originalColumnName,
            String originalTableName) {
        String optimized = originalColumnName;
        Pattern pattern = foreignKeyPattern;
        Matcher matcher = pattern.matcher(originalColumnName);
        while (matcher.find()) {
            optimized = matcher.replaceAll("$1");
        }
        return originalTableName + "_"
                + generateTableSigName(optimized);
    }

    public static String generateForeignKeyNameReversed(String refTableName,
            String originalTableName) {
        Inflector inflector = Inflector.getInstance();
        return refTableName + "_"
                + inflector.upperCamelCase(originalTableName);
    }

    public static List<Sig> generateDefaultPropertyFactor(
            final String originalColumnName, final String originalTableName) {
        List<String> factors = new ArrayList<String>() {
            {
                Inflector inflector = Inflector.getInstance();
                this.add(new String(originalTableName
                        + "_"
                        + inflector.upperCamelCase(originalColumnName) + "HIGH"));
                // this.add(
                // new String(
                // originalTableName +
                // "_" +
                // inflector.upperCamelCase(originalColumnName)
                // +
                // "MID")
                // );
                this.add(new String(originalTableName
                        + "_"
                        + inflector.upperCamelCase(originalColumnName) + "LOW"));
            }
        };
        List<Sig> sigs = new ArrayList<Sig>();
        for (String factor : factors) {
            Sig sig = new Sig();
            sig.originPropertyName = originalColumnName;
            sig.name = factor;
            sig.type = Sig.Tipify.PROPERTY_FACTOR;
            sig.isAbstruct = Boolean.TRUE;
            sigs.add(sig);
        }
        return sigs;
    }
    
}
