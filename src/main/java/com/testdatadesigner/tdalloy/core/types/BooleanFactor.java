package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;

public class BooleanFactor extends Atom implements Serializable, IAtom, IColumnValue {
	private static final long serialVersionUID = 1L;

    private Entity parent;

	public BooleanFactor() {
		super();
	}

	@Override
    public IAtom getParent() {
        return this.parent;
    }

	@Override
    public void setParent(IAtom parent) throws IllegalAccessException {
        if (!parent.getClass().equals(Entity.class)) {
            throw new IllegalAccessException("No need parent.");
        }
        this.parent = (Entity)parent;
    }
}
