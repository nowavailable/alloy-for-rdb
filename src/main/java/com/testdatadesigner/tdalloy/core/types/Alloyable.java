package com.testdatadesigner.tdalloy.core.types;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Alloyable implements Serializable,IAlloyable {

	private static final long serialVersionUID = 1L;
	private List<Atom> atoms = new ArrayList<>();
	private List<Relation> relations = new ArrayList<>();
	private List<Fact> facts = new ArrayList<>();
	private Boolean isRailsOriented = Boolean.FALSE;

	@Override
    public List<Atom> getAtoms() {
		return atoms;
	}
	@Override
	public void setAtoms(List<Atom> atoms) {
		this.atoms = atoms;
	}
	@Override
	public List<Relation> getRelations() {
		return relations;
	}
	@Override
	public void setRelations(List<Relation> relations) {
		this.relations = relations;
	}
	@Override
	public List<Fact> getFacts() {
		return facts;
	}
	@Override
	public void setFacts(List<Fact> facts) {
		this.facts = facts;
	}
	@Override
	public Boolean getIsRailsOriented() {
		return isRailsOriented;
	}
	@Override
	public void setIsRailsOriented(Boolean isRailsOriented) {
		this.isRailsOriented = isRailsOriented;
	}

}
