package com.testdatadesigner.tdalloy.core.naming;

import java.util.List;

public interface IRulesForAlloyable {

    public String foreignKeySuffix();
    public String polymorphicSuffix();
    public String coupler();
    public String columnAtomPrefix();

    public String singularize(String originalTableName);
    public String tableize(String atomName);
    public String reverse(String atomName);

    public List<List<String>> guessedRelations(List<String> columnNames);
    public Boolean isGuessedPolymorphic(String originalColumnName, List<String> list);

    public String tableNameFromFKey(String originalColumnName) throws IllegalAccessException;
    public String fkeyFromTableName(String refTableName);
    public String foreignKeyName(String originalColumnName, String originalTableName);
    public String foreignKeyNameReversed(String refTableName, String originalTableName);
}
