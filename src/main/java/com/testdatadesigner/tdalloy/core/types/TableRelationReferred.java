package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;
import java.util.Arrays;

public class TableRelationReferred extends Relation implements Serializable, IRelation {
	private static final long serialVersionUID = 1L;

    private Entity refTo;
    private ITable owner;
    
	public TableRelationReferred() {
		super();
	}

	@Override
	public IAtom getRefTo() {
		return this.refTo;
	}

	@Override
	public void setRefTo(IAtom refTo) throws IllegalAccessException {
		if (!Arrays.asList(Entity.class, MissingAtom.class).contains(refTo.getClass())) {
			throw new IllegalAccessException(refTo.getClass().toString() + " is not for refTo.");
		}
		this.refTo = (Entity)refTo;
	}

	@Override
	public IAtom getOwner() {
		return (IAtom)this.owner;
	}

	@Override
	public void setOwner(IAtom owner) throws IllegalAccessException {
		if (!Arrays.asList(Entity.class, MissingAtom.class).contains(owner.getClass())) {
			throw new IllegalAccessException(owner.getClass().toString() + " is not for refTo.");
		}
		this.owner = (ITable)owner;
	}

}
