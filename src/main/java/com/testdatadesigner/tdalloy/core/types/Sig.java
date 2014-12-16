package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;

public class Sig implements Serializable {
    private static final long serialVersionUID = 1L;

    public String name;
    public Integer seq;
    public enum tipify {
        ENTITY,                     // テーブル相当
        PROPERTY_FACTOR,            // 属性値としてモデリングされたenum
        PROPERTY_PROTOTYPE,         // 属性値としてモデリングされたenumの列挙子
        POLYMOPHIC_TYPE_ABSTRACT,   // ポリモーフィック関連のtypeの抽象化されたsig
        POLYMOPHIC_IMPLIMENT,       // ポリモーフィック関連のtypeの抽象化されたsigの継承先
        // TODO: 状態sig用。
        //STATE,
    }
    public Boolean isAbstruct;
    public String originPropertyName;
}
