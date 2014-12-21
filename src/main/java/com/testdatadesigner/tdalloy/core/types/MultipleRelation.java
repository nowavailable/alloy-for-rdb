package com.testdatadesigner.tdalloy.core.types;

import java.util.ArrayList;
import java.util.List;

public class MultipleRelation<T extends Sig> extends Relation {


    private static final long serialVersionUID = 1L;

    public List<T> refToTypes = new ArrayList<>();

    public MultipleRelation(Tipify type) {
        super(type);
    }
}
