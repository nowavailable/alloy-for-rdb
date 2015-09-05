package com.testdatadesigner.tdalloy.core.types;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Alloyable implements Serializable {
    private static final long serialVersionUID = 1L;
    public List<Atom> atoms = new ArrayList<>();
    public List<Relation> relations = new ArrayList<>();
    public List<Fact> facts = new ArrayList<>();
    public Boolean isRailsOriented = Boolean.FALSE;

}
