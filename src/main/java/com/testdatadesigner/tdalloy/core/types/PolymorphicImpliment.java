package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;

public class PolymorphicImpliment extends Atom implements Serializable, IAtom {
	private static final long serialVersionUID = 1L;

    private IAtom parent = null;
    private PolymorphicAbstract extended;

	public PolymorphicImpliment() {
		super();
	}

	@Override
    public IAtom getParent() {
        return this.parent;
    }

	@Override
    public void setParent(IAtom parent) throws IllegalAccessException {
        throw new IllegalAccessException("No need parent.");
    }

    public PolymorphicAbstract getExtended() {
        return this.extended;
    }

    public void setExtended(PolymorphicAbstract extended) {
        this.extended = extended;
    }
}
