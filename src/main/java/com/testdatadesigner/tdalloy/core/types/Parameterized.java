package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import com.testdatadesigner.tdalloy.client.types.DtoForPrepare;
import com.testdatadesigner.tdalloy.client.types.DtoForPrepare.Column;

public class Parameterized implements Serializable {
    private static final long serialVersionUID = 1L;

    /*
     * テーブルリスト
     */
    public List<Column> tables;
    /*
     * カラムリスト 
     */
    public List<Column> columns;
    
    
    /*
     * 関係リスト
     */
    // テーブル - 参照関係
    HashMap<String,String> table_relation = new HashMap<>(); 
    // テーブル - ポリモーフィック関係（多重許容）
    HashMap<String,String> table_polymophic = new HashMap<>();
    // カラム - 参照関係
    HashMap<String,String> column_relation = new HashMap<>(); 
    // カラム - ポリモーフィック関係
    HashMap<String,String> column_polymophic = new HashMap<>(); 

}
