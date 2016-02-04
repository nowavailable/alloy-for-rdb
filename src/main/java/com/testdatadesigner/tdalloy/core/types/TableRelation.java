package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;
import java.util.Arrays;

public class TableRelation extends Relation implements Serializable, IRelation {
	private static final long serialVersionUID = 1L;

    private ITable refTo;
    private Entity owner;
    
	public TableRelation() {
		super();
	}

	@Override
	public IAtom getRefTo() {
		return (IAtom)this.refTo;
	}

	@Override
	public void setRefTo(IAtom refTo) throws IllegalAccessException {
		if (!Arrays.asList(Entity.class, MissingAtom.class).contains(refTo.getClass())) {
			throw new IllegalAccessException(refTo.getClass().toString() + " is not for refTo.");
		}
		this.refTo = (ITable)refTo;
	}

	@Override
	public IAtom getOwner() {
		return this.owner;
	}

	@Override
	public void setOwner(IAtom owner) throws IllegalAccessException {
		if (!owner.getClass().equals(Entity.class)) {
			throw new IllegalAccessException(owner.getClass().toString() + " is not for refTo.");
		}
		this.owner = (Entity)owner;
	}

}
