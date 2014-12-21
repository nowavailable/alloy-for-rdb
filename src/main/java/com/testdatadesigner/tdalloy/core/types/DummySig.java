package com.testdatadesigner.tdalloy.core.types;


public class DummySig extends Sig {
    private static final long serialVersionUID = 1L;

    public DummySig(Tipify type, Integer seq) {
        super(type);
        this.name = "Dummy_" + String.valueOf(seq);
    }

}
