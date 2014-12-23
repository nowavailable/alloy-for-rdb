package com.testdatadesigner.tdalloy.core.types;

public class DummySig extends Sig {
    private static final long serialVersionUID = 1L;

    public Integer namingSeq;

    public DummySig(Tipify type, Integer seq) {
        super(type);
        this.namingSeq = seq;
        this.name = "Dummy" + String.valueOf(this.namingSeq);
        this.originPropertyName = "dummy_" + String.valueOf(this.namingSeq) + "s";
    }
}
