package com.testdatadesigner.tdalloy.core.types;

import java.util.List;

public interface IAlloyable {
    public List<Atom> getAtoms();
    public void setAtoms(List<Atom> atoms);
	public List<Relation> getRelations();
	public void setRelations(List<Relation> relations);
	public List<Fact> getFacts();
	public void setFacts(List<Fact> facts);
	public Boolean getIsRailsOriented();
	public void setIsRailsOriented(Boolean isRailsOriented);
}
