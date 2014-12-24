package com.testdatadesigner.tdalloy.core.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Fact implements Serializable {
    private static final long serialVersionUID = 1L;

    public String value;
    public Tipify type;
    public enum Tipify {
        RELATION,
        RELATION_POLYMOPHIC,
        RELATION_POLYMOPHIC_COLUMN,
        ROWS_CONSTRAINT,
    }
    public List<Relation> owners = new ArrayList<>();

    public Fact() {
        super();
    }

    public Fact(Tipify type) {
        super();
        this.type = type;
    }
}
