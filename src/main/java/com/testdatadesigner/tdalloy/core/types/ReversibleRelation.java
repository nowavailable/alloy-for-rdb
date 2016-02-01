package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ReversibleRelation extends TableRelation implements Serializable, IRelation {
    private static final long serialVersionUID = 1L;

    private IAtom refTo;
    private IAtom owner;

    //private List<? extends Atom> refToTypes = new ArrayList<>();
    //private List<? extends IAtom> reverseOfrefToTypes = new ArrayList<>();
    
    private Class<? extends Relation> injected;
    private Class<? extends Atom> boundedOwner;
    private Class<? extends Atom> boundedRefTo;
    
    public ReversibleRelation(Class<? extends Relation> injected) throws IllegalAccessException {
        super();
    	if (injected.equals(RelationPolymorphic.class)) {
    	    this.injected = injected;
    		this.boundedOwner = Entity.class;
    		this.boundedRefTo = PolymorphicAbstract.class;
    		return;
    	} 
    	if (injected.equals(AbstractRelationPolymorphic.class)) {
            this.injected = injected;
    		this.boundedOwner = PolymorphicAbstract.class;
    		this.boundedRefTo = Entity.class;
    		return;
    	}
    	throw new IllegalAccessException();
    }

    public Class<? extends Relation> getInjected() {
        return injected;
    }

    @Override
    public IAtom getRefTo() {
        if (this.refTo == null) {
//          throw new ParseError(this.name + ":" + this.type.toString() +  " does not have owner.");
        }
        return this.refTo;
    }

    @Override
    public void setRefTo(IAtom refTo) throws IllegalAccessException {
        this.refTo = refTo;
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
		if (!owner.getClass().equals(this.boundedOwner)) {
			throw new IllegalAccessException(owner.getClass().toString() + " is not for owner.");
		}
		this.owner = owner;
	}
}
