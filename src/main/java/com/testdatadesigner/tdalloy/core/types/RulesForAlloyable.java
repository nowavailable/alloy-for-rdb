package com.testdatadesigner.tdalloy.core.types;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.testdatadesigner.tdalloy.util.Inflector;

public class RulesForAlloyable {
    public static String generateTableSigName(String originalTableName) {
        Inflector inflector = Inflector.getInstance();
        return inflector.upperCamelCase(inflector.singularize(originalTableName));
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
        Pattern pattern = Pattern.compile("^(.+)(_id)$");
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
    
    public static List<Relation> inferenceForeignKey() {
        List<Relation> relations = new ArrayList<Relation>();
        return relations;
    }

    public static List<Relation> inferencePolymophics() {
        List<Relation> relations = new ArrayList<Relation>();
        return relations;
    }
}
