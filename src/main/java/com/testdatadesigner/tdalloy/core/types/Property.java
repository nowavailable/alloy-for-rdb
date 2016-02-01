package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;
import java.util.Arrays;

public class Property extends Atom implements Serializable, IAtom, IColumnValue {
	private static final long serialVersionUID = 1L;

    private IAtom parent;

	public Property() {
		super();
	}

	@Override
    public IAtom getParent() {
        return this.parent;
    }

	@Override
    public void setParent(IAtom parent) throws IllegalAccessException {
        if (!Arrays.asList(Entity.class, PolymorphicAbstract.class).contains(parent.getClass())) {
            throw new IllegalAccessException("No need parent.");
        }
        this.parent = parent;
    }
}
