package com.testdatadesigner.tdalloy.core.types;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Relation implements Serializable {

	private static final long serialVersionUID = 1L;

    public String name;
//    public Typify type;
//    public static enum Typify {
//        VALUE,                      // カラム値である
//        //VALUE_REVERSED,             // カラム値の側から候補キーを見た関係
//        RELATION,                   // テーブルリレーション
//        RELATION_REFERRED,          // 外部キーを持たれる側から見たテーブルリレーション,
//        RELATION_POLYMORPHIC,       // ポリモーフィック関連のための文字列カラムを表現
//        // TODO: ポリモーフィック関連のための外部キーカラムは？als上では必要ないんだが。。。
//        ABSTRACT_RELATION,          // 便宜上想定した実在しないテーブルリレーション。ポリモーフィック関連などで使用。
//        ABSTRACT_RELATION_REFERRED, // 前記の逆
//        ABSTRACT_RELATION_TYPIFIED, //
//        //SHORTCUT_RELATION,
//        // TODO: 状態sig用。
//        //ON_STATE,
//    }

    public List<String> originColumnName = new ArrayList<>();
    public Boolean ignore = Boolean.FALSE;

    public Boolean isNotEmpty = Boolean.FALSE;
    public Boolean isUnique = Boolean.FALSE;


    public Relation() {
        super();
    }

    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getOriginColumnName() {
		return originColumnName;
	}

	public void setOriginColumnName(List<String> originColumnName) {
		this.originColumnName = originColumnName;
	}

	public Boolean getIgnore() {
		return ignore;
	}

	public void setIgnore(Boolean ignore) {
		this.ignore = ignore;
	}

	public Boolean getIsNotEmpty() {
		return isNotEmpty;
	}

	public void setIsNotEmpty(Boolean isNotEmpty) {
		this.isNotEmpty = isNotEmpty;
	}

	public Boolean getIsUnique() {
		return isUnique;
	}

	public void setIsUnique(Boolean isUnique) {
		this.isUnique = isUnique;
	}
}
