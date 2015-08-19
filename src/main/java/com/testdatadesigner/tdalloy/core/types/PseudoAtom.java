package com.testdatadesigner.tdalloy.core.types;

public class PseudoAtom extends Atom {
    private static final long serialVersionUID = 1L;

    public Integer namingSeq;

    public PseudoAtom(Tipify type, Integer seq) {
        super(type);
        this.namingSeq = seq;
        this.name = "Dummy" + String.valueOf(this.namingSeq);
        this.originPropertyName = "dummy_" + String.valueOf(this.namingSeq) + "s";
    }
}
