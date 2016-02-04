package com.testdatadesigner.tdalloy.core.types;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class MissingAtomFactory {
    private final static MissingAtomFactory INSTANCE = new MissingAtomFactory();
    private List<MissingAtom> missingAtoms = new ArrayList<>();

	private MissingAtomFactory() {
    }

    public static MissingAtomFactory getInstance() {
        return INSTANCE;
    }

    public MissingAtom getMissingAtom(String atomName) {
    	List<MissingAtom> exits = missingAtoms.stream().
    			filter(atom -> atom.getName().equals(atomName)).
    			collect(Collectors.toList());
    	if (exits.isEmpty()) {
    		MissingAtom missingAtom = new MissingAtom(atomName);
    		missingAtoms.add(missingAtom);
    		return missingAtom;
		} else {
			return exits.get(0);
		}
    }

    public List<MissingAtom> getMissingAtoms() {
		return missingAtoms;
	}
}
