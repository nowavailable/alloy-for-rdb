package com.testdatadesigner.tdalloy.core.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.testdatadesigner.tdalloy.util.Inflector;

public class RulesForAlloyable {

    public static final String FOREIGN_KEY_SUFFIX = "_id";
    public static final String POLYMOPHIC_SUFFIX = "_type";
    public static final String COUPLER = "_";
    static Pattern foreignKeyPattern = Pattern.compile("^(.+)(" + FOREIGN_KEY_SUFFIX + ")$");
    static Pattern patternsForPolymophic = Pattern.compile("^(.+)(" + POLYMOPHIC_SUFFIX + ")$");
    public static final String COLMN_SIG_PREFIX = "";
    
    public static List<List<String>> inferencedRelations(List<String> columnNames) {
        final List<String> matchedPolymophic = new ArrayList<>();
        List<String> candidate = new ArrayList<>();
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

        final List<String> matchedForeignKey = new ArrayList<>();
        for (String columnName : columnNames) {
            Matcher matcher = foreignKeyPattern.matcher(columnName);
            if (matcher.find()) {
                if (matchedPolymophic.contains(matcher.group(1))) {
                    continue;
                }
                matchedForeignKey.add(columnName);
            }
        }
        return Arrays.asList(matchedPolymophic, matchedForeignKey);
    }

    public static String singularize(String originalTableName) {
        Inflector inflector = Inflector.getInstance();
        return inflector.underscore(inflector.singularize(originalTableName));
    }

    public static String reverse(String sigName) {
        Inflector inflector = Inflector.getInstance();
        return inflector.underscore(inflector.pluralize(sigName));
    }

    public static String tableSigName(String originalTableName) {
        Inflector inflector = Inflector.getInstance();
        return inflector.upperCamelCase(inflector.singularize(originalTableName));
    }

    public static String tableSigNameFromFKey(String originalColumnName)
            throws IllegalAccessException {
        Inflector inflector = Inflector.getInstance();
        return inflector
                .upperCamelCase(inflector.singularize(tableNameFromFKey(originalColumnName)));
    }

    public static String tableNameFromFKey(String originalColumnName)
            throws IllegalAccessException {
        Matcher matcher = foreignKeyPattern.matcher(originalColumnName);
        if (!matcher.find()) {
            throw new IllegalAccessException("this is not foreign key.");
        }
        return matcher.group(1);
    }
    
    public static String colmnSigName(String originalColumnName,
            String originalTableName) {
        Inflector inflector = Inflector.getInstance();
        return COLMN_SIG_PREFIX + inflector.upperCamelCase(originalTableName) + COUPLER
                + inflector.upperCamelCase(originalColumnName);
    }

    public static String implimentedPolymophicSigName(String keystr,
            String ownerTableName) {
        return tableSigName(keystr) + tableSigName(ownerTableName);
    }

    public static String colmnRelationName(String originalColumnName,
            String originalTableName) {
        Inflector inflector = Inflector.getInstance();
        return originalTableName + COUPLER
                + inflector.upperCamelCase(originalColumnName);
    }
    
    public static String foreignKeyName(String originalColumnName,
            String originalTableName) {
        String optimized = originalColumnName;
        Pattern pattern = foreignKeyPattern;
        Matcher matcher = pattern.matcher(originalColumnName);
        while (matcher.find()) {
            optimized = matcher.replaceAll("$1");
        }
        return originalTableName + COUPLER
                + tableSigName(optimized);
    }

    public static String foreignKeyNameReversed(String refTableName,
            String originalTableName) {
        Inflector inflector = Inflector.getInstance();
    	// NOTICE: defaultは複数形。OneToManyのOne側と見做して。
        return refTableName + COUPLER
                + inflector.upperCamelCase(originalTableName);
    }

    public static List<Sig> defaultPropertyFactor(
            final String originalColumnName, final String originalTableName) {
        List<String> factors = new ArrayList<String>() {
            {
                Inflector inflector = Inflector.getInstance();
                this.add(new String(originalTableName
                        + COUPLER
                        + inflector.upperCamelCase(originalColumnName) + "HIGH"));
                this.add(new String(originalTableName
                        + COUPLER
                        + inflector.upperCamelCase(originalColumnName) + "LOW"));
            }
        };
        List<Sig> sigs = new ArrayList<>();
        for (String factor : factors) {
            Sig sig = new Sig(Sig.Tipify.PROPERTY_FACTOR);
            sig.originPropertyName = originalColumnName;
            sig.name = factor;
            sig.isAbstruct = Boolean.TRUE;
            sigs.add(sig);
        }
        return sigs;
    }
    
}
