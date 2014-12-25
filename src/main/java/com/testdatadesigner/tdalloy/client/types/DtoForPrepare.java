package com.testdatadesigner.tdalloy.client.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * JSONに変換して、クライアントの、初期設定ビューにGETさせる、 初期設定ビューからPOSTさせる プレースホルダーオブジェクト。
 * 
 * @author tsutsumi
 *
 */
public class DtoForPrepare {
    public List<Table> tables = new ArrayList<>();

    public static enum RelationType {
        // NONE,
        MANY_TO_ONE, ONE_TO_ONE, POLYMOPHIC
    };

    public class Table {
        public String name;
        public List<Column> columns;
        public Boolean ignore = Boolean.FALSE;
    }

    public class Column {
        public String name;
        public Relation relation;
        public Boolean ignore = Boolean.FALSE;
    }

    public class Relation {
        public RelationType type;
        public List<String> refTo = new ArrayList<>();
        public Boolean ignore = Boolean.FALSE;
    }

    public Table constructTable() {
        return new Table();
    }

    public Column constructColumn() {
        return new Column();
    }

    public Relation constructRelation() {
        return new Relation();
    }
}
