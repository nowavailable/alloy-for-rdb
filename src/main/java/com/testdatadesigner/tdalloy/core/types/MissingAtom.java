package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;

public class MissingAtom extends Atom implements Serializable, IAtom, ITable {
	private static final long serialVersionUID = 1L;

    private IAtom parent;
    
	public MissingAtom(String name) {
		this.name = name;
	}

	@Override
	public IAtom getParent() {
        return this.parent;
	}

	@Override
	public void setParent(IAtom parent) throws IllegalAccessException {
		this.parent = parent;
	}

}
