package com.testdatadesigner.tdalloy.core.types;

import java.util.ArrayList;
import java.util.List;

public class MultipleRelation extends Relation {


    private static final long serialVersionUID = 1L;

    public List<? extends Sig> refToTypes = new ArrayList<>();
    public List<? extends Sig> reverseOfrefToTypes = new ArrayList<>();

    public MultipleRelation(Tipify type) {
        super(type);
    }
}
