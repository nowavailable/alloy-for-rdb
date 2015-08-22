package com.testdatadesigner.tdalloy.core.naming;

import java.util.List;

public interface IRulesForAlloyable {

    public String foreign_key_suffix();
    public String polymorphic_suffix();
    public String coupler();
    public String column_atom_prefix();

    public String singularize(String originalTableName);
    public String tableize(String atomName);
    public String reverse(String atomName);

    public List<List<String>> inferencedRelations(List<String> columnNames);
	public Boolean isInferencedPolymorphic(String originalColumnName, List<String> list);
    public String tableAtomNameFromFKey(String originalColumnName) throws IllegalAccessException;
    public String tableNameFromFKey(String originalColumnName) throws IllegalAccessException;
    public String fkeyFromTableName(String refTableName) throws IllegalAccessException;
    public String foreignKeyName(String originalColumnName, String originalTableName);
    public String foreignKeyNameReversed(String refTableName, String originalTableName);

    public String tableAtomName(String originalTableName);
    public String columnAtomName(String originalColumnName, String originalTableName);
    public String columnFieldName(String originalColumnName, String originalTableName);
    public String polymorphicAbstractAtomName(String polymorphicStr, String parentAtomName);
    public String polymorphicImplAtomName(String polymorphicStr, String refToAtomName);
    public String implementedPolymorphicAtomName(String keystr, String ownerTableName);
    public String columnRelationName(String originalColumnName, String originalTableName);
}
