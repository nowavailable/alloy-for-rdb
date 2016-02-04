package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;

public class RelationPolymorphicTypeHolder extends Relation implements Serializable, IRelation {
	private static final long serialVersionUID = 1L;

    private PolymorphicAbstract refTo;
    private Entity owner;

    public RelationPolymorphicTypeHolder() {
		super();
	}

	@Override
	public IAtom getRefTo() {
		return this.refTo;
	}

	@Override
	public void setRefTo(IAtom refTo) throws IllegalAccessException {
        if (!refTo.getClass().equals(PolymorphicAbstract.class)) {
            throw new IllegalAccessException(refTo.getClass().toString() + " is not for refTo.");
        }
		this.refTo = (PolymorphicAbstract)refTo;
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
