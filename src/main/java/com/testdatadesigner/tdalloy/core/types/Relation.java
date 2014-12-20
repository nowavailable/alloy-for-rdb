package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;

public class Relation implements Serializable {
    private static final long serialVersionUID = 1L;

    public String name;
    public Tipify type;
    public static enum Tipify {
        VALUE,                      // カラム値である
        VALUE_REVERSED,             // カラム値の側から候補キーを見た関係
        RELATION,                   // テーブルリレーション
        RELATION_REVERSED,          // 外部キーを持たれる側から見たテーブルリレーション
        ABSTRUCT_RELATION,          // 便宜上想定した実在しないテーブルリレーション。ポリモーフィック関連などで使用。
        ABSTRUCT_RELATION_REVERSED, // 前記の逆
        //SHORTCUT_RELATION,
        // TODO: 状態sig用。
        //ON_STATE,
    }
    public Sig referTo;
    public Sig owner;           // alloy定義上の親sig
    public String originOwner;  // dbスキーマ上の親（テーブル名）
    public String originPropertyName;
    public Boolean ignore = Boolean.FALSE;
}
