package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;
import java.util.Arrays;

public class AbstractRelationPolymorphicTypified extends Relation implements Serializable, IRelation {
	private static final long serialVersionUID = 1L;

    private Entity refTo;
    private IPolymorphicColumn owner;
    private PolymorphicAbstract extended;

    public AbstractRelationPolymorphicTypified() {
		super();
	}

	public PolymorphicAbstract getExtended() {
		return extended;
	}

	public void setExtended(PolymorphicAbstract extended) {
		this.extended = extended;
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
        if (!Arrays.asList(refTo.getClass().getInterfaces()).contains(ITable.class)) {
            throw new IllegalAccessException(refTo.getClass().toString() + " is not for refTo.");
        }
		this.refTo = (Entity)refTo;
	}

	@Override
	public IAtom getOwner() {
		if (this.owner == null) {
//			throw new ParseError(this.name + ":" + this.type.toString() +  " does not have owner.");
		}
		return (IAtom)this.owner;
	}

	@Override
	public void setOwner(IAtom owner) throws IllegalAccessException {
        if (!Arrays.asList(owner.getClass().getInterfaces()).contains(IPolymorphicColumn.class)) {
            throw new IllegalAccessException(owner.getClass().toString() + " is not for owner.");
        }
		this.owner = (IPolymorphicColumn)owner;
	}
}
