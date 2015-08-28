package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;

public class Relation implements Serializable {
    private static final long serialVersionUID = 1L;

    public String name;
    public Typify type;
    public static enum Typify {
        VALUE,                      // カラム値である
        //VALUE_REVERSED,             // カラム値の側から候補キーを見た関係
        RELATION,                   // テーブルリレーション
        RELATION_REFERRED,          // 外部キーを持たれる側から見たテーブルリレーション,
        RELATION_POLYMORPHIC,       // ポリモーフィック関連のための文字列カラムを表現
        // TODO: ポリモーフィック関連のための外部キーカラムは？als上では必要ないんだが。。。
        ABSTRACT_RELATION,          // 便宜上想定した実在しないテーブルリレーション。ポリモーフィック関連などで使用。
        ABSTRACT_RELATION_REFERRED, // 前記の逆
        ABSTRACT_RELATION_TYPIFIED, //
        //SHORTCUT_RELATION,
        // TODO: 状態sig用。
        //ON_STATE,
    }

    private Atom refTo;
    private Atom owner;
    public Boolean ignore = Boolean.FALSE;

    public Boolean isNotEmpty = null;  //Boolean.FALSE
    public Boolean isUnique = Boolean.FALSE;


    public Relation() {
        super();
    }

    public Relation(Typify type) {
        super();
        this.type = type;
    }

	public Atom getRefTo() {
		return refTo != null ? refTo : new Atom();
	}

	public void setRefTo(Atom refTo) {
		this.refTo = refTo;
	}

	public Atom getOwner() {
		return owner != null ? owner : new Atom();
	}

	public void setOwner(Atom owner) {
		this.owner = owner;
	}
}
