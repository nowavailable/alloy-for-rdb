package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;

public class ColumnValue extends Relation implements Serializable, IRelation {
	private static final long serialVersionUID = 1L;

    private Property refTo;
    private Entity owner;

	public ColumnValue() {
		super();
	}

	@Override
	public IAtom getRefTo() {
		if (this.refTo == null) {
//			throw new ParseError(this.name + ":" + this.type.toString() +  " does not have owner.");
		}
		return this.refTo;
	}

	@Override
	public void setRefTo(IAtom refTo) throws IllegalAccessException {
		if (!refTo.getClass().equals(Property.class)) {
			throw new IllegalAccessException(refTo.getClass().toString() + " is not for refTo.");
		}
		this.refTo = (Property)refTo;
	}

	@Override
	public IAtom getOwner() {
		if (this.owner == null) {
//			throw new ParseError(this.name + ":" + this.type.toString() +  " does not have owner.");
		}
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
