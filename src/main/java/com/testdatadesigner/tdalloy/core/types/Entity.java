package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;

public class Entity extends Atom implements Serializable, IAtom, ITable {
	private static final long serialVersionUID = 1L;

    private IAtom parent = null;

	public Entity() {
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

}
