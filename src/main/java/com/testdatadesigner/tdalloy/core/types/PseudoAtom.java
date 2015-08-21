package com.testdatadesigner.tdalloy.core.types;

public class PseudoAtom extends Atom {
    private static final long serialVersionUID = 1L;

    public PseudoAtom(Integer seq) {
        super(Atom.Tipify.ENTITY);
        this.name = "Dummy" + String.valueOf(seq);
        this.originPropertyName = "dummy_" + String.valueOf(seq) + "s";
    }

    public PseudoAtom(String name, Atom abstructAtom) throws IllegalAccessException {
        super(Atom.Tipify.POLYMOPHIC_IMPLIMENT);
    	if (!abstructAtom.type.equals(Atom.Tipify.POLYMORPHIC_ABSTRACT)) {
    		throw new IllegalAccessError();
    	}
    	this.name = name;
        this.setExtended(abstructAtom);
    }
}
