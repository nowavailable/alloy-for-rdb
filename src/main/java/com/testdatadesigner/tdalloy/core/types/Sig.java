package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;
import java.util.Arrays;

public class Sig implements Serializable {
    private static final long serialVersionUID = 1L;

    public String name;
    public Integer seq;
    public Tipify type;

    public static enum Tipify {
        ENTITY, // テーブル相当
        PROPERTY,    // カラム値である（Boolean型以外はこれにまとめる）
        POLIMORPHIC_PROTOTYPE, 
        POLYMORPHIC_TYPE_ABSTRACT, // ポリモーフィック関連のtypeの抽象化されたsig
        POLYMOPHIC_IMPLIMENT, // ポリモーフィック関連のtypeの抽象化されたsigの継承先
        BOOLEAN_FACTOR,
        // TODO: 状態sig用。
        // STATE,
    }

    public Boolean isAbstruct = false;
    public String originPropertyName = "";
    public String originTypeName = "";  // カラムの型を格納 TODO: テーブルはどうする？ Table or View ?
    
    public Boolean ignore = Boolean.FALSE;

    private Sig parent;


    public Sig() {
        super();
    }

    public Sig(Tipify type) {
        super();
        this.type = type;
    }

    public Sig getParent() {
        return this.parent;
    }

    public void setParent(Sig parent) throws IllegalAccessException {
        if (!Arrays.asList(Tipify.ENTITY, Tipify.POLYMORPHIC_TYPE_ABSTRACT).contains(parent.type)) {
            throw new IllegalAccessException("No need parent.");
        }
        this.parent = parent;
    }

}
