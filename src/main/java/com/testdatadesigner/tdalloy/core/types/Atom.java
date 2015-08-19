package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;
import java.util.Arrays;

public class Atom implements Serializable {
    private static final long serialVersionUID = 1L;

    public String name;
    public Integer seq;
    public Tipify type;

    public static enum Tipify {
        ENTITY, // テーブル相当
        PROPERTY,    // カラム値である（Boolean型以外はこれにまとめる）
        POLYMORPHIC_ABSTRACT, // ポリモーフィック関連のtypeの抽象化されたsig
        POLYMOPHIC_IMPLIMENT, // ポリモーフィック関連のtypeの抽象化されたsigの継承先
        BOOLEAN_FACTOR,
        // TODO: 状態sig用。
        // STATE,
    }

    public Boolean isAbstruct = false;
    public String originPropertyName = "";
    public String originTypeName = "";  // カラムの型を格納 TODO: テーブルはどうする？ Table or View ?
    
    public Boolean ignore = Boolean.FALSE;

    private Atom parent;
    private Atom extended;


    public Atom() {
        super();
    }

    public Atom(Tipify type) {
        super();
        this.type = type;
    }

    public Atom getParent() {
        return this.parent;
    }

    public void setParent(Atom parent) throws IllegalAccessException {
        if (!Arrays.asList(Tipify.ENTITY, Tipify.POLYMORPHIC_ABSTRACT).contains(parent.type)) {
            throw new IllegalAccessException("No need parent.");
        }
        this.parent = parent;
    }

}
