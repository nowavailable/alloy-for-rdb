package com.testdatadesigner.tdalloy.core.types;

import java.util.ArrayList;
import java.util.List;

public class MultipleRelation extends Relation {


    private static final long serialVersionUID = 1L;

    public List<? extends Atom> refToTypes = new ArrayList<>();
    public List<? extends Atom> reverseOfrefToTypes = new ArrayList<>();

    public MultipleRelation(Typify type) {
        super(type);
    }
}
