package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;
import java.util.Arrays;

public class PolymorphicAbstract extends Atom implements Serializable, IAtom {
	private static final long serialVersionUID = 1L;

    private Entity parent;

	public PolymorphicAbstract() {
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
        this.parent = (Entity)parent;
    }

}
