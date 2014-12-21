package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;

public class Sig implements Serializable {
    private static final long serialVersionUID = 1L;

    public String name;
    public Integer seq;
    public Tipify type;
    public static enum Tipify {
        ENTITY,                     // テーブル相当
        PROPERTY_FACTOR,            // 属性値としてモデリングされたenumの列挙子
        PROPERTY_PROTOTYPE,         // 属性値としてモデリングされたenum
        POLYMOPHIC_TYPE_ABSTRACT,   // ポリモーフィック関連のtypeの抽象化されたsig
        POLYMOPHIC_IMPLIMENT,       // ポリモーフィック関連のtypeの抽象化されたsigの継承先
        BOOLEAN_FACTOR,
        // TODO: 状態sig用。
        //STATE,
    }
    public Boolean isAbstruct = false;
    public String originPropertyName = "";
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

    public void setParent(Sig parent) {
        if (this.type.equals(Tipify.ENTITY)) {
            throw new RuntimeException("No need parent.");
        }
        this.parent = parent;
    }
    
}
