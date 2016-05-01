package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class Parameterized implements Serializable {
  private static final long serialVersionUID = 1L;

  // /*
  // * テーブルリスト
  // */
  // public class Tables {
  // public List<String> tables;
  // }
  // public Tables tables = new Tables();
  // /*
  // * カラムリスト
  // */
  // public class Columns {
  // public List<String> columns;
  // }
  // public Columns columns = new Columns();

  /*
   * 関係リスト
   */
  // テーブル - 参照関係
  HashMap<String, String> table_relation = new HashMap<>();
  // テーブル - ポリモーフィック関係（多重許容）
  HashMap<String, String> table_polymophic = new HashMap<>();
  // カラム - 参照関係
  HashMap<String, String> column_relation = new HashMap<>();
  // カラム - ポリモーフィック関係
  HashMap<String, String> column_polymophic = new HashMap<>();

}
