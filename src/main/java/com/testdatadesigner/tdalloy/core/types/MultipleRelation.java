package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MultipleRelation<T extends Relation> extends TableRelation implements Serializable, IRelation {
    private static final long serialVersionUID = 1L;

    private IAtom owner;

    private T injected;

    private List<? extends Atom> refToTypes = new ArrayList<>();
    //private List<? extends IAtom> reverseOfrefToTypes = new ArrayList<>();
    private Class<? extends Atom> boundedOwner;
    private Class<? extends Atom> boundedRefTo;
    //private Class<? extends IAtom> boundedReverseOfRefTo;
    
    public MultipleRelation() throws IllegalAccessException {
        super();
    	if (this.injected.getClass().equals(RelationPolymorphic.class)) {
    		this.boundedOwner = Entity.class;
    		this.boundedRefTo = PolymorphicAbstract.class;
    	} 
    	else if (this.injected.getClass().equals(AbstractRelationPolymorphic.class)) {
    		this.boundedOwner = PolymorphicAbstract.class;
    		this.boundedRefTo = Entity.class;
    	}
    	throw new IllegalAccessException();
    }

    public List<? extends Atom> getRefToTypes() {
		return refToTypes;
	}

	public void setRefToTypes(List<? extends Atom> refToTypes) throws IllegalAccessException {
		if (!refToTypes.get(0).getClass().equals(this.boundedRefTo.getClass())) {
			throw new IllegalAccessException(refToTypes.get(0).getClass().toString() + " is not for refToTypes.");
		}
		this.refToTypes = refToTypes;
	}

//	public List<? extends IAtom> getReverseOfrefToTypes() {
//		return reverseOfrefToTypes;
//	}
//
//	public void setReverseOfrefToTypes(List<? extends IAtom> reverseOfrefToTypes) throws IllegalAccessException {
//		if (!reverseOfrefToTypes.get(0).getClass().equals(this.boundedReverseOfRefTo.getClass())) {
//			throw new IllegalAccessException(reverseOfrefToTypes.get(0).getClass().toString() + " is not for reverseOfrefToTypes.");
//		}
//		this.reverseOfrefToTypes = reverseOfrefToTypes;
//	}

	@Override
	public IAtom getRefTo() {
		return null;
	}

	@Override
	public void setRefTo(IAtom refTo) throws IllegalAccessException {
		;
	}

	@Override
	public IAtom getOwner() {
		if (this.getOwner() == null) {
//			throw new ParseError(this.name + ":" + this.type.toString() +  " does not have owner.");
		}
		return this.getOwner();
	}

	@Override
	public void setOwner(IAtom owner) throws IllegalAccessException {
		if (!owner.getClass().equals(this.boundedOwner.getClass())) {
			throw new IllegalAccessException(owner.getClass().toString() + " is not for owner.");
		}
		this.setOwner(owner);
	}
}
