package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;
import java.util.Arrays;

public class RelationProperty extends Relation implements Serializable, IRelation {
	private static final long serialVersionUID = 1L;

    private IColumnValue refTo;
    private Entity owner;

	public RelationProperty() {
		super();
	}

	@Override
	public IAtom getRefTo() {
		return (IAtom)this.refTo;
	}

	@Override
	public void setRefTo(IAtom refTo) throws IllegalAccessException {
		if (!Arrays.asList(refTo.getClass().getInterfaces()).contains(IColumnValue.class)) {
			throw new IllegalAccessException(refTo.getClass().toString() + " is not for refTo.");
		}
		this.refTo = (IColumnValue)refTo;
	}

	@Override
	public IAtom getOwner() {
		return this.owner;
	}

	@Override
	public void setOwner(IAtom owner) throws IllegalAccessException {
        if (!owner.getClass().equals(Entity.class)) {
        	throw new IllegalAccessException(owner.getClass().toString() + " is not for owner.");
        }
		this.owner = (Entity)owner;
	}

}
